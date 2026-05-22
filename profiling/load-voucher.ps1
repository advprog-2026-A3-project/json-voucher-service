param(
  [string]$BaseUrl = "http://localhost:8080",
  [string]$VoucherCode = "PROFILEJSON10",
  [int]$Rounds = 30,
  [int]$WarmupRounds = 5,
  [int]$PauseMs = 150
)

$ErrorActionPreference = "Stop"

$scriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$outputDir = Join-Path $scriptRoot "out"
New-Item -ItemType Directory -Force -Path $outputDir | Out-Null

function Get-Timestamp {
  Get-Date -Format "yyyyMMdd-HHmmss"
}

function Get-Percentile {
  param(
    [double[]]$Values,
    [double]$Percentile
  )

  if (-not $Values -or $Values.Count -eq 0) {
    return $null
  }

  $sorted = $Values | Sort-Object
  $index = [Math]::Ceiling(($Percentile / 100.0) * $sorted.Count) - 1
  if ($index -lt 0) { $index = 0 }
  if ($index -ge $sorted.Count) { $index = $sorted.Count - 1 }
  return [Math]::Round([double]$sorted[$index], 2)
}

function Invoke-TimedRequest {
  param(
    [string]$Method,
    [string]$Url,
    [object]$Body = $null
  )

  $sw = [System.Diagnostics.Stopwatch]::StartNew()
  $statusCode = 0
  $content = $null

  try {
    if ($null -ne $Body) {
      $json = $Body | ConvertTo-Json -Depth 6
      $response = Invoke-WebRequest -Uri $Url -Method $Method -ContentType "application/json" -Body $json -UseBasicParsing
    } else {
      $response = Invoke-WebRequest -Uri $Url -Method $Method -UseBasicParsing
    }

    $sw.Stop()
    $statusCode = [int]$response.StatusCode
    $content = $response.Content
  } catch {
    $sw.Stop()
    if ($_.Exception.Response -and $_.Exception.Response.StatusCode) {
      $statusCode = [int]$_.Exception.Response.StatusCode
    }
    $content = $_.Exception.Message
  }

  [pscustomobject]@{
    method = $Method
    url = $Url
    durationMs = [Math]::Round($sw.Elapsed.TotalMilliseconds, 2)
    statusCode = $statusCode
    success = ($statusCode -ge 200 -and $statusCode -lt 300)
    timestamp = (Get-Date).ToString("o")
    content = $content
  }
}

function Ensure-ProfilingVoucher {
  param(
    [string]$VoucherCode,
    [string]$BaseUrl
  )

  $allVouchers = Invoke-RestMethod -Uri "$BaseUrl/api/v1/vouchers" -Method GET
  $existing = $allVouchers | Where-Object { $_.voucherCode -eq $VoucherCode } | Select-Object -First 1
  if ($existing) {
    Write-Host "Profiling voucher $VoucherCode sudah ada." -ForegroundColor Green
    return $VoucherCode
  }

  try {
    $payload = @{
      voucherCode = $VoucherCode
      validFrom = (Get-Date).AddDays(-1).ToString("yyyy-MM-ddTHH:mm:ss")
      validUntil = (Get-Date).AddDays(30).ToString("yyyy-MM-ddTHH:mm:ss")
      totalQuota = 5000
      discountPercent = 10
      minimumPurchaseAmount = 100000
      maxDiscountAmount = 20000
      terms = "Voucher profiling internal. Diskon 10% untuk minimum pembelian Rp100.000. Maksimal potongan Rp20.000."
    }

    [void](Invoke-RestMethod `
      -Uri "$BaseUrl/api/v1/vouchers" `
      -Method POST `
      -ContentType "application/json" `
      -Body ($payload | ConvertTo-Json -Depth 5))

    Write-Host "Profiling voucher $VoucherCode berhasil dibuat." -ForegroundColor Green
    return $VoucherCode
  } catch {
    $fallback = $allVouchers | Where-Object { $_.active -eq $true } | Select-Object -First 1
    if ($fallback) {
      Write-Host "Create voucher profiling gagal, fallback ke voucher aktif yang sudah ada: $($fallback.voucherCode)" -ForegroundColor Yellow
      return $fallback.voucherCode
    }

    throw "Tidak ada voucher yang bisa dipakai untuk profiling. Detail create error: $($_.Exception.Message)"
  }
}

$VoucherCode = Ensure-ProfilingVoucher -VoucherCode $VoucherCode -BaseUrl $BaseUrl

$records = New-Object System.Collections.Generic.List[object]
$validatePayload = @{ subtotal = 200000 }

Write-Host "Warm-up $WarmupRounds putaran..." -ForegroundColor Cyan
for ($i = 1; $i -le $WarmupRounds; $i++) {
  [void](Invoke-TimedRequest -Method GET -Url "$BaseUrl/api/v1/vouchers")
  [void](Invoke-TimedRequest -Method GET -Url "$BaseUrl/api/v1/vouchers/$VoucherCode")
  [void](Invoke-TimedRequest -Method POST -Url "$BaseUrl/api/v1/vouchers/$VoucherCode/validate" -Body $validatePayload)
  Start-Sleep -Milliseconds $PauseMs
}

Write-Host "Profiling load $Rounds putaran..." -ForegroundColor Cyan
for ($i = 1; $i -le $Rounds; $i++) {
  $records.Add((Invoke-TimedRequest -Method GET -Url "$BaseUrl/api/v1/vouchers"))
  $records.Add((Invoke-TimedRequest -Method GET -Url "$BaseUrl/api/v1/vouchers/$VoucherCode"))
  $records.Add((Invoke-TimedRequest -Method POST -Url "$BaseUrl/api/v1/vouchers/$VoucherCode/validate" -Body $validatePayload))
  Start-Sleep -Milliseconds $PauseMs
}

$summary = $records |
  Group-Object { "$($_.method) $($_.url.Replace($BaseUrl, ''))" } |
  ForEach-Object {
    $durations = $_.Group | ForEach-Object { [double]$_.durationMs }
    $successCount = ($_.Group | Where-Object { $_.success }).Count
    [pscustomobject]@{
      endpoint = $_.Name
      requests = $_.Count
      success = $successCount
      failed = ($_.Count - $successCount)
      avgMs = [Math]::Round((($durations | Measure-Object -Average).Average), 2)
      minMs = [Math]::Round((($durations | Measure-Object -Minimum).Minimum), 2)
      maxMs = [Math]::Round((($durations | Measure-Object -Maximum).Maximum), 2)
      p95Ms = Get-Percentile -Values $durations -Percentile 95
    }
  }

$timestamp = Get-Timestamp
$rawPath = Join-Path $outputDir "load-raw-$timestamp.json"
$summaryPath = Join-Path $outputDir "load-summary-$timestamp.json"

$records | ConvertTo-Json -Depth 6 | Set-Content -Path $rawPath
$summary | ConvertTo-Json -Depth 6 | Set-Content -Path $summaryPath

Write-Host ""
Write-Host "Ringkasan load profiling:" -ForegroundColor Yellow
$summary | Format-Table -AutoSize

Write-Host ""
Write-Host "Raw request log   : $rawPath"
Write-Host "Summary statistics: $summaryPath"
