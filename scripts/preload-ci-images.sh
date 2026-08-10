#!/usr/bin/env bash
# Preload workflow step images into the local k3d cluster so runs don't cold-pull
# them. The Playwright image is ~880MB compressed (~2min pull); caching it takes
# the Playwright workflow from minutes to seconds.
#
# Re-run after recreating the cluster, or after bumping any image tag below.
set -euo pipefail

CLUSTER="${CLUSTER:-guide-rails}"

# Must match the node architecture, not the host's — k3d nodes here are arm64.
ARCH="${ARCH:-$(kubectl get nodes -o jsonpath='{.items[0].status.nodeInfo.architecture}')}"

IMAGES=(
  "mcr.microsoft.com/playwright:v1.60.0-noble"
  "maven:3.9-eclipse-temurin-17"
  "alpine:3.19"
)

for img in "${IMAGES[@]}"; do
  echo "==> $img (linux/$ARCH)"
  docker pull --platform "linux/$ARCH" "$img"
done

# One import call so the tarball is built and shipped to every node once.
k3d image import "${IMAGES[@]}" -c "$CLUSTER"

echo
echo "==> cached per node"
for node in $(kubectl get nodes -o jsonpath='{range .items[*]}{.metadata.name}{"\n"}{end}'); do
  echo "--- $node"
  docker exec "$node" crictl images 2>/dev/null \
    | grep -E 'playwright|maven|alpine' \
    | awk '{print "    "$1":"$2}'
done
