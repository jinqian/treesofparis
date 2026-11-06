#!/usr/bin/env bash
# Tests for commit-msg-check.sh. Usage: scripts/commit-msg-check_test.sh
set -u
here="$(cd "$(dirname "$0")" && pwd)"
check="$here/commit-msg-check.sh"
fail=0

expect() { # expected_exit message
  local want="$1" msg="$2" file
  file="$(mktemp)"
  printf '%s\n' "$msg" > "$file"
  "$check" "$file" >/dev/null 2>&1
  local got=$?
  rm -f "$file"
  if { [ "$want" = ok ] && [ $got -ne 0 ]; } || { [ "$want" = bad ] && [ $got -eq 0 ]; }; then
    echo "FAIL: expected $want for: '$msg'"
    fail=1
  fi
}

expect ok  "feat: add map"
expect ok  "feat(map)!: change contract"
expect ok  "fix(data): handle empty species"
expect ok  "Merge branch 'main' into feat/x"
expect bad "added stuff"
expect bad "feat:missing space"
expect bad "unknown: type"
expect bad ""

[ $fail -eq 0 ] && echo "all commit-msg tests passed"
exit $fail
