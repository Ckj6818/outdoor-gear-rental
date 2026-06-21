$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")

if (-not $env:MYSQL_PASSWORD) {
    $env:MYSQL_PASSWORD = "123456"
}

$mvnPath = $null
$mvnCmd = Get-Command mvn -ErrorAction SilentlyContinue
if ($mvnCmd) {
    $mvnPath = $mvnCmd.Source
} else {
    $fallback = Join-Path $env:LOCALAPPDATA "Temp\apache-maven-3.9.6\bin\mvn.cmd"
    if (Test-Path $fallback) {
        $mvnPath = $fallback
    }
}

if (-not $mvnPath) {
    Write-Error "Maven not found. Install Maven or add mvn to PATH."
    exit 1
}

Write-Host "Starting backend: http://localhost:8081"
& $mvnPath spring-boot:run
