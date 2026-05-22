param(
  [Parameter(Mandatory = $true)]
  [string]$BeforeMetrics,

  [Parameter(Mandatory = $true)]
  [string]$AfterMetrics,

  [Parameter(Mandatory = $false)]
  [string]$BeforeLoadSummary,

  [Parameter(Mandatory = $false)]
  [string]$AfterLoadSummary
)

$ErrorActionPreference = "Stop"

function Get-JsonFile {
  param([string]$Path)

  if (-not (Test-Path $Path)) {
    throw "File tidak ditemukan: $Path"
  }

  return Get-Content $Path -Raw | ConvertFrom-Json
}

function Get-DeltaRow {
  param(
    [string]$Metric,
    [double]$Before,
    [double]$After,
    [string]$Unit = ""
  )

  $delta = $After - $Before
  [pscustomobject]@{
    metric = $Metric
    before = if ($Unit) { "{0:N4} {1}" -f $Before, $Unit } else { "{0:N4}" -f $Before }
    after = if ($Unit) { "{0:N4} {1}" -f $After, $Unit } else { "{0:N4}" -f $After }
    delta = if ($Unit) { "{0:N4} {1}" -f $delta, $Unit } else { "{0:N4}" -f $delta }
  }
}

function Get-NumberOrDefault {
  param(
    [object]$Value,
    [double]$Default = 0
  )

  if ($null -eq $Value -or $Value -eq "") {
    return $Default
  }

  return [double]$Value
}

function Normalize-EndpointMap {
  param([object[]]$SummaryRows)

  $map = @{}
  foreach ($row in $SummaryRows) {
    $map[$row.endpoint] = $row
  }
  return $map
}

$beforeMetricsJson = Get-JsonFile -Path $BeforeMetrics
$afterMetricsJson = Get-JsonFile -Path $AfterMetrics

Write-Host ""
Write-Host "== Metrics comparison ==" -ForegroundColor Cyan
$metricRows = @(
  (Get-DeltaRow -Metric "Service Status" -Before (Get-NumberOrDefault $beforeMetricsJson.serviceStatus) -After (Get-NumberOrDefault $afterMetricsJson.serviceStatus)),
  (Get-DeltaRow -Metric "Request Rate" -Before (Get-NumberOrDefault $beforeMetricsJson.requestRate) -After (Get-NumberOrDefault $afterMetricsJson.requestRate) -Unit "req/s"),
  (Get-DeltaRow -Metric "Average Latency" -Before (Get-NumberOrDefault $beforeMetricsJson.avgLatencyMs) -After (Get-NumberOrDefault $afterMetricsJson.avgLatencyMs) -Unit "ms"),
  (Get-DeltaRow -Metric "JVM Heap Used" -Before (Get-NumberOrDefault $beforeMetricsJson.jvmHeapUsedMiB) -After (Get-NumberOrDefault $afterMetricsJson.jvmHeapUsedMiB) -Unit "MiB"),
  (Get-DeltaRow -Metric "Process CPU Usage" -Before (Get-NumberOrDefault $beforeMetricsJson.processCpuUsage) -After (Get-NumberOrDefault $afterMetricsJson.processCpuUsage))
)
$metricRows | Format-Table -AutoSize

if ($BeforeLoadSummary -and $AfterLoadSummary) {
  $beforeLoadJson = Get-JsonFile -Path $BeforeLoadSummary
  $afterLoadJson = Get-JsonFile -Path $AfterLoadSummary
  $beforeLoadMap = Normalize-EndpointMap -SummaryRows $beforeLoadJson
  $afterLoadMap = Normalize-EndpointMap -SummaryRows $afterLoadJson

  $allEndpoints = @($beforeLoadMap.Keys + $afterLoadMap.Keys | Sort-Object -Unique)
  $comparisonRows = foreach ($endpoint in $allEndpoints) {
    $beforeRow = $beforeLoadMap[$endpoint]
    $afterRow = $afterLoadMap[$endpoint]
    [pscustomobject]@{
      endpoint = $endpoint
      beforeAvgMs = if ($beforeRow) { [double]$beforeRow.avgMs } else { $null }
      afterAvgMs = if ($afterRow) { [double]$afterRow.avgMs } else { $null }
      avgDeltaMs = if ($beforeRow -and $afterRow) { [Math]::Round(([double]$afterRow.avgMs - [double]$beforeRow.avgMs), 2) } else { $null }
      beforeP95Ms = if ($beforeRow) { [double]$beforeRow.p95Ms } else { $null }
      afterP95Ms = if ($afterRow) { [double]$afterRow.p95Ms } else { $null }
      p95DeltaMs = if ($beforeRow -and $afterRow) { [Math]::Round(([double]$afterRow.p95Ms - [double]$beforeRow.p95Ms), 2) } else { $null }
      beforeFailed = if ($beforeRow) { [int]$beforeRow.failed } else { $null }
      afterFailed = if ($afterRow) { [int]$afterRow.failed } else { $null }
    }
  }

  Write-Host ""
  Write-Host "== Load test comparison ==" -ForegroundColor Cyan
  $comparisonRows | Format-Table -AutoSize
}
else {
  Write-Host ""
  Write-Host "Load summary comparison dilewati karena file summary before/after tidak diberikan." -ForegroundColor Yellow
}
