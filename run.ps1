$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot
$sourceFiles = Get-ChildItem -Path (Join-Path $projectRoot "src") -Recurse -Filter "*.java" |
    ForEach-Object { $_.FullName }

if (-not $sourceFiles) {
    throw "No Java source files were found."
}

$binDir = Join-Path $projectRoot "bin"
$gsonJar = Join-Path $projectRoot "lib\gson-2.10.1.jar"

New-Item -ItemType Directory -Force -Path $binDir | Out-Null

Write-Host "Compiling Campus Connect..."
& javac -cp $gsonJar -d $binDir $sourceFiles
if ($LASTEXITCODE -ne 0) {
    throw "Compilation failed."
}

Write-Host "Starting Campus Connect at http://localhost:8080"
Set-Location $projectRoot
& java -cp "$binDir;$gsonJar" Main
