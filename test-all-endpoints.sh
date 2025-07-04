#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BASE_URL="http://localhost:8080/magnoliaAuthor"
AUTH="superuser:superuser"

# Test counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Function to print test header
print_header() {
    echo -e "\n${BLUE}=================================================================================${NC}"
    echo -e "${BLUE}                    easya11y REST API Endpoint Tests${NC}"
    echo -e "${BLUE}=================================================================================${NC}"
    echo -e "Base URL: ${YELLOW}$BASE_URL${NC}"
    echo -e "Testing as: ${YELLOW}${AUTH%:*}${NC}"
    echo -e "${BLUE}=================================================================================${NC}\n"
}

# Function to test an endpoint
test_endpoint() {
    local method=$1
    local endpoint=$2
    local description=$3
    local data=$4
    local expected_status=${5:-200}
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    echo -e "${YELLOW}Test $TOTAL_TESTS:${NC} $description"
    echo -e "  ${BLUE}Method:${NC} $method"
    echo -e "  ${BLUE}Endpoint:${NC} $endpoint"
    
    if [ "$method" == "POST" ] && [ -n "$data" ]; then
        echo -e "  ${BLUE}Data:${NC} $data"
        response=$(curl -s -w "\n%{http_code}" -X $method \
            -u "$AUTH" \
            -H "Content-Type: application/x-www-form-urlencoded" \
            -d "$data" \
            "$BASE_URL$endpoint")
    else
        response=$(curl -s -w "\n%{http_code}" -X $method \
            -u "$AUTH" \
            "$BASE_URL$endpoint")
    fi
    
    # Split response body and status code
    body=$(echo "$response" | sed '$d')
    status=$(echo "$response" | tail -n1)
    
    if [ "$status" == "$expected_status" ]; then
        echo -e "  ${GREEN}✓ Status: $status (Expected: $expected_status)${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
        
        # Try to parse JSON and show key information
        if command -v jq &> /dev/null; then
            if echo "$body" | jq . &> /dev/null; then
                # It's valid JSON
                success=$(echo "$body" | jq -r '.success // empty')
                if [ -n "$success" ]; then
                    echo -e "  ${BLUE}Success:${NC} $success"
                fi
                
                # Show specific info based on endpoint
                case "$endpoint" in
                    *"/formlist")
                        count=$(echo "$body" | jq '.items | length')
                        echo -e "  ${BLUE}Forms found:${NC} $count"
                        ;;
                    *"/submissions/list"*)
                        total=$(echo "$body" | jq -r '.total // 0')
                        echo -e "  ${BLUE}Submissions found:${NC} $total"
                        ;;
                    *"/submit")
                        submission_id=$(echo "$body" | jq -r '.submissionId // empty')
                        if [ -n "$submission_id" ]; then
                            echo -e "  ${BLUE}Submission ID:${NC} $submission_id"
                        fi
                        ;;
                esac
            fi
        else
            # jq not available, show raw response preview
            preview=$(echo "$body" | head -c 100)
            echo -e "  ${BLUE}Response preview:${NC} $preview..."
        fi
    else
        echo -e "  ${RED}✗ Status: $status (Expected: $expected_status)${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        
        # Show error message if available
        if command -v jq &> /dev/null && echo "$body" | jq . &> /dev/null; then
            error_msg=$(echo "$body" | jq -r '.message // .error.message // empty')
            if [ -n "$error_msg" ]; then
                echo -e "  ${RED}Error:${NC} $error_msg"
            fi
        else
            # Show raw error
            echo -e "  ${RED}Response:${NC} $body"
        fi
    fi
    echo ""
}

# Function to print summary
print_summary() {
    echo -e "\n${BLUE}=================================================================================${NC}"
    echo -e "${BLUE}                                Test Summary${NC}"
    echo -e "${BLUE}=================================================================================${NC}"
    echo -e "Total Tests: ${YELLOW}$TOTAL_TESTS${NC}"
    echo -e "Passed: ${GREEN}$PASSED_TESTS${NC}"
    echo -e "Failed: ${RED}$FAILED_TESTS${NC}"
    
    if [ $FAILED_TESTS -eq 0 ]; then
        echo -e "\n${GREEN}✓ All tests passed!${NC}"
    else
        echo -e "\n${RED}✗ Some tests failed!${NC}"
    fi
    echo -e "${BLUE}=================================================================================${NC}\n"
}

# Main test execution
print_header

# Test 1: Form List Endpoint
test_endpoint "GET" "/.rest/easya11y/formlist" \
    "Get list of all forms"

# Test 2: Form List with specific path
test_endpoint "GET" "/.rest/easya11y/formlist?path=/easya11y" \
    "Get forms under specific path"

# Test 3: Submit form - valid submission
form_data="formPath=/easya11y/Contact-Form"
form_data="${form_data}&formData[name]=Test User"
form_data="${form_data}&formData[email]=test@example.com"
form_data="${form_data}&formData[message]=Automated test submission"

test_endpoint "POST" "/.rest/easya11y/submit" \
    "Submit a test form" \
    "$form_data"

# Test 4: Submit form - invalid path
invalid_data="formPath=/nonexistent/form&formData[test]=value"
test_endpoint "POST" "/.rest/easya11y/submit" \
    "Submit to non-existent form (should fail)" \
    "$invalid_data" \
    500

# Test 5: Get all submissions
test_endpoint "GET" "/.rest/easya11y/submissions/list" \
    "Get all form submissions"

# Test 6: Get submissions for specific form
test_endpoint "GET" "/.rest/easya11y/submissions/list?formPath=/easya11y/Contact-Form" \
    "Get submissions for Contact Form"

# Test 7: Get submissions by path parameter
test_endpoint "GET" "/.rest/easya11y/submissions/easya11y/Contact-Form" \
    "Get submissions using path parameter"

# Test 8: Export submissions as CSV
test_endpoint "GET" "/.rest/easya11y/submissions/export/csv" \
    "Export all submissions as CSV"

# Test 9: Export specific form submissions as CSV
test_endpoint "GET" "/.rest/easya11y/submissions/export/csv?formPath=/easya11y/Contact-Form" \
    "Export Contact Form submissions as CSV"

# Test 10: Submit with email notification (if configured)
email_data="formPath=/easya11y/Contact-Form"
email_data="${email_data}&submissionType=email"
email_data="${email_data}&emailTo=test@example.com"
email_data="${email_data}&emailSubject=Test Submission"
email_data="${email_data}&formData[name]=Email Test"
email_data="${email_data}&formData[email]=sender@example.com"
email_data="${email_data}&formData[message]=Testing email submission"

test_endpoint "POST" "/.rest/easya11y/submit" \
    "Submit form with email notification" \
    "$email_data"

# Test 11: Test JCR delivery endpoint for forms (if configured)
test_endpoint "GET" "/.rest/delivery/forms/v2/easya11y" \
    "Get forms via JCR delivery endpoint (if configured)" \
    "" \
    200

print_summary

# Exit with appropriate code
if [ $FAILED_TESTS -eq 0 ]; then
    exit 0
else
    exit 1
fi