const axios = require('axios');

const BASE_URL = 'http://localhost:8080/magnoliaAuthor/.rest/easya11y';
const auth = {
    username: 'superuser',
    password: 'superuser'
};

async function testEndpoints() {
    console.log('Testing EasyA11y Endpoints...\n');
    
    try {
        // Test 1: List pages
        console.log('1. Testing GET /pages endpoint...');
        const pagesResponse = await axios.get(`${BASE_URL}/pages`, { auth });
        console.log('✓ Pages endpoint working');
        console.log(`  Found ${pagesResponse.data.totalPages} pages\n`);
        
        // Test 2: List scan results
        console.log('2. Testing GET /results endpoint...');
        const resultsResponse = await axios.get(`${BASE_URL}/results`, { auth });
        console.log('✓ Results endpoint working');
        console.log(`  Found ${resultsResponse.data.totalResults} scan results\n`);
        
        // Test 3: Test scan initiation (with a fake page path)
        console.log('3. Testing POST /scan/initiate endpoint...');
        try {
            const scanInitResponse = await axios.post(`${BASE_URL}/scan/initiate`, {
                pagePath: '/test-page'
            }, { auth });
            console.log('✓ Scan initiate endpoint working');
            console.log(`  Response: ${scanInitResponse.data.message}\n`);
        } catch (error) {
            if (error.response && error.response.status === 404) {
                console.log('✓ Scan initiate endpoint working (correctly returned 404 for non-existent page)\n');
            } else {
                throw error;
            }
        }
        
        // Test 4: Export CSV endpoint
        console.log('4. Testing GET /results/export/csv endpoint...');
        const csvResponse = await axios.get(`${BASE_URL}/results/export/csv`, { auth });
        console.log('✓ CSV export endpoint working');
        console.log(`  Response type: ${csvResponse.headers['content-type']}\n`);
        
        console.log('All endpoints are working correctly! ✨');
        
    } catch (error) {
        console.error('Error testing endpoints:', error.message);
        if (error.response) {
            console.error('Response data:', error.response.data);
        }
    }
}

testEndpoints();