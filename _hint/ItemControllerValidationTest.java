package jp.co.nekonet.foodshop.bs.item.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.*;
import static org.mockito.Mockito.*;
import java.net.URI;
import java.util.Collections;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import jp.co.nekonet.foodshop.bs.item.controller.request.ItemDeleteRequest;
import jp.co.nekonet.foodshop.bs.item.service.ItemService;

/**
 * ItemControllerのValidationテストクラス
 * 
 * @author YAMATO SYSTEM DEVELOPMENT
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ItemControllerValidationTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @LocalServerPort
    private int port;

    @MockitoBean
    private ItemService mockService;

    @InjectMocks
    private ItemController target;

    @BeforeEach
    void setUp() {
        Mockito.reset(mockService);
    }

    static Stream<Arguments> getItemsProvider() {
        // ケース概要、商品ID、商品名、商品名カナ、カテゴリID、ソート順、期待値JSON
        // ※第一引数はケースの概要説明で、テストコード内では使用しない。
        // 　JUnit実行結果にケース概要を表示させ、確認しやすくするためのもの。
        return Stream.of(
                arguments("文字種チェック(商品ID、商品カテゴリID、商品名カナ)", "@0001", "", "商品A2個セット", "#9", "", """
                        {
                            "errorCode": "error.unexpect.method-argument-not-valid",
                            "message": "Validation is failed. Error count:3",
                            "detail": [
                                {
                                    "Field": "id",
                                    "ValidationMessage": "id,HalfAlphaNumeric"
                                },
                                {
                                    "Field": "nameKana",
                                    "ValidationMessage": "nameKana,FullKatakana"
                                },
                                {
                                    "Field": "itemCategoryId",
                                    "ValidationMessage": "itemCategoryId,HalfAlphaNumeric"
                                }
                            ]
                        }
                        """),
                arguments("既定値チェック(ソート順)", "", "", "", "", "99", """
                        {
                            "errorCode": "error.unexpect.method-argument-not-valid",
                            "message": "Validation is failed. Error count:1",
                            "detail": [
                                {
                                    "Field": "order",
                                    "ValidationMessage": "正規表現 \\"11|12|21|22\\" にマッチさせてください"
                                }
                            ]
                        }
                        """),
                arguments("規定サイズ超過チェック(商品ID、商品名、商品名カナ、商品カテゴリID)]", "A12345", "商品名４５６７８９０１２３４５６７８９０１",
                        "アイウエオカキクケコサシスセソタチツテトナニヌネノハヒフヘホマミムメモヤイユエヨラ", "123", "", """
                                {
                                    "errorCode": "error.unexpect.method-argument-not-valid",
                                    "message": "Validation is failed. Error count:4",
                                    "detail": [
                                        {
                                            "Field": "id",
                                            "ValidationMessage": "id,Size,0,5"
                                        },
                                        {
                                            "Field": "name",
                                            "ValidationMessage": "name,Size,0,20"
                                        },
                                        {
                                            "Field": "nameKana",
                                            "ValidationMessage": "nameKana,Size,0,40"
                                        },
                                        {
                                            "Field": "itemCategoryId",
                                            "ValidationMessage": "itemCategoryId,Size,2,2"
                                        }
                                    ]
                                }
                                """),
                arguments("規定サイズ未満チェック(商品カテゴリID)", "", "", "", "1", "", """
                        {
                            "errorCode": "error.unexpect.method-argument-not-valid",
                            "message": "Validation is failed. Error count:1",
                            "detail": [
                                {
                                    "Field": "itemCategoryId",
                                    "ValidationMessage": "itemCategoryId,Size,2,2"
                                }
                            ]
                        }
                        """));
    }

    @ParameterizedTest
    @MethodSource("getItemsProvider")
    @DisplayName("[getItems][正常系][バリデーション]")
    void testGetItems(String summary, String id, String name, String nameKana, String itemCategoryId,
            String order, String json) throws Exception {

        // リクエスト先URL
        URI uri = UriComponentsBuilder.fromUriString("http://localhost:" + port)
                .path("/items")
                .queryParam("id", "{id}")
                .queryParam("name", "{name}")
                .queryParam("nameKana", "{nameKana}")
                .queryParam("itemCategoryId", "{itemCategoryId}")
                .queryParam("order", "{order}")
                .build(id, name, nameKana, itemCategoryId, order);

        // リクエストヘッダ設定
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<String> request = new HttpEntity<>(headers);


        // リクエスト発行
        ResponseEntity<String> result = testRestTemplate.exchange(uri, HttpMethod.GET, request, String.class);


        // HTTPステータス検証
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        // Body検証。比較モードはLENIENT（配列順序は厳密にチェックしない。期待値にないフィールドは無視する）、
        JSONAssert.assertEquals(json, result.getBody(), JSONCompareMode.LENIENT);

        // Service処理が呼び出されていないことの検証
        verifyNoInteractions(this.mockService);
    }

    // ProviderはgetItemsと共通。
    @ParameterizedTest
    @MethodSource("getItemsProvider")
    @DisplayName("[getItemsCsv][正常系][バリデーション]")
    void testGetItemsCsv(String summary, String id, String name, String nameKana, String itemCategoryId,
            String order, String json) throws Exception {

        // リクエスト先URL
        URI uri = UriComponentsBuilder.fromUriString("http://localhost:" + port)
                .path("/items-csv")
                .queryParam("id", "{id}")
                .queryParam("name", "{name}")
                .queryParam("nameKana", "{nameKana}")
                .queryParam("itemCategoryId", "{itemCategoryId}")
                .queryParam("order", "{order}")
                .build(id, name, nameKana, itemCategoryId, order);

        // リクエストヘッダ設定
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<String> request = new HttpEntity<>(headers);


        // リクエスト発行
        ResponseEntity<String> result = testRestTemplate.exchange(uri, HttpMethod.GET, request, String.class);


        // HTTPステータス検証
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        // Body検証。比較モードはLENIENT（配列順序は厳密にチェックしない。期待値にないフィールドは無視する）、
        JSONAssert.assertEquals(json, result.getBody(), JSONCompareMode.LENIENT);

        // Service処理が呼び出されていないことの検証
        verifyNoInteractions(this.mockService);
    }

    static Stream<Arguments> getItemProvider() {
        // ケース概要、商品ID、期待値JSON
        // ※第一引数はケースの概要説明で、テストコード内では使用しない。
        // 　JUnit実行結果にケース概要を表示させ、確認しやすくするためのもの。
        return Stream.of(
                arguments("文字種チェック(商品ID)", "@0001", """
                        {
                            "errorCode": "error.unexpect.constraint-violation",
                            "message": "Validation is failed. Error count:1",
                            "detail": [
                                {
                                    "Field": "id",
                                    "ValidationMessage": "id,HalfAlphaNumeric"
                                }
                            ]
                        }
                        """),
                arguments("規定サイズ超過チェック(商品ID)", "A12345", """
                        {
                            "errorCode": "error.unexpect.constraint-violation",
                            "message": "Validation is failed. Error count:1",
                            "detail": [
                                {
                                    "Field": "id",
                                    "ValidationMessage": "id,Size,5,5"
                                }
                            ]
                        }
                        """),
                arguments("規定サイズ未満チェック(商品ID)", "A123", """
                        {
                            "errorCode": "error.unexpect.constraint-violation",
                            "message": "Validation is failed. Error count:1",
                            "detail": [
                                {
                                    "Field": "id",
                                    "ValidationMessage": "id,Size,5,5"
                                }
                            ]
                        }
                        """));
    }

    @ParameterizedTest
    @MethodSource("getItemProvider")
    @DisplayName("[getItems][正常系][バリデーション]")
    void testGetItem(String summary, String id, String json) throws Exception {

        // リクエスト先URL
        URI uri = UriComponentsBuilder.fromUriString("http://localhost:" + port)
                .path("/items/{id}")
                .build(id);

        // リクエストヘッダ設定
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<String> request = new HttpEntity<>(headers);


        // リクエスト発行
        ResponseEntity<String> result = testRestTemplate.exchange(uri, HttpMethod.GET, request, String.class);


        // HTTPステータス検証
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        // Body検証。比較モードはLENIENT（配列順序は厳密にチェックしない。期待値にないフィールドは無視する）、
        JSONAssert.assertEquals(json, result.getBody(), JSONCompareMode.LENIENT);

        // Service処理が呼び出されていないことの検証
        verifyNoInteractions(this.mockService);
    }

    /**
     * 登録・更新処理の共通プロバイダ
     */
    static Stream<Arguments> registerCommonItemProvider() {
        // ケース概要、商品ID、商品名、商品名カナ、カテゴリID、メーカーID、発売日、販売終了日、価格、説明、商品画像、バージョン、期待値JSON
        // ※第一引数はケースの概要説明で、テストコード内では使用しない。
        // 　JUnit実行結果にケース概要を表示させ、確認しやすくするためのもの。
        return Stream.of(
                arguments("文字種チェック(商品ID、商品カテゴリID、メーカーID、商品名カナ)",
                        "@0001", "テスト商品", "てすと商品", "０１", "Ｍ０００１", "yyyy-MM-dd",
                        "DATE-ABC", "ABC", "商品説明", new byte[] {0}, "A", """
                                {
                                    "errorCode": "error.unexpect.method-argument-not-valid",
                                    "message": "Validation is failed. Error count:8",
                                    "detail": [
                                        {
                                            "Field": "id",
                                            "ValidationMessage": "id,HalfAlphaNumeric"
                                        },
                                        {
                                            "Field": "nameKana",
                                            "ValidationMessage": "nameKana,FullKatakana"
                                        },
                                        {
                                            "Field": "itemCategoryId",
                                            "ValidationMessage": "itemCategoryId,HalfAlphaNumeric"
                                        },
                                        {
                                            "Field": "makerId",
                                            "ValidationMessage": "makerId,HalfAlphaNumeric"
                                        },
                                        {
                                            "Field": "startDate",
                                            "ValidationMessage": "startDate,typeMismatch,LocalDate"
                                        },
                                        {
                                            "Field": "endDate",
                                            "ValidationMessage": "endDate,typeMismatch,LocalDate"
                                        },
                                        {
                                            "Field": "price",
                                            "ValidationMessage": "price,typeMismatch,Integer"
                                        },
                                        {
                                            "Field": "version",
                                            "ValidationMessage": "version,typeMismatch,Integer"
                                        }
                                     ]
                                }
                                """),
                arguments("規定サイズ超過チェック(商品ID、商品名、商品名カナ、商品カテゴリID、メーカーID、説明)",
                        "A12345", "商品名４５６７８９０１２３４５６７８９０１", "アイウエオカキクケコサシスセソタチツテトナニヌネノハヒフヘホマミムメモヤイユエヨラ", "012", "M12345", "2022-03-01",
                        "2025-03-31", "1000",
                        "商品説明56789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901",
                        new byte[] {0}, "1", """
                                {
                                    "errorCode": "error.unexpect.method-argument-not-valid",
                                    "message": "Validation is failed. Error count:6",
                                    "detail": [
                                        {
                                            "Field": "id",
                                            "ValidationMessage": "id,Size,5,5"
                                        },
                                        {
                                            "Field": "name",
                                            "ValidationMessage": "name,Size,1,20"
                                        },
                                        {
                                            "Field": "nameKana",
                                            "ValidationMessage": "nameKana,Size,1,40"
                                        },
                                        {
                                            "Field": "itemCategoryId",
                                            "ValidationMessage": "itemCategoryId,Size,2,2"
                                        },
                                        {
                                            "Field": "makerId",
                                            "ValidationMessage": "makerId,Size,5,5"
                                        },
                                        {
                                            "Field": "description",
                                            "ValidationMessage": "description,Size,1,200"
                                        }
                                     ]
                                }
                                """),
                arguments("規定サイズ未満チェック(商品ID、商品名、商品名カナ、商品カテゴリID、メーカーID、説明)",
                        "A123", "", "", "0", "M123", "2022-03-01",
                        "2025-03-31", "1000", "", new byte[] {0}, "1", """
                                {
                                    "errorCode": "error.unexpect.method-argument-not-valid",
                                    "message": "Validation is failed. Error count:6",
                                    "detail": [
                                        {
                                            "Field": "id",
                                            "ValidationMessage": "id,Size,5,5"
                                        },
                                        {
                                            "Field": "name",
                                            "ValidationMessage": "name,NotEmpty"
                                        },
                                        {
                                            "Field": "nameKana",
                                            "ValidationMessage": "nameKana,NotEmpty"
                                        },
                                        {
                                            "Field": "itemCategoryId",
                                            "ValidationMessage": "itemCategoryId,Size,2,2"
                                        },
                                        {
                                            "Field": "makerId",
                                            "ValidationMessage": "makerId,Size,5,5"
                                        },
                                        {
                                            "Field": "description",
                                            "ValidationMessage": "description,NotEmpty"
                                        }
                                     ]
                                }
                                """),
                arguments("形式チェック(発売日、販売終了日)",
                        "A0001", "テスト商品", "テストショウヒン", "01", "M0001", "2022/03/01",
                        "20250331", "1000", "商品説明", new byte[] {0}, "1", """
                                {
                                    "errorCode": "error.unexpect.method-argument-not-valid",
                                    "message": "Validation is failed. Error count:2",
                                    "detail": [
                                        {
                                            "Field": "startDate",
                                            "ValidationMessage": "startDate,typeMismatch,LocalDate"
                                        },
                                        {
                                            "Field": "endDate",
                                            "ValidationMessage": "endDate,typeMismatch,LocalDate"
                                        }
                                     ]
                                }
                                """),
                arguments("最大値チェック(価格、画像ファイルサイズ)",
                        "A0001", "テスト商品", "テストショウヒン", "01", "M0001", "2022-03-01",
                        "2025-03-31", "1000000", "商品説明", new byte[200 * 1024 + 1], "1", """
                                {
                                    "errorCode": "error.unexpect.method-argument-not-valid",
                                    "message": "Validation is failed. Error count:2",
                                    "detail": [
                                        {
                                            "Field": "price",
                                            "ValidationMessage": "price,Max,999999"
                                        },
                                        {
                                            "Field": "image",
                                            "ValidationMessage": "image,UploadFileMaxSize,204800"
                                        }
                                     ]
                                }
                                """));
    }

    /**
     * 登録処理のプロバイダ
     */
    static Stream<Arguments> createItemProvider() {
        // ケース概要、商品ID、商品名、商品名カナ、カテゴリID、メーカーID、発売日、販売終了日、価格、説明、商品画像、バージョン、期待値JSON
        // ※第一引数はケースの概要説明で、テストコード内では使用しない。
        // 　JUnit実行結果にケース概要を表示させ、確認しやすくするためのもの。
        return Stream.of(
                arguments("必須チェック(null)",
                        null, null, null, null, null, null,
                        null, null, null, null, null, """
                                {
                                    "errorCode": "error.unexpect.method-argument-not-valid",
                                    "message": "Validation is failed. Error count:9",
                                    "detail": [
                                        {
                                            "Field": "id",
                                            "ValidationMessage": "id,NotEmpty"
                                        },
                                        {
                                            "Field": "name",
                                            "ValidationMessage": "name,NotEmpty"
                                        },
                                        {
                                            "Field": "nameKana",
                                            "ValidationMessage": "nameKana,NotEmpty"
                                        },
                                        {
                                            "Field": "itemCategoryId",
                                            "ValidationMessage": "itemCategoryId,NotEmpty"
                                        },
                                        {
                                            "Field": "makerId",
                                            "ValidationMessage": "makerId,NotEmpty"
                                        },
                                        {
                                            "Field": "startDate",
                                            "ValidationMessage": "startDate,NotNull"
                                        },
                                        {
                                            "Field": "price",
                                            "ValidationMessage": "price,NotNull"
                                        },
                                        {
                                            "Field": "description",
                                            "ValidationMessage": "description,NotEmpty"
                                        },
                                        {
                                            "Field": "image",
                                            "ValidationMessage": "image,UploadFileRequired"
                                        }
                                    ]
                                }
                                """),
                arguments("必須チェック(空文字)",
                        "", "", "", "", "", "",
                        "", "", "", new byte[] {}, "", """
                                {
                                    "errorCode": "error.unexpect.method-argument-not-valid",
                                    "message": "Validation is failed. Error count:9",
                                    "detail": [
                                        {
                                            "Field": "id",
                                            "ValidationMessage": "id,NotEmpty"
                                        },
                                        {
                                            "Field": "name",
                                            "ValidationMessage": "name,NotEmpty"
                                        },
                                        {
                                            "Field": "nameKana",
                                            "ValidationMessage": "nameKana,NotEmpty"
                                        },
                                        {
                                            "Field": "itemCategoryId",
                                            "ValidationMessage": "itemCategoryId,NotEmpty"
                                        },
                                        {
                                            "Field": "makerId",
                                            "ValidationMessage": "makerId,NotEmpty"
                                        },
                                        {
                                            "Field": "startDate",
                                            "ValidationMessage": "startDate,NotNull"
                                        },
                                        {
                                            "Field": "price",
                                            "ValidationMessage": "price,NotNull"
                                        },
                                        {
                                            "Field": "description",
                                            "ValidationMessage": "description,NotEmpty"
                                        },
                                        {
                                            "Field": "image",
                                            "ValidationMessage": "image,UploadFileNotEmpty"
                                        }
                                    ]
                                }
                                """));
    }

    @ParameterizedTest
    @MethodSource("createItemProvider")
    @MethodSource("registerCommonItemProvider")
    @DisplayName("[createItem][正常系][バリデーション]")
    void testCreateItem(String summary, String id, String name, String nameKana, String itemCategoryId,
            String makerId, String startDate, String endDate, String price, String description,
            byte[] image, String version, String json) throws Exception {

        // リクエスト先URL
        URI uri = UriComponentsBuilder.fromUriString("http://localhost:" + port)
                .path("/items")
                .build(Collections.emptyMap());

        // リクエストBody(画像以外)
        MultiValueMap<String, Object> values = new LinkedMultiValueMap<>();
        values.add("id", id);
        values.add("name", name);
        values.add("nameKana", nameKana);
        values.add("itemCategoryId", itemCategoryId);
        values.add("makerId", makerId);
        values.add("startDate", startDate);
        values.add("endDate", endDate);
        values.add("price", price);
        values.add("description", description);
        values.add("version", version);

        // リクエストヘッダ設定
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(values, headers);

        // リクエストBody（画像）
        if (image != null) {
            HttpHeaders imageHeaders = new HttpHeaders();
            imageHeaders.setContentDispositionFormData("image", "FoodShopImage.jpg");
            imageHeaders.setContentType(MediaType.IMAGE_JPEG);
            values.add("image", new HttpEntity<>(image, imageHeaders));
        }

        
        // リクエスト発行
        ResponseEntity<String> result = this.testRestTemplate.exchange(uri, HttpMethod.POST, request, String.class);


        // レスポンス検証
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        JSONAssert.assertEquals(json, result.getBody(), JSONCompareMode.LENIENT);

        // Service処理が呼び出されていないことの検証
        verifyNoInteractions(this.mockService);
    }

    /**
     * 更新処理のプロバイダ
     */
    static Stream<Arguments> updateItemProvider() {
        // ケース概要、商品ID、商品名、商品名カナ、カテゴリID、メーカーID、発売日、販売終了日、価格、説明、商品画像、バージョン、期待値JSON
        // ※第一引数はケースの概要説明で、テストコード内では使用しない。
        // 　JUnit実行結果にケース概要を表示させ、確認しやすくするためのもの。
        return Stream.of(
                arguments("必須チェック(null)",
                        null, null, null, null, null, null,
                        null, null, null, null, null, """
                                {
                                    "errorCode": "error.unexpect.method-argument-not-valid",
                                    "message": "Validation is failed. Error count:9",
                                    "detail": [
                                        {
                                            "Field": "id",
                                            "ValidationMessage": "id,NotEmpty"
                                        },
                                        {
                                            "Field": "name",
                                            "ValidationMessage": "name,NotEmpty"
                                        },
                                        {
                                            "Field": "nameKana",
                                            "ValidationMessage": "nameKana,NotEmpty"
                                        },
                                        {
                                            "Field": "itemCategoryId",
                                            "ValidationMessage": "itemCategoryId,NotEmpty"
                                        },
                                        {
                                            "Field": "makerId",
                                            "ValidationMessage": "makerId,NotEmpty"
                                        },
                                        {
                                            "Field": "startDate",
                                            "ValidationMessage": "startDate,NotNull"
                                        },
                                        {
                                            "Field": "price",
                                            "ValidationMessage": "price,NotNull"
                                        },
                                        {
                                            "Field": "description",
                                            "ValidationMessage": "description,NotEmpty"
                                        },
                                        {
                                            "Field": "version",
                                            "ValidationMessage": "version,NotNull"
                                        }
                                    ]
                                }
                                """),
                arguments("必須チェック(空文字)",
                        "", "", "", "", "", "",
                        "", "", "", new byte[] {}, "", """
                                {
                                    "errorCode": "error.unexpect.method-argument-not-valid",
                                    "message": "Validation is failed. Error count:10",
                                    "detail": [
                                        {
                                            "Field": "id",
                                            "ValidationMessage": "id,NotEmpty"
                                        },
                                        {
                                            "Field": "name",
                                            "ValidationMessage": "name,NotEmpty"
                                        },
                                        {
                                            "Field": "nameKana",
                                            "ValidationMessage": "nameKana,NotEmpty"
                                        },
                                        {
                                            "Field": "itemCategoryId",
                                            "ValidationMessage": "itemCategoryId,NotEmpty"
                                        },
                                        {
                                            "Field": "makerId",
                                            "ValidationMessage": "makerId,NotEmpty"
                                        },
                                        {
                                            "Field": "startDate",
                                            "ValidationMessage": "startDate,NotNull"
                                        },
                                        {
                                            "Field": "price",
                                            "ValidationMessage": "price,NotNull"
                                        },
                                        {
                                            "Field": "description",
                                            "ValidationMessage": "description,NotEmpty"
                                        },
                                        {
                                            "Field": "image",
                                            "ValidationMessage": "image,UploadFileNotEmpty"
                                        },
                                        {
                                            "Field": "version",
                                            "ValidationMessage": "version,NotNull"
                                        }
                                    ]
                                }
                                """));

    }

    @ParameterizedTest
    @MethodSource("updateItemProvider")
    @MethodSource("registerCommonItemProvider")
    @DisplayName("[updateItem][正常系][バリデーション]")
    void testUpdateItem(String summary, String id, String name, String nameKana, String itemCategoryId,
            String makerId, String startDate, String endDate, String price, String description,
            byte[] image, String version, String json) throws Exception {

        // リクエスト先URL
        URI uri = UriComponentsBuilder.fromUriString("http://localhost:" + port)
                .path("/items/{id}")
                .build((id == null || id.isEmpty()) ? "A0001" : id);

        // リクエストBody(画像以外)
        MultiValueMap<String, Object> values = new LinkedMultiValueMap<>();
        values.add("id", id);
        values.add("name", name);
        values.add("nameKana", nameKana);
        values.add("itemCategoryId", itemCategoryId);
        values.add("makerId", makerId);
        values.add("startDate", startDate);
        values.add("endDate", endDate);
        values.add("price", price);
        values.add("description", description);
        values.add("version", version);

        // リクエストヘッダ設定
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(values, headers);

        // リクエストBody（画像）
        if (image != null) {
            HttpHeaders imageHeaders = new HttpHeaders();
            imageHeaders.setContentDispositionFormData("image", "FoodShopImage.jpg");
            imageHeaders.setContentType(MediaType.IMAGE_JPEG);
            values.add("image", new HttpEntity<>(image, imageHeaders));
        }


        // リクエスト発行
        ResponseEntity<String> result = this.testRestTemplate.exchange(uri, HttpMethod.PUT, request, String.class);


        // レスポンス検証
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        JSONAssert.assertEquals(json, result.getBody(), JSONCompareMode.LENIENT);

        // Service処理が呼び出されていないことの検証
        verifyNoInteractions(this.mockService);
    }

    /**
     * 削除処理のプロバイダ
     */
    static Stream<Arguments> deleteItemProvider() {
        // ケース概要、商品ID、バージョン、期待値JSON
        // ※第一引数はケースの概要説明で、テストコード内では使用しない。
        // 　JUnit実行結果にケース概要を表示させ、確認しやすくするためのもの。
        return Stream.of(
                arguments("必須チェック(null)", null, null, """
                        {
                            "errorCode": "error.unexpect.method-argument-not-valid",
                            "message": "Validation is failed. Error count:2",
                            "detail": [
                                {
                                    "Field": "id",
                                    "ValidationMessage": "id,NotEmpty"
                                },
                                {
                                    "Field": "version",
                                    "ValidationMessage": "version,NotNull"
                                }
                            ]
                        }
                        """),
                // バージョン番号はテストコード内のparseIntで失敗するので除外。
                arguments("必須チェック(空文字)", "", "1", """
                        {
                            "errorCode": "error.unexpect.method-argument-not-valid",
                            "message": "Validation is failed. Error count:2",
                            "detail": [
                                {
                                    "Field": "id",
                                    "ValidationMessage": "id,NotEmpty"
                                },
                                {
                                    "Field": "id",
                                    "ValidationMessage": "id,Size,5,5"
                                }
                            ]
                        }
                        """),
                // バージョン番号はテストコード内のparseIntで失敗するので除外。
                arguments("文字種チェック(商品ID)", "＠１２３Ｘ", "1", """
                        {
                            "errorCode": "error.unexpect.method-argument-not-valid",
                            "message": "Validation is failed. Error count:1",
                            "detail": [
                                {
                                    "Field": "id",
                                    "ValidationMessage": "id,HalfAlphaNumeric"
                                }
                            ]
                        }
                        """),
                arguments("規定サイズ超過チェック(商品ID)", "A12345", "1", """
                        {
                            "errorCode": "error.unexpect.method-argument-not-valid",
                            "message": "Validation is failed. Error count:1",
                            "detail": [
                                {
                                    "Field": "id",
                                    "ValidationMessage": "id,Size,5,5"
                                }
                            ]
                        }
                        """),
                arguments("規定サイズ未満チェック(商品ID)", "A123", "1", """
                        {
                            "errorCode": "error.unexpect.method-argument-not-valid",
                            "message": "Validation is failed. Error count:1",
                            "detail": [
                                {
                                    "Field": "id",
                                    "ValidationMessage": "id,Size,5,5"
                                }
                            ]
                        }
                        """));
    }

    @ParameterizedTest
    @MethodSource("deleteItemProvider")
    @DisplayName("[deleteItem][正常系][バリデーション]")
    void testDeleteItem(String summary, String id, String version, String json) throws Exception {

        // リクエスト先URL
        URI uri = UriComponentsBuilder.fromUriString("http://localhost:" + port)
                .path("/items/{id}/delete")
                .build((id == null || id.isEmpty()) ? "A0001" : id);

        // リクエストBody
        ItemDeleteRequest body = new ItemDeleteRequest();
        body.setId(id);
        body.setVersion(version == null ? null : Integer.parseInt(version));

        // リクエストヘッダ設定
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<ItemDeleteRequest> request = new HttpEntity<>(body, headers);


        // リクエスト発行
        ResponseEntity<String> result = this.testRestTemplate.exchange(uri, HttpMethod.POST, request, String.class);


        // レスポンス検証
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        JSONAssert.assertEquals(json, result.getBody(), JSONCompareMode.LENIENT);

        // Service処理が呼び出されていないことの検証
        verifyNoInteractions(this.mockService);
    }

    /**
     * 商品画像取得処理のプロバイダ
     */
    static Stream<Arguments> getItemImageProvider() {
        // ケース概要、商品ID、期待値JSON
        // ※第一引数はケースの概要説明で、テストコード内では使用しない。
        // 　JUnit実行結果にケース概要を表示させ、確認しやすくするためのもの。
        return Stream.of(
                arguments("文字種チェック(商品ID)", "＠１２３Ｘ", """
                        {
                            "errorCode": "error.unexpect.constraint-violation",
                            "message": "Validation is failed. Error count:1",
                            "detail": [
                                {
                                    "Field": "id",
                                    "ValidationMessage": "id,HalfAlphaNumeric"
                                }
                            ]
                        }
                        """),
                arguments("規定サイズ超過チェック(商品ID)", "A12345", """
                        {
                            "errorCode": "error.unexpect.constraint-violation",
                            "message": "Validation is failed. Error count:1",
                            "detail": [
                                {
                                    "Field": "id",
                                    "ValidationMessage": "id,Size,5,5"
                                }
                            ]
                        }
                        """),
                arguments("規定サイズ未満チェック(商品ID)", "A123", """
                        {
                            "errorCode": "error.unexpect.constraint-violation",
                            "message": "Validation is failed. Error count:1",
                            "detail": [
                                {
                                    "Field": "id",
                                    "ValidationMessage": "id,Size,5,5"
                                }
                            ]
                        }
                        """));
    }
    
    @ParameterizedTest
    @MethodSource("getItemImageProvider")
    @DisplayName("[getItemImage][正常系][バリデーション]")
    void testGetItemImage(String summary, String id, String json) throws Exception {

        // リクエスト先URL
        URI uri = UriComponentsBuilder.fromUriString("http://localhost:" + port)
                .path("/items/{id}/image")
                .build(id);

        // リクエストヘッダ設定
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<ItemDeleteRequest> request = new HttpEntity<>(headers);


        // リクエスト発行
        ResponseEntity<String> result = this.testRestTemplate.exchange(uri, HttpMethod.GET, request, String.class);


        // レスポンス検証
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        JSONAssert.assertEquals(json, result.getBody(), JSONCompareMode.LENIENT);

        // Service処理が呼び出されていないことの検証
        verifyNoInteractions(this.mockService);
    }
}
