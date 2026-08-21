# ADR-006: 既存 Write/Edit 系フック（check_java 等 4 件）のブロック実効性欠陥の解消方針

> ✅ **本ADRは実装済み**（2026-07-08）。ADR-No55対応中に発見した問題を調査・ドラフト作成後、ユーザーの「先に実際のClaude Code上で動作確認してから決めたい」との指示に応じ、実機（Write ツールでLombokインポートを含むJavaファイルを実際に書き込み）で欠陥1〜3の実在を再確認した上でユーザー承認を得て実装した。

## 目的（この対応で達成したいこと）

- **Why（なぜ対応するか）**: ADR-No55（工程内バックログ解消ゲート）の新設フック `check_backlog_gate.py`/`.sh` を実装する過程で、既存の4フック（`.claude/hooks/check_java.py`・`check_typescript.py`・`check_mapper_xml.py`・`check_pom_xml.py`。以下「既存4フック」）について、CLAUDE.md §7・各フックのdocstringが明記する「違反検知時に書き込みをブロックします」という挙動が、実際には機能していない疑いのある**3つの独立した実装上の欠陥**を発見した。これらはLombok禁止・スターインポート禁止・`any`型禁止・SQLインジェクション注意・Lombok依存禁止等、コーディング規約・セキュリティガード機構の実効性そのものに関わる問題であり、放置できない。
- **What（何を対応するか）**: (1) 入力受け取り方式の誤り（stdinではなく実際には設定されない環境変数から読んでいるため検知ロジックに到達しない）、(2) exit codeの誤用（`sys.exit(1)`はPreToolUseの非ブロッキングエラーであり処理が継続される）、(3) シェルの`python3 ... || python ...`フォールバック連結が、python3不在時だけでなく「違反検知でexit(1)する」その動作自体によっても誤発動し、2回目の呼び出しが空のstdinしか受け取れず誤って通過する、という3つの相互依存する欠陥を対象とする。
- **How（どう対応するか）**: 本ADRでは実装しない。3欠陥それぞれを実証した結果と、既に別ブランチ（`feature/walkthrough-no55-backlog-resolution-gate`・PR #20）で実装済みの新設フックが採用した解決策（単一インタプリタ選定ラッパー＋stdin優先読み込み＋`exit(2)`使用）を既存4フックに適用する対応計画をドラフトし、レビュー・承認後に別タスクで実装する。

## メタ情報

| 項目 | 値 |
|---|---|
| 日付 | 2026-07-08 |
| ステータス | ✅ 実装済み（2026-07-08） |
| 対象 Issue | N/A（案件に属さない機構メンテナンス） |
| 起票 Step | 工程外（ADR-No55〔工程内バックログ解消ゲート〕の新設フック実装中に発見。ADR-No55コミットメッセージにも「既存フック（check_java.py 等）の exit(1) 問題は別ADRで対応予定のため本対応では変更しない」と明記され、本ADRへ先送りされていた） |
| 関連 ADR | ADR-No55-工程内バックログ解消ゲート（`ウォークスルー管理台帳_対応計画/ADR-No55-工程内バックログ解消ゲート.md`。本ADRが対応する3欠陥のうち欠陥2・3を、新設フック`check_backlog_gate.py`/`.sh`のみ独自に回避済み。既存4フックへの同種対応は本ADRのスコープとして先送りされた） |

## 背景

### ①状況

ADR-No55対応（工程内バックログ解消ゲート、新規PreToolUseフック`check_backlog_gate.py`）の実装中、既存4フックを参考にした際、これらが実際にブロックとして機能しているかを実機検証したところ、以下3つの独立した欠陥が判明した。3欠陥は互いに影響し合っており、いずれか1つだけを修正しても実効性は回復しない（詳細は②）。

### ②課題・問題

**欠陥1: 入力受け取り方式の誤り（環境変数 vs 標準入力）**

既存4フックはいずれも次の形でツール入力を取得している。

```python
tool_input = json.loads(os.environ.get("CLAUDE_TOOL_INPUT", "{}"))
```

