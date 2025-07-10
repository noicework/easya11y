#!/bin/bash

echo "Generating GPG key for Maven Central signing..."
echo ""
echo "You will be prompted for:"
echo "1. Your name and email"
echo "2. A PASSPHRASE - remember this for your settings.xml!"
echo ""

# Generate key with specific requirements for Maven Central
gpg --full-generate-key

echo ""
echo "Listing your keys..."
gpg --list-secret-keys --keyid-format LONG

echo ""
echo "To publish your public key to a keyserver, run:"
echo "gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID"
echo ""
echo "Add your passphrase to settings.xml in the gpg.passphrase field"