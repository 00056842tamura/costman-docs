#!/usr/bin/env bash
# case-bootstrap.sh — 案件(Milestone)・統合ブランチ・ラベルを各リポに用意する（3 リポ前提）
#
# 使い方:
#   case-bootstrap.sh --case {案件キー} \
#       --case-repo {owner}/{案件リポ} \
#       [--stack-repo {owner}/{スタックリポ}]... \
#       [--base develop]
#
# 例:
#   case-bootstrap.sh --case inventory-2026-001 \
#       --case-repo myorg/inventory-case \
#       --stack-repo myorg/inventory-bs --stack-repo myorg/inventory-us-api
#
# 安全: DRY_RUN=1 を付けると gh/git の変更系コマンドを実行せず表示のみ。
#   DRY_RUN=1 case-bootstrap.sh --case ... --case-repo ...
#
# 前提: 事前に `--case-repo`・`--stack-repo` の各リポジトリが GitHub 上に存在していること
#   （本スクリプトはリポジトリを作成しない。存在しない場合はエラーで停止する）。
#
# 規約: .claude/rules/github-ops.md / .claude/rules/product-rules.md
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
. "$SCRIPT_DIR/lib/proxy-detect.sh"

CASE=""; BASE="develop"; CASE_REPO=""; STACK_REPOS=()
DRY_RUN="${DRY_RUN:-0}"

usage() { sed -n '2,18p' "$0"; exit 1; }

while [ $# -gt 0 ]; do
  case "$1" in
    --case)       CASE="$2"; shift 2 ;;
    --base)       BASE="$2"; shift 2 ;;
    --case-repo)  CASE_REPO="$2"; shift 2 ;;
    --stack-repo) STACK_REPOS+=("$2"); shift 2 ;;
    -h|--help)    usage ;;
    *) echo "不明な引数: $1" >&2; usage ;;
  esac
done
[ -n "$CASE" ] || { echo "ERROR: --case (案件キー) は必須" >&2; usage; }
[ -n "$CASE_REPO" ] || { echo "ERROR: --case-repo は必須" >&2; usage; }

run() {  # DRY_RUN なら表示のみ
  if [ "$DRY_RUN" = "1" ]; then echo "  [dry-run] $*"; else "$@"; fi
}

# 工程・type・area・rework・repo の各ラベル（冪等: --force）
LABELS_KEY=(工程:sa 工程:ui 工程:ss-plan 工程:ss 工程:pg-plan 工程:pg 工程:pt-plan 工程:pt)
LABELS_TYPE=(type:phase type:task)
LABELS_AREA=(area:bs area:us-api area:us-mpa area:frontend area:batch)
LABELS_MISC=(rework repo:case repo:stack)

ensure_repo_exists() {  # リポジトリの実在確認（読み取り専用・副作用なし。DRY_RUNに関わらず常に実行）
  local repo="$1"
  if ! gh api "repos/$repo" >/dev/null 2>&1; then
    echo "ERROR: リポジトリ '$repo' が見つかりません。case-bootstrap.sh はリポジトリを作成しません。" >&2
    echo "       人間が作成してから再実行してください（docs/onboarding/gh-cli-setup.md §6.1/§6.2）。" >&2
    exit 1
  fi
}

ensure_milestone() {
  local repo="$1"
  if gh api "repos/$repo/milestones" --jq '.[].title' 2>/dev/null | grep -qx "$CASE"; then
    echo "  Milestone '$CASE' は既存（$repo）"
  else
    run gh api -X POST "repos/$repo/milestones" -f title="$CASE" -f state=open >/dev/null
    echo "  Milestone '$CASE' を作成（$repo）"
  fi
}

ensure_labels() {
  local repo="$1"; shift
  local color="$1"; shift
  for name in "$@"; do
    # --force はコロン含みラベル名の PATCH URL でコケるため、存在時はスキップ
    run gh label create "$name" --repo "$repo" --color "$color" >/dev/null 2>&1 || true
  done
}

ensure_branch() {  # 統合ブランチ feature/{案件キー} を base から作成（ローカル clone 不要）
  local repo="$1" branch="feature/$CASE"
  if gh api "repos/$repo/git/ref/heads/$branch" >/dev/null 2>&1; then
    echo "  ブランチ '$branch' は既存（$repo）"
  else
    local sha
    sha="$(gh api "repos/$repo/git/ref/heads/$BASE" --jq '.object.sha')" \
      || { echo "  WARN: base '$BASE' が無い（$repo）。手動で統合ブランチを作成してください" >&2; return 0; }
    run gh api -X POST "repos/$repo/git/refs" -f ref="refs/heads/$branch" -f sha="$sha" >/dev/null
    echo "  ブランチ '$branch' を作成（$repo・base=$BASE）"
  fi
}

bootstrap_repo() {
  local repo="$1" kind="$2"   # kind=case|stack
  echo "▶ $repo（$kind）"
  ensure_repo_exists "$repo"
  ensure_milestone "$repo"
  ensure_labels "$repo" BFD4F2 "${LABELS_KEY[@]}"
  ensure_labels "$repo" C2E0C6 "${LABELS_TYPE[@]}"
  ensure_labels "$repo" FEF2C0 "${LABELS_AREA[@]}"
  ensure_labels "$repo" F9D0C4 "${LABELS_MISC[@]}"
  ensure_branch "$repo"
}

echo "=== 案件ブートストラップ: $CASE（base=$BASE・DRY_RUN=$DRY_RUN）==="
bootstrap_repo "$CASE_REPO" case
for r in "${STACK_REPOS[@]:-}"; do [ -n "$r" ] && bootstrap_repo "$r" stack; done

echo "✅ 完了。次: 各リポに Milestone '$CASE'・ラベル・統合ブランチ feature/$CASE が揃いました。"
echo "   meta.md の「リポジトリ」欄にリポ slug を記録し、/issue-init で工程 issue を起票してください。"
