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
     - Discover branches: Strategy as needed
     - Filter by name (with regular expression): `main|PR-.*`
   
   **⚠️ Important:** Use the regex pattern `main|PR-.*` to avoid duplicate builds. Without this filter, Jenkins will build both feature branches AND pull requests, causing redundant builds. This pattern ensures:
   - `main` branch builds on push
   - Pull requests build with `PR-` prefix (e.g., `PR-123`)
   - Feature branches are ignored (only their PRs are built)
   
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
   
   **⚠️ Critical:** Even if your GitHub App has the correct permissions, you MUST configure webhook event subscriptions per repository. Without these events, Jenkins won't receive Re-run notifications or send stage updates to GitHub.
   
   - Ensure webhook is enabled in the repository settings
   - Webhook URL: `https://your-jenkins-instance.com/github-webhook/`
   - Events: Select "Let me select individual events" and enable:
     - ✅ **Pushes** (triggers builds on commits)
     - ✅ **Pull requests** (triggers builds on PRs)
     - ✅ **Check runs** (required for Re-run button detection)
     - ✅ **Check suites** (required for Re-run button detection)
   
   **To verify webhook events:**
   ```bash
   gh api repos/pankaj-test-org/<repo-name>/hooks --jq '.[] | {id: .id, events: .events}'
   ```
   
   **To update webhook events if missing:**
   ```bash
   gh api repos/pankaj-test-org/<repo-name>/hooks/<hook-id> \
     --method PATCH \
     --field 'events[]=push' \
     --field 'events[]=pull_request' \
     --field 'events[]=check_run' \
     --field 'events[]=check_suite'
   ```

5. **Optional Environment Variables**
   - `SKIP_JENKINS`: Set to `true` to skip Jenkins pipeline execution (marks build as SUCCESS)
   - `JENKINS_FAIL_BUILD`: Set to `true` to intentionally fail builds for testing

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

**Add secrets via GitHub CLI:**
```bash
# Add Docker Hub username
gh secret set DOCKER_USERNAME --repo pankaj-test-org/marvels-mock

# Add Docker Hub password/token
gh secret set DOCKER_PASSWORD --repo pankaj-test-org/marvels-mock
```

Or via GitHub UI: `Settings → Secrets and variables → Actions → New repository secret`

**Repository Variables:**
- `CLOUDBEES_API_URL`: CloudBees Platform API URL
  - PREPROD: `https://api.saas-preprod.beescloud.com` (default)
  - QA: `https://api.saas-qa.beescloud.com` (default)
  - Production: `https://api.cloudbees.io`
- `SKIP_GHA`: Set to `true` to skip GitHub Actions workflow (optional)
- `GH_CHECK_FAIL`: Set to `true` to intentionally fail GitHub Actions checks (optional)

**Add variables via GitHub CLI:**
```bash
# Add CloudBees API URL (QA environment)
gh variable set CLOUDBEES_API_URL --repo pankaj-test-org/marvels-mock --body "https://api.saas-preprod.beescloud.com"

# For Production environment, use:
# gh variable set CLOUDBEES_API_URL --repo pankaj-test-org/marvels-mock-prod --body "https://api.cloudbees.io"

# Add GH_CHECK_FAIL flag (optional)
gh variable set GH_CHECK_FAIL --repo pankaj-test-org/marvels-mock --body "false"

# Add SKIP_GHA flag (optional)
gh variable set SKIP_GHA --repo pankaj-test-org/marvels-mock --body "false"
```

Or via GitHub UI: `Settings → Secrets and variables → Actions → Variables → New repository variable`

## Testing Re-run Functionality

**Test Variables:**
- **Jenkins:** Set `JENKINS_FAIL_BUILD=true` in job configuration → Environment section
- **GitHub Actions:** Set `GH_CHECK_FAIL=true` as repository variable

**Verify ReRunCause:**

After clicking Re-run in GitHub, check Jenkins console output:

```
Cause: [_class:com.cloudbees.jenkins.plugins.github_reporting.remote.AppsAndChecks$ReRunCause, 
        shortDescription:GitHub Checks re-run of #X]
```

This confirms CloudBees GitHub Reporting generated the correct cause for CBP-31531 testing.