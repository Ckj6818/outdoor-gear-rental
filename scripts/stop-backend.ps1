# 仅释放后端端口 8081（不影响前端 5173）
Get-NetTCPConnection -LocalPort 8081 -State Listen -ErrorAction SilentlyContinue |
    ForEach-Object {
        Write-Host "Stopping backend on port 8081 (PID $($_.OwningProcess))..."
        Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue
    }
