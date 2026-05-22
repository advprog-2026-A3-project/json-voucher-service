param(
  [string]$BaseUrl = "http://localhost:8080",
  [string]$PrometheusUrl = "http://localhost:9090",
  [string]$VoucherCode = "PROFILEJSON10",
  [int]$Rounds = 30,
  [int]$WarmupRounds = 5,
  [int]$PauseMs = 150,
  [int]$PostLoadWaitSeconds = 8
)

$ErrorActionPreference = "Stop"
$scriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host "== Baseline metrics ==" -ForegroundColor Cyan
& (Join-Path $scriptRoot "prometheus-snapshot.ps1") -PrometheusUrl $PrometheusUrl

Write-Host ""
Write-Host "== Generate voucher traffic ==" -ForegroundColor Cyan
& (Join-Path $scriptRoot "load-voucher.ps1") `
  -BaseUrl $BaseUrl `
  -VoucherCode $VoucherCode `
  -Rounds $Rounds `
  -WarmupRounds $WarmupRounds `
  -PauseMs $PauseMs

Write-Host ""
Write-Host "Menunggu $PostLoadWaitSeconds detik agar scrape Prometheus menangkap traffic terbaru..." -ForegroundColor Yellow
Start-Sleep -Seconds $PostLoadWaitSeconds

Write-Host ""
Write-Host "== Metrics after load ==" -ForegroundColor Cyan
& (Join-Path $scriptRoot "prometheus-snapshot.ps1") -PrometheusUrl $PrometheusUrl