しかし、Claude Code公式ドキュメント（`https://code.claude.com/docs/en/hooks`）をWebFetchで確認した結果、PreToolUseフックの入力は**標準入力（stdin）経由でJSON**として渡される仕様であり、`tool_input`フィールドにファイルパス等が含まれる（例: `{"tool_name":"Edit","tool_input":{"file_path":"/path/to/file.ts"}}`）。`CLAUDE_TOOL_INPUT`という環境変数についての記載は同ドキュメントに存在しない。別途フック実行時に渡される環境変数を確認したところ、明記されているのは `CLAUDE_PROJECT_DIR`・`CLAUDE_PLUGIN_ROOT`・`CLAUDE_PLUGIN_DATA`・`CLAUDE_ENV_FILE`・`CLAUDE_EFFORT`・`CLAUDE_CODE_REMOTE` のみで、`CLAUDE_TOOL_INPUT`は含まれない。`.claude/settings.json`のフック起動コマンド自体にも`CLAUDE_TOOL_INPUT`を設定する処理は存在しない。

結果として、既存4フックは実際のClaude Code実行時には常に`tool_input = {}`を受け取り、`file_path = tool_input.get("file_path", "")`は常に空文字列になる。4フックいずれも `if not file_path.endswith(".java"): return` 等の拡張子判定から検知処理を始めるため、この判定が常に不成立となり、**検知ロジックには実際には一度も到達しない**。

**実証**: `check_java.py`に対し、Claude Codeの実際の呼び出し方（stdin経由でLombokインポートを含む違反コードのJSONを渡す）を再現したところ、検知されず（出力なし・exit 0）。同一データを`CLAUDE_TOOL_INPUT`環境変数として渡した場合のみ検知された（違反メッセージ出力・exit 1）ことを確認した。

```
$ cat hook_input.json | python3 .claude/hooks/check_java.py   # stdin経由（公式仕様どおり）
（無出力）EXIT_CODE=0

$ CLAUDE_TOOL_INPUT='{...}' python3 .claude/hooks/check_java.py < /dev/null   # 環境変数経由（現行フックの実装）
[Springer 規約チェック] Foo.java に違反を検出（書き込みをブロックしました）:
  [規約違反 L1] Lombok インポート禁止: import lombok.Data; → 削除すること
EXIT_CODE=1
```

なお、同じ`.claude/hooks/`配下の`log_work.py`（PostToolUse）・`log_llm_usage.py`（Stop/SubagentStop）は`sys.stdin.read()`で正しくstdinから読み取っている（grep実証済み）。今回のADR-No55新設フック`check_backlog_gate.py`も`read_hook_input()`内で`sys.stdin.read()`を第一候補とし、`CLAUDE_TOOL_INPUT`環境変数は後方互換のための保険的フォールバックとしてのみ併用している。**既存4フックのみ**が環境変数単独読み取りという誤った実装になっている。

**欠陥2: `sys.exit(1)`はPreToolUseの非ブロッキングエラー**

既存4フックのうち3ファイル（`check_java.py`・`check_typescript.py`・`check_pom_xml.py`。`check_mapper_xml.py`は警告専用設計のため対象外）は違反検知時に`sys.exit(1)`を呼んでいる。docstring・CLAUDE.md §7はいずれも「書き込みをブロックします」と明記しているが、Claude Code公式ドキュメントをWebFetchで確認した結果、以下が明記されている。

> "For most hook events, only exit code 2 blocks the action. Claude Code treats exit code 1 as a non-blocking error and proceeds with the action, even though 1 is the conventional Unix failure code. If your hook is meant to enforce a policy, use `exit 2`."
>
> "Exit 2 means a blocking error. Claude Code ignores stdout and any JSON in it. Instead, stderr text is fed back to Claude as an error message."（PreToolUseでの効果: "Blocks the tool call"）

