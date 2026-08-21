---
paths: ["**/*.ts", "**/*.js", "**/*.tsx", "**/*.jsx", "**/*.css", "**/*.module.css", "**/*.html", "specs/issues/**/*.md", "**/docs/design/**/*.md", "**/docs/test/**/*.md"]
---

# フォーム・バリデーション（Reacter）

> 制約レベルの凡例は `rule-overview.md` を参照。

## 🚫 禁止 (NEVER)

- [STR-04] 🚫 **禁止 (NEVER)**: フォームのサブミット処理で `then()`（プロミスチェーン）を使わない。非同期待機が必要な場合は `async/await` を使用する（可読性・エラーハンドリングの観点）。
  - 根拠: `01.Reacterプログラミング/.github/instructions/reacter-blank-programming.instructions.md` §2.1

## ⚠ 非推奨 (AVOID)

- （該当なし）

## ✅ 必須 (ALWAYS)

- [FRM-01] ✅ **必須 (ALWAYS)**: 入力値の管理（フォームコントロール）には **React Hook Form** を使用すること。
  - 根拠: `01.Reacterプログラミング/.github/instructions/reacter-blank-programming.instructions.md` §2.4
- [FRM-02] ✅ **必須 (ALWAYS)**: バリデーションチェックには **React Hook Form + Zod** を使用すること。
  - 根拠: `01.Reacterプログラミング/.github/instructions/reacter-blank-programming.instructions.md` §2.5
- [STR-22] ✅ **必須 (ALWAYS)**: 型定義・React Hook Form のスキーマ定義の配置（`types/types.ts`・`types/schema.ts`）は **`reacter-folder-structure.md` を参照**（DRY のため本ファイルでは詳細を重複させない）。要点: `types` フォルダーは `components` と同階層、ファイル名は `xxxTypes.ts`/`xxxSchema.ts` でも可（`s` 省略可）。
  - 根拠: `01.Reacterプログラミング/.github/instructions/reacter-blank-programming.instructions.md` §2.5
- [FRM-03] ✅ **必須 (ALWAYS)**: 文字列（`string`）項目の必須チェックを行う場合は、`.min(1)` を併用して 1 文字以上であることもチェックすること（`.optional()` を付けない場合でも空文字 `''` は許容されるため、必須チェックには不十分）。
  - 根拠: `reacter-docs-main/05.プログラミングガイド/01.React一般知識編/01.入力フォーム/README.md` 2-1
- [FRM-04] ✅ **必須 (ALWAYS)**: `number` 型・`Date` 型のプロパティは入力値が `string` として渡るため、React Hook Form と関連付けるには必ず変換処理を行うこと（`register` の `setValueAs`、または zod の `coerce()`／`preprocess`）。
  - 根拠: `reacter-docs-main/05.プログラミングガイド/01.React一般知識編/01.入力フォーム/README.md` 2-2
- [FRM-05] ✅ **必須 (ALWAYS)**: フォームの `onSubmit` には Promise ではなく `void` を返す関数を設定すること（`void handleSubmit(...)(e)` のように `void` 演算子を付与し、ESLint 警告を回避する）。
  - 根拠: `reacter-docs-main/05.プログラミングガイド/01.React一般知識編/01.入力フォーム/README.md` 2-2
- [FRM-06] ✅ **必須 (ALWAYS)**: データ送信先が複数ある場合、または一部の項目のみバリデーションを行いたい場合は `<input type="button">` を用い、`onSubmit` とは別のイベント（`onClick` 等）で `trigger()` による明示的なバリデーションを行うこと。全項目をバリデーションする場合は `<input type="submit">` を用いること。
  - 根拠: `reacter-docs-main/05.プログラミングガイド/03.実践編/10.ボタン押下時の処理.md` 1.2
