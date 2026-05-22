param(
  [string]$PrometheusUrl = "http://localhost:9090",
  [string]$Job = "voucher-service"
)

$ErrorActionPreference = "Stop"

$scriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$outputDir = Join-Path $scriptRoot "out"
New-Item -ItemType Directory -Force -Path $outputDir | Out-Null

function Get-Timestamp {
  Get-Date -Format "yyyyMMdd-HHmmss"
}

function Invoke-PromQuery {
  param([string]$Query)

  $encoded = [System.Uri]::EscapeDataString($Query)
  $response = Invoke-RestMethod -Uri "$PrometheusUrl/api/v1/query?query=$encoded" -Method GET
  if ($response.status -ne "success") {
    throw "Prometheus query gagal: $Query"
  }
  return $response.data.result
}

function Get-SingleMetricValue {
  param([object[]]$Result)

  if (-not $Result -or $Result.Count -eq 0) {
    return $null
  }

  return [double]$Result[0].value[1]
}

$queries = [ordered]@{
  serviceStatus = "up{job=""$Job""}"
  requestRate = "sum(rate(http_server_requests_seconds_count{job=""$Job""}[1m]))"
  avgLatencyMs = "1000 * sum(rate(http_server_requests_seconds_sum{job=""$Job""}[1m])) / clamp_min(sum(rate(http_server_requests_seconds_count{job=""$Job""}[1m])), 0.0001)"
  jvmHeapUsedBytes = "sum(jvm_memory_used_bytes{job=""$Job"", area=""heap""})"
  processCpuUsage = "process_cpu_usage{job=""$Job""}"
  endpointRate = "sum by (uri, status) (rate(http_server_requests_seconds_count{job=""$Job""}[1m]))"
}

$rawResults = [ordered]@{}
foreach ($entry in $queries.GetEnumerator()) {
  $rawResults[$entry.Key] = Invoke-PromQuery -Query $entry.Value
}

$snapshot = [pscustomobject]@{
  capturedAt = (Get-Date).ToString("o")
  job = $Job
  serviceStatus = Get-SingleMetricValue -Result $rawResults.serviceStatus
  requestRate = [Math]::Round((Get-SingleMetricValue -Result $rawResults.requestRate), 4)
  avgLatencyMs = [Math]::Round((Get-SingleMetricValue -Result $rawResults.avgLatencyMs), 4)
  jvmHeapUsedBytes = [Math]::Round((Get-SingleMetricValue -Result $rawResults.jvmHeapUsedBytes), 0)
  jvmHeapUsedMiB = [Math]::Round(((Get-SingleMetricValue -Result $rawResults.jvmHeapUsedBytes) / 1MB), 2)
  processCpuUsage = [Math]::Round((Get-SingleMetricValue -Result $rawResults.processCpuUsage), 4)
  endpointRate = $rawResults.endpointRate | ForEach-Object {
    [pscustomobject]@{
      uri = $_.metric.uri
      status = $_.metric.status
      rate = [Math]::Round([double]$_.value[1], 4)
    }
  }
}

$timestamp = Get-Timestamp
$snapshotPath = Join-Path $outputDir "metrics-snapshot-$timestamp.json"
$rawPath = Join-Path $outputDir "metrics-raw-$timestamp.json"

$snapshot | ConvertTo-Json -Depth 8 | Set-Content -Path $snapshotPath
$rawResults | ConvertTo-Json -Depth 8 | Set-Content -Path $rawPath

Write-Host "Prometheus snapshot tersimpan di: $snapshotPath"
Write-Host "Prometheus raw result tersimpan di: $rawPath"
Write-Host ""
$snapshot | Select-Object capturedAt, serviceStatus, requestRate, avgLatencyMs, jvmHeapUsedMiB, processCpuUsage | Format-List
