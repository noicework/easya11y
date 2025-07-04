#!/bin/bash

# Quick test script for easya11y endpoints
# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

BASE_URL="http://localhost:8080/magnoliaAuthor"
AUTH="superuser:superuser"

echo -e "${BLUE}Quick easya11y Endpoint Test${NC}\n"

# Test form list
echo -e "${BLUE}1. Testing form list endpoint...${NC}"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" -u "$AUTH" "$BASE_URL/.rest/easya11y/formlist")
if [ "$STATUS" == "200" ]; then
    echo -e "${GREEN}✓ Form list: OK${NC}"
else
    echo -e "${RED}✗ Form list: FAILED (Status: $STATUS)${NC}"
fi

# Test submissions list
echo -e "\n${BLUE}2. Testing submissions list endpoint...${NC}"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" -u "$AUTH" "$BASE_URL/.rest/easya11y/submissions/list")
if [ "$STATUS" == "200" ]; then
    echo -e "${GREEN}✓ Submissions list: OK${NC}"
else
    echo -e "${RED}✗ Submissions list: FAILED (Status: $STATUS)${NC}"
fi

# Test form submission
echo -e "\n${BLUE}3. Testing form submission endpoint...${NC}"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST -u "$AUTH" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "formPath=/easya11y/Contact-Form&formData[name]=Quick Test&formData[email]=quick@test.com" \
    "$BASE_URL/.rest/easya11y/submit")
if [ "$STATUS" == "200" ]; then
    echo -e "${GREEN}✓ Form submission: OK${NC}"
else
    echo -e "${RED}✗ Form submission: FAILED (Status: $STATUS)${NC}"
fi

# Test CSV export
echo -e "\n${BLUE}4. Testing CSV export endpoint...${NC}"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" -u "$AUTH" "$BASE_URL/.rest/easya11y/submissions/export/csv")
if [ "$STATUS" == "200" ]; then
    echo -e "${GREEN}✓ CSV export: OK${NC}"
else
    echo -e "${RED}✗ CSV export: FAILED (Status: $STATUS)${NC}"
fi

echo -e "\n${BLUE}Done!${NC}"