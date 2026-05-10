$ErrorActionPreference = "Stop"

$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$FrontendDir = Join-Path $Root "frontend"
$BackendDir = Join-Path $Root "atm-server-auth"

function Write-Step($Message) {
    Write-Host ""
    Write-Host "==> $Message" -ForegroundColor Cyan
}

function Require-Command($Name, $Hint) {
    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "$Name not found. $Hint"
    }
}

Write-Step "Checking required tools"
Require-Command "java" "Install JDK 17 or newer."
Require-Command "node" "Install Node.js 20 or newer."
Require-Command "npm" "Install Node.js with npm."

java -version
node --version
npm --version

if (-not (Test-Path (Join-Path $BackendDir "mvnw.cmd"))) {
    throw "Missing atm-server-auth\mvnw.cmd. Please run this script from the project root."
}

Write-Step "Preparing frontend environment"
$EnvLocal = Join-Path $FrontendDir ".env.local"
if (-not (Test-Path $EnvLocal)) {
    @"
VITE_API_BASE_URL=http://localhost:8080
VITE_USE_MOCK=false
"@ | Set-Content -Path $EnvLocal -Encoding ASCII
    Write-Host "Created frontend\.env.local for real backend mode."
} else {
    Write-Host "frontend\.env.local already exists, keeping it unchanged."
}

Write-Step "Installing frontend dependencies"
Push-Location $FrontendDir
try {
    npm install
    Write-Step "Verifying frontend build"
    npm run build
} finally {
    Pop-Location
}

Write-Host ""
Write-Host "Setup finished." -ForegroundColor Green
Write-Host "Start real backend + frontend: start-dev.cmd"
Write-Host "Frontend mock demo only: cd frontend; npm run dev:mock"
Write-Host "Real frontend only: cd frontend; npm run dev"
Write-Host ""
Write-Host "Note: iteration-3 backend APIs for device, receipt, and history still need backend implementation."
