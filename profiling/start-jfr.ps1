param(
  [string]$ProcessMatch = "json-voucher-service",
  [int]$DurationSeconds = 120
)

$ErrorActionPreference = "Stop"

$scriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$outputDir = Join-Path $scriptRoot "out"
New-Item -ItemType Directory -Force -Path $outputDir | Out-Null

if (-not (Get-Command jcmd -ErrorAction SilentlyContinue)) {
  throw "jcmd tidak ditemukan. Jalankan script ini dari JDK 21 yang sudah tersedia di PATH."
}

$processLine = jcmd -l | Select-String -Pattern $ProcessMatch | Select-Object -First 1
if (-not $processLine) {
  throw "Tidak menemukan proses Java yang cocok dengan pattern '$ProcessMatch'."
}

$pid = ($processLine.ToString().Trim() -split "\s+")[0]
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$jfrPath = Join-Path $outputDir "voucher-profile-$timestamp.jfr"

Write-Host "Menjalankan JFR untuk PID $pid selama $DurationSeconds detik..."
jcmd $pid JFR.start name=VoucherProfile settings=profile duration="$($DurationSeconds)s" filename="$jfrPath"

Write-Host "JFR recording dijadwalkan. File hasil akan muncul di:"
Write-Host $jfrPath
Write-Host "Setelah selesai, buka file itu di JDK Mission Control atau VisualVM."
