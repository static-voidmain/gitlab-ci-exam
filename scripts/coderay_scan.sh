#!/usr/bin/env bash
set -euo pipefail

readonly COMMENT_MARKER="<!-- coderay-xg-scan-comment -->"
readonly DEFAULT_TIMEOUT_SECONDS="1800"
readonly DEFAULT_POLL_INTERVAL_SECONDS="15"
readonly DEFAULT_STABLE_SECONDS="5"
readonly DEFAULT_COMMENT_MAX_BYTES="60000"

log() {
  printf '[coderay-xg] %s\n' "$*" >&2
}

die() {
  log "ERROR: $*"
  exit 1
}

require_command() {
  local command_name="$1"

  if ! command -v "$command_name" >/dev/null 2>&1; then
    die "'$command_name' 명령어가 필요합니다."
  fi
}

require_env() {
  local env_name="$1"

  if [[ -z "${!env_name:-}" ]]; then
    die "$env_name 환경 변수가 필요합니다."
  fi
}

is_positive_integer() {
  local value="$1"

  [[ "$value" =~ ^[0-9]+$ ]] && ((value > 0))
}

is_boolean_true() {
  local value="${1:-}"

  [[ "$value" == "true" || "$value" == "1" || "$value" == "yes" ]]
}

bytes_of() {
  local file_path="$1"

  wc -c <"$file_path" | tr -d '[:space:]'
}

find_result_file() {
  local result_dir="$1"

  find "$result_dir" -maxdepth 1 -type f -name '*.txt' -size +0c -print | sort | tail -n 1
}

wait_for_result_file() {
  local result_dir="$1"
  local timeout_seconds="$2"
  local poll_interval_seconds="$3"
  local stable_seconds="$4"
  local deadline
  local last_file=""
  local last_size=""
  local stable_since="0"
  local last_log_at="0"

  deadline=$(($(date +%s) + timeout_seconds))

  while (($(date +%s) < deadline)); do
    local candidate=""
    local candidate_size=""
    local now

    candidate="$(find_result_file "$result_dir" || true)"
    now="$(date +%s)"

    if [[ -n "$candidate" ]]; then
      candidate_size="$(bytes_of "$candidate")"

      if [[ "$candidate" == "$last_file" && "$candidate_size" == "$last_size" ]]; then
        if ((stable_since == 0)); then
          stable_since="$now"
        fi

        if ((now - stable_since >= stable_seconds)); then
          printf '%s\n' "$candidate"
          return 0
        fi
      else
        last_file="$candidate"
        last_size="$candidate_size"
        stable_since="0"
        log "결과 후보 파일 감지: $candidate (${candidate_size} bytes)"
      fi
    elif ((now - last_log_at >= 60)); then
      log "결과 txt 파일을 기다리는 중입니다. timeout=${timeout_seconds}s, poll=${poll_interval_seconds}s"
      last_log_at="$now"
    fi

    sleep "$poll_interval_seconds"
  done

  return 1
}

gitlab_api_base() {
  printf '%s/projects/%s/merge_requests/%s' \
    "${CI_API_V4_URL%/}" \
    "$CI_PROJECT_ID" \
    "$CI_MERGE_REQUEST_IID"
}

gitlab_curl() {
  curl --silent --show-error --fail \
    --header "PRIVATE-TOKEN: ${GITLAB_TOKEN_VALUE}" \
    "$@"
}

ensure_current_mr_commit() {
  if is_boolean_true "${CODERAY_SKIP_STALE_COMMIT_CHECK:-false}"; then
    log "MR 최신 커밋 확인을 건너뜁니다."
    return 0
  fi

  local response_file
  local mr_head_sha

  response_file="$(mktemp)"
  gitlab_curl "$(gitlab_api_base)" >"$response_file"
  mr_head_sha="$(jq -r '.sha // .diff_refs.head_sha // empty' "$response_file")"

  if [[ -n "$mr_head_sha" && "$mr_head_sha" != "$CI_COMMIT_SHA" ]]; then
    log "MR 최신 커밋($mr_head_sha)과 현재 파이프라인 커밋($CI_COMMIT_SHA)이 다릅니다."
    log "더 최신 커밋의 스캔 결과를 보호하기 위해 MR 댓글 갱신을 건너뜁니다."
    exit 0
  fi
}