つまり`sys.exit(1)`は「フックの実行自体が失敗した」という非ブロッキングエラーとして扱われ、Claude Code側はWrite/Editツールの実行を継続する。ブロックを意図するなら`sys.exit(2)`を使う必要がある。ADR-No55の`check_backlog_gate.py`はこれを踏まえ、docstring内に明示的な注記（「重要: PreToolUse フックで実際にツール呼び出しをブロックできるのは exit code 2 のみ」）を残し、最初から`sys.exit(2)`を使用している。

**欠陥3: シェルの`python3`/`python`フォールバック連結によるstdin二重読み取り（自己誘発型を含む）**

`.claude/settings.json`は既存4フックを以下の形式で起動している（`check_mapper_xml.py`のみ末尾に`2>/dev/null || true`が追加。他は`|| python ...`のみ）。

```json
"command": "bash -c \"python3 .claude/hooks/check_java.py 2>/dev/null || python .claude/hooks/check_java.py\""
```

この`||`連結は「python3が無い環境ではpythonにフォールバックする」意図だが、bashの`||`は「直前のコマンドの終了コードが非ゼロなら次を実行する」という単純な規則であり、**「python3コマンドが見つからない」ケースと「python3が正常に実行され、かつスクリプトが意図的に非ゼロで終了した」ケースを区別しない**。

**実証（1）＝コマンド未検出の場合**: `python3`コマンドが存在しない場合、bashは該当コマンドをfork・execせず即座に`command not found`として次のコマンドに移るため、stdinは消費されず、フォールバック先は完全な入力を受け取ることを確認した。

```
$ echo "hello_stdin_content" | bash -c "nonexistentcmd_xyz_12345 2>/dev/null || cat"
hello_stdin_content        # catが正しく受け取れている（drainされていない）
```

**実証（2）＝より重大な自己誘発型（python3が正常動作する環境でも発生）**: 欠陥1のみを仮に修正し（stdinから正しくJSONを読むロジックに変更）、欠陥2は未修正のまま（`sys.exit(1)`を維持）にした場合を想定し、実際の`settings.json`と同一の起動パターン（`bash -c "python3 X 2>/dev/null || python X"`）で再現実験を行った。本機（`python3`・`python`とも正常動作するWindows/Git Bash環境）で以下の結果を得た。

```
$ echo "$INPUT_JSON" | bash -c "python3 check_java_fixed_stdin.py 2>stderr1.log || python check_java_fixed_stdin.py 2>stderr2.log"
[NO VIOLATION] passed through          # ← 2回目(python)の標準出力
$ echo $?
0                                       # ← bash -c 全体の終了コード
$ cat stderr1.log
[VIOLATION DETECTED] lombok import found   # ← 1回目(python3)は正しく検知していた
$ cat stderr2.log
（空）                                   # ← 2回目は何も検知できていない
```

流れは以下の通り。

1. 1回目の`python3 check_java.py`がstdinを正しく読み、Lombokインポートの違反を検知し、意図通り`sys.exit(1)`する（stderr1.logに検知メッセージが出ている＝検知自体は成功していた）。
2. bashは終了コード1（非ゼロ）を「python3コマンドが失敗した」と解釈し、`||`右辺の`python check_java.py`にフォールバックする。
3. パイプのstdinは1回目の`python3`プロセスが読み切っているため、2回目の`python`プロセスは空のstdinしか受け取れない（stdinは1回しか読めないため）。
4. 2回目は`tool_input`が空になり違反を検知できず、暗黙的に`exit 0`で終了する（stderr2.logが空）。
5. `bash -c "..."`全体の終了コードは最後に実行されたコマンド（2回目の`python`）の終了コード、すなわち`0`になる。

つまり、欠陥1（env var）だけを直しても、欠陥2（`exit 1`）・欠陥3（`||`連結）が残っていれば、**「違反を検知した」という正しい判定そのものが、シェルのフォールバック機構によって握り潰される**。しかもこれは「python3が使えない」という限定的な環境問題ではなく、**python3が完全に正常動作する環境でも、違反を検知してexit(1)する設計自体が原因で毎回発生する**普遍的な欠陥である。3欠陥は独立に見えるが「1つ直すと別の欠陥が露出する」形で連動しており、3点セット同時解消が必要と判断した。

