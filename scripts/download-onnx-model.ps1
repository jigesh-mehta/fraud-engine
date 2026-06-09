# Downloads all-MiniLM-L6-v2 ONNX assets into src/main/resources (run from repo root).
$ErrorActionPreference = "Stop"
$dir = Join-Path $PSScriptRoot "..\src\main\resources\onnx\all-MiniLM-L6-v2"
New-Item -ItemType Directory -Force -Path $dir | Out-Null

$tokenizerUrl = "https://raw.githubusercontent.com/spring-projects/spring-ai/v1.0.0/models/spring-ai-transformers/src/main/resources/onnx/all-MiniLM-L6-v2/tokenizer.json"
$modelUrl = "https://huggingface.co/optimum/all-MiniLM-L6-v2/resolve/main/model.onnx"

Write-Host "Downloading tokenizer.json..."
Invoke-WebRequest -Uri $tokenizerUrl -OutFile (Join-Path $dir "tokenizer.json") -UseBasicParsing

Write-Host "Downloading model.onnx (~90 MB)..."
Invoke-WebRequest -Uri $modelUrl -OutFile (Join-Path $dir "model.onnx") -UseBasicParsing

Get-ChildItem $dir | Format-Table Name, Length -AutoSize
Write-Host "Done."
