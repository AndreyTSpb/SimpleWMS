<#
  Резервная копия базы SimpleWMS (pg_dump внутри контейнера simplewms-db).
  Запуск из папки проекта:
      powershell -ExecutionPolicy Bypass -File scripts\backup.ps1
  Результат: backups\wms_ГГГГММДД_ЧЧММ.sql (SQL-скрипт; восстановление — scripts\restore.ps1).
#>
param(
    [string]$OutDir = ""
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
if (-not $OutDir) { $OutDir = Join-Path $root "backups" }

# Параметры БД из .env (как у docker compose), иначе значения по умолчанию
function Read-EnvValue([string]$Name, [string]$Default) {
    $envFile = Join-Path $root ".env"
    if (Test-Path $envFile) {
        foreach ($line in Get-Content -Encoding UTF8 $envFile) {
            if ($line -match "^\s*$Name\s*=\s*(.*)$") { return $Matches[1].Trim() }
        }
    }
    return $Default
}

$dbName = Read-EnvValue "DB_NAME" "wms"
$dbUser = Read-EnvValue "DB_USER" "admin"
$container = "simplewms-db"
$stamp = Get-Date -Format "yyyyMMdd_HHmm"
New-Item -ItemType Directory -Force -Path $OutDir | Out-Null
$file = Join-Path $OutDir "wms_$stamp.sql"
$tmp = "/tmp/wms_backup.sql"

Write-Host "Резервная копия базы $dbName → $file"
# Дамп создаётся внутри контейнера и копируется целиком (без перекодировки консолью PowerShell)
docker exec $container sh -c "pg_dump -U $dbUser -d $dbName --clean --if-exists --no-owner -f $tmp"
if ($LASTEXITCODE -ne 0) { throw "pg_dump завершился с ошибкой (контейнер $container запущен?)" }
docker cp "${container}:$tmp" $file
if ($LASTEXITCODE -ne 0) { throw "Не удалось скопировать файл из контейнера" }
docker exec $container rm -f $tmp | Out-Null

$size = [math]::Round((Get-Item $file).Length / 1KB, 1)
Write-Host "Готово: $file ($size КБ)" -ForegroundColor Green
