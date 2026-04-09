# Jenkins Setup Steps for GitHub Integration

Follow these steps **in exact order** on your Jenkins instance.

> **⚠️ IMPORTANT:** Do not skip steps! Each step depends on the previous one. If you skip Step 1 (Install Plugins), the pipeline will fail with `"No such DSL method 'githubNotify' found"`.

---

## STEP 1: Install Required Plugins

1. Go to **Manage Jenkins** → **Plugins** → **Available Plugins**

2. Search and install these plugins:
   - ✅ **Generic Webhook Trigger Plugin**
   - ✅ **GitHub Plugin** 
   - ✅ **Pipeline Plugin** (usually pre-installed)

3. If prompted, restart Jenkins after installation

---

## STEP 2: Create GitHub Personal Access Token

1. Open: https://github.com/settings/tokens/new

2. Configure the token:
   - **Note**: `Jenkins Status Reporter for marvels-mock`
   - **Expiration**: 90 days (or your preference)
   - **Scopes** - Check these boxes:
     - ✅ `repo:status` ← **REQUIRED** (to post commit statuses)
     - ✅ `repo` (if using private repos)

3. Click **"Generate token"**

4. **COPY THE TOKEN** (it looks like: `ghp_xxxxxxxxxxxxxxxxxxxx`)
   - Save it temporarily - you'll need it in the next step

---

## STEP 3: Add GitHub Token to Jenkins Credentials

1. Go to **Manage Jenkins** → **Credentials**

2. Click on **(global)** domain

3. Click **Add Credentials** (button on the left)

4. Fill in the form:
   - **Kind**: `Secret text`
   - **Scope**: `Global`
   - **Secret**: Paste your GitHub token from Step 2
   - **ID**: `github-status-token` ← **IMPORTANT: Use this exact ID**
   - **Description**: `GitHub Status Reporter Token`

5. Click **Create**

---

## STEP 4: Configure GitHub Server in Jenkins

1. Go to **Manage Jenkins** → **System** (or **Configure System**)

2. Scroll down to **"GitHub"** section

3. Click **"Add GitHub Server"** → **"GitHub Server"**

4. Configure:
   - **Name**: `github.com`
   - **API URL**: `https://api.github.com`
   - **Credentials**: Select `github-status-token` from dropdown
   - ✅ Check **"Manage hooks"** (optional but recommended)

5. Click **"Test connection"** button
   - Should show: ✅ "Credentials verified for user [your-username]"

6. Click **"Save"** at the bottom

---

## STEP 5: Create Jenkins Pipeline Job

1. From Jenkins dashboard, click **"New Item"**

2. Configure:
   - **Name**: `marvels-mock-webhook`
   - **Type**: Select **"Pipeline"**
   - Click **OK**

---

## STEP 6: Configure Generic Webhook Trigger

Still in the job configuration page:

1. Scroll to **"Build Triggers"** section

2. ✅ Check **"Generic Webhook Trigger"**

3. In the **"Post content parameters"** section, click **"Add"** for each of these:

   **Parameter 1:**
   - Variable: `repository`
   - Expression: `$.repository`
   - JSONPath

   **Parameter 2:**
   - Variable: `sha`
   - Expression: `$.sha`
   - JSONPath

   > **Note**: Only `repository` and `sha` are required for the pipeline to work (for GitHub status updates and checkout). Other parameters like `branch`, `author`, `commit_message`, `run_id` are optional and only used for logging.

4. In the **"Token"** field (under "Generic Webhook Trigger"):
   - Enter: `marvels-mock-trigger-token`
   - (You can choose any token name, but remember it!)

5. **Optional but recommended**:
   - ✅ Check "Print post content"
   - ✅ Check "Print contributed variables"
   - (Helps with debugging)

---

## STEP 7: Configure Pipeline Source

Still in the same job configuration:

1. Scroll to **"Pipeline"** section

2. Configure:
   - **Definition**: Select `Pipeline script from SCM`
   - **SCM**: Select `Git`
   - **Repository URL**: `https://github.com/pankaj-test-org/marvels-mock.git`
   - **Credentials**: 
     - If public repo: Leave as `- none -`
     - If private repo: Add GitHub credentials
   - **Branch Specifier**: `*/main`
   - **Script Path**: `Jenkinsfile`

3. Click **"Save"**

---

## STEP 8: Get Your Webhook URL

