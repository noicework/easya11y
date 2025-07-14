#!/bin/bash

# Set script to exit on error
set -e

echo "Building easya11y..."
mvn clean package

echo "Copying JAR to Magnolia Author instance..."
cp target/easya11y-1.1.0.jar ~/Projects/mmp/apache-tomcat/webapps/magnoliaAuthor/WEB-INF/lib

echo "Shutting down Tomcat..."
~/Projects/mmp/apache-tomcat/bin/shutdown.sh

echo "Waiting 15 seconds for Tomcat to shut down completely..."
sleep 15

echo "Starting Tomcat..."
~/Projects/mmp/apache-tomcat/bin/startup.sh

echo "Deployment complete. Magnolia is starting up..."
echo "Showing log output (press Ctrl+C to stop):"
sleep 15

