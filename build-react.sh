#!/bin/bash

# EasyA11y React Build Script
# This script builds the React-based accessibility checker

echo "🔨 Building EasyA11y React App..."

# Check if node_modules exists
if [ ! -d "node_modules" ]; then
    echo "📦 Installing dependencies..."
    npm install
fi

# Clean previous builds
echo "🧹 Cleaning previous builds..."
npm run clean

# Build the React app
echo "🏗️  Building React app..."
npm run build

# Copy HTML files
echo "📄 Copying HTML files..."
cp src/main/resources/easya11y/webresources-src/accessibility-checker-react.html src/main/resources/easya11y/webresources/

# Verify build
if [ -f "src/main/resources/easya11y/webresources/js/accessibility-checker.js" ]; then
    echo "✅ Build successful!"
    echo "📍 React app available at: src/main/resources/easya11y/webresources/accessibility-checker-react.html"
else
    echo "❌ Build failed! Check the output above for errors."
    exit 1
fi

echo "🎉 Done!"