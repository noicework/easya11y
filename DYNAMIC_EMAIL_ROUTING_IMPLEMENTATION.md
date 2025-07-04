# Dynamic Email Routing Implementation

## Overview
This implementation adds dynamic email routing functionality to easya11y, allowing forms to send emails to addresses determined by form field values rather than just static addresses.

## Features Implemented

### 1. Email Routing Types
- **Static Routing**: Uses a fixed email address (existing functionality)
- **Dynamic Routing**: Uses an email address from a form field

### 2. Configuration Properties
- `emailRoutingType`: "static" or "dynamic" 
- `emailFieldKey`: The form field name containing the email address (for dynamic routing)

### 3. Fallback Behavior
When dynamic routing is used but fails, the system falls back to the static email address:
- Invalid email format in the dynamic field
- Missing dynamic field in form data  
- Empty dynamic field value
- Empty or missing emailFieldKey configuration

## Files Modified

### Backend Changes

#### 1. FormSubmissionEndpoint.java
- Added new parameters: `emailRoutingType` and `emailFieldKey`
- Implemented `determineEmailAddress()` method for routing logic
- Added `isValidEmail()` method for email validation
- Enhanced logging for routing decisions
- Cleaned up unused imports

#### 2. container.yaml (Dialog Configuration)
The dialog already contained the necessary fields:
- `emailRoutingType`: Radio button group (static/dynamic)
- `emailFieldKey`: Text field for dynamic field name

### Frontend Changes

#### 3. container.ftl (Template)
- Added `emailRoutingType` and `emailFieldKey` to useContentOrPage configuration
- Included new parameters in JSON configuration script tag

#### 4. forms.js (JavaScript)
- Added support for reading new configuration parameters
- Updated `saveFormSubmissionToJcr()` method signature and implementation
- Pass routing parameters to backend

## Usage Examples

### Static Email Routing (Default)
```javascript
{
  "emailRoutingType": "static",
  "emailTo": "admin@example.com",
  "emailFieldKey": ""
}
```
Result: All emails sent to `admin@example.com`

### Dynamic Email Routing
```javascript
{
  "emailRoutingType": "dynamic", 
  "emailTo": "fallback@example.com",
  "emailFieldKey": "departmentEmail"
}
```

Form data:
```javascript
{
  "name": "John Doe",
  "email": "john@example.com", 
  "departmentEmail": "sales@example.com"
}
```
Result: Email sent to `sales@example.com` (from departmentEmail field)

### Dynamic Routing with Fallback
Same configuration as above, but with invalid dynamic email:
```javascript
{
  "name": "Jane Smith",
  "departmentEmail": "invalid-email"
}
```
Result: Email sent to `fallback@example.com` (fallback due to invalid email)

## Testing

### Test Scripts Created
1. `test-dynamic-email-routing.js`: Basic dynamic routing tests
2. `test-all-email-features.js`: Comprehensive test suite covering all scenarios

### Test Coverage
- Static email routing (baseline functionality)
- Dynamic routing with valid emails
- Dynamic routing with invalid emails (fallback)
- Dynamic routing with missing fields (fallback)
- Dynamic routing with empty field keys (fallback)
- Edge cases (unknown routing types, empty configurations)

## Logging and Debugging

The implementation includes comprehensive logging:

- `DEBUG`: Routing decisions for successful cases
- `WARN`: Fallback scenarios with reasons
- `INFO`: Final email sending confirmation with routing type
- `ERROR`: Email sending failures

Example log messages:
```
DEBUG: Using static email routing: admin@example.com
DEBUG: Using dynamic email from field 'departmentEmail': sales@example.com
WARN: Invalid email address found in field 'departmentEmail': invalid-email, falling back to static email: fallback@example.com
INFO: Email sent to: sales@example.com using routing type: dynamic for form: /easya11y/test/contact-form
```

## Backward Compatibility

This implementation is fully backward compatible:
- Existing forms without routing configuration continue to work
- Default routing type is "static" 
- All existing emailTo configurations are preserved
- No breaking changes to existing APIs

## Configuration in Magnolia

In the Form Container dialog:
1. Go to "Email Settings" tab
2. Set "Email Routing Type":
   - "Static Email Address": Uses the "Send Form To" field
   - "Email from Form Field": Uses the "Email Field Key" field
3. For dynamic routing, specify the form field name in "Email Field Key"

## Security Considerations

- Email validation prevents invalid addresses from being used
- Fallback behavior ensures emails are never lost
- Logging provides audit trail for routing decisions
- No user input is executed as code - only used for email addresses

## Future Enhancements

Potential future improvements:
- Multiple email recipients based on form data
- Complex routing rules (e.g., multiple conditions)
- Email template selection based on routing
- Integration with external address books
- CC/BCC routing based on form fields