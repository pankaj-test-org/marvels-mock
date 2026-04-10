# marvels-mock

Test repository for CloudBees CI + GitHub integration with ReRunCause testing.

## Testing Re-run Functionality

### To make builds fail (for testing Re-run button):

```bash
touch .jenkins-fail-build
git add .jenkins-fail-build
git commit -m "Enable test failure"
git push
```

### To make builds pass normally:

```bash
git rm .jenkins-fail-build
git commit -m "Disable test failure"
git push
```

The presence of `.jenkins-fail-build` file triggers intentional build failures.
