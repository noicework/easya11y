const axios = require('axios');

// Base URL for your Magnolia instance
const BASE_URL = 'http://localhost:8080';
const AUTH = {
  username: 'superuser',
  password: 'superuser'
};

async function testSubmissionsAPI() {
  try {
    console.log('Testing Form Submissions API...\n');

    // Test 1: Get all submissions
    console.log('1. Getting all submissions:');
    const allSubmissionsResponse = await axios.get(
      `${BASE_URL}/.rest/easya11y/submissions`,
      { auth: AUTH }
    );
    console.log('Total submissions:', allSubmissionsResponse.data.total);
    console.log('Success:', allSubmissionsResponse.data.success);
    console.log('\n');

    // Test 2: Get submissions for a specific form
    const formPath = 'easya11y/test/Contact-Form'; // Update this to match your form path
    console.log(`2. Getting submissions for form: ${formPath}`);
    const formSubmissionsResponse = await axios.get(
      `${BASE_URL}/.rest/easya11y/submissions/${formPath}`,
      { auth: AUTH }
    );
    console.log('Form submissions:', formSubmissionsResponse.data.submissions.length);
    console.log('Success:', formSubmissionsResponse.data.success);
    
    if (formSubmissionsResponse.data.submissions.length > 0) {
      console.log('\nFirst submission details:');
      const firstSubmission = formSubmissionsResponse.data.submissions[0];
      console.log('ID:', firstSubmission.id);
      console.log('Timestamp:', firstSubmission.timestamp);
      console.log('Fields:', firstSubmission.fields);
    }
    console.log('\n');

    // Test 3: Export to CSV
    console.log('3. Testing CSV export:');
    const csvResponse = await axios.get(
      `${BASE_URL}/.rest/easya11y/submissions/export/csv`,
      { 
        auth: AUTH,
        responseType: 'text'
      }
    );
    console.log('CSV export successful!');
    console.log('First 200 characters of CSV:');
    console.log(csvResponse.data.substring(0, 200) + '...');

  } catch (error) {
    console.error('Error:', error.response ? error.response.data : error.message);
  }
}

testSubmissionsAPI();