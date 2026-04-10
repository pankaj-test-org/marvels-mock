# marvels-mock

Test repository for CloudBees CI + GitHub integration with ReRunCause testing.

## Testing Re-run Functionality

Builds can be configured to fail intentionally using the `JENKINS_FAIL_BUILD` environment variable.

### To enable test failures:

1. Go to Jenkins: `https://cm.pankajy-dev.me/job/cb-jenkins-test/configure`
2. Scroll to **"Properties"** section
3. Add environment variable:
   - **Name:** `JENKINS_FAIL_BUILD`
   - **Value:** `true`
4. Save configuration
5. Next build will fail → Re-run button appears in GitHub

### To disable test failures:

1. Go to Jenkins job configuration
2. Set `JENKINS_FAIL_BUILD=false` or remove the variable
3. Save configuration
4. Builds will pass normally

See `RERUN_SETUP.md` for complete ReRunCause setup documentation.