なお、この自己誘発型のリスクは`.claude/hooks/check_backlog_gate.sh`のコメント内に既に言語化されていた（ADR-No55実装時に発見・回避済み。「python3 が実行できた場合（＝コマンドが見つからず失敗するのではなく、スクリプト自身が sys.exit(2) でブロックした場合）に、標準入力（フック入力 JSON）を python3 側が読み切ってしまい、`||` で起動する python 側が空の標準入力を受け取って誤って通過（exit 0）してしまう問題がある」）。本ADRの発見は、これを既存4フックに当てはめて実際に再現・実証したものである。

### ③制約

- `check_mapper_xml.py`は違反検知時も`sys.exit`を呼ばず警告表示のみで常に`exit 0`となる設計（そもそもブロックする意図が無い）であるため、欠陥2・欠陥3（自己誘発型）は適用されない。欠陥1（env var読み取り）のみが実害となる。
- PostToolUse（`log_work.py`）・Stop/SubagentStop（`log_llm_usage.py`）は`sys.stdin.read()`を正しく使用しており欠陥1の対象外。両ファイルとも明示的な`sys.exit(非ゼロ)`呼び出しが無いため（grep確認済み）、欠陥3の自己誘発シナリオも現状は発生しない。ただし起動コマンドは同じ`python3 ... || python ...`連結パターンを保持しており、将来これらのスクリプトに非ゼロ終了処理を追加する場合は同種のリスクを再導入する点に注意が必要（本ADRのスコープ外・将来の留意点として記録のみ）。
- 本タスクでは実装を行わない（ユーザー指示）。対応計画のレビュー・承認後、別タスクで実装する。
- 本機（Windows環境・Git Bash上のClaude Code実行環境）では`python3`・`python`いずれもWindowsAppsのPython 3.14.5エイリアスに解決され、両方が正常に動作することを確認した（`python3 --version`／`python --version`）。したがって「python3が見つからない」という意味でのフォールバック発動は本機では自然には再現されないが、欠陥3の自己誘発シナリオ（実証2）は`python3`が正常に動作する状況でも発生することを確認済みであり、環境に依存しない普遍的なリスクである。

## 決定事項

上記3欠陥はいずれも解消することを決定する。ただし、本ADRでは実装は行わない。具体的な実装方法（下記「対応計画」に示す推奨案）は、人間のレビュー・承認を経て確定する。

## 理由

- 欠陥1〜3は個別に修正すると別の欠陥が新たに顕在化する相互依存関係にあるため、一括で解消する対応計画が必要と判断した（実証2参照）。
- ADR-No55の`check_backlog_gate.py`/`.sh`が採用した「ラッパースクリプトで事前にインタプリタを1つに確定し、stdinを1回だけ読んで渡す」設計は、既に本リポジトリで実機検証済みかつ実装済みの解法であり、既存4フックにも構造的に同じ解法を適用できる。ゼロから設計せず実績のある解法を再利用する方が、新たな不具合を持ち込むリスクが小さいと判断した。
- exit codeの変更（1→2）はClaude Code側の公式仕様に合わせる訂正であり、設計判断ではなく修正である。ADR-No55のコミット時点でも「既存フックのexit(1)問題は別ADRで対応予定」として本ADRのスコープに委ねられていた。
- 一方で、具体的な実装（ファイル変更）はユーザーから明示的に「本タスクでは実装しない」と指示されているため、対応計画のドラフト提示に留めた。

## 影響

