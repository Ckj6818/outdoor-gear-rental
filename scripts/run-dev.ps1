# 一键启动前后端（各开一个 PowerShell 窗口，无需手动输入命令）
$ErrorActionPreference = "Stop"
$root = Join-Path $PSScriptRoot ".."

Write-Host "Launching backend (8081) and frontend (5173)..."

Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-ExecutionPolicy", "Bypass",
    "-File", (Join-Path $PSScriptRoot "run-backend.ps1")
) -WorkingDirectory $root

Start-Sleep -Seconds 1

Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-ExecutionPolicy", "Bypass",
    "-File", (Join-Path $PSScriptRoot "run-frontend.ps1")
) -WorkingDirectory $root

Write-Host ""
Write-Host "Done. Open in browser: http://localhost:5173"
Write-Host "To stop: run scripts/stop-dev.ps1 or close both windows."
