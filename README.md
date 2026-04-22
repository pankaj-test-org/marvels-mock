# marvels-mock

Test repository for CloudBees CI + GitHub integration with ReRunCause testing.

## Purpose

This repository validates that CloudBees GitHub Reporting plugin generates native `ReRunCause` when GitHub's Re-run button is clicked. Used to test the CBP-31531 fix in Platform.

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