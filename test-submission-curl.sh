#!/bin/bash

# Test form submission endpoint with curl
BASE_URL="http://localhost:8080/magnoliaAuthor"
ENDPOINT="$BASE_URL/.rest/easya11y/submit"

# Test form path - adjust this to match an actual form
FORM_PATH="/easya11y/Forms/test-form"

echo "Testing form submission endpoint..."
echo "Endpoint: $ENDPOINT"
echo "Form Path: $FORM_PATH"
echo ""

# Test submission
curl -X POST "$ENDPOINT" \
  -u superuser:superuser \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "formPath=$FORM_PATH" \
  -d "submissionType=jcr" \
  -d "formData[name]=John Doe" \
  -d "formData[email]=john@example.com" \
  -d "formData[message]=This is a test submission" \
  -v

echo ""
echo "Check the response above for any errors."