Your Jenkins webhook URL is:

```
https://cm.pankajy-dev.me/generic-webhook-trigger/invoke?token=marvels-mock-trigger-token
```

**Copy this exact URL - you'll need it for GitHub in Step 9!**

---

## STEP 9: Add Webhook URL to GitHub Secrets

1. Go to your GitHub repo: https://github.com/pankaj-test-org/marvels-mock

2. Click **Settings** → **Secrets and variables** → **Actions**

3. Click **"New repository secret"**

4. Add:
   - **Name**: `JENKINS_WEBHOOK_URL`
   - **Secret**: Paste your webhook URL from Step 8

5. Click **"Add secret"**

---

## STEP 10: Test the Setup

### Test 1: Manual Trigger

1. In Jenkins, go to your `marvels-mock-webhook` job

2. Click **"Build with Parameters"** (left sidebar)

3. Fill in test values:
   - repository: `pankaj-test-org/marvels-mock`
   - sha: (any recent commit SHA)
   - branch: `main`

4. Click **"Build"**

5. Check console output - should see the build running

### Test 2: Webhook Trigger Test

From your terminal, run:

```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "repository": "pankaj-test-org/marvels-mock",
    "sha": "test123"
  }' \
  "https://cm.pankajy-dev.me/generic-webhook-trigger/invoke?token=marvels-mock-trigger-token"
```

You should see a new build triggered in Jenkins!

### Test 3: End-to-End Test

1. Merge your PR to `main` branch

2. Watch GitHub Actions workflow trigger

3. Check Jenkins - should start building

4. Check GitHub commit page - should see Jenkins status appear inline

5. The **Re-run** button should appear on the status check!

---

## Troubleshooting

### Issue: "No such DSL method 'githubNotify' found"

This is the most common error! It means the GitHub Plugin is not installed.

**Solution:**
1. Go to **Manage Jenkins** → **Plugins** → **Available Plugins**
2. Search for **"GitHub Plugin"**
3. Check the box and click **"Install"**
4. **IMPORTANT:** Restart Jenkins after installation:
   - Click **"Restart Jenkins when installation is complete and no jobs are running"**
   - Or manually restart: `sudo systemctl restart jenkins` (Linux) or restart the Jenkins service
5. After restart, go back to Step 4 and configure the GitHub Server
6. Re-run your pipeline

**Verification:**
- After restart, go to your pipeline and click **"Pipeline Syntax"** (left sidebar)
- In the "Sample Step" dropdown, search for "githubNotify"
- If it appears, the plugin is installed correctly!

### Issue: "Credentials not found: github-status-token"

**Solution:**
- Go back to Step 3
- Make sure credential ID is exactly: `github-status-token`
- No typos, case-sensitive!

### Issue: "Test connection failed" in Step 4

**Solution:**
- Verify GitHub token has `repo:status` scope
- Check token hasn't expired
- Try creating a new token

### Issue: "Webhook not triggering Jenkins"

**Solution:**
- Check Jenkins URL is accessible from GitHub
- Verify token in webhook URL matches Step 6
- Check Jenkins System Log for errors
- Make sure Generic Webhook Trigger plugin is installed

### Issue: "Build triggers but status not posted to GitHub"

**Solution:**
- Check Jenkins console output for errors
- Verify GitHub Server configuration (Step 4)
- Make sure `repository` and `sha` parameters are passed correctly
- Check Jenkins System Log: Manage Jenkins → System Log

---

## What Happens After Setup

1. **Push to main** → GitHub Actions triggers → Webhook to Jenkins
2. **Jenkins builds** → Posts status to GitHub using `githubNotify`
3. **Status appears inline on PR** with Re-run button
4. **Click Re-run** → Re-triggers the whole flow!

---

## Summary of What You Created

✅ GitHub token for Jenkins → stored in Jenkins credentials  
✅ Jenkins job with webhook trigger → receives events from GitHub Actions  
✅ Jenkinsfile with `githubNotify` → posts status back to GitHub  
✅ GitHub secret with webhook URL → allows Actions to trigger Jenkins  

This creates a **bidirectional integration** where GitHub and Jenkins communicate!

---

## Need Help?

- Check Jenkins logs: **Manage Jenkins → System Log**
- Check job console output: Click on build number → **Console Output**
- Test webhook manually with curl command above
- Verify all credentials and tokens are correct

Good luck! 🚀
