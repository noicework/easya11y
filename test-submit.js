const fetch = require('node-fetch');
const FormData = require('form-data');

// Create form data
const form = new FormData();
form.append('formPath', '/easya11y/test/contact-form');
form.append('name', 'Test User');
form.append('email', 'test@example.com');
form.append('message', 'This is a test submission');

// Send POST request to the endpoint
fetch('http://localhost:8080/magnoliaAuthor/.rest/easya11y/submit', {
  method: 'POST',
  body: form,
  headers: {
    // Basic auth for superuser:superuser (base64 encoded)
    'Authorization': 'Basic c3VwZXJ1c2VyOnN1cGVydXNlcg=='
  }
})
.then(response => {
  console.log('Status:', response.status);
  return response.text().then(text => {
    console.log('Raw Response:', text);
    try {
      return JSON.parse(text);
    } catch (e) {
      console.log('Not JSON response');
      return { error: 'Invalid JSON', text };
    }
  });
})
.then(data => {
  console.log('Response:', JSON.stringify(data, null, 2));
})
.catch(error => {
  console.error('Error:', error);
});

console.log('Sending test request to FormSubmissionEndpoint...');