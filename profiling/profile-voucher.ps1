param(
  [string]$BaseUrl = "http://localhost:8080",
  [string]$VoucherCode = "PROFILEJSON10",
  [long]$Subtotal = 200000,
  [string]$JMeterPath = "",
  [switch]$SkipPrepareData
)

$ErrorActionPreference = "Stop"

$scriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$jmeterDir = Join-Path $scriptRoot "jmeter"
$outputDir = Join-Path $scriptRoot "out\\jmeter"
New-Item -ItemType Directory -Force -Path $outputDir | Out-Null

function Resolve-JMeterExecutable {
  param([string]$PreferredPath)

  if ($PreferredPath) {
    if (Test-Path $PreferredPath) {
      return (Resolve-Path $PreferredPath).Path
    }
    throw "JMeter tidak ditemukan pada path yang diberikan: $PreferredPath"
  }

  $commands = @("jmeter.bat", "jmeter")
  foreach ($command in $commands) {
    $resolved = Get-Command $command -ErrorAction SilentlyContinue
    if ($resolved) {
      return $resolved.Source
    }
  }

  $commonCandidates = @(
    "C:\apache-jmeter\bin\jmeter.bat",
    "C:\Program Files\Apache\JMeter\bin\jmeter.bat",
    "C:\Program Files\apache-jmeter\bin\jmeter.bat"
  )

  foreach ($candidate in $commonCandidates) {
    if (Test-Path $candidate) {
      return $candidate
    }
  }

  throw "JMeter tidak ditemukan. Install JMeter atau jalankan script ini dengan -JMeterPath <path-ke-jmeter.bat>."
}

function Get-Timestamp {
  Get-Date -Format "yyyyMMdd-HHmmss"
}

function Invoke-JMeterPlan {
  param(
    [string]$Executable,
    [string]$PlanPath,
    [string]$ResultsPath,
    [string]$DashboardDir,
    [Uri]$BaseUri,
    [string]$VoucherCode,
    [long]$Subtotal
  )

  $arguments = @(
    "-n"
    "-t", $PlanPath
    "-l", $ResultsPath
    "-e"
    "-o", $DashboardDir
    "-Jprotocol=$($BaseUri.Scheme)"
    "-Jhost=$($BaseUri.Host)"
    "-Jport=$($BaseUri.Port)"
    "-JvoucherCode=$VoucherCode"
    "-Jsubtotal=$Subtotal"
  )

  if (Test-Path $DashboardDir) {
    Remove-Item -LiteralPath $DashboardDir -Recurse -Force
  }

  & $Executable @arguments
  if ($LASTEXITCODE -ne 0) {
    throw "JMeter gagal menjalankan plan: $PlanPath"
  }
}

if (-not $SkipPrepareData) {
  & (Join-Path $scriptRoot "prepare-jmeter-data.ps1") -BaseUrl $BaseUrl -VoucherCode $VoucherCode
}

$jmeterExecutable = Resolve-JMeterExecutable -PreferredPath $JMeterPath
$baseUri = [Uri]$BaseUrl
$timestamp = Get-Timestamp

$plans = @(
  @{
    Name = "test_plan_1_list_vouchers"
    File = Join-Path $jmeterDir "test_plan_1_list_vouchers.jmx"
  }
  @{
    Name = "test_plan_2_voucher_detail"
    File = Join-Path $jmeterDir "test_plan_2_voucher_detail.jmx"
  }
  @{
    Name = "test_plan_3_validate_voucher"
    File = Join-Path $jmeterDir "test_plan_3_validate_voucher.jmx"
  }
)

Write-Host "Menjalankan profiling ala modul dengan JMeter..." -ForegroundColor Cyan
Write-Host "Base URL    : $BaseUrl"
Write-Host "Voucher Code: $VoucherCode"
Write-Host "Subtotal    : $Subtotal"
Write-Host "JMeter      : $jmeterExecutable"
Write-Host ""

foreach ($plan in $plans) {
  $resultsPath = Join-Path $outputDir "$($plan.Name)-$timestamp.jtl"
  $dashboardDir = Join-Path $outputDir "$($plan.Name)-dashboard-$timestamp"

  Write-Host "Running $($plan.Name)..." -ForegroundColor Yellow
  Invoke-JMeterPlan `
    -Executable $jmeterExecutable `
    -PlanPath $plan.File `
    -ResultsPath $resultsPath `
    -DashboardDir $dashboardDir `
    -BaseUri $baseUri `
    -VoucherCode $VoucherCode `
    -Subtotal $Subtotal

  Write-Host "JTL      : $resultsPath" -ForegroundColor Green
  Write-Host "Dashboard: $dashboardDir" -ForegroundColor Green
  Write-Host ""
}

Write-Host "Selesai." -ForegroundColor Green
Write-Host "Langkah modul berikutnya:"
Write-Host "1. Buka salah satu .jmx di JMeter GUI untuk screenshot listener."
Write-Host "2. Jalankan profiler CPU di IntelliJ saat salah satu endpoint JMeter dibebani."
Write-Host "3. Catat method dengan Total Time / CPU Time tertinggi lalu bandingkan sebelum dan sesudah optimasi."
