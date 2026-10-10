# REST API для ERP

Интерактивное описание с возможностью отправить запрос — Swagger UI: `http://<сервер>:8080/swagger-ui.html`
(вход администратором или менеджером, кнопка «Authorize» → ключ). Машиночитаемое описание OpenAPI 3 — `api/openapi.json`
(снимок) или `/v3/api-docs`. Примеры запросов — `api/erp.http`, `api/samples/`, сценарий обмена — `api/erp-demo.ps1`.

## Общие правила

| | |
|---|---|
| Адрес | `/api/v1` |
| Формат | JSON, UTF-8 (`Content-Type: application/json`) |
| Аутентификация | заголовок `X-API-Key: <ключ>` (параметр `wms.api.key`, переменная `WMS_API_KEY`); со страниц системы — сессия администратора или менеджера с CSRF-токеном |
| Даты | дата документа — `ГГГГ-ММ-ДД ЧЧ:ММ:СС`; плановая отгрузка и параметр `from` — `ГГГГ-ММ-ДД` |
| Количества | целые числа больше 0 (без кавычек) |
| Справочники | создание или обновление по коду; запрос выполняется целиком или не выполняется совсем |

Формат ответа:

```json
{"success": true, "message": "текст для журнала", "data": { ... }}
{"success": false, "message": "описание ошибки", "errors": [{"field": "поле", "message": "что не так"}]}
```

| Код | Значение |
|---|---|
| 200 | выполнено |
| 201 | создан документ (накладная, заказ) |
| 400 | ошибки проверки данных (`errors` — по полям), некорректный JSON, неверный формат даты или числа |
| 401 | нет ключа или неверный ключ |
| 403 | недостаточно прав; запрос из браузера с сессией без CSRF-токена |
| 404 | документ, поставщик, клиент или ABC-класс не найден |
| 409 | номер документа уже загружен, недопустимый статус, повтор кода в одном запросе |
| 500 | внутренняя ошибка (подробности — в журнале приложения) |

## Методы

| Метод | Адрес | Назначение |
|---|---|---|
| PUT | `/products` | товары |
| PUT | `/suppliers` | поставщики |
| PUT | `/customers` | клиенты |
| POST | `/receivings` | загрузить приходную накладную |
| GET | `/receivings/{номер}` | статус накладной и результаты приёмки |
| GET | `/receivings?from=ГГГГ-ММ-ДД` | завершённые приёмки начиная с даты (по умолчанию — 30 дней) |
| POST | `/orders` | загрузить заказ клиента (сразу резервируется) |
| GET | `/orders/{номер}` | статус заказа и выполнение по строкам |
| POST | `/orders/{номер}/cancel` | отменить заказ (до передачи в сборку) |
| GET | `/shipments?from=ГГГГ-ММ-ДД` | отгрузки начиная с даты (по умолчанию — 30 дней) |
| POST | `/shipments/{id}/confirm` | подтвердить получение отгрузки |

### PUT /products — товары

Поля: `code`*, `name`*, `supplierCode`* (поставщик должен быть в справочнике), `category` (по названию; новая
создаётся), `abc` (A–D), `weight` (кг), `volume` (л), `expirationRequired`, `extBarcode`, `intBarcode`, `description`.
Не переданные необязательные поля у существующего товара не меняются.

```json
[
  {
    "code": "029952",
    "name": "Карандаш чернографитный HB",
    "supplierCode": "П000019",
    "category": "Письменные принадлежности",
    "abc": "B",
    "weight": 0.01,
    "volume": 0.01,
    "expirationRequired": false,
    "extBarcode": "4600000029952",
    "intBarcode": "2000000029952"
  },
  {
    "code": "215530",
    "name": "Маркер текстовыделитель жёлтый 1-5 мм",
    "description": "Скошенный наконечник",
    "supplierCode": "П000020",
    "category": "Письменные принадлежности",
    "abc": "A",
    "weight": 0.02,
    "volume": 0.03,
    "expirationRequired": false,
    "extBarcode": "4600000215530",
    "intBarcode": "2000000215530"
  }
]
```

Ответ: `{"success": true, "message": "Товары: создано 1, обновлено 1", "data": {"created": 1, "updated": 1}}`

### PUT /suppliers, PUT /customers

Поставщик: `code`*, `name`*, `contactPerson`, `phone`, `email`. Клиент: `code`*, `name`*, `phone`, `email`, `address`,
`workingFrom`, `workingTo` (ЧЧ:ММ).

