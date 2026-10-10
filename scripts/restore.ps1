<#
  Восстановление базы SimpleWMS из резервной копии (scripts\backup.ps1).
  ВНИМАНИЕ: текущие данные базы заменяются данными из копии.
  Запуск из папки проекта:
      powershell -ExecutionPolicy Bypass -File scripts\restore.ps1 backups\wms_20261010_1200.sql
  Параметр -Force — без запроса подтверждения.
#>
param(
    [Parameter(Mandatory = $true)][string]$File,
    [switch]$Force
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot

function Read-EnvValue([string]$Name, [string]$Default) {
    $envFile = Join-Path $root ".env"
    if (Test-Path $envFile) {
        foreach ($line in Get-Content -Encoding UTF8 $envFile) {
            if ($line -match "^\s*$Name\s*=\s*(.*)$") { return $Matches[1].Trim() }
        }
    }
    return $Default
}

if (-not (Test-Path $File)) { throw "Файл не найден: $File" }
$File = (Resolve-Path $File).Path
$dbName = Read-EnvValue "DB_NAME" "wms"
$dbUser = Read-EnvValue "DB_USER" "admin"
$container = "simplewms-db"
$app = "simplewms-app"
$tmp = "/tmp/wms_restore.sql"

if (-not $Force) {
    $answer = Read-Host "Данные базы $dbName будут заменены копией $(Split-Path -Leaf $File). Продолжить? (да/нет)"
    if ($answer -notin @("да", "д", "yes", "y")) { Write-Host "Отменено."; exit 0 }
}

# Приложение в контейнере останавливается на время восстановления, если запущено
$appRunning = (docker ps --filter "name=^$app$" --format "{{.Names}}") -eq $app
if ($appRunning) {
    Write-Host "Остановка приложения $app"
    docker stop $app | Out-Null
}

try {
    docker cp $File "${container}:$tmp"
    if ($LASTEXITCODE -ne 0) { throw "Не удалось передать файл в контейнер $container (он запущен?)" }
    docker exec $container psql -q -U $dbUser -d $dbName -v ON_ERROR_STOP=1 -f $tmp
    if ($LASTEXITCODE -ne 0) { throw "Восстановление завершилось с ошибкой, подробности выше" }
    docker exec $container rm -f $tmp | Out-Null
    Write-Host "База $dbName восстановлена из $File" -ForegroundColor Green
} finally {
    if ($appRunning) {
        Write-Host "Запуск приложения $app"
        docker start $app | Out-Null
    }
}
