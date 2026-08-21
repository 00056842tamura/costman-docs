# Web API I/F 定義書

| 項目 | 内容 |
|---|---|
| プロダクト名 | コスト管理システム |
| 作成日 | 2026/08/20 |
| 作成者 | 不明（旧設計書からの移行） |
| 最終更新日 | 2026/08/20 |
| 最終更新者 | 不明（旧設計書からの移行） |

> bs は別リポジトリ（`ctm-costman-backend-main`）で実装済み。本書は実装コード（`SecurityConfig`のformLogin設定）との照合確認済みの内容に更新している（`old_docs/03.Web_API_IF定義書/Web_API_IF定義書(LOGIN).md` からの移行を基に、実装調査で判明した相違点を反映）。ログインAPIは専用Controllerを持たず、Spring Securityの`formLogin`フィルタで処理される。

---

## ログインAPI

| 項目 | 内容 |
|---|---|
| 機能ID | bs-002 |
| API ID | LOGIN |
| API名 | ログインAPI |
| API説明 | ユーザーID・パスワードによる認証を行う（Spring Security `formLogin`。専用Controllerなし） |
| Method | POST |
| Path | `/api/login`（ベースURL: `http://localhost:8080`。`loginProcessingUrl("/api/login")`で確定） |
| Request Header | `Content-Type: application/x-www-form-urlencoded`（**旧記載の`multipart/form-data`は誤り。Spring Securityの標準フォーム認証はurlencodedを要求するため修正**）。認証情報（Cookie）は初回のためこの時点では送信しない |
| 認可 | 認証不要（`permitAll`） |
| Status Code | 200, 401 |

### Path Variable

なし

### Query String

なし

### Request Body

| No. | 論理名 | 物理名 | 型 | 桁数（Min/Max） | 必須 | 編集仕様 |
|---|---|---|---|---|---|---|
| 1 | ユーザーID | `userId` | string | 1 / 8 | ○ | ログインするユーザーID。`application/x-www-form-urlencoded`形式で送信する（`usernameParameter("userId")`） |
| 2 | パスワード | `password` | string | 1 / - | ○ | ログインするユーザーのパスワード。`application/x-www-form-urlencoded`形式で送信する（`passwordParameter("password")`） |

> テストユーザー（bs実装にハードコードされたDB照合なしの簡易実装）: `userId=admin`/`password=admin`（role: `admin`）、`userId=ytcpd2`/`password=ytcpd2`（role: `member`）。

### Response Header

| ステータス | ヘッダー |
|---|---|
| 200 | セッションは Cookie で保持される（応答本文に認証情報は含まれない） |

### Response Body

#### 200 OK

| No. | 論理名 | 物理名 | 型 | 桁数 | 必須 | 編集仕様 |
|---|---|---|---|---|---|---|
| 1 | アカウント有効期限 | accountNonExpired | boolean | - | ○ | |
| 2 | アカウントロック状態 | accountNonLocked | boolean | - | ○ | |
| 3 | 権限情報 | authorities | object[] | - | ○ | フロントエンドは先頭要素の `authority` のみを権限として使用する |
| 3-1 | 権限 | authorities.authority | string | - | ○ | |
| 4 | 認証情報有効期限 | credentialsNonExpired | boolean | - | ○ | |
| 5 | アカウント有効状態 | enabled | boolean | - | ○ | |
| 6 | パスワード | password | string | - | - | ⚠️ 実装（`CustomUserDetails`をそのままJSONシリアライズ）ではこのフィールドが応答に含まれる。**フロントエンドは受信しても状態（Context等）へ保持せず、画面表示・ログ出力を一切行わない** |
| 7 | ユーザー名 | username | string | - | ○ | ログイン成功時、フロントエンドで「ユーザーID」として保持する |

#### 401 Unauthorized

> ログイン失敗はSpring Securityの標準認証失敗ハンドラで処理され、バリデーションエラー（`detail`に`Field`/`ValidationMessage`を持つ構造）ではなく認証エラーとして扱われる。`detail`は`null`になる想定（実機起動でのレスポンス確認は未実施）。旧設計書が記載していた`400`/`404`/`409`は、ログインAPI（`formLogin`フィルタ処理・専用Controllerなし）では発生しない想定のため削除した。

| No. | 論理名 | 物理名 | 型 | 説明 |
|---|---|---|---|---|
| 1 | エラーコード | errorCode | string | `BadCredentials`（ユーザーID・パスワードいずれの不一致でも同一メッセージ・同一エラーコードを返す。攻撃者にヒントを与えないためのセキュリティ規約に準拠） |
| 2 | メッセージ | message | string | エラーメッセージ |
| 3 | 明細 | detail | null | ログイン失敗時は常に`null`（バリデーションエラーではないため） |

`401` の場合、フロントエンドは `message` をトースト表示する。

### 電文サンプル

#### Status Code: 200

```json
{
  "accountNonExpired": true,
  "accountNonLocked": true,
  "authorities": [{ "authority": "member" }],
  "credentialsNonExpired": true,
  "enabled": true,
  "username": "0000001"
}
```

#### Status Code: 401

```json
{
  "detail": null,
  "errorCode": "BadCredentials",
  "message": "ユーザーIDまたはパスワードが正しくありません。"
}
```

## 更新履歴

| Ver | 更新日 | 更新者 | 更新内容 |
|---|---|---|---|
| 1.0 | 2026/08/20 | 不明（旧設計書からの移行） | 初版 |
| 1.1 | 2026/08/20 | Claude（実装調査） | bs実装（`SecurityConfig`のformLogin設定）と照合し、Path/Content-Type/StatusCode/passwordフィールドの取り扱いを修正 |
