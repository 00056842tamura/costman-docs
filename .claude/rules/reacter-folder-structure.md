---
paths: ["**/*.ts", "**/*.js", "**/*.tsx", "**/*.jsx", "**/*.css", "**/*.module.css", "**/*.html", "specs/issues/**/*.md", "**/docs/design/**/*.md", "**/docs/test/**/*.md"]
---

# フォルダー構成・命名（Reacter）

> 制約レベルの凡例は `rule-overview.md` を参照。

## 🚫 禁止 (NEVER)

- （該当なし）

## ⚠ 非推奨 (AVOID)

- （該当なし）

## ✅ 必須 (ALWAYS)

- [STR-12] ✅ **必須 (ALWAYS)**: 本ファイル（`reacter-folder-structure.md`）に定義された命名規約を満たすこと（下記 ✅必須の各命名ルールを参照）。
- [STR-14] ✅ **必須 (ALWAYS)**: React コンポーネント名は PascalCase とすること（例: `MenuPage`）。
- [STR-15] ✅ **必須 (ALWAYS)**: React コンポーネントのファイル名は PascalCase とし、コンポーネント名と一致させること（例: `MenuPage.tsx`）。
- [STR-16] ✅ **必須 (ALWAYS)**: React のカスタムフック名は `use...` から始まる camelCase とすること（例: `useLogin`）。
- [STR-17] ✅ **必須 (ALWAYS)**: React のカスタムフックのファイル名は `use...` から始まる camelCase とし、カスタムフック名と一致させること（例: `useLogin.ts`）。
- [STR-18] ✅ **必須 (ALWAYS)**: Type 名は PascalCase とすること（例: `MenuProps`）。
- [STR-19] ✅ **必須 (ALWAYS)**: CSS クラス名は camelCase とすること（例: `myInput`）。
- [STR-20] ✅ **必須 (ALWAYS)**: CSS Modules のファイル名は `コンポーネント名.module.css` とすること（例: `MenuPage.module.css`）。
- [STR-21] ✅ **必須 (ALWAYS)**: `.env` ファイルに記載する環境変数名は大文字の SNAKE_CASE とすること。
- [STR-22] ✅ **必須 (ALWAYS)**: 型定義は `types/types.ts` に記述すること（`types` フォルダーは `components` と同階層。ファイル名 `xxxTypes.ts` でも可、複数形の `s` は省略可）。
- [STR-23] ✅ **必須 (ALWAYS)**: React Hook Form のスキーマ定義は `types/schema.ts` に記述すること（配置・命名条件は型定義と同様。`xxxSchema.ts` でも可）。
- [STR-35] ✅ **必須 (ALWAYS)**: `div` などの HTML 要素の `id` 属性を使用する場合は、全画面で `id` が重複しないような命名にすること（`id` はドキュメント内で一意でなければならないため、画面 ID や機能名をプレフィックスとして付与するなど重複を防ぐ命名を採用すること）。
- [STR-36] ✅ **必須 (ALWAYS)**: Vitest のユニットテストファイルは `src/test/` 配下に、テスト対象のソースファイルと同じディレクトリ構造をミラーリングして配置すること（例: `src/features/item/components/ItemListPage.tsx` → `src/test/features/item/components/ItemListPage.test.tsx`）。`src/test/` にはミラーリングされたテストファイルに加え、`setup.ts` 等の共通セットアップファイルも配置する。ソースコードと同一フォルダーへの直置き（下記「素テンプレート参考」が示す素テンプレート既定の配置指針）は本プロダクトでは採用しない。
  - 理由 / 背景: Springer（バックエンド）の `src/test/java` が `src/main/java` をミラーリングする構成（`springer-package-class-naming.md` R-03-N5）と揃え、バックエンド/フロントエンドで一貫したテスト配置規約とするため。

## ✨ 推奨 (PREFER)

- [STR-13] ✨ **推奨 (PREFER)**: フォルダー構成が本ファイル（`reacter-folder-structure.md`）に定義された内容を満たしていること。
- [STR-25] ✨ **推奨 (PREFER)**: 単一文字の名前を避け、説明的な命名をすること（Airbnb 23.1）。
- [STR-25] ✨ **推奨 (PREFER)**: オブジェクト・関数・インスタンスは camelCase で命名すること（Airbnb 23.2、ESLint `camelcase`）。
- [STR-25] ✨ **推奨 (PREFER)**: コンストラクター・クラスの命名にのみ PascalCase を使うこと（Airbnb 23.3、ESLint `new-cap`）。
- [STR-25] ✨ **推奨 (PREFER)**: 頭字語・イニシャリズムは全て大文字または全て小文字に統一すること（Airbnb 23.9）。

## 条件付き事項（一覧）

- [参考] `types` フォルダーのファイル名は `types.ts`/`schema.ts` のほか `xxxTypes.ts`/`xxxSchema.ts` 形式でも可。複数形の `s` は省略可。
  - **条件成立時: 任意（命名の許容）**
- [参考] 末尾／先頭のアンダースコアは使用しない（Airbnb 23.4）。ただし ESLint `@typescript-eslint/no-unused-vars` で `argsIgnorePattern: '^_'` が設定されており、未使用引数の `_` プレフィックスは許容される。
  - **条件成立時: 任意（例外の許容）**
- [参考] 定数を大文字（UPPER_CASE）にしてよいのは (1) export 済み (2) const で再代入不可 (3) ネストプロパティ含め不変が信頼できる、を満たす場合（Airbnb 23.10、任意）。
  - **条件成立時: 任意（許容）**
