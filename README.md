# marvels-mock

Test repository for CloudBees CI + GitHub integration using CloudBees GitHub Reporting plugin.

## Purpose

This repository validates that CloudBees GitHub Reporting plugin generates native `ReRunCause` when GitHub's Re-run button is clicked. This is needed to test the CBP-31531 fix in Platform.

## Setup

- **Jenkins Job:** Multibranch Pipeline configured with GitHub Branch Source
- **GitHub App:** `pankaj-test-org-gh-app` with Checks (Read & write) permission and subscribed to Check run events
- **CloudBees Plugin:** CloudBees GitHub Reporting plugin installed

See `RERUN_SETUP.md` for complete configuration details.
