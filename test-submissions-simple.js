const fetch = require('node-fetch');

const BASE_URL = 'http://localhost:8080/magnoliaAuthor';
const AUTH = 'Basic c3VwZXJ1c2VyOnN1cGVydXNlcg=='; // superuser:superuser

async function testSubmissionsAPI() {
  try {
    console.log('Testing Form Submissions API...\n');

    // Test 1: Get submissions for a specific form
    const formPath = 'easya11y/Contact-Form';
    console.log(`1. Getting submissions for form: ${formPath}`);
    
    const response = await fetch(
      `${BASE_URL}/.rest/easya11y/submissions/${formPath}`,
      { 
        headers: {
          'Authorization': AUTH
        }
      }
    );
    
    console.log('Status:', response.status);
    const data = await response.json();
    console.log('Response:', JSON.stringify(data, null, 2));
    
    // Test 2: Get all submissions via list endpoint
    console.log('\n2. Getting all submissions via list endpoint:');
    const listResponse = await fetch(
      `${BASE_URL}/.rest/easya11y/submissions/list`,
      { 
        headers: {
          'Authorization': AUTH
        }
      }
    );
    
    console.log('Status:', listResponse.status);
    const listData = await listResponse.json();
    console.log('Response:', JSON.stringify(listData, null, 2));
    
    // Test 3: Export to CSV
    console.log('\n3. Testing CSV export:');
    const csvResponse = await fetch(
      `${BASE_URL}/.rest/easya11y/submissions/export/csv?formPath=${formPath}`,
      { 
        headers: {
          'Authorization': AUTH
        }
      }
    );
    
    console.log('CSV Status:', csvResponse.status);
    if (csvResponse.ok) {
      const csvData = await csvResponse.text();
      console.log('CSV export successful!');
      console.log('First 200 characters of CSV:');
      console.log(csvData.substring(0, 200) + '...');
    }

  } catch (error) {
    console.error('Error:', error.message);
  }
}

testSubmissionsAPI();