- [参考] ベースファイル名は default export 名と一致させる／default export する関数は camelCase でファイル名を関数名と同一にする（Airbnb 23.6・23.7・23.8）。
  - **条件成立時: 任意（Airbnb推奨事項）**


### [参考] 素テンプレート参考（reacter-blank-template）

- [参考] **配置指針**:
  - アプリ全体で利用するコンポーネント・hooks・ユーティリティは `src` 直下の各フォルダーへ格納する。
  - アプリ全体で利用するコンポーネントは `components` 配下に機能ごとにフォルダーを作成して格納する。
  - 特定機能のみで利用するコンポーネント・hooks・ユーティリティは `features` 配下に機能ごとにフォルダーを作成して格納する。
- [参考] **推奨フォルダー構成（サンプル）**:
  ```
  src/
    assets/        # アプリ全体の画像・フォント等の静的ファイル（コンポーネント/機能ごとのassetsも集約）
    components/    # アプリ全体で使用する共通コンポーネント（機能ごとにサブフォルダー）
    const/         # const.ts（環境変数等のエクスポート）・message.ts（メッセージ管理）
    css/           # グローバルに適用する CSS ファイル
    features/      # 機能ベースモジュール（メニューから呼ばれる機能単位）
      featureX/    #   components / hooks / routes / types / utils
    hooks/         # アプリ全体で使用できる共通 hooks
    lib/           # ライブラリをアプリ用に設定して再エクスポートしたもの
    providers/     # アプリの全プロバイダー
    routes/        # ルーティング設定
    test/          # テストユーティリティとモックサーバ（各テストはソースと同フォルダーに配置）
    types/         # アプリ全体で使用する基本的な型定義
    utils/         # 共通ユーティリティ関数
  e2e/             # E2E テストコード（src 外）
  ```
- [参考] 素テンプレート自体の既定方針: 各テストファイルはソースコードと同じフォルダーに配置し、`src/test/` には共通ファイルのみを置く。**本プロダクトではこの既定方針を採用せず、[STR-36]（`src/test/` へのミラーリング配置）を正とする**（詳細は次項「本プロジェクトの実構成」参照）。

- [参考] **テンプレート実体（reacter-blank-template v2.1.0）の `src/` 実構成**（空でないもの）:
  ```
  src/
    main.tsx                     # エントリポイント
    vite-env.d.ts
    css/index.css                # グローバル CSS
    const/
      const.ts                   # ROUTER・HTTP_CLIENT を as const で定義
      message.ts                 # メッセージ定義配列
    lib/
      axiosConfig.ts             # axiosDefault インスタンス
      messageInitializer.ts      # getMessage 初期化
    utils/messageUtil.ts         # MessageUtil クラス
    routes/AppRoutes.tsx         # ErrorBoundary > BrowserRouter > Routes
    components/Error/
      ErrorPage.tsx
      NotFoundPage.tsx
    features/
      login/
        components/Login.tsx
        components/Login.module.css
        types/index.ts           # barrel export
        types/schema.ts
        types/types.ts
      menu/
        components/Menu.tsx
        components/Menu.module.css
    test/setup.ts                # import '@testing-library/jest-dom/vitest'
    assets/                      # 空フォルダで用意
    hooks/                       # 空フォルダで用意
    providers/                   # 空フォルダで用意
    types/                       # 空フォルダで用意（アプリ全体共通の型）
  ```
  - [参考] 機能固有の型/スキーマは `features/{機能}/types/` に置き、`index.ts` で barrel export する。アプリ全体で共通の型のみ `src/types/` に置く。

### 本プロジェクトの実構成

> 上記「素テンプレート参考」は素テンプレート（reacter-blank-template）自体の既定構成であり、本プロダクトでは一部を上書きしている。特にテスト配置は [STR-36] のミラーリング配置（`src/test/` 配下）を正とし、素テンプレート既定の「ソースと同じフォルダーに配置」は採用しない。実際に採用しているディレクトリ構成は以下（詳細・最新の実構成は `frontend/CLAUDE.md`「ディレクトリ構成」を参照。本節と相違があれば `frontend/CLAUDE.md` を正とする）。

```
src/
├── assets/          … 画像・フォント等の静的ファイル
├── components/      … 横断 UI（ErrorBoundary 等）
├── constants/       … 定数・メッセージ定義
├── design-system/   … デザインシステム（components/・hooks/・tokens/・theme.ts）
├── features/        … 機能単位のモジュール（admin/ 配下）
│   └── {feature}/
│       ├── components/
│       ├── hooks/
│       └── types/
├── hooks/           … 共通カスタムフック
├── routes/          … ルーティング定義
├── shared/          … 横断ロジック（api/・types/・utils/）
├── test/            … ユニットテスト（[STR-36] のミラーリング配置。共通セットアップ setup.ts を含む）
│   ├── setup.ts
│   ├── components/
│   ├── features/{feature}/{components,hooks}/
│   └── shared/{api,utils}/
├── index.css        … グローバル CSS（src 直下）
└── main.tsx / App.tsx / vite-env.d.ts
e2e/                 … E2E テスト（src 外）
```

- [参考] `test/` 配下のパスは常に `src/` 配下のミラーであり、`src/{path}/{Name}.tsx` に対して `src/test/{path}/{Name}.test.tsx` となる。新規ディレクトリ（`components/`・`features/{feature}/`・`shared/` 以外）を `src/` に追加した場合も同じ規則で `src/test/` 配下にミラーする。
