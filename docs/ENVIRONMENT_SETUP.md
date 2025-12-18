# Environment Setup Guide

This guide explains how to configure environment variables for the Smart Customer Support Ticket Triage System.

## Why Environment Variables?

The Gemini API key is stored in an environment variable instead of being hardcoded in `application.yml` for security reasons:
- ✅ Prevents accidental exposure of API keys in version control
- ✅ Allows different keys for development, staging, and production
- ✅ Follows security best practices
- ✅ Makes it easy to rotate keys without code changes

## Setup Methods

### Method 1: Using .env File (Recommended for Local Development)

1. **Create a `.env` file** in the project root:
   ```bash
   cd smart-customer-support-ticket-triage
   touch .env
   ```

2. **Add your API key** to the `.env` file:
   ```bash
   GEMINI_API_KEY=your-actual-api-key-here
   ```

3. **Load the environment variable** before running:
   ```bash
   export $(cat .env | xargs) && ./gradlew bootRun
   ```

   Or use this one-liner:
   ```bash
   export GEMINI_API_KEY=your-api-key && ./gradlew bootRun
   ```

### Method 2: Export in Terminal Session

For a single terminal session:
```bash
export GEMINI_API_KEY=your-actual-api-key-here
./gradlew bootRun
```

### Method 3: Add to Shell Profile (Permanent)

For permanent setup, add to your shell profile:

**For Zsh (macOS default)**:
```bash
echo 'export GEMINI_API_KEY=your-actual-api-key-here' >> ~/.zshrc
source ~/.zshrc
```

**For Bash**:
```bash
echo 'export GEMINI_API_KEY=your-actual-api-key-here' >> ~/.bashrc
source ~/.bashrc
```

### Method 4: IDE Configuration

**IntelliJ IDEA**:
1. Go to Run → Edit Configurations
2. Select your Spring Boot run configuration
3. Add to Environment Variables: `GEMINI_API_KEY=your-key`

**VS Code**:
1. Create `.vscode/launch.json`
2. Add to configuration:
   ```json
   {
     "env": {
       "GEMINI_API_KEY": "your-api-key-here"
     }
   }
   ```

## Verify Configuration

To check if the environment variable is set:
```bash
echo $GEMINI_API_KEY
```

You should see your API key printed (or a placeholder if using the default).

## Security Best Practices

### ✅ DO:
- Store API keys in `.env` files (already in `.gitignore`)
- Use different keys for different environments
- Rotate keys regularly
- Use environment variables or secret management systems

### ❌ DON'T:
- Commit `.env` files to Git
- Hardcode API keys in source code
- Share API keys in chat/email
- Use production keys in development

## Troubleshooting

### Application fails to start with "API key not found"

**Problem**: Environment variable not set

**Solution**:
```bash
export GEMINI_API_KEY=your-actual-key
./gradlew bootRun
```

### API returns 404 errors

**Problem**: API key might be invalid or API not enabled

**Solution**:
1. Verify your API key at: https://console.cloud.google.com/apis/credentials
2. Enable Generative Language API: https://console.cloud.google.com/apis/library/generativelanguage.googleapis.com

### .env file not being read

**Problem**: Need to explicitly load the file

**Solution**:
```bash
# Load and export all variables from .env
export $(cat .env | xargs)
./gradlew bootRun
```

## Production Deployment

For production environments, use proper secret management:

- **AWS**: AWS Secrets Manager or Parameter Store
- **Google Cloud**: Secret Manager
- **Azure**: Key Vault
- **Kubernetes**: Kubernetes Secrets
- **Docker**: Docker secrets or environment variables in docker-compose

Example for Docker:
```yaml
# docker-compose.yml
services:
  app:
    environment:
      - GEMINI_API_KEY=${GEMINI_API_KEY}
```

Then run:
```bash
GEMINI_API_KEY=your-key docker-compose up
```

## Getting Your API Key

If you don't have a Gemini API key:

1. Go to: https://makersuite.google.com/app/apikey
2. Click "Create API Key"
3. Select or create a Google Cloud project
4. Copy the generated key
5. Add it to your `.env` file

## Configuration File Reference

The `application.yml` uses this syntax to read the environment variable:

```yaml
spring:
  gemini:
    api-key: ${GEMINI_API_KEY:your-api-key-here}
```

- `${GEMINI_API_KEY}` - Reads from environment variable
- `:your-api-key-here` - Default value if variable not set (for documentation purposes)
