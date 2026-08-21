#!/usr/bin/env bash
# proxy-detect.sh — gh CLI 用 proxy フォールバック検出
# source するだけで有効になる。直接実行不可。
#
# フォールバックチェーン:
#   1. HTTPS_PROXY が既にセットされている → そのまま使用
#   2. git config --global http.proxy が設定されている → 自動引用
#   3. どちらもなし → プロキシなしで動作（proxy 不要な環境）
#
# 詳細: C:\Users\n015u035\gitHub開発ワークフロー検証\ADR-improvement-002_gh-CLI-プロキシ接続問題.md

# 空文字列セット（HTTPS_PROXY=""）も「未設定」と同等に扱い、git config を優先する
if [ -z "${HTTPS_PROXY:-}" ] && [ -z "${HTTP_PROXY:-}" ]; then
  _proxy=$(git config --global http.proxy 2>/dev/null || true)
  if [ -n "$_proxy" ]; then
    export HTTPS_PROXY="$_proxy"
    export HTTP_PROXY="$_proxy"
  fi
  unset _proxy
fi
