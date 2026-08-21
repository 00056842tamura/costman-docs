"""
LLM 使用量記録フック — Stop / SubagentStop

1 やり取り（アシスタント1応答）の終了時に発火し、工数計上のための記録を
issue（チケット）単位で 1 行追記します:

  specs/{案件キー}/{工程dir}/{issue_id}/llm-usage.jsonl   ← 1 行 = 1 やり取り（JSONL）
  （SS 以降は {スタック}/specs/{案件キー}/{工程dir}/{issue_id}[_{機能ID}]/llm-usage.jsonl）

記録内容（1 レコード）:
  - source            : "main"（メイン会話）/ "subagent"（サブエージェント）
  - model             : 使用した LLM モデル ID
  - input/output/cache_read/cache_creation_input_tokens : トークン内訳（合算）
  - total_tokens      : 上記合計
  - duration_sec      : 壁時計の作業時間（直前ユーザー発言 → 最終アシスタント応答）
  - exchange_uuid     : 当該やり取りの一意キー（集計時の重複排除用）
  - branch / user / session_id / ts(date)

設計方針:
  - 読むべきトランスクリプトは hook 入力の `agent_transcript_path`（`SubagentStop` 時に
    存在。当該サブエージェント自身の孤立したトランスクリプト）を最優先とし、
    無ければ `transcript_path`（`Stop` 時。メイン会話自身のトランスクリプト）に
    フォールバックする。**`transcript_path` は `SubagentStop` でも常に親（メイン会話）
    セッションを指し、サブエージェント自身の実行内容を含まない**（ADR-010で実機確認）。
  - 「直前のユーザー発言」の境界は、ツール実行ループの折り返し（tool_result のみで
    構成される合成 user エントリ）を除外した `find_last_real_user_index()` で判定する
    （tool_result エコーを新規ユーザー発言と誤判定すると、それより前のツール呼び出し
    ターンが集計から漏れる。ADR-010で実機確認）。
  - usage は `message.id` 単位でデデュープしてから合算する（1ターンがストリーミングで
    複数トランスクリプトエントリに分割され、同一 usage を重複して持つ場合があるため。
    ADR-010で実機確認）。
  - チケットへの帰属は **git ブランチ** から解決する（log_work.py と同一ロジックを再利用）。
    feature/{案件キー}-{工程}[-{機能ID}] → specs 配下の issue ディレクトリ。
    編集対象ファイルがスタックディレクトリ配下（独立リポジトリ）の場合は、
    そのスタックリポ自身の現在ブランチを見る（`log_work.find_owning_repo_root` で解決）。
    `issue-init` が書き込むスコープ確定マーカー（`.claude/.issue-scope.json`）が存在する
    場合はブランチ名解析より優先する（`log_work.resolve_issue_work_logs` 内で解決）。
  - メイン会話とサブエージェントは **別トランスクリプト**＝トークンが重複しないので、
    source タグを付けて別行で計上し、合算（集計）は後段に委ねる。
  - モデル・トークンはトランスクリプト(JSONL)の `message.usage`/`message.model` から取得。
    作業時間は各エントリの `timestamp` 差分（壁時計・ツール実行時間込みの近似）。
  - トランスクリプトのスキーマはバージョン依存があるため **ベストエフォート**。
    取得不能ならその行をスキップする **silent-failure**（本処理・会話を妨げない）。
    調査時は `.claude/.hook-debug-enabled`（マーカーファイル）または環境変数
    `CLAUDE_HOOK_DEBUG=1` で `.claude/hook-debug.log` への診断ログ出力を有効化できる
    （既定は無効・本番の llm-usage.jsonl とは別ファイル）。
"""
import os
import sys
import json
from datetime import datetime
from pathlib import Path

# 同ディレクトリの log_work.py から「ブランチ→issue ディレクトリ解決」を再利用する。
sys.path.insert(0, str(Path(__file__).resolve().parent))
try:
    import log_work
except Exception:
    log_work = None

TOKEN_KEYS = (
    "input_tokens",
    "output_tokens",
    "cache_read_input_tokens",
    "cache_creation_input_tokens",
)


