# 修复 frontend 依赖：清理损坏临时目录并重新安装
$ErrorActionPreference = "Continue"
Set-Location (Join-Path $PSScriptRoot "..")

Write-Host "Stopping node processes..."
Get-Process node -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 2

Write-Host "Cleaning broken temp folders..."
Get-ChildItem node_modules -Force -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -like ".*" -or $_.Name -like ".element-plus*" -or $_.Name -like ".esbuild*" -or $_.Name -like ".rollup*" } |
    ForEach-Object {
        Write-Host "  Remove: $($_.Name)"
        Remove-Item $_.FullName -Recurse -Force -ErrorAction SilentlyContinue
    }

Write-Host "Running npm install..."
npm install

if (Test-Path "node_modules\element-plus\dist\index.css") {
    Write-Host "OK: element-plus installed successfully."
} else {
    Write-Host "ERROR: element-plus still incomplete. Try deleting node_modules and run npm install again."
    exit 1
}

Write-Host "Done. Run: npm run dev"
