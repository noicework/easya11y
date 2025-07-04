const axios = require('axios');

// Configuration
const baseUrl = 'http://localhost:8080/magnoliaAuthor';
const restEndpoint = '/.rest/easya11y/submit';

// Comprehensive test for all email features
async function testAllEmailFeatures() {
  console.log('Testing All easya11y Email Features\n');
  console.log('=' .repeat(60));
  
  // Test cases covering both static and dynamic routing
  const testCases = [
    {
      category: 'Static Email Routing',
      name: 'Basic Static Email',
      description: 'Standard static email routing to a fixed address',
      config: {
        emailRoutingType: 'static',
        emailTo: 'admin@example.com',
        emailFieldKey: ''
      },
      formData: {
        name: 'John Doe',
        email: 'john@example.com',
        department: 'sales',
        message: 'Static routing test message'
      },
      expectedBehavior: 'Email sent to admin@example.com (static)'
    },
    {
      category: 'Dynamic Email Routing',
      name: 'Dynamic to Department Email',
      description: 'Dynamic routing using department email field',
      config: {
        emailRoutingType: 'dynamic',
        emailTo: 'fallback@example.com',
        emailFieldKey: 'departmentEmail'
      },
      formData: {
        name: 'Jane Smith',
        email: 'jane@example.com',
        departmentEmail: 'sales@example.com',
        department: 'sales',
        message: 'Dynamic routing to sales department'
      },
      expectedBehavior: 'Email sent to sales@example.com (from departmentEmail field)'
    },
    {
      category: 'Dynamic Email Routing',
      name: 'Dynamic to Contact Email',
      description: 'Dynamic routing using the main contact email field',
      config: {
        emailRoutingType: 'dynamic',
        emailTo: 'fallback@example.com',
        emailFieldKey: 'email'
      },
      formData: {
        name: 'Bob Johnson',
        email: 'support@example.com',
        department: 'support',
        message: 'Dynamic routing using main email field'
      },
      expectedBehavior: 'Email sent to support@example.com (from email field)'
    },
    {
      category: 'Dynamic Email Routing - Fallback',
      name: 'Invalid Dynamic Email',
      description: 'Dynamic routing with invalid email should fallback',
      config: {
        emailRoutingType: 'dynamic',
        emailTo: 'fallback@example.com',
        emailFieldKey: 'departmentEmail'
      },
      formData: {
        name: 'Alice Brown',
        email: 'alice@example.com',
        departmentEmail: 'not-an-email',
        department: 'marketing',
        message: 'Test with invalid email in dynamic field'
      },
      expectedBehavior: 'Email sent to fallback@example.com (invalid dynamic email)'
    },
    {
      category: 'Dynamic Email Routing - Fallback',
      name: 'Missing Dynamic Field',
      description: 'Dynamic routing with missing field should fallback',
      config: {
        emailRoutingType: 'dynamic',
        emailTo: 'fallback@example.com',
        emailFieldKey: 'nonExistentField'
      },
      formData: {
        name: 'Charlie Wilson',
        email: 'charlie@example.com',
        department: 'finance',
        message: 'Test with missing dynamic field'
      },
      expectedBehavior: 'Email sent to fallback@example.com (missing field)'
    },
    {
      category: 'Dynamic Email Routing - Fallback',
      name: 'Empty Dynamic Field',
      description: 'Dynamic routing with empty field should fallback',
      config: {
        emailRoutingType: 'dynamic',
        emailTo: 'fallback@example.com',
        emailFieldKey: 'departmentEmail'
      },
      formData: {
        name: 'Diana Davis',
        email: 'diana@example.com',
        departmentEmail: '', // Empty field
        department: 'hr',
        message: 'Test with empty dynamic field'
      },
      expectedBehavior: 'Email sent to fallback@example.com (empty field)'
    },
    {
      category: 'Edge Cases',
      name: 'No Routing Type Specified',
      description: 'Should default to static routing',
      config: {
        emailRoutingType: '', // Empty routing type should default to static
        emailTo: 'default@example.com',
        emailFieldKey: 'email'
      },
      formData: {
        name: 'Eve Evans',
        email: 'eve@example.com',
        department: 'legal',
        message: 'Test with no routing type specified'
      },
      expectedBehavior: 'Email sent to default@example.com (default static)'
    },
    {
      category: 'Edge Cases',
      name: 'Unknown Routing Type',
      description: 'Should fallback to static routing',
      config: {
        emailRoutingType: 'unknown',
        emailTo: 'default@example.com',
        emailFieldKey: 'email'
      },
      formData: {
        name: 'Frank Foster',
        email: 'frank@example.com',
        department: 'operations',
        message: 'Test with unknown routing type'
      },
      expectedBehavior: 'Email sent to default@example.com (unknown type fallback)'
    }
  ];

  let passedTests = 0;
  let totalTests = testCases.length;

  for (const [index, testCase] of testCases.entries()) {
    console.log(`\n${index + 1}. ${testCase.category}: ${testCase.name}`);
    console.log(`   ${testCase.description}`);
    console.log(`   Expected: ${testCase.expectedBehavior}`);
    
    try {
      // Build form data
      const formData = new URLSearchParams();
      
      // Add form configuration
      formData.append('formPath', '/easya11y/test/comprehensive-email-test');
      formData.append('submissionType', 'email');
      formData.append('emailTo', testCase.config.emailTo);
      formData.append('emailSubject', `Test: ${testCase.name}`);
      formData.append('emailFrom', 'noreply@example.com');
      formData.append('emailRoutingType', testCase.config.emailRoutingType);
      formData.append('emailFieldKey', testCase.config.emailFieldKey);
      
      // Add form field data
      for (const [key, value] of Object.entries(testCase.formData)) {
        formData.append(`formData[${key}]`, value);
      }
      
      // Show configuration for debugging
      const configSummary = [
        `routing: ${testCase.config.emailRoutingType || 'default'}`,
        `fieldKey: ${testCase.config.emailFieldKey || 'none'}`,
        `staticEmail: ${testCase.config.emailTo}`
      ].join(', ');
      console.log(`   Config: ${configSummary}`);
      
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
          },
          timeout: 10000 // 10 second timeout
        }
      );
      
      if (response.data.success) {
        console.log(`   ✓ SUCCESS - Submission ID: ${response.data.submissionId}`);
        passedTests++;
      } else {
        console.log(`   ✗ FAILED - ${response.data.message || 'Unknown error'}`);
      }
      
    } catch (error) {
      console.log(`   ✗ ERROR - ${error.response?.data?.message || error.message}`);
      if (error.response?.data) {
        console.log(`     Response: ${JSON.stringify(error.response.data)}`);
      }
    }
    
    // Small delay between requests
    await new Promise(resolve => setTimeout(resolve, 500));
  }
  
  console.log('\n' + '=' .repeat(60));
  console.log('COMPREHENSIVE EMAIL ROUTING TEST SUMMARY');
  console.log('=' .repeat(60));
  console.log(`Total Tests: ${totalTests}`);
  console.log(`Passed: ${passedTests}`);
  console.log(`Failed: ${totalTests - passedTests}`);
  console.log(`Success Rate: ${((passedTests / totalTests) * 100).toFixed(1)}%`);
  
  console.log('\nIMPORTANT NOTES:');
  console.log('- Check server logs for detailed email routing decisions');
  console.log('- Verify that emails are sent to the expected recipients');
  console.log('- Static routing should always use the configured emailTo address');
  console.log('- Dynamic routing should use form field values when valid');
  console.log('- Invalid/missing dynamic values should fallback to static emailTo');
  
  console.log('\nSERVER LOG VERIFICATION:');
  console.log('Look for log messages like:');
  console.log('  "Using static email routing: email@example.com"');
  console.log('  "Using dynamic email from field \'fieldName\': email@example.com"');
  console.log('  "Invalid email address found in field \'fieldName\': ..., falling back to static email: ..."');
  console.log('  "Email sent to: email@example.com using routing type: static/dynamic for form: ..."');
}

// Run the comprehensive test
testAllEmailFeatures().catch(console.error);