# GitHub Actions + Jenkins: Approaches Comparison

## Problem
- External status checks (webhook → Jenkins → GitHub Status API) **don't support "Re-run jobs" button**
- GitHub Actions workflows **do support "Re-run jobs" button**

## Solutions

### ✅ Approach 1: GitHub Actions Triggers Jenkins via API (RECOMMENDED)
**File:** `.github/workflows/test.yml`

**Pros:**
- Full control over when Jenkins runs
- Can pass custom parameters
- Reliable monitoring of job status
- **Re-run button works perfectly**

**Cons:**
- Requires Jenkins API token
- Slightly more complex setup

**Setup needed:**
1. Jenkins API token
2. GitHub secrets (JENKINS_URL, JENKINS_USER, JENKINS_TOKEN)
3. Configure Jenkins job to accept parameters

**Use when:** You want the most reliable solution

---

### ⚠️  Approach 2: Webhook Triggers, GitHub Actions Monitors
**File:** `.github/workflows/webhook-trigger.yml`

**Pros:**
- Uses existing webhook configuration
- Only needs read-only Jenkins token
- **Re-run button works** (but re-triggers via webhook delay)

**Cons:**
- Needs to "find" the Jenkins build by commit SHA
- Small delay waiting for webhook to trigger
- Still requires Jenkins API token (read-only)

**Setup needed:**
1. Configure Jenkins webhook (may already exist)
2. Read-only Jenkins API token (for monitoring)
3. GitHub secrets (same as Approach 1)

**Use when:** You already have webhooks set up and prefer not to change trigger mechanism

---

### ❌ Approach 3: Pure Webhook + Status Monitoring (LIMITED)
**File:** `.github/workflows/webhook-status-monitor.yml`

**Pros:**
- No Jenkins credentials needed
- Uses only GitHub token (automatic)
- **Re-run button works** (but Jenkins must support status updates)

**Cons:**
- **Jenkins must be configured to send GitHub commit statuses** (via plugin)
- Less visibility into job progress
- Can't get detailed error information
- Harder to debug failures

**Setup needed:**
1. Configure Jenkins to send GitHub commit statuses
2. Install GitHub Branch Source / GitHub plugin in Jenkins
3. Configure OAuth or GitHub App credentials in Jenkins

**Use when:** Security policies prevent API token usage, and Jenkins is already configured for status updates

---

## Why You Need *Something* Beyond Pure Webhooks

```
❌ Pure Webhook (Current Setup - No Re-run):
GitHub → webhook → Jenkins → Status API → GitHub
Result: External check (no re-run button)

✅ With GitHub Actions (Any Approach - Has Re-run):
GitHub Actions → [trigger mechanism] → Jenkins → [monitoring] → Actions
Result: Workflow run (re-run button works!)
```

## Recommended Setup

**For most users:** Use **Approach 1** (GitHub Actions triggers via API)

**Minimal secrets needed:**
```bash
JENKINS_URL=https://jenkins.example.com
JENKINS_USER=your-user
JENKINS_TOKEN=your-api-token  # Generate at Jenkins → User → Configure → API Token
```

## Token Permissions Needed

### Approach 1 & 2:
- **Jenkins API token**: Read + Trigger jobs
- **GitHub token**: Automatic (no setup needed)

### Approach 3:
- **Jenkins**: Must have GitHub credentials configured (for status updates)
- **GitHub token**: Automatic (no setup needed)

## Security Note

All approaches keep credentials secure:
- Stored in GitHub Secrets (encrypted)
- Never exposed in logs
- Only accessible during workflow execution
- Read-only tokens work for Approach 2

## Next Steps

1. Choose an approach based on your requirements
2. Delete the workflows you don't want to use
3. Configure secrets if using Approach 1 or 2
4. Test with a commit
5. Verify "Re-run jobs" button appears and works
