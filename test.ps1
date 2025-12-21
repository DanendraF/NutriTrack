# NutriTrack Backend API Test Script (PowerShell)
# Usage: .\test.ps1 [command]

param(
    [Parameter(Position=0)]
    [string]$Command = "help",

    [Parameter(Position=1)]
    [string]$Param = ""
)

$BaseURL = "http://localhost:8080"
$TokenFile = ".token"

# Colors
function Write-Header {
    param([string]$Message)
    Write-Host "`n========================================" -ForegroundColor Blue
    Write-Host $Message -ForegroundColor Blue
    Write-Host "========================================`n" -ForegroundColor Blue
}

function Write-Success {
    param([string]$Message)
    Write-Host "✓ $Message" -ForegroundColor Green
}

function Write-Error-Custom {
    param([string]$Message)
    Write-Host "✗ $Message" -ForegroundColor Red
}

function Write-Info {
    param([string]$Message)
    Write-Host "ℹ $Message" -ForegroundColor Yellow
}

# Get token
function Get-Token {
    if (Test-Path $TokenFile) {
        $script:Token = Get-Content $TokenFile
        Write-Info "Using token from $TokenFile"
    } else {
        $script:Token = Read-Host "Enter your Firebase ID token"
        $script:Token | Out-File -FilePath $TokenFile
        Write-Success "Token saved to $TokenFile"
    }
}

# Test functions
function Test-Health {
    Write-Header "Testing Health Endpoint"
    $response = Invoke-RestMethod -Uri "$BaseURL/health" -Method Get
    $response | ConvertTo-Json
}

function Test-Ping {
    Write-Header "Testing Ping Endpoint"
    $response = Invoke-RestMethod -Uri "$BaseURL/api/v1/ping" -Method Get
    $response | ConvertTo-Json
}

function Create-User {
    Get-Token
    Write-Header "Creating User Profile"

    $body = @{
        email = "test@nutritrack.com"
        name = "Test User"
        dateOfBirth = "1995-05-15"
        gender = "male"
        height = 175
        weight = 70
    } | ConvertTo-Json

    $headers = @{
        "Authorization" = "Bearer $Token"
        "Content-Type" = "application/json"
    }

    try {
        $response = Invoke-RestMethod -Uri "$BaseURL/api/v1/users" -Method Post -Headers $headers -Body $body
        $response | ConvertTo-Json
    } catch {
        Write-Error-Custom $_.Exception.Message
    }
}

function Get-User {
    Get-Token
    Write-Header "Getting Current User"

    $headers = @{
        "Authorization" = "Bearer $Token"
    }

    try {
        $response = Invoke-RestMethod -Uri "$BaseURL/api/v1/users/me" -Method Get -Headers $headers
        $response | ConvertTo-Json
    } catch {
        Write-Error-Custom $_.Exception.Message
    }
}

function Update-User {
    Get-Token
    Write-Header "Updating User Profile"

    $body = @{
        weight = 72
    } | ConvertTo-Json

    $headers = @{
        "Authorization" = "Bearer $Token"
        "Content-Type" = "application/json"
    }

    try {
        $response = Invoke-RestMethod -Uri "$BaseURL/api/v1/users/me" -Method Put -Headers $headers -Body $body
        $response | ConvertTo-Json
    } catch {
        Write-Error-Custom $_.Exception.Message
    }
}

function Update-Goals {
    Get-Token
    Write-Header "Updating Nutrition Goals"

    $body = @{
        activityLevel = "active"
        nutritionGoal = "lose"
    } | ConvertTo-Json

    $headers = @{
        "Authorization" = "Bearer $Token"
        "Content-Type" = "application/json"
    }

    try {
        $response = Invoke-RestMethod -Uri "$BaseURL/api/v1/users/me/goals" -Method Put -Headers $headers -Body $body
        $response | ConvertTo-Json
    } catch {
        Write-Error-Custom $_.Exception.Message
    }
}

function Search-Foods {
    param([string]$Query = "nasi")

    Get-Token
    Write-Header "Searching Foods"
    Write-Info "Search query: $Query"

    $headers = @{
        "Authorization" = "Bearer $Token"
    }

    try {
        $response = Invoke-RestMethod -Uri "$BaseURL/api/v1/foods?q=$Query" -Method Get -Headers $headers
        $response | ConvertTo-Json
    } catch {
        Write-Error-Custom $_.Exception.Message
    }
}

function Get-FoodsByCategory {
    param([string]$Category = "protein")

    Get-Token
    Write-Header "Getting Foods by Category: $Category"

    $headers = @{
        "Authorization" = "Bearer $Token"
    }

    try {
        $response = Invoke-RestMethod -Uri "$BaseURL/api/v1/foods/category/$Category" -Method Get -Headers $headers
        $response | ConvertTo-Json
    } catch {
        Write-Error-Custom $_.Exception.Message
    }
}

