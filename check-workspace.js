const axios = require('axios');

const BASE_URL = 'http://localhost:8080/magnoliaAuthor/.rest/nodes/v1/easya11y';
const auth = {
    username: 'superuser',
    password: 'superuser'
};

async function checkWorkspace() {
    try {
        console.log('Checking easya11y workspace structure...\n');
        
        // Check root
        const rootResponse = await axios.get(BASE_URL, { auth });
        console.log('Root nodes:', Object.keys(rootResponse.data.nodes || {}));
        
        // Try to create scanResults node if it doesn't exist
        console.log('\nChecking if scanResults node exists...');
        try {
            await axios.get(`${BASE_URL}/scanResults`, { auth });
            console.log('scanResults node exists');
        } catch (error) {
            if (error.response && error.response.status === 404) {
                console.log('scanResults node does not exist, creating it...');
                try {
                    await axios.put(`${BASE_URL}/scanResults`, {
                        name: 'scanResults',
                        type: 'mgnl:folder',
                        properties: []
                    }, { auth });
                    console.log('✓ scanResults node created successfully');
                } catch (createError) {
                    console.error('Error creating scanResults node:', createError.message);
                }
            }
        }
        
    } catch (error) {
        console.error('Error checking workspace:', error.message);
    }
}

checkWorkspace();