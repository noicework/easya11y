const axios = require('axios');

// Configuration
const baseUrl = 'http://localhost:8080/magnoliaAuthor';

// Test various endpoints
async function testEndpoints() {
  console.log('Testing easya11y REST Endpoints\n');
  
  const endpoints = [
    { path: '/.rest/easya11y/formlist', method: 'GET', name: 'Form List' },
    { path: '/.rest/easya11y/submissions/list', method: 'GET', name: 'Submissions List' },
    { path: '/.rest/easya11y/configuration', method: 'GET', name: 'Configuration' },
    { path: '/.rest/delivery/pages/v1/website', method: 'GET', name: 'Pages API (test)' }
  ];
  
  for (const endpoint of endpoints) {
    try {
      console.log(`Testing ${endpoint.name} (${endpoint.method} ${endpoint.path})...`);
      
      const response = await axios({
        method: endpoint.method,
        url: `${baseUrl}${endpoint.path}`,
        headers: {
          'Accept': 'application/json',
          'X-Requested-With': 'XMLHttpRequest'
        },
        auth: {
          username: 'superuser',
          password: 'superuser'
        },
        validateStatus: () => true // Accept any status code
      });
      
      console.log(`  Status: ${response.status} ${response.statusText}`);
      if (response.status === 200) {
        console.log(`  ✓ Endpoint is working`);
      } else if (response.status === 404) {
        console.log(`  ✗ Endpoint not found`);
      } else {
        console.log(`  ? Unexpected status`);
      }
      
    } catch (error) {
      console.log(`  ✗ Error: ${error.message}`);
    }
    console.log('');
  }
}

// Run the test
testEndpoints().catch(console.error);