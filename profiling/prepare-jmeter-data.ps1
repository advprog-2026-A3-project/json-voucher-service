param(
  [string]$BaseUrl = "http://localhost:8080",
  [string]$VoucherCode = "PROFILEJSON10"
)

$ErrorActionPreference = "Stop"

function Get-JsonErrorMessage {
  param([string]$Content)

  if ([string]::IsNullOrWhiteSpace($Content)) {
    return $null
  }

  try {
    $parsed = $Content | ConvertFrom-Json
    if ($parsed.message) { return [string]$parsed.message }
    if ($parsed.error) { return [string]$parsed.error }
    return $Content
  } catch {
    return $Content
  }
}

function Invoke-JsonRequest {
  param(
    [string]$Method,
    [string]$Url,
    [object]$Body = $null
  )

  try {
    if ($null -ne $Body) {
      $json = $Body | ConvertTo-Json -Depth 8
      return Invoke-RestMethod -Uri $Url -Method $Method -ContentType "application/json" -Body $json
    }

    return Invoke-RestMethod -Uri $Url -Method $Method
  } catch {
    $message = $null

    if ($_.Exception.Response) {
      try {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $message = Get-JsonErrorMessage -Content ($reader.ReadToEnd())
      } catch {
        $message = $_.Exception.Message
      }
    }

    if (-not $message) {
      $message = $_.Exception.Message
    }

    throw $message
  }
}

Write-Host "Memeriksa voucher profiling $VoucherCode..." -ForegroundColor Cyan
$allVouchers = Invoke-JsonRequest -Method GET -Url "$BaseUrl/api/v1/vouchers"
$existing = $allVouchers | Where-Object { $_.voucherCode -eq $VoucherCode } | Select-Object -First 1

if ($existing) {
  Write-Host "Voucher profiling sudah tersedia: $VoucherCode" -ForegroundColor Green
  return
}

$payload = @{
  voucherCode = $VoucherCode
  validFrom = (Get-Date).AddDays(-1).ToString("yyyy-MM-ddTHH:mm:ss")
  validUntil = (Get-Date).AddDays(30).ToString("yyyy-MM-ddTHH:mm:ss")
  totalQuota = 5000
  discountPercent = 10
  minimumPurchaseAmount = 100000
  maxDiscountAmount = 20000
  terms = "Voucher khusus profiling JMeter. Diskon 10% minimum pembelian Rp100.000 dengan potongan maksimal Rp20.000."
}

[void](Invoke-JsonRequest -Method POST -Url "$BaseUrl/api/v1/vouchers" -Body $payload)
Write-Host "Voucher profiling berhasil dibuat: $VoucherCode" -ForegroundColor Green
