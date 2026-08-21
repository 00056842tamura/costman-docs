# US ↔ BS 連携設計

## 基本原則

- US アプリ（MPA / API）は **DB に直接アクセスしない**。必ず BS の REST API を経由する
- BS は US が誰であるかを意識しない（複数の US から呼ばれてよい）
- BS が他システムを呼ぶ場合も同様に HTTP API 経由とする（直接 DB アクセス共有はしない）

## 通信プロトコル

| 区間 | プロトコル | データ形式 |
|---|---|---|
| ブラウザ → US-MPA | HTTPS | HTML（Thymeleaf 描画） |
| ブラウザ / Reacter → US-API | HTTPS | JSON |
| US-MPA → BS | HTTP/HTTPS（社内ネットワーク） | JSON |
| US-API → BS | HTTP/HTTPS（社内ネットワーク） | JSON |
| BS → DB | JDBC | — |

## US 側の Repository 実装

US の Repository は **BS API クライアント** として実装する（MyBatis Mapper は持たない）。

### 実装例
```java
@Repository
public class BsUserRepositoryImpl implements UserRepository {

    private static final Logger LOGGER = LoggerFactory.APP.getLogger(BsUserRepositoryImpl.class);

    private final RestClient bsApiRestClient;

    public BsUserRepositoryImpl(RestClient bsApiRestClient) {
        this.bsApiRestClient = bsApiRestClient;
    }

    @Override
    public User findById(String id) {
        try {
            return bsApiRestClient.get()
                    .uri("/api/v1/users/{id}", id)
                    .retrieve()
                    .body(User.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new UserNotFoundException("error.user.notFound", id, e);
        } catch (RestClientException e) {
            throw new BsApiAccessException("error.bs.accessFailed", id, e);
        }
    }
}
```

## BS 側の Controller 実装

BS の Controller は `@RestController` で JSON を返す。US 種別ごとに分岐させない（汎用 API として設計する）。

### 実装例
```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserView> createUser(@Validated @RequestBody UserCreateRequest request) {
        UserCreateResult result = userService.createUser(request.toModel());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserView.from(result));
    }
}
```

## 共通レスポンス形式

### 正常系
パッケージごとに API 設計書（`docs/{product}/design/...` 配下）で定義する。

### 異常系（エラーレスポンス）
```json
{
  "code": "ERROR_CODE",
  "message": "ユーザー向けメッセージ（日本語）",
  "details": {}
}
```

| code 例 | HTTP ステータス | 意味 |
|---|---|---|
| `UNAUTHORIZED` | 401 | 認証が必要 |
| `FORBIDDEN` | 403 | 権限がない |
| `NOT_FOUND` | 404 | リソースが存在しない |
| `VALIDATION_ERROR` | 400 | 入力値が不正 |
| `DUPLICATE_ENTRY` | 409 | 重複エントリー |
| `INTERNAL_ERROR` | 500 | サーバー内部エラー |

エラーコードはプロダクトごとに `docs/{product}/design/` 配下で拡張定義する。

## タイムアウト・リトライ方針

- ✅ US → BS の呼び出しはタイムアウトを必ず設定する（推奨: connect 3s / read 10s）
- ✅ リトライは冪等な GET のみ許可（POST/PUT/PATCH/DELETE はリトライしない）
- ✅ 設定値はパッケージごとの `application.yml` で定義する

## 関連ルール

- API 呼び出し規約: `.claude/rules/springer-api-call.md`
- 例外ハンドリング: `.claude/rules/springer-exception.md`