- [FRM-07] ✅ **必須 (ALWAYS)**: `trigger()` は `Promise` を返すため、結果を受けて後続処理を行う場合は `async/await` で待機すること。
  - 根拠: `reacter-docs-main/05.プログラミングガイド/03.実践編/10.ボタン押下時の処理.md` 2.1 (5)(6)

## ✨ 推奨 (PREFER)

- [FRM-08] ✨ **推奨 (PREFER)**: 入力値の型変換は、デバッグのしやすさの観点から原則 `setValueAs` を利用すること（`preprocess` は変換前の値で状態保持されるため）。
  - 根拠: `reacter-docs-main/05.プログラミングガイド/01.React一般知識編/01.入力フォーム/README.md` 2-2 ヒント
- [FRM-09] ✨ **推奨 (PREFER)**: 共通的に全エラーメッセージを変更したい場合は、スキーマ個別指定ではなく `ZodErrorMap`（`z.setErrorMap()`）で共通定義し、個別メッセージは各スキーマ定義で上書きすること（スキーマ個別定義が `ZodErrorMap` より優先される）。
  - 根拠: `reacter-docs-main/05.プログラミングガイド/01.React一般知識編/01.入力フォーム/appendix/01.ZodErrorMapを利用したエラーメッセージのカスタマイズ.md`
- [FRM-10] ✨ **推奨 (PREFER)**: 複数項目チェック（`refine()`）で一度のバリデーションで全結果を得たい場合は、既存の単項目チェックと同じプロパティに紐づけず、別プロパティまたは複数プロパティを組み合わせたパスを指定すること（バリデーションは宣言順に実行され、同一プロパティでは後続結果が取得できないことがある）。
  - 根拠: `reacter-docs-main/05.プログラミングガイド/03.実践編/09.入力チェック.md` (3) ヒント

## 条件付き事項（一覧）

- [参考] 必須項目ではないが値が入力された場合のみチェックしたい項目は、`.optional()` を付与する。空文字 `''` も許容する場合は `.or(z.string().length(0))` も併記できる。
  - **条件成立時: ✅必須（その入力仕様にする場合）**
  - 根拠: `reacter-docs-main/05.プログラミングガイド/01.React一般知識編/01.入力フォーム/README.md` 2-1 (7)
- [参考] 数値型・日付型などの不正値を Zod のスキーマチェックで検出させたい場合、`setValueAs` で変換できない値はそのまま `value` を返してよい（Zod 側で型エラーとして検出される）。
  - **条件成立時: 任意（実装選択）**
  - 根拠: `reacter-docs-main/05.プログラミングガイド/01.React一般知識編/01.入力フォーム/README.md` 2-2
- [参考] フォーム初期値は、非同期取得が不要なら `useForm` の `defaultValues` を利用できる。Web API 結果など非同期の場合は `setValue` または `reset` を利用できる。
  - **条件成立時: 任意（手段の選択）**
  - 根拠: `reacter-docs-main/05.プログラミングガイド/01.React一般知識編/01.入力フォーム/README.md` 2-3
- [参考] Zod 組み込み関数で表現できないバリデーションは `refine()` で実装できる。複数項目の関係検証も可能（`path` でエラー紐づけ先プロパティを指定）。
  - **条件成立時: 任意（拡張手段）**
  - 根拠: `reacter-docs-main/05.プログラミングガイド/01.React一般知識編/01.入力フォーム/README.md` 2-4 独自のバリデーションをZodに組み込む
- [参考] 通信を伴う非同期バリデーションなど、Zod を使わず任意タイミングでエラーを設定したい場合は `setError('プロパティ名', { message })` を利用できる。
  - **条件成立時: 任意（手段）**
  - 根拠: `reacter-docs-main/05.プログラミングガイド/01.React一般知識編/01.入力フォーム/README.md` 2-4 setErrorを利用したバリデーション