- 既存4フックが実際に機能するようになることで、これまで検知されていなかった規約違反（Lombok禁止・スターインポート禁止・フィールドインジェクション禁止・`protected`禁止・SQLアノテーション禁止・広スコープcatch禁止・旧一時ファイルAPI禁止・SLF4J直接利用禁止・`System.out/err`禁止・ログ文字列連結禁止・ラッパークラスの`new`禁止・`WebSecurityConfigurerAdapter`継承禁止・`BCryptPasswordEncoder`強度未指定禁止・`@SchemaMapping`禁止・`React.FC`禁止・`any`型禁止・`dangerouslySetInnerHTML`禁止・`innerHTML`直接代入禁止・`console.log`残留禁止・`debugger`残留禁止・package.jsonバージョンワイルドカード禁止・Lombok依存関係禁止）が、初めて実際にブロックされるようになる。これは「新しい制約の追加」ではなく「文書化済みの既存制約が初めて実効化される」という性質の変化だが、これまで違反を含んだまま書き込めていたコードパターンが、修正後は書き込み時にブロックされるようになる点で、開発者（AIエージェント含む）から見た挙動は変化する（ブロックされる書き込みが増える）。
- `check_mapper_xml.py`は現状どおり警告のみ（`exit 0`固定）を維持する前提であれば、欠陥1のみの修正となり、動作は「`${}`使用を実際に検知して警告ログに出す」ようになる（ブロックはしない）。
- ラッパースクリプト化（`.sh`ファイル4本新設）により、`.claude/settings.json`の該当4コマンド文字列の変更が必要になる。
- `.claude/hooks/*.py`のdocstring・CLAUDE.md §7の説明文（「ブロックします」という記述自体は正しい意図の記述として変更不要だが、これまで実装が伴っていなかった期間があったことをどう扱うかは検討事項として残る）。

## 未確定事項（バックログ）

- [ ] `check_mapper_xml.py`は警告専用の設計を維持するか、他の3ファイルと同様に将来ブロック運用へ変更する可能性を検討するか（本ADRでは「現状の警告専用設計を維持」を前提としたが、製品判断としての確定はユーザー確認が必要）
- [ ] 欠陥1（env var読み取り）が「意図的な設計（Claude Codeの旧バージョン・別の実行方式を想定していた等）」なのか「単純な実装ミス」なのかの経緯は、実装当時のwork-log・commit logから追加調査すれば特定できる可能性があるが、本ADRの範囲では未調査
- [ ] PostToolUse（`log_work.py`）・Stop/SubagentStop（`log_llm_usage.py`）は本ADRのスコープ外としたが、将来これらに非ゼロ終了処理を追加する場合は同種のラッパー化が必要になる。その適用可否は将来のタスクとして記録するのみで、本ADRでは対応しない
- [ ] 修正後の`.claude/settings.json`のコマンド文字列・新設`.sh`ファイルの命名規約（`check_backlog_gate.sh`との統一）は対応計画のレビュー時に確定する
- [ ] 本ADRが対象とした4フック以外に、同種の「`CLAUDE_TOOL_INPUT`環境変数依存」または「`python3 ... || python ...`連結」パターンを持つフックが将来追加されないよう、フック新設時のチェックリスト化（`.claude/hooks/README.md`等への注記）を検討する余地があるが、本ADRの範囲では提案のみで実施しない
- [ ] **【実装時に新規発見・本ADRのスコープ外】** `check_typescript.py`の`any`型検知の正規表現`(?<!\w)(:\s*any\b|as\s+any\b|<any>|Array<any>)`に、本ADRとは無関係の別の欠陥がある。先頭の否定後読み`(?<!\w)`は「コロンの直前が単語文字でないこと」を要求するが、実際のTypeScriptの型注釈（`変数名: any`）はコロンの直前が常に単語文字（識別子の末尾）であるため、この最も一般的な書き方を検知できない（`let y : any;`のように直前に空白を置く非一般的な書き方でのみ検知される）。実装後の検証（`console.log`違反での検知確認は成功）でこの別欠陥に気付いた。本ADRの対応計画には含めず、別途新規Issue/ADRでの対応を推奨する。

## 対応計画（実装詳細・実装済み）

> ✅ 以下は2026-07-08に実装済み。

### 推奨方針: ADR-No55と同一のラッパースクリプト方式を4フックに適用する

