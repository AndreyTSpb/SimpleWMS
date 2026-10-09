-- V002. Демо-данные для разработки и демонстрации (только профиль dev).
--
-- Что делает: заполняет справочники тестовыми записями — категории,
-- поставщик, клиент, сотрудники и их учётные записи, зоны хранения с ячейками, товары.
-- Зачем: чтобы на чистой БД можно было сразу пройти сценарий приёмки.
-- Коды товаров и поставщика совпадают с примером запроса POST /api/new_receiving.
--
-- Идемпотентный: ON CONFLICT DO NOTHING. Требует V001.

-- Категории
INSERT INTO categoryes (categoryid, category_name, description, del) VALUES
    (1, 'Письменные принадлежности', 'Ручки, карандаши, маркеры', false),
    (2, 'Бумажная продукция',        'Бумага, тетради, блокноты', false),
    (3, 'Офисные принадлежности',    'Скрепки, степлеры, папки',  false)
ON CONFLICT (categoryid) DO NOTHING;

-- Поставщик (id = 1 используется в /api/new_receiving до доработки на этапе 3)
INSERT INTO supplier (supplierid, supplier_name, supplier_code, contact_person, phone_number, email, del) VALUES
    (1, 'ООО «Демо-Поставщик»', 'П000019', 'Сидоров С. С.', '+7 812 000-00-01', 'supply@example.com', false)
ON CONFLICT (supplierid) DO NOTHING;

-- Клиент
INSERT INTO customers (customer_id, customer_code, customer_name, phone, email, address, working_time_str, working_time_end, del) VALUES
    (1, 'К000001', 'ООО «Демо-Клиент»', '+7 812 000-00-02', 'client@example.com',
     '190000, г. Санкт-Петербург, ул. Примерная, д. 1', '09:00', '18:00', false)
ON CONFLICT (customer_id) DO NOTHING;

-- Сотрудники
INSERT INTO employee (employeeid, employee_name, tab_num, del, roleid) VALUES
    (2, 'Петрова А. В.', '0002', 0, 2),
    (3, 'Иванов И. И.',  '0003', 0, 3),
    (4, 'Смирнов П. К.', '0004', 0, 3)
ON CONFLICT (employeeid) DO NOTHING;

-- Демо-пользователи (пароль совпадает с логином). Администратор admin/admin создаётся в V001.
INSERT INTO users (userid, login, pass, del, employeeid) VALUES
    (2, 'manager',     '$2a$10$eSSFhyToC9TIWEW/ex677eIQHnxqb2XPhmzy21tPvC3h.2qBollk.', false, 2),
    (3, 'storekeeper', '$2a$10$NcILvllQg9Dcg1ewX46s8O4YfWWx2sHpbqC7EssG/Ks2x0miQy/aS', false, 3)
ON CONFLICT (userid) DO NOTHING;

-- Зоны хранения
INSERT INTO zones (zone_id, zone_name, description, code, del) VALUES
    (3, 'Стеллажи, секция 1', 'Ходовой товар (A, B)', 'SC1', false),
    (4, 'Стеллажи, секция 2', 'Прочий товар (C, D)',  'SC2', false)
ON CONFLICT (zone_id) DO NOTHING;