function Get-AllFoods {
    Get-Token
    Write-Header "Listing All Foods"

    $headers = @{
        "Authorization" = "Bearer $Token"
    }

    try {
        $response = Invoke-RestMethod -Uri "$BaseURL/api/v1/foods?limit=50" -Method Get -Headers $headers
        $response | ConvertTo-Json
    } catch {
        Write-Error-Custom $_.Exception.Message
    }
}

function Create-Food {
    Get-Token
    Write-Header "Creating New Food"

    $body = @{
        name = "Test Food"
        nameIndonesian = "Makanan Test"
        category = "snacks"
        nutrition = @{
            calories = 100
            protein = 5
            carbs = 15
            fat = 3
            fiber = 2
            sugar = 5
            sodium = 50
        }
        servingSize = @{
            amount = 100
            unit = "g"
        }
    } | ConvertTo-Json

    $headers = @{
        "Authorization" = "Bearer $Token"
        "Content-Type" = "application/json"
    }

    try {
        $response = Invoke-RestMethod -Uri "$BaseURL/api/v1/foods" -Method Post -Headers $headers -Body $body
        $response | ConvertTo-Json
    } catch {
        Write-Error-Custom $_.Exception.Message
    }
}

function Count-Foods {
    Get-Token
    Write-Header "Counting Foods by Category"

    $headers = @{
        "Authorization" = "Bearer $Token"
    }

    try {
        $response = Invoke-RestMethod -Uri "$BaseURL/api/v1/foods?limit=100" -Method Get -Headers $headers
        Write-Success "Total foods: $($response.total)"

        $categories = @("fruits", "vegetables", "protein", "dairy", "grains", "snacks")
        foreach ($category in $categories) {
            $catResponse = Invoke-RestMethod -Uri "$BaseURL/api/v1/foods/category/$category" -Method Get -Headers $headers
            Write-Host "  - ${category}: $($catResponse.total)" -ForegroundColor Green
        }
    } catch {
        Write-Error-Custom $_.Exception.Message
    }
}

function Run-FullTest {
    Write-Header "Running Full Test Suite"

    Write-Host "`n1. Public Endpoints" -ForegroundColor Yellow
    Test-Health
    Start-Sleep -Seconds 1
    Test-Ping
    Start-Sleep -Seconds 1

    Write-Host "`n2. User Endpoints" -ForegroundColor Yellow
    Create-User
    Start-Sleep -Seconds 1
    Get-User
    Start-Sleep -Seconds 1
    Update-User
    Start-Sleep -Seconds 1
    Update-Goals
    Start-Sleep -Seconds 1

    Write-Host "`n3. Food Endpoints" -ForegroundColor Yellow
    Search-Foods -Query "nasi"
    Start-Sleep -Seconds 1
    Get-FoodsByCategory -Category "protein"
    Start-Sleep -Seconds 1
    Count-Foods

    Write-Success "Full test suite completed!"
}

function Show-Help {
    Write-Host "NutriTrack Backend Test Script" -ForegroundColor Yellow
    Write-Host "`nUsage: .\test.ps1 [command] [param]`n"
    Write-Host "Commands:"
    Write-Host "  health          - Test health endpoint"
    Write-Host "  ping            - Test ping endpoint"
    Write-Host "  create-user     - Create test user profile"
    Write-Host "  get-user        - Get current user"
    Write-Host "  update-user     - Update user profile"
    Write-Host "  update-goals    - Update nutrition goals"
    Write-Host "  search [query]  - Search foods (default: 'nasi')"
    Write-Host "  list-foods      - List all foods"
    Write-Host "  category [cat]  - Get foods by category (default: 'protein')"
    Write-Host "  create-food     - Create new food"
    Write-Host "  count           - Count foods by category"
    Write-Host "  full            - Run full test suite"
    Write-Host "`nExamples:"
    Write-Host "  .\test.ps1 health"
    Write-Host "  .\test.ps1 search ayam"
    Write-Host "  .\test.ps1 category fruits"
    Write-Host "  .\test.ps1 full"
}

# Command router
switch ($Command.ToLower()) {
    "health" { Test-Health }
    "ping" { Test-Ping }
    "create-user" { Create-User }
    "get-user" { Get-User }
    "update-user" { Update-User }
    "update-goals" { Update-Goals }
    "search" {
        if ($Param) { Search-Foods -Query $Param }
        else { Search-Foods }
    }
    "list-foods" { Get-AllFoods }
    "category" {
        if ($Param) { Get-FoodsByCategory -Category $Param }
        else { Get-FoodsByCategory }
    }
    "create-food" { Create-Food }
    "count" { Count-Foods }
    "full" { Run-FullTest }
    default { Show-Help }
}