#### 1. `.claude/hooks/check_java.py`・`check_typescript.py`・`check_pom_xml.py`（ブロック系3ファイル）

- 入力取得を`os.environ.get("CLAUDE_TOOL_INPUT", "{}")`単独から、`check_backlog_gate.py`の`read_hook_input()`と同等の「stdin優先→環境変数フォールバック」方式に変更する。
- 違反検知時の`sys.exit(1)`を`sys.exit(2)`に変更する。
- docstringの「違反を検出した場合は sys.exit(1) でツール呼び出しをブロックします」という記述を`sys.exit(2)`に修正する。

#### 2. `.claude/hooks/check_mapper_xml.py`（警告専用1ファイル）

- 入力取得のみ同様に修正する（stdin優先→環境変数フォールバック）。exit codeは変更しない（現状どおり常に`exit 0`・ブロックしない設計を維持する。バックログ①参照）。

#### 3. ラッパースクリプト新設（4本）

`check_backlog_gate.sh`と同一パターンで、各Pythonフックに対応する`.sh`ラッパーを新設する。

- `.claude/hooks/check_java.sh`
- `.claude/hooks/check_typescript.sh`
- `.claude/hooks/check_mapper_xml.sh`
- `.claude/hooks/check_pom_xml.sh`

各`.sh`は次の構造とする（`check_backlog_gate.sh`と同一設計。stdinを一度だけ`$(cat)`で読み取り、`command -v`で実行可能なインタプリタを事前に1つ確定してから、そのインタプリタにのみ入力を渡すことで、二重読み取りによるdrainingを構造的に回避する）。

```bash
#!/usr/bin/env bash
# check_java.py 呼び出しラッパー（PreToolUse・matcher: Write|Edit）。
# check_backlog_gate.sh と同一の理由により、python3/python を事前に1つ確定してから
# 選んだインタプリタ1つだけに標準入力を渡す（詳細はADR-006参照）。
set -u
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
INPUT="$(cat)"

if command -v python3 >/dev/null 2>&1; then
  PYBIN="python3"
elif command -v python >/dev/null 2>&1; then
  PYBIN="python"
else
  exit 0   # インタプリタなし → fail-safe（素通り）
fi

printf '%s' "$INPUT" | "$PYBIN" "$SCRIPT_DIR/check_java.py"
exit $?
```

（`check_typescript.sh`・`check_pom_xml.sh`も同様。`check_mapper_xml.sh`も同一構造だが、対象スクリプト自体が常に`exit 0`のため実質的な差異は無い。）

#### 4. `.claude/settings.json`の更新

PreToolUse（matcher: `Write|Edit`）の4エントリを、現行の`bash -c "python3 ... || python ..."`形式から、`bash .claude/hooks/check_java.sh`（他3本も対応するファイル名で同様）に変更する。`check_mapper_xml.py`のみ現状`|| true`が付与されているが、ラッパー内部で未検出時に`exit 0`固定（fail-safe）になるため、`settings.json`側の`|| true`は不要になる。

#### 5. 対象外（本ADRでは変更しない）

- `.claude/hooks/log_work.py`・`log_llm_usage.py`：既にstdinを正しく読み、明示的な非ゼロ終了も無いため対象外（③制約・バックログ③に将来留意点のみ記録）。
- `.claude/hooks/check_backlog_gate.py`・`.sh`：既に正しい設計のため変更不要。

#### 6. 検証（実施済み）

- ✅ `.sh`ラッパー経由（`echo '{...}' | bash .claude/hooks/check_java.sh`。実際の`settings.json`起動パターンと同一の経路）で4フック全てを検証。
  - `check_java.sh`: Lombokインポート違反 → `exit 2`（検知メッセージ出力あり）／クリーンなコード → `exit 0`。
  - `check_typescript.sh`: `console.log`違反 → `exit 2`／クリーンなコード → `exit 0`。
  - `check_pom_xml.sh`: Lombok依存関係違反 → `exit 2`／クリーンな`pom.xml` → `exit 0`。
  - `check_mapper_xml.sh`: `${}`使用 → 警告表示のみで`exit 0`（設計通りブロックしない）。
