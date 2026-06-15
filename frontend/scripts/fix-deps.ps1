# 修复 frontend 依赖（Windows 权限/损坏包专用）
$ErrorActionPreference = "Continue"
Set-Location (Join-Path $PSScriptRoot "..")

Write-Host "1. Stopping node..."
Get-Process node -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 2

Write-Host "2. Removing broken temp folders..."
Get-ChildItem node_modules -Force -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -like ".*" } |
    ForEach-Object { Remove-Item $_.FullName -Recurse -Force -ErrorAction SilentlyContinue }

Write-Host "3. Reinstalling element-plus..."
Remove-Item -Recurse -Force "node_modules\element-plus","node_modules\@element-plus" -ErrorAction SilentlyContinue
npm install element-plus@2.8.4 @element-plus/icons-vue@2.3.1

$ok = (Test-Path "node_modules\element-plus\es\index.mjs") -and (Test-Path "node_modules\element-plus\dist\index.css")
if ($ok) {
    Write-Host "OK: dependencies fixed. Run: npm run dev"
} else {
    Write-Host "WARN: element-plus incomplete. Delete node_modules and run: npm install"
}
