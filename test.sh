#!/bin/bash

# NutriTrack Backend API Test Script
# Usage: ./test.sh [command]

BASE_URL="http://localhost:8080"
TOKEN_FILE=".token"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Helper functions
print_header() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}\n"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ $1${NC}"
}

# Get token from file or prompt
get_token() {
    if [ -f "$TOKEN_FILE" ]; then
        TOKEN=$(cat $TOKEN_FILE)
        print_info "Using token from $TOKEN_FILE"
    else
        echo -e "${YELLOW}Enter your Firebase ID token:${NC}"
        read -r TOKEN
        echo "$TOKEN" > $TOKEN_FILE
        print_success "Token saved to $TOKEN_FILE"
    fi
}

# Commands

health() {
    print_header "Testing Health Endpoint"
    curl -s $BASE_URL/health | jq .
}

ping() {
    print_header "Testing Ping Endpoint"
    curl -s $BASE_URL/api/v1/ping | jq .
}

create_user() {
    get_token
    print_header "Creating User Profile"

    curl -s -X POST $BASE_URL/api/v1/users \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d '{
            "email": "test@nutritrack.com",
            "name": "Test User",
            "dateOfBirth": "1995-05-15",
            "gender": "male",
            "height": 175,
            "weight": 70
        }' | jq .
}

get_user() {
    get_token
    print_header "Getting Current User"

    curl -s -X GET $BASE_URL/api/v1/users/me \
        -H "Authorization: Bearer $TOKEN" | jq .
}

update_user() {
    get_token
    print_header "Updating User Profile"

    curl -s -X PUT $BASE_URL/api/v1/users/me \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d '{
            "weight": 72
        }' | jq .
}

update_goals() {
    get_token
    print_header "Updating Nutrition Goals"

    curl -s -X PUT $BASE_URL/api/v1/users/me/goals \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d '{
            "activityLevel": "active",
            "nutritionGoal": "lose"
        }' | jq .
}

search_foods() {
    get_token
    print_header "Searching Foods"

    QUERY=${1:-"nasi"}
    print_info "Search query: $QUERY"

    curl -s -X GET "$BASE_URL/api/v1/foods?q=$QUERY" \
        -H "Authorization: Bearer $TOKEN" | jq .
}

list_foods() {
    get_token
    print_header "Listing All Foods"

    curl -s -X GET "$BASE_URL/api/v1/foods?limit=50" \
        -H "Authorization: Bearer $TOKEN" | jq .
}

get_category() {
    get_token
    CATEGORY=${1:-"protein"}
    print_header "Getting Foods by Category: $CATEGORY"

    curl -s -X GET "$BASE_URL/api/v1/foods/category/$CATEGORY" \
        -H "Authorization: Bearer $TOKEN" | jq .
}

create_food() {
    get_token
    print_header "Creating New Food"

    curl -s -X POST $BASE_URL/api/v1/foods \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d '{
            "name": "Test Food",
            "nameIndonesian": "Makanan Test",
            "category": "snacks",
            "nutrition": {
                "calories": 100,
                "protein": 5,
                "carbs": 15,
                "fat": 3,
                "fiber": 2,
                "sugar": 5,
                "sodium": 50
            },
            "servingSize": {
                "amount": 100,
                "unit": "g"
            }
        }' | jq .
}

count_foods() {
    get_token
    print_header "Counting Foods by Category"

    TOTAL=$(curl -s -X GET "$BASE_URL/api/v1/foods?limit=100" \
        -H "Authorization: Bearer $TOKEN" | jq '.total')

    print_success "Total foods: $TOTAL"

    for category in fruits vegetables protein dairy grains snacks; do
        COUNT=$(curl -s -X GET "$BASE_URL/api/v1/foods/category/$category" \
            -H "Authorization: Bearer $TOKEN" | jq '.total')
        echo -e "${GREEN}  - $category: $COUNT${NC}"
    done
}

full_test() {
    print_header "Running Full Test Suite"

    echo -e "\n${YELLOW}1. Public Endpoints${NC}"
    health
    sleep 1
    ping
    sleep 1

    echo -e "\n${YELLOW}2. User Endpoints${NC}"
    create_user
    sleep 1
    get_user
    sleep 1
    update_user
    sleep 1
    update_goals
    sleep 1

    echo -e "\n${YELLOW}3. Food Endpoints${NC}"
    search_foods "nasi"
    sleep 1
    get_category "protein"
    sleep 1
    count_foods

    print_success "Full test suite completed!"
}

# Command router
case "$1" in
    health)
        health
        ;;
    ping)
        ping
        ;;
    create-user)
        create_user
        ;;
    get-user)
        get_user
        ;;
    update-user)
        update_user
        ;;
    update-goals)
        update_goals
        ;;
    search)
        search_foods "$2"
        ;;
    list-foods)
        list_foods
        ;;
    category)
        get_category "$2"
        ;;
    create-food)
        create_food
        ;;
    count)
        count_foods
        ;;
    full)
        full_test
        ;;
    *)
        echo -e "${YELLOW}NutriTrack Backend Test Script${NC}"
        echo -e "\nUsage: ./test.sh [command]\n"
        echo "Commands:"
        echo "  health          - Test health endpoint"
        echo "  ping            - Test ping endpoint"
        echo "  create-user     - Create test user profile"
        echo "  get-user        - Get current user"
        echo "  update-user     - Update user profile"
        echo "  update-goals    - Update nutrition goals"
        echo "  search [query]  - Search foods (default: 'nasi')"
        echo "  list-foods      - List all foods"
        echo "  category [cat]  - Get foods by category (default: 'protein')"
        echo "  create-food     - Create new food"
        echo "  count           - Count foods by category"
        echo "  full            - Run full test suite"
        echo ""
        echo "Examples:"
        echo "  ./test.sh health"
        echo "  ./test.sh search ayam"
        echo "  ./test.sh category fruits"
        echo "  ./test.sh full"
        ;;
esac
