#!/bin/bash

echo "Checking Magnolia REST endpoints registration..."
echo ""

# Check if the endpoint YAML files are in the JAR
echo "1. Checking if configuration.yaml is in the deployed JAR:"
jar tf ~/magnolia-6.3/apache-tomcat-9.0.99/webapps/magnoliaAuthor/WEB-INF/lib/easya11y-1.0-SNAPSHOT.jar | grep -E "restEndpoints.*yaml"

echo ""
echo "2. Checking Magnolia logs for REST endpoint registration:"
tail -n 1000 ~/magnolia-6.3/apache-tomcat-9.0.99/logs/catalina.out | grep -i "rest\|endpoint\|configuration" | tail -20

echo ""
echo "3. Testing if the basic REST services are working:"
curl -s -u superuser:superuser "http://localhost:8080/magnoliaAuthor/.rest/nodes/v1/website" | head -c 100