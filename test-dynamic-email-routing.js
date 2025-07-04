const axios = require('axios');

// Configuration
const baseUrl = 'http://localhost:8080/magnoliaAuthor';
const restEndpoint = '/.rest/easya11y/submit';

// Test dynamic email routing feature
async function testDynamicEmailRouting() {
  console.log('Testing easya11y Dynamic Email Routing Feature\n');
  
  // Test cases for different routing scenarios
  const testCases = [
    {
      name: 'Static Email Routing',
      description: 'Should use the static emailTo address',
      emailRoutingType: 'static',
      emailTo: 'static@example.com',
      emailFieldKey: '', // Not used for static routing
      formData: {
        name: 'John Doe',
        email: 'john@example.com', // This should NOT be used for email routing
        department: 'sales',
        message: 'Test message for static routing'
      },
      expectedRecipient: 'static@example.com'
    },
    {
      name: 'Dynamic Email Routing - Valid Email',
      description: 'Should use the email from the specified form field',
      emailRoutingType: 'dynamic',
      emailTo: 'fallback@example.com', // Fallback if dynamic fails
      emailFieldKey: 'departmentEmail',
      formData: {
        name: 'Jane Smith',
        email: 'jane@example.com',
        departmentEmail: 'hr@example.com', // This should be used for routing
        department: 'hr',
        message: 'Test message for dynamic routing'
      },
      expectedRecipient: 'hr@example.com'
    },
    {
      name: 'Dynamic Email Routing - Invalid Email',
      description: 'Should fallback to static emailTo when dynamic email is invalid',
      emailRoutingType: 'dynamic',
      emailTo: 'fallback@example.com',
      emailFieldKey: 'departmentEmail',
      formData: {
        name: 'Bob Johnson',
        email: 'bob@example.com',
        departmentEmail: 'invalid-email', // Invalid email should cause fallback
        department: 'support',
        message: 'Test message with invalid dynamic email'
      },
      expectedRecipient: 'fallback@example.com'
    },
    {
      name: 'Dynamic Email Routing - Missing Field',
      description: 'Should fallback to static emailTo when dynamic field is missing',
      emailRoutingType: 'dynamic',
      emailTo: 'fallback@example.com',
      emailFieldKey: 'nonExistentField',
      formData: {
        name: 'Alice Brown',
        email: 'alice@example.com',
        department: 'marketing',
        message: 'Test message with missing dynamic field'
      },
      expectedRecipient: 'fallback@example.com'
    },
    {
      name: 'Dynamic Email Routing - Empty Field Key',
      description: 'Should fallback to static emailTo when field key is empty',
      emailRoutingType: 'dynamic',
      emailTo: 'fallback@example.com',
      emailFieldKey: '', // Empty field key should cause fallback
      formData: {
        name: 'Charlie Wilson',
        email: 'charlie@example.com',
        department: 'finance',
        message: 'Test message with empty field key'
      },
      expectedRecipient: 'fallback@example.com'
    }
  ];

  for (const testCase of testCases) {
    console.log(`\n--- ${testCase.name} ---`);
    console.log(`Description: ${testCase.description}`);
    console.log(`Expected recipient: ${testCase.expectedRecipient}`);
    
    try {
      // Build form data
      const formData = new URLSearchParams();
      
      // Add form configuration
      formData.append('formPath', '/easya11y/test/dynamic-routing-form');
      formData.append('submissionType', 'email');
      formData.append('emailTo', testCase.emailTo);
      formData.append('emailSubject', `Test Dynamic Routing - ${testCase.name}`);
      formData.append('emailFrom', 'noreply@example.com');
      formData.append('emailRoutingType', testCase.emailRoutingType);
      formData.append('emailFieldKey', testCase.emailFieldKey);
      
      // Add form field data
      for (const [key, value] of Object.entries(testCase.formData)) {
        formData.append(`formData[${key}]`, value);
      }
      
      console.log(`Config: routingType=${testCase.emailRoutingType}, fieldKey=${testCase.emailFieldKey}`);
      
      // Make the request
      const response = await axios.post(
        `${baseUrl}${restEndpoint}`,
        formData.toString(),
        {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-Requested-With': 'XMLHttpRequest'
          },
          auth: {
            username: 'superuser',
            password: 'superuser'
          }
        }
      );
      
      console.log('Response:', response.data);
      
      if (response.data.success) {
        console.log('✓ Submission successful');
        console.log(`  Submission ID: ${response.data.submissionId}`);
        console.log(`  Check server logs to verify email was sent to: ${testCase.expectedRecipient}`);
      } else {
        console.log('✗ Submission failed');
      }
      
    } catch (error) {
      console.error('Error:', error.response?.data || error.message);
    }
  }
  
  console.log('\n\n--- Dynamic Email Routing Test Complete ---');
  console.log('\nSummary:');
  console.log('- Static routing should send to the configured static email address');
  console.log('- Dynamic routing should use the email from the specified form field');
  console.log('- Invalid or missing dynamic emails should fallback to the static email');
  console.log('\nCheck your server logs for email send confirmations and routing decisions.');
}

// Run the test
testDynamicEmailRouting().catch(console.error);