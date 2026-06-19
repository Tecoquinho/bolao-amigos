param(
    [string]$BaseUrl = "http://localhost:8080"
)

$payloadPath = Join-Path $PSScriptRoot "src\main\resources\import\group-stage-seed.json"
$payload = Get-Content -LiteralPath $payloadPath -Raw
$uri = "$BaseUrl/api/admin/imports/seed"

Invoke-RestMethod `
    -Method Post `
    -Uri $uri `
    -ContentType "application/json" `
    -Body $payload
