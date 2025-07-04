const fetch = require('node-fetch');
const { performance } = require('perf_hooks');

// Colors for console output
const colors = {
    reset: '\x1b[0m',
    bright: '\x1b[1m',
    red: '\x1b[31m',
    green: '\x1b[32m',
    yellow: '\x1b[33m',
    blue: '\x1b[34m',
    cyan: '\x1b[36m'
};

// Configuration
const BASE_URL = 'http://localhost:8080/magnoliaAuthor';
const AUTH = 'Basic ' + Buffer.from('superuser:superuser').toString('base64');

// Test results
let totalTests = 0;
let passedTests = 0;
let failedTests = 0;
const testResults = [];

// Helper functions
function log(message, color = '') {
    console.log(color + message + colors.reset);
}

function logSection(title) {
    console.log('\n' + colors.blue + '═'.repeat(80) + colors.reset);
    log(title.padStart(40 + title.length/2).padEnd(80), colors.bright + colors.blue);
    console.log(colors.blue + '═'.repeat(80) + colors.reset + '\n');
}

async function testEndpoint(method, endpoint, description, options = {}) {
    totalTests++;
    const testNumber = totalTests;
    
    log(`Test ${testNumber}: ${description}`, colors.yellow);
    log(`  Method: ${method}`, colors.cyan);
    log(`  Endpoint: ${endpoint}`, colors.cyan);
    
    const startTime = performance.now();
    
    try {
        const fetchOptions = {
            method,
            headers: {
                'Authorization': AUTH,
                ...options.headers
            }
        };
        
        if (options.body) {
            fetchOptions.body = options.body;
            log(`  Data: ${options.body}`, colors.cyan);
        }
        
        const response = await fetch(`${BASE_URL}${endpoint}`, fetchOptions);
        const endTime = performance.now();
        const duration = (endTime - startTime).toFixed(2);
        
        const expectedStatus = options.expectedStatus || 200;
        const success = response.status === expectedStatus;
        
        if (success) {
            log(`  ✓ Status: ${response.status} (Expected: ${expectedStatus})`, colors.green);
            log(`  ✓ Response time: ${duration}ms`, colors.green);
            passedTests++;
        } else {
            log(`  ✗ Status: ${response.status} (Expected: ${expectedStatus})`, colors.red);
            log(`  ✗ Response time: ${duration}ms`, colors.red);
            failedTests++;
        }
        
        // Parse response
        let data;
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            data = await response.json();
            
            // Show relevant data based on endpoint
            if (endpoint.includes('/formlist')) {
                const formCount = data.items ? data.items.length : 0;
                log(`  Forms found: ${formCount}`, colors.blue);
                if (data.items && data.items.length > 0) {
                    log(`  Sample forms:`, colors.blue);
                    data.items.slice(0, 3).forEach(form => {
                        log(`    - ${form.name} (${form.path})`, colors.blue);
                    });
                }
            } else if (endpoint.includes('/submissions')) {
                if (endpoint.includes('/export/csv')) {
                    // CSV export - just show success
                    log(`  CSV export successful`, colors.blue);
                } else {
                    const total = data.total || 0;
                    log(`  Submissions found: ${total}`, colors.blue);
                    if (data.submissions && data.submissions.length > 0) {
                        log(`  Latest submission: ${new Date(data.submissions[0].timestamp).toLocaleString()}`, colors.blue);
                    }
                }
            } else if (endpoint.includes('/submit')) {
                if (data.submissionId) {
                    log(`  Submission ID: ${data.submissionId}`, colors.blue);
                }
                if (data.message) {
                    log(`  Message: ${data.message}`, colors.blue);
                }
            }
        } else if (contentType && contentType.includes('text/csv')) {
            const text = await response.text();
            const lines = text.split('\n').length;
            log(`  CSV rows: ${lines}`, colors.blue);
            log(`  CSV preview: ${text.substring(0, 100)}...`, colors.blue);
        }
        
        // Store result
        testResults.push({
            test: testNumber,
            description,
            endpoint,
            method,
            status: response.status,
            expectedStatus,
            success,
            duration,
            error: !success ? data?.message || data?.error?.message : null
        });
        
        if (!success && data) {
            const errorMsg = data.message || data.error?.message || JSON.stringify(data);
            log(`  Error: ${errorMsg}`, colors.red);
        }
        
    } catch (error) {
        failedTests++;
        log(`  ✗ Error: ${error.message}`, colors.red);
        
        testResults.push({
            test: testNumber,
            description,
            endpoint,
            method,
            status: 'ERROR',
            expectedStatus: options.expectedStatus || 200,
            success: false,
            duration: 0,
            error: error.message
        });
    }
    
    console.log(''); // Empty line between tests
}

