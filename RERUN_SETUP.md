# CloudBees GitHub Reporting - ReRunCause Setup

## Problem Statement

**Ticket:** [CBP-31531](https://cloudbees.atlassian.net/browse/CBP-31531)

Platform doesn't recognize `ReRunCause` trigger type from CBCI/Jenkins builds. When Jenkins sends a build with `ReRunCause`, Platform fails to create run mappings, causing errors when processing stage data.

## Solution

Configure Jenkins Multibranch Pipeline with **CloudBees GitHub Reporting plugin** to generate native `ReRunCause` when GitHub's Re-run button is clicked.

---

## Key Finding

The `ReRunCause` we need is **NOT** `hudson.model.Cause.RerunCause` (standard Jenkins).

It's a custom class from CloudBees GitHub Reporting plugin:
```
com.cloudbees.jenkins.plugins.github_reporting.remote.AppsAndChecks$ReRunCause
```

When CBCI integration plugin sends this to Platform, it extracts the class name and sends `"ReRunCause"` as the trigger type.

---

## Configuration Steps

### 1. Create GitHub App

Go to: `https://github.com/organizations/pankaj-test-org/settings/apps/new`

**Settings:**
- **Name:** `pankaj-test-org-gh-app`
- **Homepage URL:** `https://cm.pankajy-dev.me`
- **Webhook URL:** `https://cm.pankajy-dev.me/github-webhook/`
- **Webhook:** Active

**Repository Permissions:**
- **Checks:** Read & write ⭐ (Critical)
- **Contents:** Read
- **Pull requests:** Read & write
- **Commit statuses:** Read & write

**Subscribe to Events:** ⭐ (Critical - this was the missing piece!)
- ✅ Check run
- ✅ Check suite
- ✅ Pull request
- ✅ Push

**After creation:**
1. Generate private key (download `.pem` file)
2. Convert to PKCS#8 format:
   ```bash
   openssl pkcs8 -topk8 -inform PEM -outform PEM \
     -in downloaded-key.pem -out converted-key.pem -nocrypt
   ```
3. Install app on `marvels-mock` repository

### 2. Configure Jenkins Credential

1. Go to: `https://cm.pankajy-dev.me/manage/credentials/`
2. Add new credential:
   - **Kind:** GitHub App
   - **App ID:** (from GitHub App settings)
   - **Key:** (paste contents of converted PKCS#8 `.pem` file)

### 3. Configure Jenkins Multibranch Pipeline

**Job Configuration:**
- **Name:** `cb-jenkins-test`
- **Type:** Multibranch Pipeline

**Branch Sources:**
- **Source:** GitHub
- **Repository:** `pankaj-test-org/marvels-mock`
- **Credentials:** (Select the GitHub App credential created above)

**Behaviors:**
- ✅ Discover branches
- ✅ Discover pull requests from origin
- ✅ **CloudBees SCM Reporting** (Critical!)
  - Fatal errors: 100 lines
  - Pipeline stages: All stages
  - Test results: Enabled

**Build Configuration:**
- **Mode:** by Jenkinsfile
- **Script Path:** `Jenkinsfile`

### 4. Jenkins Plugins Required

Ensure these plugins are installed:
- CloudBees GitHub Reporting (`cloudbees-github-reporting`)
- GitHub Branch Source (`github-branch-source`)
- GitHub API (`github-api`)

---

## How It Works

### Normal Build Flow

1. Push code or open PR
2. GitHub sends webhook to Jenkins
3. Multibranch pipeline triggers build
4. CloudBees GitHub Reporting creates check runs for each stage
5. Build cause: `jenkins.branch.BranchEventCause`

### Re-run Flow

1. Build completes (success or failure)
2. GitHub shows Re-run button on each check
3. User clicks Re-run button
4. GitHub sends `check_run.rerequested` webhook to Jenkins
5. CloudBees plugin's `ReRunListener` receives event
6. Creates new build with `AppsAndChecks$ReRunCause`
7. Build cause shows:
   ```
   Cause: [_class:com.cloudbees.jenkins.plugins.github_reporting.remote.AppsAndChecks$ReRunCause, 
           shortDescription:GitHub Checks re-run of #X]
   ```

### Platform Integration

When CBCI integration plugin sends build metadata to Platform:
1. Extracts cause class: `com.cloudbees.jenkins.plugins.github_reporting.remote.AppsAndChecks$ReRunCause`
2. Takes simple name: `ReRunCause`
3. Sends `trigger_info.cause = "ReRunCause"` to Platform
4. Platform's CBP-31531 fix recognizes this and handles it like GitHub reruns
5. Creates new run mapping with incremented `RunAttempt`

---

## Testing

### Test Re-run Functionality

1. **Trigger a build:**
   ```bash
   git commit --allow-empty -m "Test build"
   git push
   ```

2. **Wait for build to complete** (check GitHub PR)

3. **Click Re-run** on any failed check in GitHub

4. **Verify in Jenkins console output:**
   ```
   Cause: [_class:com.cloudbees.jenkins.plugins.github_reporting.remote.AppsAndChecks$ReRunCause, ...]
   ```

### Test Intentional Failures

The Jenkinsfile has a `FAIL_BUILD` parameter:

1. Go to Jenkins build page
2. Click **"Build with Parameters"**
3. Set `FAIL_BUILD = true`
4. Build will fail in Test stage
5. Re-run button will appear in GitHub

---

## Troubleshooting

### Re-run button doesn't appear

**Issue:** GitHub App not subscribed to check_run events

**Solution:** Edit GitHub App settings → Subscribe to events → Enable "Check run" and "Check suite"

### Re-run button does nothing

**Issue:** Webhook not reaching Jenkins or incorrect webhook URL

**Solution:**
- Check webhook deliveries: `https://github.com/pankaj-test-org/marvels-mock/settings/hooks`
- Look for `check_run.rerequested` events
- Verify Response is 200 OK

### Jenkins doesn't create checks

**Issue:** Wrong credential type or plugin not installed

**Solution:**
- Verify credential is "GitHub App" (not username/password)
- Verify CloudBees GitHub Reporting plugin is installed
- Check Jenkins logs for errors

### Private key format error

**Issue:** GitHub App private key must be PKCS#8 format

**Solution:**
```bash
openssl pkcs8 -topk8 -inform PEM -outform PEM \
  -in original-key.pem -out converted-key.pem -nocrypt
```

---

## Critical Success Factors

1. ✅ **GitHub App with proper permissions** (Checks: Read & write)
2. ✅ **Subscribe to check_run events** (This was the missing piece!)
3. ✅ **CloudBees GitHub Reporting plugin installed** on Jenkins
4. ✅ **Multibranch Pipeline** (not regular pipeline)
5. ✅ **GitHub App credential** (not PAT or username/password)

---

## References

- **CloudBees GitHub Reporting Plugin:** `/Users/pyadav/all/workspace/codebase/repositories/cbci/cloudbees-github-reporting-plugin`
- **ReRunListener Source:** `src/main/java/com/cloudbees/jenkins/plugins/github_reporting/remote/AppsAndChecks.java`
- **Jira Ticket:** [CBP-31531](https://cloudbees.atlassian.net/browse/CBP-31531)
- **Documentation:** https://docs.cloudbees.com/docs/cloudbees-ci/latest/scm-integration/configuring-scmr-notifications

---

**Date:** April 10, 2026  
**Repository:** `pankaj-test-org/marvels-mock`  
**Jenkins:** `https://cm.pankajy-dev.me`  
**GitHub App:** `pankaj-test-org-gh-app`