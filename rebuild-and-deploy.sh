#!/bin/bash

# Set script to exit on error
set -e

echo "Building easya11y..."
mvn clean package

# Copy Selenium and WebDriverManager dependencies to a folder
echo "Copying Selenium dependencies..."
mvn dependency:copy-dependencies -DincludeGroupIds=org.seleniumhq.selenium,io.github.bonigarcia -DoutputDirectory=target/selenium-deps

echo "Copying JAR to Magnolia Author instance..."
cp target/easya11y-1.2.1.jar ~/Projects/mmp/apache-tomcat/webapps/magnoliaAuthor/WEB-INF/lib

echo "Copying Selenium JARs to Magnolia Author instance..."
cp target/selenium-deps/*.jar ~/Projects/mmp/apache-tomcat/webapps/magnoliaAuthor/WEB-INF/lib/

echo "Shutting down Tomcat..."
~/Projects/mmp/apache-tomcat/bin/shutdown.sh

echo "Waiting 15 seconds for Tomcat to shut down completely..."
sleep 15

echo "Starting Tomcat..."
~/Projects/mmp/apache-tomcat/bin/startup.sh

echo "Deployment complete. Magnolia is starting up..."
sleep 15

