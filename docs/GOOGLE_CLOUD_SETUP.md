# Google Cloud Authentication Setup for Vertex AI

This guide will help you set up proper authentication for Google Vertex AI (Gemini) API.

## Understanding the Authentication Issue

The error you're seeing is:
```
Your default credentials were not found. To set up Application Default Credentials for your environment, see https://cloud.google.com/docs/authentication/external/set-up-adc.
```

**Why this happens**: Vertex AI requires OAuth2 authentication using **Application Default Credentials (ADC)**, not just an API key. The API key alone is not sufficient.

---

## Step-by-Step Setup

### Step 1: Install Google Cloud SDK

**On macOS** (using Homebrew):
```bash
brew install --cask google-cloud-sdk
```

**Alternative** (manual installation):
1. Download from: https://cloud.google.com/sdk/docs/install
2. Extract and run the installer
3. Restart your terminal

**Verify installation**:
```bash
gcloud --version
```

### Step 2: Authenticate with Google Cloud

Run this command to log in:
```bash
gcloud auth login
```

This will:
1. Open your browser
2. Ask you to sign in with your Google account
3. Grant permissions to Google Cloud SDK

### Step 3: Set Your Project

Set your default project (use your project ID):
```bash
gcloud config set project gen-lang-client-0489352917
```

**Verify it's set**:
```bash
gcloud config get-value project
```

Should output: `gen-lang-client-0489352917`

### Step 4: Set Up Application Default Credentials (ADC)

This is the **critical step** that fixes the authentication issue:

```bash
gcloud auth application-default login
```

This will:
1. Open your browser again
2. Ask for permissions
3. Save credentials to your local machine at:
   - macOS/Linux: `~/.config/gcloud/application_default_credentials.json`
   - Windows: `%APPDATA%\gcloud\application_default_credentials.json`

### Step 5: Enable Vertex AI API

Enable the Vertex AI API for your project:

```bash
gcloud services enable aiplatform.googleapis.com
```

**Alternative** (via Console):
1. Go to: https://console.cloud.google.com/apis/library/aiplatform.googleapis.com
2. Select your project: `gen-lang-client-0489352917`
3. Click **"Enable"**

### Step 6: Verify Authentication

Check that credentials are set up:
```bash
gcloud auth application-default print-access-token
```

If this prints a long token, you're authenticated! ✅

---

## Testing the Application

Once authentication is set up, restart your application:

```bash
# Stop the current app (Ctrl+C in the terminal running it)
cd /Users/dipkothari/Project/smart-customer-support-ticket-triage
./gradlew bootRun
```

Then test ticket classification:

```bash
# Submit a ticket
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "Billing issue",
    "description": "I was charged twice for my subscription!"
  }'

# Wait 3-5 seconds for AI processing
sleep 5

# Check the classification
curl http://localhost:8080/api/tickets/223
```

You should see:
- `status`: `CLASSIFIED`
- `category`: `BILLING`
- `priority`: `HIGH`
- `sentiment`: `2-3` (negative)

---

## Troubleshooting

### Error: "Project not found"

**Solution**: Make sure you're using the correct project ID
```bash
gcloud projects list
gcloud config set project gen-lang-client-0489352917
```

### Error: "API not enabled"

**Solution**: Enable Vertex AI API
```bash
gcloud services enable aiplatform.googleapis.com
```

### Error: "Permission denied"

**Solution**: Make sure your account has permissions
1. Go to: https://console.cloud.google.com/iam-admin/iam
2. Find your email
3. Ensure you have role: **Vertex AI User** or **Editor**

To add the role via command line:
```bash
# Get your email
gcloud config get-value account

# Add Vertex AI User role (replace YOUR_EMAIL)
gcloud projects add-iam-policy-binding gen-lang-client-0489352917 \
  --member="user:YOUR_EMAIL@gmail.com" \
  --role="roles/aiplatform.user"
```

### Credentials Not Found After Setup

**Solution**: Re-run the ADC login
```bash
gcloud auth application-default login
```

### Still Getting Errors?

Check the credentials file exists:
```bash
cat ~/.config/gcloud/application_default_credentials.json
```

If the file doesn't exist, run:
```bash
gcloud auth application-default login
```

---

## Quick Command Reference

```bash
# 1. Install gcloud (macOS)
brew install --cask google-cloud-sdk

# 2. Login
gcloud auth login

# 3. Set project
gcloud config set project gen-lang-client-0489352917

# 4. Set up ADC (MOST IMPORTANT!)
gcloud auth application-default login

# 5. Enable Vertex AI
gcloud services enable aiplatform.googleapis.com

# 6. Verify
gcloud auth application-default print-access-token
```

---

## What Happens After Setup?

Once ADC is configured:
1. ✅ Your application will automatically find credentials
2. ✅ Gemini API calls will work
3. ✅ Tickets will be classified automatically
4. ✅ No code changes needed!

The Spring AI library automatically looks for credentials in:
1. Environment variable `GOOGLE_APPLICATION_CREDENTIALS`
2. Application Default Credentials (what we just set up)
3. Metadata server (for Google Cloud deployments)

---

## Alternative: Service Account (For Production)

For production deployments, use a service account instead of your personal credentials:

### Create Service Account
```bash
# Create service account
gcloud iam service-accounts create ticket-triage-sa \
  --display-name="Ticket Triage Service Account"

# Grant Vertex AI permissions
gcloud projects add-iam-policy-binding gen-lang-client-0489352917 \
  --member="serviceAccount:ticket-triage-sa@gen-lang-client-0489352917.iam.gserviceaccount.com" \
  --role="roles/aiplatform.user"

# Create and download key
gcloud iam service-accounts keys create ~/ticket-triage-key.json \
  --iam-account=ticket-triage-sa@gen-lang-client-0489352917.iam.gserviceaccount.com
```

### Use Service Account
```bash
export GOOGLE_APPLICATION_CREDENTIALS=~/ticket-triage-key.json
./gradlew bootRun
```

---

## Next Steps

After completing the setup:
1. ✅ Run the commands above
2. ✅ Restart your application
3. ✅ Test ticket classification
4. ✅ Check the [API Testing Guide](API_TESTING.md) for more examples
5. ✅ Review the walkthrough documentation for full system overview

---

## Need Help?

If you encounter any issues:
1. Check the application logs for specific error messages
2. Verify each step above was completed
3. Make sure you're using the correct project ID
4. Ensure Vertex AI API is enabled in your project