async function runAllTests() {
    logSection('easya11y REST API Endpoint Tests');
    
    log(`Base URL: ${BASE_URL}`, colors.yellow);
    log(`Testing as: superuser\n`, colors.yellow);
    
    // Test 1: Form List
    await testEndpoint('GET', '/.rest/easya11y/formlist', 
        'Get list of all forms');
    
    // Test 2: Form List with path
    await testEndpoint('GET', '/.rest/easya11y/formlist?path=/easya11y', 
        'Get forms under specific path');
    
    // Test 3: Valid form submission
    const formData = new URLSearchParams({
        formPath: '/easya11y/Contact-Form',
        'formData[name]': 'Test User',
        'formData[email]': 'test@example.com',
        'formData[message]': 'Automated test submission from Node.js'
    });
    
    await testEndpoint('POST', '/.rest/easya11y/submit', 
        'Submit a test form', {
            body: formData.toString(),
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            }
        });
    
    // Test 4: Invalid form submission
    const invalidData = new URLSearchParams({
        formPath: '/nonexistent/form',
        'formData[test]': 'value'
    });
    
    await testEndpoint('POST', '/.rest/easya11y/submit', 
        'Submit to non-existent form (should fail)', {
            body: invalidData.toString(),
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            expectedStatus: 500
        });
    
    // Test 5: Get all submissions
    await testEndpoint('GET', '/.rest/easya11y/submissions/list', 
        'Get all form submissions');
    
    // Test 6: Get submissions for specific form
    await testEndpoint('GET', '/.rest/easya11y/submissions/list?formPath=/easya11y/Contact-Form', 
        'Get submissions for Contact Form');
    
    // Test 7: Get submissions by path parameter
    await testEndpoint('GET', '/.rest/easya11y/submissions/easya11y/Contact-Form', 
        'Get submissions using path parameter');
    
    // Test 8: Export all submissions as CSV
    await testEndpoint('GET', '/.rest/easya11y/submissions/export/csv', 
        'Export all submissions as CSV');
    
    // Test 9: Export specific form submissions as CSV
    await testEndpoint('GET', '/.rest/easya11y/submissions/export/csv?formPath=/easya11y/Contact-Form', 
        'Export Contact Form submissions as CSV');
    
    // Test 10: Submit with email notification
    const emailData = new URLSearchParams({
        formPath: '/easya11y/Contact-Form',
        submissionType: 'email',
        emailTo: 'test@example.com',
        emailSubject: 'Test Submission',
        'formData[name]': 'Email Test User',
        'formData[email]': 'sender@example.com',
        'formData[message]': 'Testing email submission feature'
    });
    
    await testEndpoint('POST', '/.rest/easya11y/submit', 
        'Submit form with email notification', {
            body: emailData.toString(),
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            }
        });
    
    // Test 11: Test JCR delivery endpoint (if configured)
    await testEndpoint('GET', '/.rest/delivery/forms/v2/easya11y', 
        'Get forms via JCR delivery endpoint (if configured)', {
            expectedStatus: 200  // May be 404 if not configured
        });
    
    // Print summary
    logSection('Test Summary');
    
    console.log(colors.bright + 'Results by Status:' + colors.reset);
    const statusGroups = {};
    testResults.forEach(result => {
        const key = `${result.status} (${result.success ? 'PASS' : 'FAIL'})`;
        statusGroups[key] = (statusGroups[key] || 0) + 1;
    });
    
    Object.entries(statusGroups).forEach(([status, count]) => {
        const color = status.includes('PASS') ? colors.green : colors.red;
        log(`  ${status}: ${count}`, color);
    });
    
    console.log('\n' + colors.bright + 'Performance Summary:' + colors.reset);
    const times = testResults.filter(r => r.duration > 0).map(r => parseFloat(r.duration));
    if (times.length > 0) {
        const avgTime = (times.reduce((a, b) => a + b, 0) / times.length).toFixed(2);
        const minTime = Math.min(...times).toFixed(2);
        const maxTime = Math.max(...times).toFixed(2);
        
        log(`  Average response time: ${avgTime}ms`, colors.blue);
        log(`  Fastest response: ${minTime}ms`, colors.green);
        log(`  Slowest response: ${maxTime}ms`, colors.yellow);
    }
    
    console.log('\n' + colors.bright + 'Overall Results:' + colors.reset);
    log(`  Total Tests: ${totalTests}`, colors.yellow);
    log(`  Passed: ${passedTests}`, colors.green);
    log(`  Failed: ${failedTests}`, colors.red);
    
    if (failedTests === 0) {
        log('\n✓ All tests passed!', colors.bright + colors.green);
    } else {
        log('\n✗ Some tests failed!', colors.bright + colors.red);
        console.log('\n' + colors.bright + 'Failed Tests:' + colors.reset);
        testResults.filter(r => !r.success).forEach(result => {
            log(`  Test ${result.test}: ${result.description}`, colors.red);
            log(`    Endpoint: ${result.method} ${result.endpoint}`, colors.red);
            log(`    Error: ${result.error || `Status ${result.status}`}`, colors.red);
        });
    }
    
    console.log('\n' + colors.blue + '═'.repeat(80) + colors.reset + '\n');
    
    // Exit with appropriate code
    process.exit(failedTests === 0 ? 0 : 1);
}

// Run tests
runAllTests().catch(error => {
    log(`\nFatal error: ${error.message}`, colors.red);
    process.exit(1);
});