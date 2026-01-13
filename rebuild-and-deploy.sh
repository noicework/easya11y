#!/bin/bash

# Set script to exit on error
set -e

echo "Building easya11y..."
mvn clean package

# Copy Selenium and WebDriverManager dependencies to a folder
echo "Copying Selenium dependencies..."
mvn dependency:copy-dependencies -DincludeGroupIds=org.seleniumhq.selenium,io.github.bonigarcia -DoutputDirectory=target/selenium-deps

MAGNOLIA_HOME=~/Projects/vccmhw/magnolia-6.4
MAGNOLIA_LIB=$MAGNOLIA_HOME/webapps/magnoliaAuthor/WEB-INF/lib

echo "Removing old easya11y JARs..."
rm -f $MAGNOLIA_LIB/easya11y-*.jar

echo "Copying JAR to Magnolia Author instance..."
cp target/easya11y-1.4.0.jar $MAGNOLIA_LIB/

echo "Copying Selenium JARs to Magnolia Author instance..."
cp target/selenium-deps/*.jar $MAGNOLIA_LIB/

echo "Shutting down Tomcat..."
$MAGNOLIA_HOME/bin/shutdown.sh

echo "Waiting 20 seconds for Tomcat to shut down completely..."
sleep 20

echo "Starting Tomcat..."
$MAGNOLIA_HOME/bin/startup.sh

echo "Deployment complete. Magnolia is starting up..."

