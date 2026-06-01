#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Usage: $0 <coverage-xml> [<coverage-xml> ...]"
  exit 1
fi

min=$(printf "%s" "$CODE_COVERAGE_MIN")

for report in "$@"; do
  if [[ ! -f "$report" ]]; then
    echo "Coverage report not found: $report"
    exit 1
  fi
  line=$(grep '<counter type="LINE"' "$report" | head -n 1)
  missed=$(echo "$line" | sed -E 's/.*missed="([0-9]+)".*/\1/')
  covered=$(echo "$line" | sed -E 's/.*covered="([0-9]+)".*/\1/')
  total=$((missed + covered))
  percent=$(awk "BEGIN { if ($total == 0) print 0; else printf \"%.2f\", ($covered / $total) * 100 }")
  echo "Report: $report -> Line coverage: $percent%"
  ok=$(awk "BEGIN { print ($percent >= $min) ? 1 : 0 }")
  if [[ "$ok" -ne 1 ]]; then
    echo "Coverage gate failed: $percent% < $min%"
    exit 1
  fi
  echo "Coverage gate passed for $report"
 done
