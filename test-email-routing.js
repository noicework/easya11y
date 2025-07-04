const axios = require('axios');

// Configuration
const baseUrl = 'http://localhost:8080/magnoliaAuthor';
const restEndpoint = '/.rest/easya11y/submit';

// Test form submission with email routing
async function testEmailRouting() {
  console.log('Testing easya11y Email Routing Feature\n');
  
  // Test cases for different states
  const testCases = [
    {
      state: 'NSW',
      expectedEmail: 'nsw@example.com',
      data: {
        name: 'John Doe',
        email: 'john@example.com',
        state: 'NSW',
        message: 'Test submission for NSW'
      }
    },
    {
      state: 'QLD',
      expectedEmail: 'qld@example.com',
      data: {
        name: 'Jane Smith',
        email: 'jane@example.com',
        state: 'QLD',
        message: 'Test submission for QLD'
      }
    },
    {
      state: 'VIC',
      expectedEmail: 'default@example.com', // Should use default if not in routing rules
      data: {
        name: 'Bob Johnson',
        email: 'bob@example.com',
        state: 'VIC',
        message: 'Test submission for VIC'
      }
    }
  ];

  // Email routing configuration that would be set in the form container
  const emailRoutingRules = [
    { fieldValue: 'NSW', emailAddress: 'nsw@example.com', ccAddresses: 'manager@example.com' },
    { fieldValue: 'QLD', emailAddress: 'qld@example.com', ccAddresses: '' },
    { fieldValue: 'SA', emailAddress: 'sa@example.com', ccAddresses: 'regional@example.com' },
    { fieldValue: 'NT', emailAddress: 'nt@example.com', ccAddresses: '' }
  ];

  for (const testCase of testCases) {
    console.log(`\n--- Testing submission for ${testCase.state} ---`);
    
    try {
      // Build form data
      const formData = new URLSearchParams();
      
      // Add form configuration
      formData.append('formPath', '/easya11y/test/contact-form');
      formData.append('submissionType', 'email');
      formData.append('emailTo', 'default@example.com'); // Default email
      formData.append('emailSubject', `Contact Form - ${testCase.state}`);
      formData.append('emailFrom', 'noreply@example.com');
      
      // Add email routing configuration
      formData.append('useEmailRouting', 'true');
      formData.append('emailRoutingField', 'state');
      formData.append('emailRoutingRules', JSON.stringify(emailRoutingRules));
      
      // Add form field data
      for (const [key, value] of Object.entries(testCase.data)) {
        formData.append(`formData[${key}]`, value);
      }
      
      console.log(`Submitting with state: ${testCase.state}`);
      console.log(`Expected email recipient: ${testCase.expectedEmail}`);
      
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
      } else {
        console.log('✗ Submission failed');
      }
      
    } catch (error) {
      console.error('Error:', error.response?.data || error.message);
    }
  }
  
  console.log('\n\n--- Email Routing Test Complete ---');
  console.log('\nNote: Check your email server logs to verify emails were sent to the correct addresses.');
  console.log('NSW submissions should go to: nsw@example.com with CC to manager@example.com');
  console.log('QLD submissions should go to: qld@example.com');
  console.log('VIC submissions should go to: default@example.com (fallback)');
}

// Run the test
testEmailRouting().catch(console.error);