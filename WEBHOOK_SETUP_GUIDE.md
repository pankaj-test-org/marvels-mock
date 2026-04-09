# Jenkins Webhook Setup Guide (No Jenkins API Token in GitHub Actions)

This setup allows GitHub Actions to trigger Jenkins and support "Re-run jobs" WITHOUT storing Jenkins credentials in GitHub.

## Architecture

```
GitHub Actions → Webhook → Jenkins → GitHub Status API → GitHub
        ↓                                                   ↑
   Triggers build                              Reports status back
   (no auth needed)                            (Jenkins has GitHub token)
```

## Key Components

1. **GitHub Actions**: Triggers webhook and waits for status
2. **Jenkins Generic Webhook Trigger**: Receives webhook (no auth required)
3. **Jenkins GitHub Plugin**: Reports status back to GitHub (needs GitHub token)
4. **✅ Re-run button works** because it's a GitHub Actions workflow!

---

## Setup Steps

### Step 1: Install Jenkins Plugins

Install these plugins in Jenkins:

1. **Generic Webhook Trigger** - Receives webhooks from GitHub Actions
2. **GitHub Plugin** - Reports status back to GitHub
3. **Pipeline** - For Jenkinsfile support

```bash
# Via Jenkins Plugin Manager:
# Manage Jenkins → Plugins → Available
# Search and install:
- Generic Webhook Trigger Plugin
- GitHub Plugin
- Pipeline
```

### Step 2: Configure GitHub Credentials in Jenkins

Jenkins needs a GitHub token to REPORT STATUS BACK to GitHub.

#### Option A: GitHub Personal Access Token (Simpler)

1. Go to GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Generate new token with permissions:
   - ✅ `repo:status` (to update commit status)
   - ✅ `repo` (if private repo)
3. Copy the token

4. In Jenkins:
   - Go to: Manage Jenkins → Credentials → System → Global credentials
   - Click "Add Credentials"
   - **Kind**: Secret text
   - **Secret**: Paste your GitHub token
   - **ID**: `github-status-token`
   - **Description**: GitHub Status Reporter Token
   - Click "Create"

#### Option B: GitHub App (More Secure, Recommended for Production)

1. Create GitHub App:
   - Go to GitHub Org Settings → Developer settings → GitHub Apps
   - Click "New GitHub App"
   - Name: "Jenkins CI Reporter"
   - Homepage URL: Your Jenkins URL
   - Webhook: Disable (not needed for this flow)
   - Permissions:
     - Repository → Commit statuses: Read & Write
     - Repository → Contents: Read only
   - Install the app on your repository

2. In Jenkins:
   - Install "GitHub Branch Source" plugin
   - Add GitHub App credentials
   - Use app credentials for status reporting

### Step 3: Create Jenkins Job with Generic Webhook Trigger

#### Via Jenkins UI:

1. **Create New Item**:
   - Name: `marvels-mock-webhook`
   - Type: Pipeline
   - Click OK

2. **Configure Generic Webhook Trigger**:
   - Under "Build Triggers" → Check "Generic Webhook Trigger"
   - Add Token: `marvels-mock-trigger-token` (choose any random string)
   - Configure POST content parameters:
     ```
     Variable: repository    JSONPath: $.repository
     Variable: ref           JSONPath: $.ref
     Variable: sha           JSONPath: $.sha
     Variable: branch        JSONPath: $.branch
     Variable: author        JSONPath: $.author
     Variable: commit_message JSONPath: $.commit_message
     Variable: run_id        JSONPath: $.run_id
     ```
   - Optional: Add token parameter: `token` (for additional security)

3. **Configure Pipeline**:
   - Definition: Pipeline script from SCM
   - SCM: Git
   - Repository URL: `https://github.com/pankaj-test-org/marvels-mock.git`
   - Credentials: Your GitHub credentials
   - Branch: `*/main`
   - Script Path: `Jenkinsfile.webhook`
   - Click "Save"

4. **Note the webhook URL**:
   ```
   https://your-jenkins.com/generic-webhook-trigger/invoke?token=marvels-mock-trigger-token
   ```

### Step 4: Configure GitHub Secrets

Only ONE secret needed in GitHub:

1. Go to: GitHub repo → Settings → Secrets and variables → Actions
2. Click "New repository secret"
3. Add:
   - **Name**: `JENKINS_WEBHOOK_URL`
   - **Value**: `https://your-jenkins.com/generic-webhook-trigger/invoke?token=marvels-mock-trigger-token`

### Step 5: Update Jenkinsfile

Use the provided `Jenkinsfile.webhook`:

```bash
# Option 1: Replace existing Jenkinsfile
cp Jenkinsfile.webhook Jenkinsfile

# Option 2: Keep both and configure Jenkins job to use Jenkinsfile.webhook
# (Already done in Step 3 if you set Script Path)
```

### Step 6: Configure GitHub Status Reporting in Jenkinsfile

Ensure `Jenkinsfile.webhook` uses the correct credentials:

```groovy
// In Jenkinsfile.webhook, the githubNotify step needs configuration:

githubNotify(
    status: 'PENDING',
    description: 'Jenkins build in progress',
    context: 'continuous-integration/jenkins',
    credentialsId: 'github-status-token'  // Match Step 2
)
```