- [FRM-12] スキーマ定義はネスト構造でも定義できる。その場合 `register` の引数はプロパティ名を `.` で連結する（例: `register('applicant.email.email')`）。
  - **条件成立時: ✅必須（ネスト構造を利用する場合の記法）**
  - 根拠: `reacter-docs-main/05.プログラミングガイド/01.React一般知識編/01.入力フォーム/appendix/02.ネストしたスキーマ定義の利用方法.md`
- [参考] 複数コンポーネントにまたがるフォームは `FormProvider` + `useFormContext` で実装できる。
  - **条件成立時: 任意（手段）**
  - 根拠: `reacter-docs-main/05.プログラミングガイド/01.React一般知識編/01.入力フォーム/README.md` 2-2 複数コンポーネントにまたがるフォームの場合
- [FRM-13] 画面内にフォームが複数ある場合は、フォームごとに `useForm` を用意し、取り出す関数名を区別して実装できる（実装時は `registerFilter` 等の意味ある名前を付ける）。
  - **条件成立時: ✅必須（フォームが複数ある場合）**
  - 根拠: `reacter-docs-main/05.プログラミングガイド/01.React一般知識編/01.入力フォーム/README.md` 2-6
- [参考] 入力チェックを段階に分けたい場合は、Zod スキーマを段階別に分けて定義し、各スキーマの `parse()` を `try-catch` で手動実行できる（型は `&` で連結）。
  - **条件成立時: 任意（手段）**
  - 根拠: `reacter-docs-main/05.プログラミングガイド/03.実践編/09.入力チェック.md` (4)
- [参考] バリデーション実行タイミングは `useForm` の `mode`（初回）／`reValidateMode`（エラー後再実行）オプション、または `trigger()` 手動実行でカスタマイズできる。
  - **条件成立時: 任意（カスタマイズ）**
  - 根拠: `reacter-docs-main/05.プログラミングガイド/01.React一般知識編/01.入力フォーム/README.md` 2-4 バリデーション実行タイミングのカスタマイズ
- [参考] `trigger()` の引数で対象を絞り込める（単一: `trigger('postalCode')`、複数: `trigger(['firstName','lastName'])`、全項目: `trigger()`）。
  - **条件成立時: 任意（能力の提示）**
  - 根拠: `reacter-docs-main/05.プログラミングガイド/03.実践編/10.ボタン押下時の処理.md` 2.1 (6)

## 参考・推奨実装パターン

- Zod スキーマ定義から TypeScript 型を生成する: `export type EventRegistration = z.infer<typeof eventRegistrationSchema>`。
- `useForm` に `zodResolver(schema)` を渡して Zod と連携し、`formState.errors` からエラーメッセージを取得する（例: `errors.applicantEmail?.message`）。
- よく使う Zod チェック定義: `string()` に `min`/`max`/`length`/`regex`/`includes`/`startsWith`/`endsWith`、`number()` に `gt`/`gte`/`min`/`lt`/`lte`/`max`、`date()` に `min`/`max`。
- エラーメッセージのカスタマイズは各メソッドの引数で指定する（`string()`/`number()`/`boolean()`/`date()` は `required_error`・`invalid_type_error`、`min()`/`max()`/`regex()`/`refine()` は第 2 引数）。
- チェックボックス（取得値は `string`）は `z.coerce.boolean()` + `refine()` で true/false 判定する。ラジオボタンに初期値がない場合は未選択時 `null` となるため `nullable()` を付ける。
- 特定ボタン押下時／特定項目エラー時のみフォーカス無効にしたい場合は `useRef<boolean>` で `shouldFocusError` の値を管理し、`SubmitErrorHandler` 内で切り替える（フォーカスは `handleSubmit` 実行後に行われるため変更が適用される）。
- 段階別チェックの `parse()` 失敗時、`err instanceof ZodError` を判定し `err.errors` から `path[0]`（項目名）・`message`（メッセージ）を取得する。
- ボタン種別: `<input type="submit">` は `onSubmit` を発火（サーバ送信用）、`<input type="button">` は `onClick` 等で処理（戻る等の送信不要操作用）。
