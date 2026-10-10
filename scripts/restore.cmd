@echo off
rem Wrapper: runs restore.ps1 regardless of PowerShell execution policy
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0restore.ps1" %*
