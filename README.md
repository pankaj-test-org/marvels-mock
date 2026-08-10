# marvels-mock

Test repository for CloudBees CI + GitHub integration with ReRunCause testing.

## Table of Contents

- [Purpose](#purpose)
- [CloudBees test results pipelines](#cloudbees-test-results-pipelines)
- [Jenkins Multibranch Pipeline Setup](#jenkins-multibranch-pipeline-setup)
  - [Prerequisites](#prerequisites)
  - [Pipeline Configuration](#pipeline-configuration)
  - [GitHub Actions Configuration](#github-actions-configuration)
- [Testing Re-run Functionality](#testing-re-run-functionality)

## Purpose

This repository validates that CloudBees GitHub Reporting plugin generates native `ReRunCause` when GitHub's Re-run button is clicked. Used to test the CBP-31531 fix in Platform.

## CloudBees test results pipelines

Two CloudBees platform workflows publish test results to **Run details → Test results** and the **Analytics → Test insights** dashboard, using [`cloudbees-io/publish-test-results`](https://docs.cloudbees.com/docs/cloudbees-unify/latest/continuous-integration/how-to-guides/publish-test-results).

| Workflow | Tests | `test-type` | `results-path` |
|---|---|---|---|
| `.cloudbees/workflows/java-test-results.yaml` | Maven + JUnit 5 (`src/test/java`) | `junit` | `target/surefire-reports` (directory) |
| `.cloudbees/workflows/playwright-test-results.yaml` | Playwright (`e2e/`) | `playwright` | `results.json` (single file) |
| `.cloudbees/workflows/cbp-45873-playwright-overwrite.yaml` | Playwright (`e2e-cbp-45873/`) | `playwright` | `results.json` (single file) |

Both publish with `if: ${{ always() }}` so results still upload when tests fail.

### ⚠️ Use `@v2` with `results-path`, not `folder-name`

The docs page shows `folder-name:` in its Go and Playwright examples — that is **stale v1 syntax**. The `action.yml` at tag `v2` declares only `test-type` and `results-path` (it maps `results-path` to `folderName` internally, which is where the doc drift comes from). Other repos in this test org are inconsistent on this; prefer `@v2` + `results-path`.

### Speeding up runs — preload step images

The Playwright suite itself takes ~1s for 10 tests; nearly all the wall-clock is the
**~880MB compressed image pull** (~110s cold). Preload the step images into the local
k3d cluster once:

```bash
./scripts/preload-ci-images.sh
```

Images are pinned to tags (not `:latest`), so Kubernetes' default `IfNotPresent` pull
policy uses the cache. Re-run the script after recreating the cluster or bumping a tag.

Two gotchas it handles:
- **Node architecture.** The k3d nodes are `arm64`; a plain `docker pull` on an Intel
  host (or with a stale amd64 copy cached) imports an image the nodes can't run.
- **All nodes, not one.** `k3d image import` must cover every node or a run scheduled
  on an un-primed node still cold-pulls.

Don't try to shrink the pull by swapping in `node:20-bookworm-slim` (68MB): it has no
browsers, so `playwright install --with-deps` adds ~63s — measurably worse than the
one-time cached pull.

### CBP-45873 repro — `results.json` overwritten by successive runs

`cbp-45873-playwright-overwrite.yaml` reproduces [CBP-45873](https://cloudbees.atlassian.net/browse/CBP-45873)
(*Intermittent failures in publishing test results*). It is `workflow_dispatch`-only
and has its own suite (`e2e-cbp-45873/`) and config
(`playwright.cbp-45873.config.ts`), so the main Playwright pipeline is unaffected.

The bug is not in `publish-test-results`. The `cbp-test-automation` pods invoke
`playwright test` several times in one step — `@Login`, then `@cleanup`, then the
main suite — and the JSON reporter rewrites `results.json` on every invocation.
Only the last invocation survives to the publish step, and every run exits 0, so
the pipeline stays green while results go missing. This is why 8 of 9 pods
published nothing while `unify-ci` worked: it alone merged blob reports.

The workflow runs four tagged invocations (9 tests total) and the
`blank-last-run` input controls what the final one does:

| `blank-last-run` | Final `results.json` | Test results tab |
|---|---|---|
| `true` (default) | `expected=0, suites=0` | **empty** — "No results found" |
| `false` | `expected=2, suites=1` | 2 teardown tests |

Either way only the last invocation is published — the intermediate counts go
`2 → 2 → 3` for 7 tests run, so results are already being lost before the final
run. `blank-last-run=true` takes it to zero, reproducing the empty tab from the
ticket exactly: green run, nothing published.

The blanking works because `e2e-cbp-45873/zz-teardown.spec.ts` only registers its
`describe` block when `RUN_TEARDOWN=true`. Unset, the invocation collects no
tests, and `--pass-with-no-tests` keeps it green — but the JSON reporter still
writes `results.json`, wiping the earlier runs. (Without that flag Playwright
exits 1 with "No tests found", which would redden the step and hide the bug.)

Reproduce locally:

```bash
rm -rf results.json blob-report
for t in @Login @cleanup @regression; do
  npx playwright test -c playwright.cbp-45873.config.ts --grep $t
  node -e "console.log(require('./results.json').stats.expected)"   # 2, 2, 3
done

# blank the file, still exit 0
npx playwright test -c playwright.cbp-45873.config.ts --grep '@teardown' --pass-with-no-tests
node -e "console.log(require('./results.json').stats)"   # expected: 0

# keep the last run non-empty
RUN_TEARDOWN=true npx playwright test -c playwright.cbp-45873.config.ts --grep '@teardown'
node -e "console.log(require('./results.json').stats)"   # expected: 2
```

The `JOB_NAME`-keyed blob report + `merge-reports` approach from
[`unify-ci/action.yaml`](https://github.com/cloudbees/cbp-test-automation/blob/main/.cloudbees/pods/unify-ci/action.yaml#L100-L115)
is the candidate fix, to be evaluated against this pipeline. Note that without a
unique `JOB_NAME` every run writes `report-.zip` and overwrites the previous blob
too — so merging alone is not sufficient, which is likely why the first attempt
on the ticket still showed an empty tab. `playwright.cbp-45873.config.ts` already
attaches the blob reporter whenever `JOB_NAME` is set, so the fix can be tried
without touching the specs.

### Separately: a real parser bug in the action

`util/playwright.go` drops tests when a spec file has **both** a top-level test
and a `describe` block:

```go
if len(suite.SuitesPW) > 0 {
    for _, suitePW := range suite.SuitesPW { ... }  // nested specs only
} else if len(suite.Specs) > 0 { ... }              // never reached
```

`e2e/character-page.spec.ts` has that shape, so the main suite publishes 9 of its
10 passing tests. Unrelated to CBP-45873's overwrite, but it also loses tests
silently.

### Test outcomes

- **Playwright: 10 tests, always green.** Fully hermetic — `page.setContent()` only, no network or running service required.
- **Java: 6 tests, intentionally flaky (~50% red).** `MarvelMockControllerTests.testGetQueryParam_withEmptyValue` ends with a deliberate coin flip (commit `5d54d03`), so repeated runs exercise both pass and failure rendering in the Test results tab without needing a toggle. Unlucky runs mark the workflow failed — that's expected. To get a green build while still recording the failure in the XML, add `-Dmaven.test.failure.ignore=true` to the `mvn` command.

### Running locally

```bash
mvn -B clean test                 # 6 tests, 0-1 failures; writes target/surefire-reports/TEST-*.xml
npm install && npx playwright install chromium   # first time only
npx playwright test               # 10 tests; writes results.json
```

The Playwright CI image (`mcr.microsoft.com/playwright:v1.60.0-noble`) ships browsers preinstalled, so `playwright install` is not needed in the workflow. Keep the image tag pinned to the `@playwright/test` version in `package.json` to avoid browser-version mismatch errors.

Unlike the `cloudbees-io-gha/*` actions in `.github/workflows/`, these platform-native workflows need **no `cloudbees-url` input and no `CLOUDBEES_API_TOKEN` secret** — the runner supplies `cloudbees.api.url` and `cloudbees.api.token` implicitly.

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
- `CLOUDBEES_API_TOKEN`: CloudBees Platform API token (required for publishing evidence)

**Add secrets via GitHub CLI:**
```bash
# Add Docker Hub username
gh secret set DOCKER_USERNAME --repo pankaj-test-org/marvels-mock

# Add Docker Hub password/token
gh secret set DOCKER_PASSWORD --repo pankaj-test-org/marvels-mock

# Add CloudBees Platform API token
gh secret set CLOUDBEES_API_TOKEN --repo pankaj-test-org/marvels-mock
```

Or via GitHub UI: `Settings → Secrets and variables → Actions → New repository secret`

**How to get CloudBees Platform API Token:**
1. Log in to CloudBees Platform (e.g., https://app.saas-preprod.beescloud.com)
2. Go to User Settings → API Keys/Tokens
3. Create a new token with appropriate permissions
4. Copy the token and add it to GitHub secrets

**Repository Variables:**
- `CLOUDBEES_API_URL`: CloudBees Platform API URL
  - PREPROD: `https://api.saas-preprod.beescloud.com` (default)
  - QA: `https://api.saas-qa.beescloud.com` (default)
  - Production: `https://api.cloudbees.io`
- `SKIP_GHA`: Set to `true` to skip GitHub Actions workflow (optional)
- `GH_CHECK_FAIL`: Set to `true` to intentionally fail GitHub Actions checks (optional)

**Add variables via GitHub CLI:**

*Repository-level:*
```bash
# Add CloudBees API URL (PREPROD environment)
gh variable set CLOUDBEES_API_URL --repo pankaj-test-org/marvels-mock --body "https://api.saas-preprod.beescloud.com"

# For Production environment, use:
# gh variable set CLOUDBEES_API_URL --repo pankaj-test-org/marvels-mock-prod --body "https://api.cloudbees.io"

# Add GH_CHECK_FAIL flag for intentional failing(optional)
gh variable set GH_CHECK_FAIL --repo pankaj-test-org/marvels-mock --body "false"

# Add SKIP_GHA flag (optional)
gh variable set SKIP_GHA --repo pankaj-test-org/marvels-mock --body "false"
```

*Organization-level (visible to all repos):*
```bash
# Add variable at organization level (all repos)
gh variable set CLOUDBEES_API_URL --org pankaj-test-org --body "https://api.saas-preprod.beescloud.com" --visibility all

# Or for selected repositories only
gh variable set CLOUDBEES_API_URL --org pankaj-test-org --body "https://api.saas-preprod.beescloud.com" --visibility selected --repos marvels-mock,marvels-mock-preprod
```

Or via GitHub UI: 
- Repository: `Settings → Secrets and variables → Actions → Variables → New repository variable`
- Organization: `Organization Settings → Secrets and variables → Actions → Variables → New organization variable`

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