Or configure globally:
- Manage Jenkins → Configure System → GitHub → GitHub Servers
- Add GitHub Server:
  - Name: `github.com`
  - API URL: `https://api.github.com`
  - Credentials: Select `github-status-token`
  - Test connection
  - Click "Save"

### Step 7: Test the Setup

1. **Push a commit**:
   ```bash
   git commit --allow-empty -m "Test Jenkins webhook"
   git push
   ```

2. **Check GitHub Actions**:
   - Go to: Actions tab in GitHub
   - You should see "Jenkins CI" workflow running
   - It will show "Waiting for Jenkins to report status..."

3. **Check Jenkins**:
   - Go to Jenkins job
   - A build should have been triggered automatically
   - Check console output

4. **Check GitHub Commit Status**:
   - Go to the commit in GitHub
   - You should see Jenkins status (pending → success/failure)

5. **Test Re-run Button**:
   - In GitHub Actions, click "Re-run all jobs"
   - Should trigger Jenkins again! ✅

---

## Troubleshooting

### Problem: "Timeout: Jenkins did not report status"

**Causes:**
1. Jenkins doesn't have GitHub credentials configured
2. Wrong credentials ID in Jenkinsfile
3. GitHub token lacks `repo:status` permission

**Fix:**
- Check Jenkins logs: Manage Jenkins → System Log
- Verify credentials in Manage Jenkins → Credentials
- Test GitHub API token:
  ```bash
  curl -H "Authorization: token YOUR_TOKEN" \
    https://api.github.com/repos/pankaj-test-org/marvels-mock/statuses/COMMIT_SHA
  ```

### Problem: "Jenkins webhook not triggered"

**Causes:**
1. Wrong webhook URL
2. Network/firewall blocking
3. Wrong token in URL

**Fix:**
- Test webhook manually:
  ```bash
  curl -X POST \
    -H "Content-Type: application/json" \
    -d '{"repository":"pankaj-test-org/marvels-mock","sha":"test"}' \
    "https://your-jenkins.com/generic-webhook-trigger/invoke?token=marvels-mock-trigger-token"
  ```
- Check Jenkins System Log for webhook errors

### Problem: "Re-run button appears but nothing happens"

**This was your original issue!**

**Cause:** You're using external status checks (webhook → status API) directly, without GitHub Actions wrapper

**Fix:** Use the provided `jenkins-webhook-only.yml` workflow (already done!)

### Problem: `githubNotify` step not found

**Cause:** GitHub Plugin not installed or not configured

**Fix:**
1. Install "GitHub Plugin"
2. Configure GitHub server in Jenkins settings
3. Or use alternative status update method:
   ```groovy
   sh """
   curl -X POST \
     -H 'Authorization: token ${env.GITHUB_TOKEN}' \
     -H 'Content-Type: application/json' \
     -d '{"state":"success","description":"Build passed","context":"ci/jenkins"}' \
     https://api.github.com/repos/${params.repository}/statuses/${params.sha}
   """
   ```

---

## Security Considerations

### ✅ What's Secure:

- GitHub token stored in Jenkins (not in GitHub Actions)
- Webhook URL can use token for basic auth
- Jenkins credentials never leave Jenkins
- GitHub Actions only needs GITHUB_TOKEN (automatic, read-only for status polling)

### ⚠️ Additional Security Options:

1. **IP Whitelist**: Restrict webhook endpoint to GitHub IPs
2. **HMAC Signature**: Validate webhook payload signature
3. **API Gateway**: Put webhook behind authenticated gateway

### Example: Add HMAC Validation

Update webhook trigger in Jenkins job config:
```groovy
properties([
    pipelineTriggers([
        GenericTrigger(
            genericVariables: [...],
            genericHeaderVariables: [
                [key: 'X-Hub-Signature-256', regexpFilter: '']
            ],
            causeString: 'Triggered by GitHub Actions',
            token: 'marvels-mock-trigger-token',
            printContributedVariables: true,
            printPostContent: true,
            regexpFilterExpression: '',
            regexpFilterText: ''
        )
    ])
])
```

---

## Comparison with Token-Based Approach

| Feature | Webhook-Only (This Setup) | API Token Approach |
|---------|--------------------------|-------------------|
| **Re-run works** | ✅ Yes | ✅ Yes |
| **Jenkins token in GitHub** | ❌ No | ✅ Required |
| **Setup complexity** | Medium | Low |
| **Security** | ✅ Good | ✅ Good |
| **Monitoring** | Via GitHub Status API | Direct Jenkins API |
| **Best for** | Public repos, security-sensitive | Private setups, full control |

---

## Files in This Setup

- `.github/workflows/jenkins-webhook-only.yml` - GitHub Actions workflow
- `Jenkinsfile.webhook` - Jenkins pipeline with status reporting
- This guide (WEBHOOK_SETUP_GUIDE.md)

---

## Next Steps

1. ✅ Install Jenkins plugins
2. ✅ Configure GitHub credentials in Jenkins
3. ✅ Create Jenkins job with Generic Webhook Trigger
4. ✅ Add JENKINS_WEBHOOK_URL secret to GitHub
5. ✅ Test with a commit
6. ✅ Verify re-run button works!

---

## Support

If you encounter issues:
1. Check Jenkins System Log
2. Check GitHub Actions logs
3. Verify webhook can reach Jenkins (curl test)
4. Verify GitHub token permissions
5. Check plugin versions compatibility
