# 以管理员身份运行 PowerShell，再执行：
#   Set-ExecutionPolicy -Scope Process Bypass -Force
#   .\scripts\reset-mysql-password.ps1
#
# 将把 MySQL root 密码重置为 123456（可按需修改下方 $NewPassword）

$ErrorActionPreference = "Stop"
$MysqlBin = "D:\mysql8\bin"
$NewPassword = "123456"
$InitFile = "$env:TEMP\mysql-init-reset.sql"

@"
ALTER USER 'root'@'localhost' IDENTIFIED BY '$NewPassword';
FLUSH PRIVILEGES;
"@ | Set-Content -Path $InitFile -Encoding ASCII

Write-Host "1. 停止 MySQL 服务..."
Stop-Service MySQL -Force
Start-Sleep -Seconds 2

Write-Host "2. 使用 init-file 重置密码..."
$proc = Start-Process -FilePath "$MysqlBin\mysqld.exe" `
    -ArgumentList "--defaults-file=D:\mysql8\my.ini", "--init-file=$InitFile", "--console" `
    -PassThru -WindowStyle Hidden
Start-Sleep -Seconds 8

Write-Host "3. 结束临时 mysqld 进程..."
Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 2

Write-Host "4. 正常启动 MySQL 服务..."
Start-Service MySQL
Start-Sleep -Seconds 3

Write-Host "5. 验证新密码..."
& "$MysqlBin\mysql.exe" -u root "-p$NewPassword" -e "SELECT 'OK' AS status;" 2>&1

Remove-Item $InitFile -Force -ErrorAction SilentlyContinue
Write-Host ""
Write-Host "完成！新密码: $NewPassword"
Write-Host "请在 application-local.yml 中配置相同密码，然后重启 Spring Boot。"
