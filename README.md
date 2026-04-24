# marvels-mock

Test repository for CloudBees CI + GitHub integration with ReRunCause testing.

## Purpose

This repository validates that CloudBees GitHub Reporting plugin generates native `ReRunCause` when GitHub's Re-run button is clicked. Used to test the CBP-31531 fix in Platform.

## Jenkins Multibranch Pipeline Setup

### Prerequisites
- CloudBees CI instance with GitHub Reporting plugin installed
- GitHub App with access to the repository
- Docker Hub credentials configured in Jenkins

### Pipeline Configuration

1. **Create Multibranch Pipeline Job**
   - Go to Jenkins → New Item → Multibranch Pipeline
   - Name: `marvels-mock` (or your preferred name)

2. **Branch Sources Configuration**
   - **Source:** GitHub
   - **Credentials:** Select your GitHub App credentials
   - **Repository URL:** `https://github.com/pankaj-test-org/marvels-mock`
   - **Behaviors:**
     - Discover branches: **Only `main` branch**
     - Filter by name: `main` (regular expression)
   
3. **GitHub App Requirements**
   The GitHub App must have the following permissions and event subscriptions:
   - **Permissions:**
     - Repository contents: Read
     - Pull requests: Read & Write
     - Checks: Read & Write
     - Commit statuses: Read & Write
   - **Subscribe to events:**
     - ✅ **Check run** (required for Re-run cause detection)
     - ✅ **Check suite** (required for Re-run cause detection)
     - Push
     - Pull request

4. **Webhook Configuration**
   - Ensure webhook is enabled in the repository settings
   - Webhook URL: `https://your-jenkins-instance.com/github-webhook/`
   - Events: Select "Let me select individual events" and enable:
     - Pushes
     - Pull requests
     - Check runs
     - Check suites

5. **Environment Variables**
   Configure in Jenkins job → Configure → Pipeline section:
   
   | Variable | Default | Purpose |
   |----------|---------|---------|
   | `JENKINS_FAIL_BUILD` | `false` | Set to `true` to intentionally fail builds for testing re-run functionality |
   
   **To add environment variables:**
   - Go to job configuration → Pipeline section
   - Add environment block or use Jenkins Configuration as Code

6. **Required Jenkins Credentials**
   - **ID:** `docker-hub-credentials`
   - **Type:** Username with password
   - **Username:** Your Docker Hub username
   - **Password:** Your Docker Hub token/password

### GitHub Actions Configuration

The repository also includes a GitHub Actions workflow (`.github/workflows/gha-pan-test.yaml`) that requires:

**Repository Secrets:**
- `DOCKER_USERNAME`: Docker Hub username
- `DOCKER_PASSWORD`: Docker Hub token/password

**Repository Variables:**
- `CLOUDBEES_API_URL`: CloudBees Platform API URL (default: `https://api.saas-qa.beescloud.com`)
- `GH_CHECK_FAIL`: Set to `true` to intentionally fail GitHub Actions checks (optional)

## Testing Re-run Functionality

### Configure Build Failures (Optional)

Control failures using environment/repository variables:

**Jenkins:** `JENKINS_FAIL_BUILD` environment variable  
**GitHub Actions:** `GH_CHECK_FAIL` repository variable

**To enable intentional failures:**

1. Go to Jenkins: `https://cm.pankajy-dev.me/job/cb-jenkins-test/configure`
2. Scroll down to find **"Pipeline"** or **"Environment"** section
3. Add environment variable:
   - **Name:** `JENKINS_FAIL_BUILD`
   - **Value:** `true`
4. Click **"Save"*
5. Next build will fail in Test stage
6. Re-run button appears in GitHub checks
7. Click Re-run → generates `ReRunCause`

**To disable failures (normal builds):**

1. Go to Jenkins job configuration
2. Change `JENKINS_FAIL_BUILD` to `false` or delete the variable
3. Save configuration
4. Builds pass normally

### Configuration Variables

#### Jenkins: `JENKINS_FAIL_BUILD`
- **Location:** Jenkins job configuration → Environment section
- **Values:**
  - `true` = Build fails intentionally in Test stage
  - `false` or unset = Build passes normally
- **Scope:** Applies to all branches in the multibranch pipeline

#### GitHub Actions: `GH_CHECK_FAIL`
- **Location:** GitHub repo → Settings → Secrets and variables → Actions → Variables
- **Values:**
  - `true` = GitHub check fails
  - `false` or unset = GitHub check passes
- **Purpose:** Control GitHub checks without modifying workflow code

### Verify ReRunCause

After clicking Re-run in GitHub, check Jenkins console output:

```
Cause: [_class:com.cloudbees.jenkins.plugins.github_reporting.remote.AppsAndChecks$ReRunCause, 
        shortDescription:GitHub Checks re-run of #X]
```

This confirms CloudBees GitHub Reporting generated the correct cause for CBP-31531 testing.