delete_previous_coderay_comments() {
  local page="1"
  local deleted_count="0"
  local api_base

  api_base="$(gitlab_api_base)"

  while [[ -n "$page" ]]; do
    local body_file
    local header_file
    local note_ids
    local next_page

    body_file="$(mktemp)"
    header_file="$(mktemp)"

    gitlab_curl \
      --get \
      --dump-header "$header_file" \
      --data-urlencode "per_page=100" \
      --data-urlencode "page=$page" \
      "${api_base}/notes" >"$body_file"

    note_ids="$(
      jq -r \
        --arg marker "$COMMENT_MARKER" \
        --arg legacy_prefix "CodeRay XG scan result for MR " \
        '.[] | select((.body | contains($marker)) or (.body | startswith($legacy_prefix))) | .id' \
        "$body_file"
    )"

    while IFS= read -r note_id; do
      if [[ -n "$note_id" ]]; then
        gitlab_curl --request DELETE "${api_base}/notes/${note_id}" >/dev/null
        deleted_count=$((deleted_count + 1))
      fi
    done <<<"$note_ids"

    next_page="$(
      awk -F': ' 'tolower($1) == "x-next-page" { gsub("\r", "", $2); print $2 }' "$header_file" | tail -n 1
    )"
    page="$next_page"
  done

  log "이전 CodeRay XG MR 댓글 삭제 완료: ${deleted_count}개"
}

build_comment_body() {
  local result_file="$1"
  local snippet_file
  local result_bytes
  local max_bytes
  local commit_short
  local generated_at

  snippet_file="$(mktemp)"
  result_bytes="$(bytes_of "$result_file")"
  max_bytes="${CODERAY_COMMENT_MAX_BYTES:-$DEFAULT_COMMENT_MAX_BYTES}"
  commit_short="${CI_COMMIT_SHORT_SHA:-${CI_COMMIT_SHA:0:8}}"
  generated_at="$(date -u '+%Y-%m-%dT%H:%M:%SZ')"

  if ! is_positive_integer "$max_bytes"; then
    die "CODERAY_COMMENT_MAX_BYTES 값은 양의 정수여야 합니다. current=$max_bytes"
  fi

  if ((result_bytes > max_bytes)); then
    head -c "$max_bytes" "$result_file" >"$snippet_file"
  else
    cp "$result_file" "$snippet_file"
  fi

  {
    printf '%s\n' "$COMMENT_MARKER"
    printf '### CodeRay XG 코드 스캔 결과\n\n'
    printf '| 항목 | 값 |\n'
    printf '|---|---|\n'
    printf '| MR | !%s |\n' "$CI_MERGE_REQUEST_IID"
    printf '| Commit | `%s` |\n' "$commit_short"
    printf '| Pipeline | %s |\n' "${CI_PIPELINE_URL:-N/A}"
    printf '| Result file | `%s` |\n' "$result_file"
    printf '| Generated at | `%s` |\n\n' "$generated_at"

    if ((result_bytes > max_bytes)); then
      printf '> 결과가 %s bytes를 초과하여 앞부분 %s bytes만 MR 댓글에 표시합니다. 전체 파일은 Job artifact에서 확인하세요.\n\n' \
        "$result_bytes" \
        "$max_bytes"
    fi

    printf '<details open>\n'
    printf '<summary>스캔 결과(txt)</summary>\n\n'
    sed 's/^/    /' "$snippet_file"
    printf '\n</details>\n'
  }
}

create_coderay_comment() {
  local comment_file="$1"

  gitlab_curl \
    --request POST \
    --data-urlencode "body@${comment_file}" \
    "$(gitlab_api_base)/notes" >/dev/null

  log "새 CodeRay XG MR 댓글 등록 완료"
}

run_coderay_cli() {
  local result_dir="$1"
  local cli_log="$2"

  log "CodeRay XG CLI 스캔을 요청합니다. result_dir=$result_dir"

  (
    set +x
    cd "$result_dir"
    bash -lc "$CODERAY_CLI_COMMAND"
  ) >"$cli_log" 2>&1
}

if [[ -z "${CI_MERGE_REQUEST_IID:-}" ]]; then
  log "Merge Request 컨텍스트가 없어 CodeRay XG 스캔/댓글 단계를 건너뜁니다."
  exit 0
fi

require_command bash
require_command curl
require_command date
require_command find
require_command head
require_command jq
require_command mktemp
require_command sed
require_command tail
require_command wc

require_env CI_API_V4_URL
require_env CI_PROJECT_ID
require_env CI_COMMIT_SHA
require_env CODERAY_CLI_COMMAND

GITLAB_TOKEN_VALUE="${GITLAB_PRIVATE_TOKEN:-${GITLAB_TOKEN:-${GITLAB_API_TOKEN:-}}}"

if ! is_boolean_true "${CODERAY_COMMENT_ENABLED:-true}"; then
  log "CODERAY_COMMENT_ENABLED=false 이므로 MR 댓글 갱신 없이 스캔만 수행합니다."