def is_debug_enabled(product_root):
    """診断ログの有効化判定（オプトイン・既定は無効）。

    運用時（環境変数を設定できる場合）は CLAUDE_HOOK_DEBUG=1、
    調査・テスト時（本プロセスの起動元環境変数を書き換えられない場合。
    Stop/SubagentStop フックは Claude Code 本体が既存プロセスの環境で起動するため、
    ツール呼び出し内から動的に環境変数を注入することはできない）はマーカーファイル
    `.claude/.hook-debug-enabled` の存在で切り替える。
    """
    if os.environ.get("CLAUDE_HOOK_DEBUG") == "1":
        return True
    try:
        return product_root is not None and (product_root / ".claude" / ".hook-debug-enabled").exists()
    except Exception:
        return False


def debug_log(product_root, record):
    """診断ログを本番の llm-usage.jsonl とは別ファイルへ追記する（silent-failure）。"""
    try:
        debug_file = product_root / ".claude" / "hook-debug.log"
        payload = dict(record)
        payload["ts"] = datetime.now().strftime("%Y-%m-%dT%H:%M:%S.%f")
        payload["pid"] = os.getpid()
        debug_file.parent.mkdir(parents=True, exist_ok=True)
        with open(debug_file, "a", encoding="utf-8") as f:
            f.write(json.dumps(payload, ensure_ascii=False, default=str) + "\n")
    except Exception:
        pass


def read_hook_input():
    """フック入力 JSON を取得する。標準入力（公式仕様）→ 環境変数の順で試す。"""
    raw = ""
    try:
        raw = sys.stdin.read()
    except Exception:
        raw = ""
    if raw:
        try:
            return json.loads(raw)
        except Exception:
            pass
    env_json = os.environ.get("CLAUDE_HOOK_INPUT")
    if env_json:
        try:
            return json.loads(env_json)
        except Exception:
            pass
    return {}


def parse_ts(value):
    """ISO8601 文字列を datetime に変換。失敗時は None。"""
    if not value or not isinstance(value, str):
        return None
    text = value.strip()
    if text.endswith("Z"):
        text = text[:-1] + "+00:00"
    try:
        return datetime.fromisoformat(text)
    except Exception:
        try:
            return datetime.strptime(value[:19], "%Y-%m-%dT%H:%M:%S")
        except Exception:
            return None


