#!/usr/bin/env bash
set -euo pipefail

if [[ -z "${CI_MERGE_REQUEST_IID:-}" ]]; then
  echo "No merge request context available. Skipping CodeRay XG comment step."
  exit 0
fi

if [[ -z "${CODERAY_API_URL:-}" ]]; then
  echo "CODERAY_API_URL is not configured. Skipping CodeRay XG scan."
  exit 1
fi

scan_payload=$(cat <<'EOF'
{
  "project": "${CI_PROJECT_PATH}",
  "branch": "${CI_COMMIT_REF_NAME}",
  "source": "GitLab CI",
  "commit": "${CI_COMMIT_SHA}"
}
EOF
)

scan_response=$(curl -sSf -X POST "${CODERAY_API_URL}/scan" -H "Content-Type: application/json" -d "$scan_payload")

critical=$(echo "$scan_response" | jq -r '.critical // 0')
high=$(echo "$scan_response" | jq -r '.high // 0')
summary=$(echo "$scan_response" | jq -r '.summary // "Scan completed"')

comment="CodeRay XG scan result for MR !${CI_MERGE_REQUEST_IID}:\n- Critical: $critical\n- High: $high\n- Summary: $summary"

echo "$comment"

if [[ -n "${GITLAB_PRIVATE_TOKEN:-}" ]]; then
  curl -sSf --header "PRIVATE-TOKEN: ${GITLAB_PRIVATE_TOKEN}" \
    --form "body=$comment" \
    "${CI_API_V4_URL}/projects/${CI_PROJECT_ID}/merge_requests/${CI_MERGE_REQUEST_IID}/notes"
else
  echo "GITLAB_PRIVATE_TOKEN is not set; MR comment step skipped."
fi