elif [[ -z "$GITLAB_TOKEN_VALUE" ]]; then
  die "MR 댓글 생성/삭제를 위해 GITLAB_PRIVATE_TOKEN 또는 GITLAB_TOKEN 또는 GITLAB_API_TOKEN 이 필요합니다."
fi

CODERAY_TIMEOUT_SECONDS="${CODERAY_TIMEOUT_SECONDS:-$DEFAULT_TIMEOUT_SECONDS}"
CODERAY_POLL_INTERVAL_SECONDS="${CODERAY_POLL_INTERVAL_SECONDS:-$DEFAULT_POLL_INTERVAL_SECONDS}"
CODERAY_STABLE_SECONDS="${CODERAY_STABLE_SECONDS:-$DEFAULT_STABLE_SECONDS}"

if ! is_positive_integer "$CODERAY_TIMEOUT_SECONDS"; then
  die "CODERAY_TIMEOUT_SECONDS 값은 양의 정수여야 합니다. current=$CODERAY_TIMEOUT_SECONDS"
fi

if ! is_positive_integer "$CODERAY_POLL_INTERVAL_SECONDS"; then
  die "CODERAY_POLL_INTERVAL_SECONDS 값은 양의 정수여야 합니다. current=$CODERAY_POLL_INTERVAL_SECONDS"
fi

if ! is_positive_integer "$CODERAY_STABLE_SECONDS"; then
  die "CODERAY_STABLE_SECONDS 값은 양의 정수여야 합니다. current=$CODERAY_STABLE_SECONDS"
fi

commit_short="${CI_COMMIT_SHORT_SHA:-${CI_COMMIT_SHA:0:8}}"
result_root="${CODERAY_RESULT_ROOT:-${CI_PROJECT_DIR:-$PWD}/coderay-xg-results}"
result_dir="${CODERAY_RESULT_DIR:-${result_root}/${CI_PIPELINE_ID:-local}-${CI_JOB_ID:-$$}}"
cli_log="${result_dir}/coderay-xg-cli.log"

mkdir -p "$result_dir"

if [[ -z "${CODERAY_GIT_REPOSITORY_URL:-}" ]]; then
  if [[ -n "${CI_PROJECT_URL:-}" ]]; then
    if [[ "$CI_PROJECT_URL" == *.git ]]; then
      CODERAY_GIT_REPOSITORY_URL="$CI_PROJECT_URL"
    else
      CODERAY_GIT_REPOSITORY_URL="${CI_PROJECT_URL}.git"
    fi
  else
    CODERAY_GIT_REPOSITORY_URL="${CI_REPOSITORY_URL:-}"
  fi
fi

export CODERAY_BRANCH="${CODERAY_BRANCH:-${CI_COMMIT_REF_NAME:-}}"
export CODERAY_COMMIT_SHA="${CODERAY_COMMIT_SHA:-$CI_COMMIT_SHA}"
export CODERAY_GIT_REPOSITORY_URL
export CODERAY_MERGE_REQUEST_IID="${CODERAY_MERGE_REQUEST_IID:-$CI_MERGE_REQUEST_IID}"
export CODERAY_PROJECT_PATH="${CODERAY_PROJECT_PATH:-${CI_PROJECT_PATH:-}}"
export CODERAY_RESULT_DIR="$result_dir"
export CODERAY_RESULT_FILE="${CODERAY_RESULT_FILE:-${result_dir}/coderay-xg-result-${CI_MERGE_REQUEST_IID}-${commit_short}.txt}"

if ! run_coderay_cli "$result_dir" "$cli_log"; then
  log "CodeRay XG CLI 실행 로그:"
  sed -n '1,160p' "$cli_log" >&2 || true
  die "CodeRay XG CLI 스캔 요청이 실패했습니다."
fi

log "CLI 요청은 완료되었습니다. 결과 txt 파일 생성을 기다립니다."

if ! result_file="$(wait_for_result_file "$result_dir" "$CODERAY_TIMEOUT_SECONDS" "$CODERAY_POLL_INTERVAL_SECONDS" "$CODERAY_STABLE_SECONDS")"; then
  log "CodeRay XG CLI 실행 로그:"
  sed -n '1,160p' "$cli_log" >&2 || true
  die "제한 시간 내에 CodeRay XG 결과 txt 파일이 생성되지 않았습니다. result_dir=$result_dir"
fi

log "CodeRay XG 결과 파일 확인 완료: $result_file"

if is_boolean_true "${CODERAY_COMMENT_ENABLED:-true}"; then
  comment_file="$(mktemp)"

  ensure_current_mr_commit
  build_comment_body "$result_file" >"$comment_file"
  delete_previous_coderay_comments
  create_coderay_comment "$comment_file"
else
  log "MR 댓글 갱신은 비활성화되어 있습니다."
fi
