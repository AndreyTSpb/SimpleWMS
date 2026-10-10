<#
  Имитация ERP: прогоняет обмен с SimpleWMS через REST API.
  Запуск (приложение должно работать):
      powershell -ExecutionPolicy Bypass -File Project_Docs\api\erp-demo.ps1
  Параметры: -BaseUrl http://localhost:8080 -ApiKey dev-api-key
  Номера накладной и заказа берутся с отметкой времени, поэтому скрипт можно запускать повторно.
#>
param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$ApiKey = "dev-api-key"
)

$ErrorActionPreference = "Stop"
$api = "$BaseUrl/api/v1"
$headers = @{ "X-API-Key" = $ApiKey }
$samples = Join-Path $PSScriptRoot "samples"

function Invoke-Erp([string]$Method, [string]$Path, $Body = $null) {
    $params = @{ Method = $Method; Uri = "$api$Path"; Headers = $headers }
    if ($null -ne $Body) {
        $json = if ($Body -is [string]) { $Body } else { $Body | ConvertTo-Json -Depth 10 }
        $params.Body = [System.Text.Encoding]::UTF8.GetBytes($json)
        $params.ContentType = "application/json; charset=utf-8"
    }
    try {
        $response = Invoke-RestMethod @params
        Write-Host ("[OK]  {0} {1}: {2}" -f $Method, $Path, $response.message) -ForegroundColor Green
        return $response
    } catch {
        Write-Host ("[ERR] {0} {1}: {2}" -f $Method, $Path, (Read-ErrorMessage $_)) -ForegroundColor Red
        return $null
    }
}

# Текст ошибки API: message и ошибки полей из JSON-ответа (PowerShell 5.1 и 7)
function Read-ErrorMessage($err) {
    $text = $null
    if ($err.ErrorDetails -and $err.ErrorDetails.Message) {
        $text = $err.ErrorDetails.Message
    } elseif ($err.Exception.Response -and ($err.Exception.Response | Get-Member -Name GetResponseStream)) {
        $reader = New-Object System.IO.StreamReader($err.Exception.Response.GetResponseStream(), [System.Text.Encoding]::UTF8)
        $text = $reader.ReadToEnd()
    }
    if (-not $text) { return $err.Exception.Message }
    try {
        $json = $text | ConvertFrom-Json
        $message = $json.message
        if ($json.errors) {
            $message += " (" + (($json.errors | ForEach-Object { "$($_.field): $($_.message)" }) -join "; ") + ")"
        }
        return $message
    } catch {
        return $text
    }
}

function Show-Table($Rows, [string[]]$Columns) {
    if ($Rows) { $Rows | Format-Table $Columns -AutoSize | Out-String -Width 200 | Write-Host }
}

function Read-Sample([string]$Name) {
    return Get-Content -Raw -Encoding UTF8 (Join-Path $samples "$Name.json") | ConvertFrom-Json
}

$stamp = Get-Date -Format "MMddHHmmss"

Write-Host "== Справочники"
Invoke-Erp PUT "/suppliers" (Get-Content -Raw -Encoding UTF8 (Join-Path $samples "suppliers.json")) | Out-Null
Invoke-Erp PUT "/customers" (Get-Content -Raw -Encoding UTF8 (Join-Path $samples "customers.json")) | Out-Null
Invoke-Erp PUT "/products" (Get-Content -Raw -Encoding UTF8 (Join-Path $samples "products.json")) | Out-Null

Write-Host "== Приходная накладная"
$receiving = Read-Sample "receiving"
$receiving.number = "З-$stamp"
$receiving.date = (Get-Date -Format "yyyy-MM-dd HH:mm:ss")
$created = Invoke-Erp POST "/receivings" $receiving
if ($created) {
    $status = Invoke-Erp GET "/receivings/$($receiving.number)"
    Show-Table $status.data.lines @("code", "name", "expected", "received", "accepted")
}

Write-Host "== Заказ клиента"
$order = Read-Sample "order"
$order.number = "ПР-$stamp"
$order.date = (Get-Date -Format "yyyy-MM-dd HH:mm:ss")
$order.shipDate = (Get-Date).AddDays(1).ToString("yyyy-MM-dd")
$created = Invoke-Erp POST "/orders" $order
if ($created) {
    Write-Host ("      статус: {0}, недостача: {1} шт." -f $created.data.status, $created.data.shortage)
    $status = Invoke-Erp GET "/orders/$($order.number)"
    Show-Table $status.data.lines @("code", "name", "ordered", "reserved", "picked", "shipped")
}

Write-Host "== Проверка ошибок"
Invoke-Erp POST "/receivings" $receiving | Out-Null   # повтор номера — 409
Invoke-Erp POST "/orders" '{"number": "", "customerCode": "К000001", "lines": []}' | Out-Null   # 400

Write-Host "== Выгрузка отгрузок за 30 дней"
$from = (Get-Date).AddDays(-30).ToString("yyyy-MM-dd")
$shipments = Invoke-Erp GET "/shipments?from=$from"
if ($shipments) {
    Show-Table $shipments.data @("id", "number", "orderERP", "customerCode", "shipDate", "confirmed")
}

Write-Host ""
Write-Host "Дальше — в интерфейсе: приёмка накладной З-$stamp, размещение, сборка и отгрузка заказа ПР-$stamp."
Write-Host "После отгрузки повторите выгрузку и подтвердите получение: POST /api/v1/shipments/{id}/confirm"
