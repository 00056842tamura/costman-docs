# テスト見本コード (`_hint/`)

このディレクトリは `unit-test-*` スキルが **生成テストの体裁（改行・コメント・命名・インデント）** を揃えるための見本コードです。

## 含まれるファイル

| ファイル | 参照するスキル |
|---|---|
| `ItemControllerTest.java` | `/unit-test-controller` |
| `ItemControllerValidationTest.java` | `/unit-test-controller-validation` |
| `ItemServiceImplTest.java` | `/unit-test-service` |
| `ItemRepositoryImplTest.java` | `/unit-test-repository` |
| `ItemListPage.test.tsx` | `/reacter-unit-test-gen`（コンポーネントテスト見本） |
| `useItem.test.ts` | `/reacter-unit-test-gen`（カスタムフックテスト見本） |

## 配置ルール

- これらのファイルは **プロダクトルート直下の `_hint/`** に配置します
- 各スキルが「`_hint/Item*Test.java`」「`_hint/ItemListPage.test.tsx`」「`_hint/useItem.test.ts`」を参照すると明示しています
- Java ファイルのパッケージ宣言は `jp.co.nekonet.foodshop.bs.item.*` ですが、生成テストの実パッケージは対象クラスに合わせて Claude が書き換えます
- TypeScript ファイルの `import { ItemListPage } from './ItemListPage'`・`import { useItem } from './useItem'` は架空の参照先ですが、生成テストの実 import パスは対象コンポーネント/フックに合わせて Claude が書き換えます

## 編集ルール

- 🚫 これらの見本ファイルを実装コードとしてビルド対象に含めないでください（Java はパッケージが架空、TypeScript は import 先が架空のため）
- ✨ プロダクトに独自のテストが育ってきたら、その代表ファイルで `_hint/` を差し替えると以後の生成精度が向上します

## なぜこの仕組みが必要か

Claude Code はテストコード生成時に既存ファイルから「体裁」を学習します。
`_hint/` の見本があると、生成結果がチーム標準に揃いやすくなります。
`_hint/` がない場合、汎用的なテンプレートになり体裁がバラつきます。

## 由来

- Java 4ファイル（`Item*Test.java`）: Springer 公式の `springer-unit-test-x.x.x/Eclipse/_hint/` に含まれる foodshop サンプルアプリのテストコードを採用しています。
- TypeScript 2ファイル（`ItemListPage.test.tsx`・`useItem.test.ts`）: Reacter 側には同等の公式サンプルが存在しないため、`.claude/rules/reacter-testing-vitest.md` の規約（`describe`/`it`構成・`renderHook`・MSW `TEST_HANDLERS`）に基づき新規に著作したものです。
