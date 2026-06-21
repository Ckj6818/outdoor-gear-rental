# 释放开发端口 8081（后端）与 5173（前端）
foreach ($port in @(8081, 5173)) {
    Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue |
        ForEach-Object {
            $procId = $_.OwningProcess
            Write-Host "Stopping port $port (PID $procId)..."
            Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
        }
}
Write-Host "Done. Ports 8081 and 5173 should be free."
