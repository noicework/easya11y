#!/bin/bash

# Test form submission with actual form data
BASE_URL="http://localhost:8080/magnoliaAuthor"
ENDPOINT="$BASE_URL/.rest/easya11y/submit"

# Use an existing form path
FORM_PATH="/easya11y/Forms/Contact-Form"

echo "Testing form submission with data..."
echo "Endpoint: $ENDPOINT"
echo "Form Path: $FORM_PATH"
echo ""

# Test submission with form data
curl -X POST "$ENDPOINT" \
  -u superuser:superuser \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "formPath=$FORM_PATH" \
  -d "submissionType=jcr" \
  -d "formData[firstName]=John" \
  -d "formData[lastName]=Doe" \
  -d "formData[email]=john.doe@example.com" \
  -d "formData[phone]=555-1234" \
  -d "formData[message]=This is a test submission from curl" \
  -d "formData[company]=Test Company" \
  --silent | python3 -m json.tool

echo ""
echo "Now checking if submission was saved..."
echo ""

# Check submissions for this form
curl -X GET "$BASE_URL/.rest/easya11y/submissions/list?formPath=/easya11y/Forms/Contact-Form" \
  -u superuser:superuser \
  --silent | python3 -m json.tool