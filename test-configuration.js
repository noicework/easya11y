const axios = require('axios');

// Configuration
const baseUrl = 'http://localhost:8080/magnoliaAuthor';
const configEndpoint = '/.rest/easya11y/configuration';

// Test configuration management
async function testConfiguration() {
  console.log('Testing easya11y Configuration Management\n');
  
  // Test configuration to save
  const testConfig = {
    // reCAPTCHA settings
    recaptchaSiteKey: '6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI',
    recaptchaSecretKey: '6LeIxAcTAAAAAGG-vFI1TnRWxMZNFuojJ4WifJWe',
    recaptchaThreshold: '0.7',
    
    // Mailchimp settings
    mailchimpApiKey: 'test-api-key-12345',
    mailchimpDataCenter: 'us1',
    
    // SMTP settings
    smtpEnabled: true,
    smtpHost: 'smtp.gmail.com',
    smtpPort: '587',
    smtpAuth: true,
    smtpStartTls: true,
    smtpUsername: 'test@example.com',
    smtpPassword: 'test-password',
    smtpFrom: 'noreply@example.com',
    
    // Default form settings
    defaultSuccessMessage: 'Thank you for your submission!',
    defaultErrorMessage: 'An error occurred. Please try again.',
    defaultEmailSubject: 'New Form Submission'
  };
  
  try {
    // First, get current configuration
    console.log('1. Getting current configuration...');
    let response = await axios.get(
      `${baseUrl}${configEndpoint}`,
      {
        headers: {
          'Accept': 'application/json',
          'X-Requested-With': 'XMLHttpRequest'
        },
        auth: {
          username: 'superuser',
          password: 'superuser'
        }
      }
    );
    
    console.log('Current configuration:', response.data);
    
    // Save new configuration
    console.log('\n2. Saving new configuration...');
    response = await axios.post(
      `${baseUrl}${configEndpoint}`,
      testConfig,
      {
        headers: {
          'Content-Type': 'application/json',
          'X-Requested-With': 'XMLHttpRequest'
        },
        auth: {
          username: 'superuser',
          password: 'superuser'
        }
      }
    );
    
    console.log('Save response:', response.data);
    
    // Verify configuration was saved
    console.log('\n3. Verifying saved configuration...');
    response = await axios.get(
      `${baseUrl}${configEndpoint}`,
      {
        headers: {
          'Accept': 'application/json',
          'X-Requested-With': 'XMLHttpRequest'
        },
        auth: {
          username: 'superuser',
          password: 'superuser'
        }
      }
    );
    
    console.log('Saved configuration:', response.data);
    
    // Verify that the values match
    if (response.data.success && response.data.configuration) {
      const savedConfig = response.data.configuration;
      let allMatch = true;
      
      console.log('\n4. Verifying configuration values:');
      for (const key in testConfig) {
        const expected = String(testConfig[key]);
        const actual = savedConfig[key];
        const match = expected === actual;
        
        console.log(`  ${key}: ${match ? '✓' : '✗'} (expected: ${expected}, actual: ${actual})`);
        
        if (!match) {
          allMatch = false;
        }
      }
      
      console.log(`\nAll values match: ${allMatch ? '✓ YES' : '✗ NO'}`);
    }
    
  } catch (error) {
    console.error('Error:', error.response?.data || error.message);
  }
  
  console.log('\n\n--- Configuration Test Complete ---');
  console.log('You can now access the Configuration UI in Magnolia:');
  console.log('1. Open the easya11y app');
  console.log('2. Click on "Configuration" in the action bar');
  console.log('3. The configuration UI will open where you can manage all settings');
}

// Run the test
testConfiguration().catch(console.error);