@echo off
rem Wrapper: runs backup.ps1 regardless of PowerShell execution policy
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0backup.ps1" %*