```json
[
  {
    "code": "К000001",
    "name": "ООО «Демо-Клиент»",
    "phone": "+7 812 000-00-02",
    "email": "client@example.com",
    "address": "190000, г. Санкт-Петербург, ул. Примерная, д. 1",
    "workingFrom": "09:00",
    "workingTo": "18:00"
  },
  {
    "code": "К000010",
    "name": "ИП Синичкина Л. Н.",
    "phone": "+7 813 000-00-10",
    "email": "sinichkina@example.com",
    "address": "187600, Ленинградская обл., г. Пикалёво, ул. Школьная, д. 7",
    "workingFrom": "10:00",
    "workingTo": "17:00"
  }
]
```

### POST /receivings — приходная накладная

```json
{
  "number": "З-ДЕМО-1",
  "supplierCode": "П000019",
  "date": "2026-10-10 08:30:00",
  "lines": [
    { "code": "029952", "quantity": 200 },
    { "code": "074519", "quantity": 80 },
    { "code": "109216", "quantity": 120 }
  ]
}
```

Ответ 201: `data` — `id`, `number`, `status` («В ожидании»), `lines` (создано строк), `missingProducts`
(коды, которых нет в справочнике; эти строки пропущены).

### GET /receivings/{номер} — результаты приёмки

```json
{
  "success": true,
  "message": "Накладная ПН-1001",
  "data": {
    "id": 1,
    "number": "ПН-1001",
    "supplierCode": "П000019",
    "date": "2026-10-08 22:50:20",
    "status": "Завершена",
    "receivedAt": "2026-10-10 22:50:20",
    "lines": [
      {
        "code": "029952",
        "name": "Карандаш чернографитный HB",
        "expected": 200,
        "received": 200,
        "accepted": true,
        "expiryDate": null
      },
      {
        "code": "074519",
        "name": "Клей-карандаш 21 г",
        "expected": 80,
        "received": 80,
        "accepted": true,
        "expiryDate": "2027-03-31"
      }
    ]
  }
}
```

`expected` — по документу, `received` — принято, `accepted` — строка обработана, `expiryDate` — срок годности партии.

### POST /orders — заказ клиента

```json
{
  "number": "ПР-ДЕМО-1",
  "customerCode": "К000001",
  "date": "2026-10-10 09:00:00",
  "shipDate": "2026-10-11",
  "note": "Доставка до 12:00",
  "lines": [
    { "code": "029952", "quantity": 20 },
    { "code": "074519", "quantity": 10 }
  ]
}
```

Ответ 201 (не хватило остатка):

```json
{
  "success": true,
  "message": "Заказ ПР-ДОК загружен",
  "data": {
    "id": 5,
    "number": "ПР-ДОК",
    "status": "Частично зарезервирован",
    "lines": 2,
    "missingProducts": [],
    "shortage": 500
  }
}
```

### GET /orders/{номер} — выполнение заказа

```json
{
  "success": true,
  "message": "Заказ ПР-5001",
  "data": {
    "id": 1,
    "number": "ПР-5001",
    "customerCode": "К000001",
    "date": "2026-10-10 17:50:20",
    "shipDate": "2026-10-10",
    "status": "Отгружен",
    "shipment": "ОТГ-1",
    "lines": [
      {
        "code": "029952",
        "name": "Карандаш чернографитный HB",
        "ordered": 20,
        "reserved": 20,
        "picked": 20,
        "shipped": 20
      },
      {
        "code": "074519",
        "name": "Клей-карандаш 21 г",
        "ordered": 10,
        "reserved": 10,
        "picked": 10,
        "shipped": 10
      }
    ]
  }
}
```

`shipment` — номер документа отгрузки, если заказ отгружен.

### GET /shipments — выгрузка отгрузок

```json
{
  "success": true,
  "message": "Отгрузок: 1",
  "data": [
    {
      "id": 1,
      "number": "ОТГ-1",
      "orderERP": "ПР-5001",
      "customerCode": "К000001",
      "shipDate": "2026-10-10 22:50:21",
      "confirmed": true,
      "body": [
        {
          "code": "029952",
          "qnt": 20
        },
        {
          "code": "074519",
          "qnt": 10
        }
      ]
    }
  ]
}
```

Строки сгруппированы по коду товара. `confirmed` — ERP уже подтвердила получение
(`POST /shipments/{id}/confirm`; повторное подтверждение не меняет дату).

### Пример ошибки проверки (400)

Запрос с пустым номером и отрицательным количеством:

```json
{
  "success": false,
  "message": "Ошибки в данных запроса",
  "errors": [
    {
      "field": "number",
      "message": "не должно быть пустым"
    },
    {
      "field": "lines[0].quantity",
      "message": "должно быть больше 0"
    }
  ]
}
```
