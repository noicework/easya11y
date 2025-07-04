const fetch = require('node-fetch');

// Create URL-encoded form data
const params = new URLSearchParams();
params.append('formPath', '/easya11y/Contact-Form');
params.append('formData[name]', 'Test User');
params.append('formData[email]', 'test@example.com');
params.append('formData[message]', 'This is a test submission');

// Send POST request to the endpoint
fetch('http://localhost:8080/magnoliaAuthor/.rest/easya11y/submit', {
  method: 'POST',
  body: params,
  headers: {
    'Content-Type': 'application/x-www-form-urlencoded',
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