- ✅ 実装当初、`tool_input`の取り出し方に誤り（stdinのペイロード全体`{"tool_name":...,"tool_input":{...}}`を`tool_input`そのものとして扱ってしまい`file_path`が常に空になる新たなバグ）を検証中に発見し、`hook_data.get("tool_input", {})`で正しく取り出すよう即時修正した上で再検証し解消したことを確認した。
- ⚠️ **実機確認（Claude Code上でのWrite/Editツール経由）は本タスクでは実施していない**。ラッパースクリプト経由の検証（上記）はスクリプト単体としては実際の`settings.json`起動コマンドと同一だが、Claude Code本体からのhook呼び出し経路（hookSpecificOutput処理等）そのものは確認していない。次回、実際のWrite/Editツールでの違反コード書き込みを試行し、実際にブロックされることの最終確認を推奨する（ADR-006策定時に行った修正前の実機確認〔ブロックされないことの確認〕と対をなす、修正後の実機確認）。

## セルフレビュー（8観点チェック）

1. **中核具体性**: ✅解消。3欠陥それぞれの原因（環境変数読み取り・`exit(1)`の非ブロッキング仕様・シェルフォールバックの自己誘発ドレイン）を、実際のコード引用・公式ドキュメント引用・実行ログ付きで具体的に特定した。
2. **網羅性（実態照合）**: ✅解消。対象4フック全ての実装を実際に読み、`check_mapper_xml.py`のみ`exit(1)`を使わない設計であることを個別に確認した上で影響範囲を区別した。`log_work.py`・`log_llm_usage.py`（PostToolUse/Stop系）も実際にgrepし、同種のリスクの有無を確認した。`.claude/settings.json`記載の起動コマンドも実際に読み、`|| true`の有無の違いも捉えた。
3. **既存分岐の保持**: ✅解消。`check_mapper_xml.py`の「警告のみ・ブロックしない」という既存の設計意図は変更せず、`exit code`修正の対象から明示的に除外した。
4. **決定権者の明示**: ✅解消。本ADRは実装しないこと・対応計画は「推奨案」であり確定はレビュー後であることを明記し、ステータスを「対応計画確定待ち（人間レビュー中）」とした。`check_mapper_xml.py`の将来のブロック運用化についても製品判断としてユーザー確認が必要な未確定事項として明示した。
5. **逆依存の確認**: ✅解消。`.claude/settings.json`の実際のコマンド文字列を確認し、対象4フックの起動方式（`|| true`の有無を含む）を正確に反映した。ADR-No55の新設フック（`check_backlog_gate.py`/`.sh`）が既にこの3欠陥の一部（欠陥2・3）を回避済みであることを確認し、既存の解法を再利用する方針とした。
6. **境界条件**: ✅解消。「python3が見つからない」場合と「python3が正常実行され意図的に`exit(1)`する」場合とでstdin drainingの発生有無が異なるという境界条件を、両方を個別に実証実験で切り分けて確認した。前者はドレインが発生せず、後者（より一般的なケース）は環境を問わず発生することを示した。
7. **義務レベルの明示**: ✅解消。対応計画冒頭に「未実装・推奨案」であることを明示し、検証手順を「MUST・実施必須」として明記した。
8. **暗黙前提の再言語化**: ✅解消。「CLAUDE.md・docstringに『ブロックします』と書かれていれば実際にブロックされているはず」という暗黙の前提が、3つの独立した実装上の問題（うち1つはタスク着手前には認識されていなかった環境変数読み取りの誤り）によって崩れていたことを、推測ではなく実行ログを伴う実証で明示した。

**未解決（要確認）**: `check_mapper_xml.py`の将来のブロック運用化の要否、および対応計画（ラッパースクリプト方式の採用）自体の承認はユーザー確認が必要な事項として残す。