def load_transcript(path):
    """トランスクリプト(JSONL)を 1 行ずつ読み、パースできた要素のみ返す。"""
    entries = []
    try:
        with open(path, "r", encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if not line:
                    continue
                try:
                    entries.append(json.loads(line))
                except Exception:
                    continue
    except Exception:
        return []
    return entries


def entry_type(entry):
    """エントリ種別（user / assistant 等）を取得。"""
    t = entry.get("type")
    if t:
        return t
    msg = entry.get("message") or {}
    return msg.get("role")


def is_synthetic_user_turn(entry):
    """ツール実行ループの折り返し（tool_result のみを内容とする合成 user エントリ）かどうか判定する。

    トランスクリプト上は「人間が入力した新規プロンプト」も「直前の tool_use に対する
    tool_result」も同じ type="user" として記録される（実機検証で確認）。後者を
    「直前のユーザー発言」の境界判定に含めると、境界が同一やり取り内の直近のツール往復
    まで前進してしまい、それより前のツール呼び出しターン（usage・編集ファイルとも）が
    集計から漏れる。content が tool_result ブロックのみで構成されるエントリを合成エントリ
    として除外し、真に新しいユーザー発言のみを境界判定の対象にする。
    """
    msg = entry.get("message") or {}
    content = msg.get("content")
    if not isinstance(content, list) or not content:
        return False
    return all(isinstance(b, dict) and b.get("type") == "tool_result" for b in content)


def find_last_real_user_index(entries):
    """直前の「真の」ユーザー発言（tool_result 折り返しを除く）のインデックスを返す。無ければ -1。"""
    last_user = -1
    for i, e in enumerate(entries):
        if entry_type(e) == "user" and not is_synthetic_user_turn(e):
            last_user = i
    return last_user


def summarize_latest_exchange(entries):
    """直前のユーザー発言以降のアシスタント応答を集計して 1 やり取り分を返す。

    1 やり取りはツール実行ループで複数 API 呼び出しに分かれるため usage を合算する。
    集計対象が無ければ None。
    """
    last_user = find_last_real_user_index(entries)

    segment = entries[last_user + 1:] if last_user >= 0 else entries
    assistants = [e for e in segment if entry_type(e) == "assistant"]
    if not assistants:
        return None

    # 1 ターン（1 API 呼び出し）はストリーミングにより thinking/tool_use/text 等が
    # 複数のトランスクリプトエントリに分割され、各エントリの message.usage が
    # そのターンの usage を重複して（同一値、または部分点→最終値として）持つ
    # ことがある（実機検証で確認）。message.id が同じエントリは同一ターンとみなし、
    # 最後に観測した usage のみを採用してからターン単位で合算することで二重集計を避ける
    # （message.id を持たない旧形式のエントリはエントリ単位のまま扱う）。
    per_turn_usage = {}
    turn_order = []
    model = None
    last_uuid = None
    last_assistant_ts = None

    for idx, e in enumerate(assistants):
        msg = e.get("message") or {}
        key = msg.get("id") or ("_no_message_id_", idx)
        if key not in per_turn_usage:
            turn_order.append(key)
        per_turn_usage[key] = msg.get("usage") or {}
        if msg.get("model"):
            model = msg.get("model")
        if e.get("uuid"):
            last_uuid = e.get("uuid")
        ts = parse_ts(e.get("timestamp"))
        if ts:
            last_assistant_ts = ts

    totals = {k: 0 for k in TOKEN_KEYS}
    for key in turn_order:
        usage = per_turn_usage[key]
        for k in TOKEN_KEYS:
            v = usage.get(k)
            if isinstance(v, (int, float)):
                totals[k] += int(v)

    start_ts = parse_ts(entries[last_user].get("timestamp")) if last_user >= 0 else None
    duration = None
    if start_ts and last_assistant_ts:
        try:
            duration = (last_assistant_ts - start_ts).total_seconds()
            if duration < 0:
                duration = None
        except Exception:
            duration = None

    return {
        "model": model,
        "tokens": totals,
        "total_tokens": sum(totals.values()),
        "duration_sec": duration,
        "exchange_uuid": last_uuid,
    }


def find_latest_edited_path(entries):
    """直前のユーザー発言以降のアシスタント応答から、最後に Write/Edit 等で
    編集対象になったファイルパス（tool_use の file_path 引数）を返す。無ければ None。

    1 やり取りは複数リポ（案件リポ・複数スタックリポ）を横断しうるため厳密な帰属はできないが、
    「このやり取りの終わり際に触っていたファイル」を代表値として branch 解決に使う（ベストエフォート）。
    """
    last_user = find_last_real_user_index(entries)
    segment = entries[last_user + 1:] if last_user >= 0 else entries

    latest_path = None
    for e in segment:
        if entry_type(e) != "assistant":
            continue
        msg = e.get("message") or {}
        content = msg.get("content") or []
        if not isinstance(content, list):
            continue
        for block in content:
            if not isinstance(block, dict) or block.get("type") != "tool_use":
                continue
            tool_input = block.get("input") or {}
            fp = tool_input.get("file_path")
            if fp:
                latest_path = fp
    return latest_path


def main():
    product_root = None
    debug = False
    try:
        product_root = Path(__file__).resolve().parent.parent.parent
        debug = is_debug_enabled(product_root)
    except Exception:
        debug = False

    try:
        if log_work is None:
            if debug:
                debug_log(product_root, {"stage": "abort", "reason": "log_work import failed"})
            return

        data = read_hook_input()
        event = data.get("hook_event_name") or ""
        session_id = data.get("session_id") or ""
        # SubagentStop の hook 入力には、当該サブエージェント自身の孤立したトランスクリプト
        # （{セッションdir}/subagents/agent-{agent_id}.jsonl）を指す agent_transcript_path が
        # 別途含まれる。data.get("transcript_path") は常に親（メイン会話）セッションの
        # トランスクリプトを指しており、サブエージェントの応答・tool_use は一切含まれない
        # （実機検証で確認）。agent_transcript_path を優先し、存在しない場合（Stop イベント。
        # メイン会話自身にはサブ会話が無いため transcript_path がそのまま正しい）のみ
        # transcript_path にフォールバックする。
        transcript_path = (
            data.get("agent_transcript_path")
            or data.get("transcript_path")
            or os.environ.get("CLAUDE_TRANSCRIPT_PATH")
        )

        if debug:
            debug_log(product_root, {
                "stage": "input",
                "event": event,
                "session_id": session_id,
                "agent_transcript_path": data.get("agent_transcript_path"),
                "transcript_path": transcript_path,
                "transcript_exists": bool(transcript_path and os.path.exists(transcript_path)),
            })

        if not transcript_path:
            if debug:
                debug_log(product_root, {"stage": "abort", "reason": "no transcript_path", "session_id": session_id})
            return

        entries = load_transcript(transcript_path)
        summary = summarize_latest_exchange(entries)

        if debug:
            debug_log(product_root, {
                "stage": "transcript",
                "session_id": session_id,
                "entry_count": len(entries),
                "summary_found": summary is not None,
                "exchange_uuid": (summary or {}).get("exchange_uuid"),
            })

        if not summary:
            if debug:
                debug_log(product_root, {"stage": "abort", "reason": "no summary (no assistant entries after last user turn)", "session_id": session_id})
            return

        source = "subagent" if event == "SubagentStop" else "main"

        product_root = log_work.find_product_root()

        # 編集対象ファイル（このやり取りの最後に触れたもの）からリポジトリ（案件 or スタック）を推定
        # find_owning_repo_root は target_path から最も近い .git 境界を遡って探すため、
        # product_root 相対パスへの変換なしに絶対パスをそのまま渡す。
        edited_path = find_latest_edited_path(entries)
        owning_repo_root = product_root
        if edited_path:
            owning_repo_root = log_work.find_owning_repo_root(product_root, edited_path)

        branch = log_work.get_git_branch(owning_repo_root)
        user = log_work.get_user_name()

        # -sp-{手戻りID} ブランチは rework ディレクトリに記録する（配置先は編集対象ファイルのリポジトリ側）
        import re as _re
        sp_match = _re.search(r"-sp-(\d{3,})", branch or "")
        case_match = _re.match(r"^feature/(?P<case>.+?)-(?:ssplan|pgplan|ptplan|sa|ui|ss|pg|pt)", branch or "")
        if sp_match and case_match:
            sp_id = sp_match.group(1)
            case_key = case_match.group("case")
            rework_dir = owning_repo_root / "specs" / case_key / "rework" / f"sp-{sp_id}"
            rework_dir.mkdir(parents=True, exist_ok=True)
            work_logs = [str(rework_dir / "work-log.md")]
        else:
            # ブランチ → issue 作業ディレクトリ（work-log.md のパス一覧）を再利用解決
            work_logs = log_work.resolve_issue_work_logs(product_root, branch, owning_repo_root)

        if debug:
            debug_log(product_root, {
                "stage": "scope",
                "session_id": session_id,
                "source": source,
                "edited_path": edited_path,
                "owning_repo_root": str(owning_repo_root),
                "branch": branch,
                "work_logs": [str(w) for w in work_logs],
            })

        if not work_logs:
            if debug:
                debug_log(product_root, {"stage": "abort", "reason": "no work_logs resolved for branch", "session_id": session_id, "branch": branch})
            return  # 新ワークフロー形式でないブランチは issue 単位記録なし

        now = datetime.now()
        duration = summary["duration_sec"]
        record = {
            "ts": now.strftime("%Y-%m-%dT%H:%M:%S"),
            "date": now.strftime("%Y-%m-%d"),
            "source": source,
            "model": summary["model"],
            "input_tokens": summary["tokens"]["input_tokens"],
            "output_tokens": summary["tokens"]["output_tokens"],
            "cache_read_input_tokens": summary["tokens"]["cache_read_input_tokens"],
            "cache_creation_input_tokens": summary["tokens"]["cache_creation_input_tokens"],
            "total_tokens": summary["total_tokens"],
            "duration_sec": round(duration, 1) if duration is not None else None,
            "exchange_uuid": summary["exchange_uuid"],
            "branch": branch,
            "user": user,
            "session_id": session_id,
        }
        line = json.dumps(record, ensure_ascii=False) + "\n"

        # work-log.md と同じ issue ディレクトリへ llm-usage.jsonl を追記
        # （area別工程で複数 issue dir が解決された場合は exchange_uuid で後段重複排除）
        for wl in work_logs:
            usage_file = Path(wl).parent / "llm-usage.jsonl"
            try:
                usage_file.parent.mkdir(parents=True, exist_ok=True)
                with open(usage_file, "a", encoding="utf-8") as f:
                    f.write(line)
                if debug:
                    debug_log(product_root, {"stage": "written", "session_id": session_id, "usage_file": str(usage_file), "exchange_uuid": record["exchange_uuid"]})
            except Exception as write_exc:
                if debug:
                    debug_log(product_root, {"stage": "write_exception", "session_id": session_id, "usage_file": str(usage_file), "error": repr(write_exc)})
                pass

    except Exception as exc:
        # silent failure: 本処理・会話を妨げない（診断ログのみ記録）
        if debug:
            debug_log(product_root, {"stage": "exception", "error": repr(exc)})
        pass


if __name__ == "__main__":
    main()
