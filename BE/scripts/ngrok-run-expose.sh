#!/bin/bash

# Script to expose BE server using ngrok with PORT from .env file
# Usage: bash scripts/ngrok-run-expose.sh

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 Starting ngrok tunnel for BE server...${NC}"

# Check if .env file exists
if [ ! -f ".env" ]; then
    echo -e "${RED}❌ Error: .env file not found!${NC}"
    echo -e "${YELLOW}Please make sure you're running this from the BE directory${NC}"
    exit 1
fi

# Read PORT from .env file
PORT=$(grep "^PORT=" .env | cut -d '=' -f2 | tr -d ' ')

# Check if PORT was found
if [ -z "$PORT" ]; then
    echo -e "${RED}❌ Error: PORT not found in .env file!${NC}"
    echo -e "${YELLOW}Please add PORT variable to your .env file${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Found PORT: ${PORT}${NC}"

# Check if ngrok is installed
if ! command -v ngrok &> /dev/null; then
    echo -e "${RED}❌ Error: ngrok is not installed!${NC}"
    echo -e "${YELLOW}Please install ngrok: https://ngrok.com/download${NC}"
    echo -e "${YELLOW}Then run: ngrok authtoken YOUR_AUTH_TOKEN followed instruction on the ngrok website${NC}"
    exit 1
fi

# Check if the server is running on the specified port
echo -e "${BLUE}🔍 Checking if server is running on port ${PORT}...${NC}"
if ! nc -z localhost $PORT 2>/dev/null; then
    echo -e "${YELLOW}⚠️  Warning: No service detected on port ${PORT}${NC}"
    echo -e "${YELLOW}Make sure your BE server is running: npm start${NC}"
    echo -e "${BLUE}Continuing with ngrok tunnel anyway...${NC}"
fi

echo -e "${GREEN}🌐 Starting ngrok tunnel on port ${PORT}...${NC}"
echo -e "${BLUE}📱 Your Android app should use the ngrok URL for API calls${NC}"
echo -e "${BLUE} COPY the forwarding URL from the ngrok output below and add into .env :${NC}"
echo -e "${YELLOW}Press Ctrl+C to stop the tunnel${NC}"
echo ""

# Start ngrok with the PORT from .env
ngrok http ${PORT} --host-header="localhost:${PORT}" --log stdout