-- Ячейки хранения: код = зона-ряд-x-y-z (как в CreateCodeLocation)
INSERT INTO location (locationid, location_code, "row", x, y, z, capacity, available, del, zoneid) VALUES
    (101, 'SC1-01-01-1-1', '01', 1, 1, 1, 1, 1, 0, 3),
    (102, 'SC1-01-01-2-1', '01', 1, 2, 1, 1, 1, 0, 3),
    (103, 'SC1-01-02-1-1', '01', 2, 1, 1, 1, 1, 0, 3),
    (104, 'SC1-01-02-2-1', '01', 2, 2, 1, 1, 1, 0, 3),
    (105, 'SC1-01-03-1-1', '01', 3, 1, 1, 1, 1, 0, 3),
    (106, 'SC1-01-03-2-1', '01', 3, 2, 1, 1, 1, 0, 3),
    (107, 'SC1-02-01-1-1', '02', 1, 1, 1, 1, 1, 0, 3),
    (108, 'SC1-02-01-2-1', '02', 1, 2, 1, 1, 1, 0, 3),
    (109, 'SC1-02-02-1-1', '02', 2, 1, 1, 1, 1, 0, 3),
    (110, 'SC1-02-02-2-1', '02', 2, 2, 1, 1, 1, 0, 3),
    (111, 'SC1-02-03-1-1', '02', 3, 1, 1, 1, 1, 0, 3),
    (112, 'SC1-02-03-2-1', '02', 3, 2, 1, 1, 1, 0, 3),
    (201, 'SC2-01-01-1-1', '01', 1, 1, 1, 1, 1, 0, 4),
    (202, 'SC2-01-01-2-1', '01', 1, 2, 1, 1, 1, 0, 4),
    (203, 'SC2-01-02-1-1', '01', 2, 1, 1, 1, 1, 0, 4),
    (204, 'SC2-01-02-2-1', '01', 2, 2, 1, 1, 1, 0, 4),
    (205, 'SC2-01-03-1-1', '01', 3, 1, 1, 1, 1, 0, 4),
    (206, 'SC2-01-03-2-1', '01', 3, 2, 1, 1, 1, 0, 4),
    (207, 'SC2-02-01-1-1', '02', 1, 1, 1, 1, 1, 0, 4),
    (208, 'SC2-02-01-2-1', '02', 1, 2, 1, 1, 1, 0, 4),
    (209, 'SC2-02-02-1-1', '02', 2, 1, 1, 1, 1, 0, 4),
    (210, 'SC2-02-02-2-1', '02', 2, 2, 1, 1, 1, 0, 4),
    (211, 'SC2-02-03-1-1', '02', 3, 1, 1, 1, 1, 0, 4),
    (212, 'SC2-02-03-2-1', '02', 3, 2, 1, 1, 1, 0, 4)
ON CONFLICT (locationid) DO NOTHING;

-- Товары (коды из примера накладной ERP)
INSERT INTO product (productid, product_name, product_code, description, weight, volume,
                     expiration_date_required, del, supplierid, categoryid, ext_bar_code, int_bar_code, abcid) VALUES
    (1, 'Ручка шариковая синяя 0,7 мм',           '109216', '',                    0.01, 0.02, false, false, 1, 1, '4600000109216', '2000000109216', 1),
    (2, 'Карандаш чернографитный HB',              '029952', '',                    0.01, 0.01, false, false, 1, 1, '4600000029952', '2000000029952', 2),
    (3, 'Бумага офисная А4, 500 л.',               '028712', 'Пачка 500 листов',    2.50, 3.10, false, false, 1, 2, '4600000028712', '2000000028712', 1),
    (4, 'Скрепки 28 мм, упак. 100 шт.',           '011053', '',                    0.02, 0.01, false, false, 1, 3, '4600000011053', '2000000011053', 3),
    (5, 'Клей-карандаш 21 г',                      '074519', 'Срок годности 24 мес.', 0.03, 0.02, true,  false, 1, 3, '4600000074519', '2000000074519', 2)
ON CONFLICT (productid) DO NOTHING;

-- Сдвиг счётчиков id (см. пояснение в V001)
SELECT setval(pg_get_serial_sequence('categoryes', 'categoryid'), (SELECT MAX(categoryid)  FROM categoryes));
SELECT setval(pg_get_serial_sequence('supplier', 'supplierid'),   (SELECT MAX(supplierid)  FROM supplier));
SELECT setval(pg_get_serial_sequence('customers', 'customer_id'), (SELECT MAX(customer_id) FROM customers));
SELECT setval(pg_get_serial_sequence('employee', 'employeeid'),   (SELECT MAX(employeeid)  FROM employee));
SELECT setval(pg_get_serial_sequence('location', 'locationid'),   (SELECT MAX(locationid)  FROM location));
SELECT setval(pg_get_serial_sequence('product', 'productid'),     (SELECT MAX(productid)   FROM product));

DO '
DECLARE
    s  record;
    mx bigint;
BEGIN
    FOR s IN
        SELECT c.relname AS seq, t.tbl, t.col
        FROM pg_class c
        JOIN (VALUES (''zones_seq'',       ''zones'', ''zone_id''),
                     (''zone_entity_seq'', ''zones'', ''zone_id''),
                     (''users_seq'',       ''users'', ''userid''),
                     (''user_entity_seq'', ''users'', ''userid'')) AS t(seqname, tbl, col)
          ON c.relname = t.seqname
        WHERE c.relkind = ''S''
    LOOP
        EXECUTE format(''SELECT COALESCE(MAX(%I), 0) FROM %I'', s.col, s.tbl) INTO mx;
        PERFORM setval(quote_ident(s.seq),
                       GREATEST(mx + 50,
                                COALESCE((SELECT last_value FROM pg_sequences WHERE sequencename = s.seq), 1)));
    END LOOP;
END
';
