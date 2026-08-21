package jp.co.nekonet.foodshop.bs.item.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import jp.co.nekonet.foodshop.bs.common.ErrorCode;
import jp.co.nekonet.foodshop.bs.common.file.TempFileUtil;
import jp.co.nekonet.foodshop.bs.item.model.Item;
import jp.co.nekonet.foodshop.bs.item.model.ItemCategory;
import jp.co.nekonet.foodshop.bs.item.service.ItemService;
import jp.co.nekonet.foodshop.bs.maker.model.Maker;
import jp.co.nekonet.springer.exceptions.ApplicationException;
import jp.co.nekonet.springer.exceptions.ConflictException;
import jp.co.nekonet.springer.exceptions.ResourceNotFoundException;
import jp.co.nekonet.springer.exceptions.SystemException;

/**
 * 商品コントローラのテストクラス
 * 
 * @author YAMATO SYSTEM DEVELOPMENT
 */
@SpringBootTest
@AutoConfigureMockMvc
public class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService mockService;

    @InjectMocks
    private ItemController target;

    /** テスト用商品１ */
    private Item item1;

    /** テスト用商品２ */
    private Item item2;

    /** テスト用商品３ */
    private Item item3;

    @BeforeEach
    void setUp() {

        // モックのふるまいを初期化
        Mockito.reset(mockService);

        // テスト用商品初期化
        item1 = new Item();
        item1.setId("A0001");
        item1.setName("商品１");
        item1.setNameKana("ショウヒンイチ");
        ItemCategory itemCategory1 = new ItemCategory();
        itemCategory1.setId("01");
        itemCategory1.setName("スナック菓子");
        itemCategory1.setCreatedAt(OffsetDateTime.parse("2021-01-01T10:15:30+09:00"));
        itemCategory1.setLastModifiedAt(OffsetDateTime.parse("2021-01-01T12:30:30+09:00"));
        item1.setItemCategory(itemCategory1);
        Maker maker1 = new Maker();
        maker1.setId("M0001");
        maker1.setName("メーカー１");
        maker1.setNameKana("メーカーイチ");
        maker1.setCreatedAt(OffsetDateTime.parse("2021-02-01T10:15:30+09:00"));
        maker1.setLastModifiedAt(OffsetDateTime.parse("2021-02-01T12:30:30+09:00"));
        item1.setMaker(maker1);
        item1.setStartDate(LocalDate.parse("2021-03-01"));
        item1.setEndDate(LocalDate.parse("2021-03-31"));
        item1.setPrice(1100);
        item1.setDescription("商品説明１");
        item1.setReviewAverageScore("4.00");
        item1.setVersion(1);
        item1.setCreatedAt(OffsetDateTime.parse("2021-04-01T10:15:30+09:00"));
        item1.setLastModifiedAt(OffsetDateTime.parse("2021-04-01T12:30:30+09:00"));

        item2 = new Item();
        item2.setId("A0002");
        item2.setName("商品２");
        item2.setNameKana("ショウヒンニ");
        ItemCategory itemCategory2 = new ItemCategory();
        itemCategory2.setId("02");
        itemCategory2.setName("清涼飲料水");
        itemCategory2.setCreatedAt(OffsetDateTime.parse("2022-01-01T10:15:30+09:00"));
        itemCategory2.setLastModifiedAt(OffsetDateTime.parse("2022-01-01T12:30:30+09:00"));
        item2.setItemCategory(itemCategory2);
        Maker maker2 = new Maker();
        maker2.setId("M0002");
        maker2.setName("メーカー２");
        maker2.setNameKana("メーカーニ");
        maker2.setCreatedAt(OffsetDateTime.parse("2022-02-01T10:15:30+09:00"));
        maker2.setLastModifiedAt(OffsetDateTime.parse("2022-02-01T12:30:30+09:00"));
        item2.setMaker(maker2);
        item2.setStartDate(LocalDate.parse("2022-03-01"));
        item2.setEndDate(LocalDate.parse("2022-03-31"));
        item2.setPrice(1200);
        item2.setDescription("商品説明２");
        item2.setReviewAverageScore("3.00");
        item2.setVersion(1);
        item2.setCreatedAt(OffsetDateTime.parse("2022-04-01T10:15:30+09:00"));
        item2.setLastModifiedAt(OffsetDateTime.parse("2022-04-01T12:30:30+09:00"));

        item3 = new Item();
        item3.setId("A0003");
        item3.setName("商品３");
        item3.setNameKana("ショウヒンサン");
        ItemCategory itemCategory3 = new ItemCategory();
        itemCategory3.setId("03");
        itemCategory3.setName("氷菓子");
        itemCategory3.setCreatedAt(OffsetDateTime.parse("2023-01-01T10:15:30+09:00"));
        itemCategory3.setLastModifiedAt(OffsetDateTime.parse("2023-01-01T12:30:30+09:00"));
        item3.setItemCategory(itemCategory3);
        Maker maker3 = new Maker();
        maker3.setId("M0003");
        maker3.setName("メーカー３");
        maker3.setNameKana("メーカーサン");
        maker3.setCreatedAt(OffsetDateTime.parse("2023-02-01T10:15:30+09:00"));
        maker3.setLastModifiedAt(OffsetDateTime.parse("2023-02-01T12:30:30+09:00"));
        item3.setMaker(maker3);
        item3.setStartDate(LocalDate.parse("2023-03-01"));
        item3.setEndDate(LocalDate.parse("2023-03-31"));
        item3.setPrice(1300);
        item3.setDescription("商品説明３");
        item3.setReviewAverageScore("2.00");
        item3.setVersion(1);
        item3.setCreatedAt(OffsetDateTime.parse("2023-04-01T10:15:30+09:00"));
        item3.setLastModifiedAt(OffsetDateTime.parse("2023-04-01T12:30:30+09:00"));
    }

    @Test
    @DisplayName("[getItems][正常系][商品3件取得]")
    void testGetItems1() throws Exception {

        // MockServiceの戻り値
        List<Item> mockReturn = Arrays.asList(item1, item2, item3);

        // MockServiceの挙動を設定
        doReturn(mockReturn).when(mockService).getList(any());

        // コントローラが返す想定のJSON
        String expectedResponse = """
                [
                    {
                        "id": "A0001",
                        "name": "商品１",
                        "nameKana": "ショウヒンイチ",
                        "itemCategory": {
                            "id": "01",
                            "name": "スナック菓子"
                        },
                        "maker": {
                            "id": "M0001",
                            "name": "メーカー１",
                            "nameKana": "メーカーイチ"
                        },
                        "startDate": "2021-03-01",
                        "endDate": "2021-03-31",
                        "price": 1100,
                        "description": "商品説明１",
                        "reviewAverageScore": "4.00",
                        "version": 1
                    },
                    {
                        "id": "A0002",
                        "name": "商品２",
                        "nameKana": "ショウヒンニ",
                        "itemCategory": {
                            "id": "02",
                            "name": "清涼飲料水"
                        },
                        "maker": {
                            "id": "M0002",
                            "name": "メーカー２",
                            "nameKana": "メーカーニ"
                        },
                        "startDate": "2022-03-01",
                        "endDate": "2022-03-31",
                        "price": 1200,
                        "description": "商品説明２",
                        "reviewAverageScore": "3.00",
                        "version": 1
                    },
                    {
                        "id": "A0003",
                        "name": "商品３",
                        "nameKana": "ショウヒンサン",
                        "itemCategory": {
                            "id": "03",
                            "name": "氷菓子"
                        },
                        "maker": {
                            "id": "M0003",
                            "name": "メーカー３",
                            "nameKana": "メーカーサン"
                        },
                        "startDate": "2023-03-01",
                        "endDate": "2023-03-31",
                        "price": 1300,
                        "description": "商品説明３",
                        "reviewAverageScore": "2.00",
                        "version": 1
                    }
                ]
                """;


        // リクエストの実行、実行結果に対する検証
        mockMvc.perform(
                get("/items")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("itemCategoryId", "01")
                        .param("order", "12"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse, JsonCompareMode.STRICT));


        // 想定したパラメータでMock Serviceを呼び出したことを検証。
        verify(this.mockService).getList(argThat(condition -> {
            assertNull(condition.getId());
            assertNull(condition.getName());
            assertNull(condition.getNameKana());
            assertEquals("01", condition.getItemCategoryId());
            assertEquals("12", condition.getOrder());
            return true;
        }));
    }

    @Test
    @DisplayName("[getItems][正常系][該当商品なし]")
    void testGetItems2() throws Exception {

        // MockServiceの挙動を設定
        doReturn(Collections.EMPTY_LIST).when(mockService).getList(any());

        // コントローラが返す想定のJSON
        String expectedResponse = "[]";


        // リクエストの実行、実行結果に対する検証
        mockMvc.perform(
                get("/items")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("id", "X0001")
                        .param("name", "商品XX")
                        .param("nameKana", "ショウヒンバツバツ")
                        .param("itemCategoryId", "01")
                        .param("order", "12"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse, JsonCompareMode.STRICT));


        // 想定したパラメータでMock Serviceを呼び出したことを検証。
        verify(this.mockService).getList(argThat(condition -> {
            assertEquals("X0001", condition.getId());
            assertEquals("商品XX", condition.getName());
            assertEquals("ショウヒンバツバツ", condition.getNameKana());
            assertEquals("01", condition.getItemCategoryId());
            assertEquals("12", condition.getOrder());
            return true;
        }));
    }

    @Test
    @DisplayName("[getItems][異常系][サービスから例外がスローされる（指定された商品カテゴリが存在しない）]")
    void testGetItems3() throws Exception {

        // サービスからスローされる例外
        ApplicationException exception = new ApplicationException(ErrorCode.ITEM_CATEGORY_NOT_FOUND, new String[] {"99"});

        // MockServiceの挙動を設定
        doThrow(exception).when(mockService).getList(any());


        // リクエストの実行、実行結果に対する検証
        mockMvc.perform(
                get("/items")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("itemCategoryId", "99")
                        .param("order", "12"))
                .andExpect(status().isBadRequest());


        // 想定したパラメータでMock Serviceを呼び出したことを検証。
        verify(this.mockService).getList(argThat(condition -> {
            assertNull(condition.getId());
            assertNull(condition.getName());
            assertNull(condition.getNameKana());
            assertEquals("99", condition.getItemCategoryId());
            assertEquals("12", condition.getOrder());
            return true;
        }));
    }

    @Test
    @DisplayName("[getItemsCsv][正常系][CSV取得成功（商品3件）]")
    void testGetItemsCsv1() throws Exception {

        // ファイル内容（CSV）
        String csv = """
                商品ID,商品名,商品名カナ,商品カテゴリID,商品カテゴリ名,メーカーID,メーカー名,発売日,販売終了日,価格,説明,レビュー平均得点
                A0001,商品１,ショウヒンイチ,01,スナック菓子,M0001,メーカー１,2021-03-01,2021-03-31,1000,商品説明１,3.10
                A0002,商品２,ショウヒンイチ,02,清涼飲料水,M0002,メーカー２,2022-03-01,2021-03-31,1000,商品説明２,3.20
                A0003,商品３,ショウヒンイチ,01,氷菓子,M0003,メーカー３,2023-03-01,2023-03-31,1000,商品説明３,3.30
                """;

        // 期待値ファイルを作成
        Path csvPath = Files.createTempFile("foodshop-test-csv", ".csv");
        try {
            // 期待値ファイルにCSV内容を書き込み
            Files.write(csvPath, csv.getBytes());

            // MockServiceの挙動を設定
            doReturn(csvPath).when(mockService).getListCsv(any());


            // リクエストの実行、実行結果に対する検証
            mockMvc.perform(
                    get("/items-csv")
                            .param("order", "12"))
                    .andExpect(status().isOk())
                    .andExpect(content().bytes(csv.getBytes(StandardCharsets.UTF_8)));


            // 想定したリクエスト内容でMock Serviceを呼び出したことを検証
            verify(this.mockService).getListCsv(argThat(condition -> {
                assertNull(condition.getId());
                assertNull(condition.getName());
                assertNull(condition.getNameKana());
                assertNull(condition.getItemCategoryId());
                assertEquals("12", condition.getOrder());
                return true;
            }));

        } finally {
            File file = csvPath.toFile();
            file.delete();
        }
    }

    @Test
    @DisplayName("[getItemsCsv][正常系][CSV取得成功（商品0件）]")
    void testGetItemsCsv2() throws Exception {

        // 期待値ファイルを作成
        Path csvPath = Files.createTempFile("foodshop-test-csv", ".csv");
        try {

            // MockServiceの挙動を設定
            doReturn(csvPath).when(mockService).getListCsv(any());


            // リクエストの実行、実行結果に対する検証
            mockMvc.perform(
                    get("/items-csv")
                            .param("id", "Z0001")
                            .param("name", "商品Z")
                            .param("nameKana", "ショウヒンゼット")
                            .param("itemCategoryId", "01")
                            .param("order", "12"))
                    .andExpect(status().isOk())
                    .andExpect(content().bytes("".getBytes(StandardCharsets.UTF_8)));


            // 想定したリクエスト内容でMock Serviceを呼び出したことを検証
            verify(this.mockService).getListCsv(argThat(condition -> {
                assertEquals("Z0001", condition.getId());
                assertEquals("商品Z", condition.getName());
                assertEquals("ショウヒンゼット", condition.getNameKana());
                assertEquals("01", condition.getItemCategoryId());
                assertEquals("12", condition.getOrder());
                return true;
            }));

        } finally {
            File file = csvPath.toFile();
            file.delete();
        }
    }

    @Test
    @DisplayName("[getItemsCsv][異常系][指定された商品カテゴリが存在しない]")
    void testGetItemsCsv3() throws Exception {

        // Mock商品カテゴリリポジトリからスローされる例外
        ResourceNotFoundException exception = new ResourceNotFoundException(ErrorCode.ITEM_CATEGORY_NOT_FOUND, new String[] {"99"});

        // Mockのふるまい定義
        doThrow(exception).when(mockService).getListCsv(any());


        // リクエストの実行、実行結果に対する検証
        mockMvc.perform(
                get("/items-csv")
                        .param("itemCategoryId", "99"))
                .andExpect(status().isNotFound());


        // 想定したリクエスト内容でMock Serviceを呼び出したことを検証
        verify(this.mockService).getListCsv(argThat(condition -> {
            assertNull(condition.getId());
            assertNull(condition.getName());
            assertNull(condition.getNameKana());
            assertEquals("99", condition.getItemCategoryId());
            assertNull(condition.getOrder());
            return true;
        }));
    }

    @Test
    @DisplayName("[getItem][正常系][商品取得成功]")
    void testGetItem1() throws Exception {

        // MockServiceの挙動を設定
        doReturn(item1).when(mockService).getOne(any());

        // コントローラが返す想定のJSON
        String expectedResponse = """
                {
                    "id": "A0001",
                    "name": "商品１",
                    "nameKana": "ショウヒンイチ",
                    "itemCategory": {
                        "id": "01",
                        "name": "スナック菓子"
                    },
                    "maker": {
                        "id": "M0001",
                        "name": "メーカー１",
                        "nameKana": "メーカーイチ"
                    },
                    "startDate": "2021-03-01",
                    "endDate": "2021-03-31",
                    "price": 1100,
                    "description": "商品説明１",
                    "reviewAverageScore": "4.00",
                    "version": 1
                }
                """;


        // リクエストの実行、実行結果に対する検証
        mockMvc.perform(
                get("/items/A0001")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse, JsonCompareMode.STRICT));


        // 想定したパラメータでMock Serviceを呼び出したことを検証。
        verify(this.mockService).getOne("A0001");
    }

    @Test
    @DisplayName("[getItem][正常系][指定された商品が存在しない]")
    void testGetItem2() throws Exception {

        // Mock商品カテゴリリポジトリからスローされる例外
        ResourceNotFoundException exception = new ResourceNotFoundException(ErrorCode.ITEM_CATEGORY_NOT_FOUND, new String[] {"X0001"});

        // Mockのふるまい定義
        doThrow(exception).when(mockService).getOne(any());


        // リクエストの実行、実行結果に対する検証
        mockMvc.perform(
                get("/items/A0001")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());


        // 想定したパラメータでMock Serviceを呼び出したことを検証。
        verify(this.mockService).getOne("A0001");
    }

    @Test
    @DisplayName("[createItem][正常系][登録成功]")
    void testCreateItem1() throws Exception {

        // MockMultipartFileのふるまい定義（バリデーションチェックを通すのに必要な挙動）
        MockMultipartFile mockMultiPart = Mockito.mock(MockMultipartFile.class);
        doReturn("image").when(mockMultiPart).getName();
        doReturn("image.jpg").when(mockMultiPart).getOriginalFilename();

        try (MockedStatic<TempFileUtil> mockedTempFileUtil = mockStatic(TempFileUtil.class)) {

            // モック化したTempFileUtil.transferToのふるまい設定
            Path path = Path.of(URI.create("file:/image.jpg"));
            mockedTempFileUtil.when(() -> TempFileUtil.transferTo(any())).thenReturn(path);

            // MockServiceの挙動を設定
            doNothing().when(mockService).createOne(any(), any());

            
            // リクエストの実行、実行結果に対する検証
            mockMvc.perform(
                    multipart("/items")
                            .file(mockMultiPart)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .param("id", "A0001")
                            .param("name", "商品１")
                            .param("nameKana", "ショウヒンイチ")
                            .param("itemCategoryId", "01")
                            .param("makerId", "M0001")
                            .param("startDate", "2021-03-01")
                            .param("endDate", "2021-03-31")
                            .param("price", "1100")
                            .param("description", "商品説明１")
                            .param("version", "1"))
                    .andExpect(status().isCreated())
                    .andExpect(header().stringValues("location", "http://localhost/items/A0001"));


            // TempFileUtil.transferToの呼び出し検証
            mockedTempFileUtil.verify(() -> TempFileUtil.transferTo(mockMultiPart));

            // 想定したパラメータでMock Serviceを呼び出したことを検証。
            verify(this.mockService).createOne(argThat(condition -> {
                assertEquals("A0001", condition.getId());
                assertEquals("商品１", condition.getName());
                assertEquals("ショウヒンイチ", condition.getNameKana());
                assertEquals("01", condition.getItemCategory().getId());
                assertEquals("M0001", condition.getMaker().getId());
                assertEquals("2021-03-01", condition.getStartDate().toString());
                assertEquals("2021-03-31", condition.getEndDate().toString());
                assertEquals(1100, condition.getPrice());
                assertEquals("商品説明１", condition.getDescription());
                assertEquals(1, condition.getVersion());
                return true;

            }), eq(path));

            // TempFileUtil.deleteの呼び出し検証
            mockedTempFileUtil.verify(() -> TempFileUtil.delete(path));
        }
    }

    @Test
    @DisplayName("[createItem][異常系][指定された商品がすでに存在する]")
    void testCreateItem2() throws Exception {

        // MockMultipartFileのふるまい定義（バリデーションチェックを通すのに必要な挙動）
        MockMultipartFile mockMultiPart = Mockito.mock(MockMultipartFile.class);
        doReturn("image").when(mockMultiPart).getName();
        doReturn("image.jpg").when(mockMultiPart).getOriginalFilename();

        try (MockedStatic<TempFileUtil> mockedTempFileUtil = mockStatic(TempFileUtil.class)) {

            // モック化したTempFileUtil.transferToのふるまい設定
            Path path = Path.of(URI.create("file:/image.jpg"));
            mockedTempFileUtil.when(() -> TempFileUtil.transferTo(any())).thenReturn(path);

            // Mock商品リポジトリからスローされる例外
            ConflictException exception = new ConflictException(ErrorCode.ITEM_ALREADY_EXISTS, new String[] {"A0001"});

            // MockServiceの挙動を設定
            doThrow(exception).when(mockService).createOne(any(), any());

            // コントローラが返す想定のJSON
            String expectedResponse = """
                    {
                      "errorCode": "bs.error.business.ItemAlreadyExists",
                      "message": "商品が既に存在します。(商品ID=A0001)",
                      "detail": null
                    }
                    """;

            // リクエストの実行、実行結果に対する検証
            mockMvc.perform(
                    multipart("/items")
                            .file(mockMultiPart)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .param("id", "A0001")
                            .param("name", "商品１")
                            .param("nameKana", "ショウヒンイチ")
                            .param("itemCategoryId", "01")
                            .param("makerId", "M0001")
                            .param("startDate", "2021-03-01")
                            .param("endDate", "2021-03-31")
                            .param("price", "1100")
                            .param("description", "商品説明１")
                            .param("version", "1"))
                    .andExpect(status().isConflict())
                    .andExpect(content().json(expectedResponse, JsonCompareMode.STRICT))
                    .andExpect(header().doesNotExist("location"));


            // TempFileUtil.transferToの呼び出し検証
            mockedTempFileUtil.verify(() -> TempFileUtil.transferTo(mockMultiPart));

            // 想定したパラメータでMock Serviceを呼び出したことを検証。
            verify(this.mockService).createOne(argThat(condition -> {
                assertEquals("A0001", condition.getId());
                assertEquals("商品１", condition.getName());
                assertEquals("ショウヒンイチ", condition.getNameKana());
                assertEquals("01", condition.getItemCategory().getId());
                assertEquals("M0001", condition.getMaker().getId());
                assertEquals("2021-03-01", condition.getStartDate().toString());
                assertEquals("2021-03-31", condition.getEndDate().toString());
                assertEquals(1100, condition.getPrice());
                assertEquals("商品説明１", condition.getDescription());
                assertEquals(1, condition.getVersion());
                return true;

            }), eq(path));

            // TempFileUtil.deleteの呼び出し検証
            mockedTempFileUtil.verify(() -> TempFileUtil.delete(path));
        }
    }

    @Test
    @DisplayName("[createItem][異常系][指定された商品カテゴリが存在しない]")
    void testCreateItem3() throws Exception {

        // MockMultipartFileのふるまい定義（バリデーションチェックを通すのに必要な挙動）
        MockMultipartFile mockMultiPart = Mockito.mock(MockMultipartFile.class);
        doReturn("image").when(mockMultiPart).getName();
        doReturn("image.jpg").when(mockMultiPart).getOriginalFilename();

        try (MockedStatic<TempFileUtil> mockedTempFileUtil = mockStatic(TempFileUtil.class)) {

            // モック化したTempFileUtil.transferToのふるまい設定
            Path path = Path.of(URI.create("file:/image.jpg"));
            mockedTempFileUtil.when(() -> TempFileUtil.transferTo(any())).thenReturn(path);

            // Mock商品カテゴリリポジトリからスローされる例外
            ApplicationException exception = new ApplicationException(ErrorCode.ITEM_CATEGORY_NOT_FOUND, new String[] {"99"});

            // MockServiceの挙動を設定
            doThrow(exception).when(mockService).createOne(any(), any());

            // コントローラが返す想定のJSON
            String expectedResponse = """
                    {
                      "errorCode": "bs.error.business.ItemCategoryNotFound",
                      "message": "商品カテゴリが見つかりませんでした。(商品カテゴリID=99)",
                      "detail": null
                    }
                    """;

            // リクエストの実行、実行結果に対する検証
            mockMvc.perform(
                    multipart("/items")
                            .file(mockMultiPart)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .param("id", "A0001")
                            .param("name", "商品１")
                            .param("nameKana", "ショウヒンイチ")
                            .param("itemCategoryId", "99")
                            .param("makerId", "M0001")
                            .param("startDate", "2021-03-01")
                            .param("endDate", "2021-03-31")
                            .param("price", "1100")
                            .param("description", "商品説明１")
                            .param("version", "1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().doesNotExist("location"))
                    .andExpect(content().json(expectedResponse, JsonCompareMode.STRICT));


            // TempFileUtil.transferToの呼び出し検証
            mockedTempFileUtil.verify(() -> TempFileUtil.transferTo(mockMultiPart));

            // 想定したパラメータでMock Serviceを呼び出したことを検証。
            verify(this.mockService).createOne(argThat(condition -> {
                assertEquals("A0001", condition.getId());
                assertEquals("商品１", condition.getName());
                assertEquals("ショウヒンイチ", condition.getNameKana());
                assertEquals("99", condition.getItemCategory().getId());
                assertEquals("M0001", condition.getMaker().getId());
                assertEquals("2021-03-01", condition.getStartDate().toString());
                assertEquals("2021-03-31", condition.getEndDate().toString());
                assertEquals(1100, condition.getPrice());
                assertEquals("商品説明１", condition.getDescription());
                assertEquals(1, condition.getVersion());
                return true;

            }), eq(path));

            // TempFileUtil.deleteの呼び出し検証
            mockedTempFileUtil.verify(() -> TempFileUtil.delete(path));
        }
    }

    @Test
    @DisplayName("[createItem][異常系][一時ファイル操作時にエラー発生]")
    void testCreateItem4() throws Exception {

        // MockMultipartFileのふるまい定義（バリデーションチェックを通すのに必要な挙動）
        MockMultipartFile mockMultiPart = Mockito.mock(MockMultipartFile.class);
        doReturn("image").when(mockMultiPart).getName();
        doReturn("image.jpg").when(mockMultiPart).getOriginalFilename();

        try (MockedStatic<TempFileUtil> mockedTempFileUtil = mockStatic(TempFileUtil.class)) {

            // モック化したTempFileUtil.transferToのふるまい設定
            SystemException exception = new SystemException(ErrorCode.FILE_CREATION_FAILURE, new IOException("An error has occurred."));
            mockedTempFileUtil.when(() -> TempFileUtil.transferTo(any())).thenThrow(exception);


            // リクエストの実行、実行結果に対する検証
            mockMvc.perform(
                    multipart("/items")
                            .file(mockMultiPart)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .param("id", "A0001")
                            .param("name", "商品１")
                            .param("nameKana", "ショウヒンイチ")
                            .param("itemCategoryId", "99")
                            .param("makerId", "M0001")
                            .param("startDate", "2021-03-01")
                            .param("endDate", "2021-03-31")
                            .param("price", "1100")
                            .param("description", "商品説明１")
                            .param("version", "1"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(header().doesNotExist("location"));


            // TempFileUtil.transferToの呼び出し検証
            mockedTempFileUtil.verify(() -> TempFileUtil.transferTo(mockMultiPart));

            // Mock Serviceを呼び出していないことを検証。
            verifyNoInteractions(this.mockService);

            // TempFileUtil.deleteの呼び出し検証
            mockedTempFileUtil.verify(() -> TempFileUtil.delete(null));
        }
    }

    @Test
    @DisplayName("[updateItem][正常系][更新成功（画像なし）]")
    void testUpdateItem1() throws Exception {

        try (MockedStatic<TempFileUtil> mockedTempFileUtil = mockStatic(TempFileUtil.class)) {

            // MockServiceの挙動を設定
            doNothing().when(mockService).updateOne(any(), any());


            // リクエストの実行、実行結果に対する検証
            mockMvc.perform(
                    multipart(HttpMethod.PUT, "/items/A0001")
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .param("id", "A0001")
                            .param("name", "商品１")
                            .param("nameKana", "ショウヒンイチ")
                            .param("itemCategoryId", "01")
                            .param("makerId", "M0001")
                            .param("startDate", "2021-03-01")
                            .param("endDate", "2021-03-31")
                            .param("price", "1100")
                            .param("description", "商品説明１")
                            .param("version", "1"))
                    .andExpect(status().isNoContent());


            // 想定したパラメータでMock Serviceを呼び出したことを検証。
            verify(this.mockService).updateOne(argThat(condition -> {
                assertEquals("A0001", condition.getId());
                assertEquals("商品１", condition.getName());
                assertEquals("ショウヒンイチ", condition.getNameKana());
                assertEquals("01", condition.getItemCategory().getId());
                assertEquals("M0001", condition.getMaker().getId());
                assertEquals("2021-03-01", condition.getStartDate().toString());
                assertEquals("2021-03-31", condition.getEndDate().toString());
                assertEquals(1100, condition.getPrice());
                assertEquals("商品説明１", condition.getDescription());
                assertEquals(1, condition.getVersion());
                return true;

            }), eq(null));

            // TempFileUtilが呼び出されていないことの検証
            mockedTempFileUtil.verifyNoInteractions();
        }
    }

    @Test
    @DisplayName("[updateItem][正常系][更新成功（画像あり）]")
    void testUpdateItem2() throws Exception {

        // MockMultipartFileのふるまい定義（バリデーションチェックを通すのに必要な挙動）
        MockMultipartFile mockMultiPart = Mockito.mock(MockMultipartFile.class);
        doReturn("image").when(mockMultiPart).getName();
        doReturn("image.jpg").when(mockMultiPart).getOriginalFilename();

        try (MockedStatic<TempFileUtil> mockedTempFileUtil = mockStatic(TempFileUtil.class)) {

            // モック化したTempFileUtil.transferToのふるまい設定
            Path path = Path.of(URI.create("file:/image.jpg"));
            mockedTempFileUtil.when(() -> TempFileUtil.transferTo(any())).thenReturn(path);

            // MockServiceの挙動を設定
            doNothing().when(mockService).updateOne(any(), any());

            // リクエストの実行、実行結果に対する検証
            mockMvc.perform(
                    multipart(HttpMethod.PUT, "/items/A0001")
                            .file(mockMultiPart)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .param("id", "A0001")
                            .param("name", "商品１")
                            .param("nameKana", "ショウヒンイチ")
                            .param("itemCategoryId", "01")
                            .param("makerId", "M0001")
                            .param("startDate", "2021-03-01")
                            .param("endDate", "2021-03-31")
                            .param("price", "1100")
                            .param("description", "商品説明１")
                            .param("version", "1"))
                    .andExpect(status().isNoContent());


            // TempFileUtil.transferToの呼び出し検証
            mockedTempFileUtil.verify(() -> TempFileUtil.transferTo(mockMultiPart));

            // 想定したパラメータでMock Serviceを呼び出したことを検証。
            verify(this.mockService).updateOne(argThat(condition -> {
                assertEquals("A0001", condition.getId());
                assertEquals("商品１", condition.getName());
                assertEquals("ショウヒンイチ", condition.getNameKana());
                assertEquals("01", condition.getItemCategory().getId());
                assertEquals("M0001", condition.getMaker().getId());
                assertEquals("2021-03-01", condition.getStartDate().toString());
                assertEquals("2021-03-31", condition.getEndDate().toString());
                assertEquals(1100, condition.getPrice());
                assertEquals("商品説明１", condition.getDescription());
                assertEquals(1, condition.getVersion());
                return true;

            }), eq(path));

            // TempFileUtil.deleteの呼び出し検証
            mockedTempFileUtil.verify(() -> TempFileUtil.delete(path));
        }
    }

    @Test
    @DisplayName("[updateItem][異常系][指定されたバージョンの商品が存在しない]")
    void testUpdateItem3() throws Exception {

        try (MockedStatic<TempFileUtil> mockedTempFileUtil = mockStatic(TempFileUtil.class)) {

            // MockServiceの挙動を設定
            ConflictException exception = new ConflictException(ErrorCode.ITEM_WAS_UPDATED_BY_ANYONE, new String[] {"A0001"});
            doThrow(exception).when(mockService).updateOne(any(), any());

            // コントローラが返す想定のJSON
            String expectedResponse = """
                    {
                      "errorCode": "bs.error.business.ItemWasUpdatedByAnyone",
                      "message": "商品が他の人に更新されています。(商品ID=A0001)",
                      "detail": null
                    }
                    """;


            // リクエストの実行、実行結果に対する検証
            mockMvc.perform(
                    multipart(HttpMethod.PUT, "/items/A0001")
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .param("id", "A0001")
                            .param("name", "商品１")
                            .param("nameKana", "ショウヒンイチ")
                            .param("itemCategoryId", "01")
                            .param("makerId", "M0001")
                            .param("startDate", "2021-03-01")
                            .param("endDate", "2021-03-31")
                            .param("price", "1100")
                            .param("description", "商品説明１")
                            .param("version", "1"))
                    .andExpect(status().isConflict())
                    .andExpect(content().json(expectedResponse, JsonCompareMode.STRICT));


            // 想定したパラメータでMock Serviceを呼び出したことを検証。
            verify(this.mockService).updateOne(argThat(condition -> {
                assertEquals("A0001", condition.getId());
                assertEquals("商品１", condition.getName());
                assertEquals("ショウヒンイチ", condition.getNameKana());
                assertEquals("01", condition.getItemCategory().getId());
                assertEquals("M0001", condition.getMaker().getId());
                assertEquals("2021-03-01", condition.getStartDate().toString());
                assertEquals("2021-03-31", condition.getEndDate().toString());
                assertEquals(1100, condition.getPrice());
                assertEquals("商品説明１", condition.getDescription());
                assertEquals(1, condition.getVersion());
                return true;

            }), eq(null));

            // TempFileUtilが呼び出されていないことの検証
            mockedTempFileUtil.verifyNoInteractions();
        }
    }

    @Test
    @DisplayName("[updateItem][異常系][指定されたメーカーが存在しない]")
    void testUpdateItem4() throws Exception {

        try (MockedStatic<TempFileUtil> mockedTempFileUtil = mockStatic(TempFileUtil.class)) {

            // MockServiceの挙動を設定
            ApplicationException exception = new ApplicationException(ErrorCode.MAKER_NOT_FOUND, new String[] {"M0001"});
            doThrow(exception).when(mockService).updateOne(any(), any());

            // コントローラが返す想定のJSON
            String expectedResponse = """
                    {
                      "errorCode": "bs.error.business.MakerNotFound",
                      "message": "メーカーが見つかりませんでした。(メーカーID=M0001)",
                      "detail": null
                    }
                    """;


            // リクエストの実行、実行結果に対する検証
            mockMvc.perform(
                    multipart(HttpMethod.PUT, "/items/A0001")
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .param("id", "A0001")
                            .param("name", "商品１")
                            .param("nameKana", "ショウヒンイチ")
                            .param("itemCategoryId", "01")
                            .param("makerId", "M0001")
                            .param("startDate", "2021-03-01")
                            .param("endDate", "2021-03-31")
                            .param("price", "1100")
                            .param("description", "商品説明１")
                            .param("version", "1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().json(expectedResponse, JsonCompareMode.STRICT));


            // 想定したパラメータでMock Serviceを呼び出したことを検証。
            verify(this.mockService).updateOne(argThat(condition -> {
                assertEquals("A0001", condition.getId());
                assertEquals("商品１", condition.getName());
                assertEquals("ショウヒンイチ", condition.getNameKana());
                assertEquals("01", condition.getItemCategory().getId());
                assertEquals("M0001", condition.getMaker().getId());
                assertEquals("2021-03-01", condition.getStartDate().toString());
                assertEquals("2021-03-31", condition.getEndDate().toString());
                assertEquals(1100, condition.getPrice());
                assertEquals("商品説明１", condition.getDescription());
                assertEquals(1, condition.getVersion());
                return true;

            }), eq(null));

            // TempFileUtilが呼び出されていないことの検証
            mockedTempFileUtil.verifyNoInteractions();
        }
    }

    @Test
    @DisplayName("[updateItem][異常系][パスのIDとリクエストボディのIDが不一致]")
    void testUpdateItem6() throws Exception {

        try (MockedStatic<TempFileUtil> mockedTempFileUtil = mockStatic(TempFileUtil.class)) {

            // コントローラが返す想定のJSON
            String expectedResponse = """
                    {
                      "errorCode": "bs.error.business.InconsistentBodyWithPathVariable",
                      "message": "ボディの値がパス変数値と矛盾しています(項目=id)",
                      "detail": null
                    }
                    """;


            // リクエストの実行、実行結果に対する検証
            mockMvc.perform(
                    multipart(HttpMethod.PUT, "/items/A0001")
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .param("id", "B0001")
                            .param("name", "商品１")
                            .param("nameKana", "ショウヒンイチ")
                            .param("itemCategoryId", "01")
                            .param("makerId", "M0001")
                            .param("startDate", "2021-03-01")
                            .param("endDate", "2021-03-31")
                            .param("price", "1100")
                            .param("description", "商品説明１")
                            .param("version", "1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().json(expectedResponse, JsonCompareMode.STRICT));


            // Serviceが呼び出されていないことの検証
            verifyNoInteractions(this.mockService);

            // TempFileUtilが呼び出されていないことの検証
            mockedTempFileUtil.verifyNoInteractions();
        }
    }

    @Test
    @DisplayName("[updateItem][異常系][一時ファイル操作時にエラー発生]")
    void testUpdateItem7() throws Exception {

        // MockMultipartFileのふるまい定義（バリデーションチェックを通すのに必要な挙動）
        MockMultipartFile mockMultiPart = Mockito.mock(MockMultipartFile.class);
        doReturn("image").when(mockMultiPart).getName();
        doReturn("image.jpg").when(mockMultiPart).getOriginalFilename();

        try (MockedStatic<TempFileUtil> mockedTempFileUtil = mockStatic(TempFileUtil.class)) {

            // モック化したTempFileUtil.transferToのふるまい設定
            SystemException exception = new SystemException(ErrorCode.FILE_CREATION_FAILURE, new IOException("An error has occurred."));
            mockedTempFileUtil.when(() -> TempFileUtil.transferTo(any())).thenThrow(exception);


            // リクエストの実行、実行結果に対する検証
            mockMvc.perform(
                    multipart(HttpMethod.PUT, "/items/A0001")
                            .file(mockMultiPart)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .param("id", "A0001")
                            .param("name", "商品１")
                            .param("nameKana", "ショウヒンイチ")
                            .param("itemCategoryId", "99")
                            .param("makerId", "M0001")
                            .param("startDate", "2021-03-01")
                            .param("endDate", "2021-03-31")
                            .param("price", "1100")
                            .param("description", "商品説明１")
                            .param("version", "1"))
                    .andExpect(status().isInternalServerError());


            // TempFileUtil.transferToの呼び出し検証
            mockedTempFileUtil.verify(() -> TempFileUtil.transferTo(mockMultiPart));

            // Mock Serviceを呼び出していないことを検証。
            verifyNoInteractions(this.mockService);

            // TempFileUtil.deleteの呼び出し検証
            mockedTempFileUtil.verify(() -> TempFileUtil.delete(null));
        }
    }

    @Test
    @DisplayName("[deleteItem][正常系][削除成功]")
    void testDeleteItem1() throws Exception {

        // MockServiceの挙動を設定
        doNothing().when(mockService).deleteOne(any(), anyInt());

        // リクエストBody（JSON）
        String requestJson = """
                {
                    "id": "A0001",
                    "version": 1
                }
                """;


        // リクエストの実行、実行結果に対する検証
        mockMvc.perform(
                post("/items/A0001/delete")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNoContent());


        // 想定したパラメータでMock Serviceを呼び出したことを検証。
        verify(this.mockService).deleteOne("A0001", 1);
    }

    @Test
    @DisplayName("[deleteItem][異常系][指定されたバージョンの商品が存在しない]")
    void testDeleteItem2() throws Exception {

        // MockServiceの挙動を設定
        ConflictException exception = new ConflictException(ErrorCode.ITEM_WAS_UPDATED_BY_ANYONE, new String[] {"A0001"});
        doThrow(exception).when(mockService).deleteOne(any(), anyInt());

        // コントローラが返す想定のJSON
        String expectedResponse = """
                {
                  "errorCode": "bs.error.business.ItemWasUpdatedByAnyone",
                  "message": "商品が他の人に更新されています。(商品ID=A0001)",
                  "detail": null
                }
                """;

        // リクエストBody（JSON）
        String requestJson = """
                {
                    "id": "A0001",
                    "version": 1
                }
                """;


        // リクエストの実行、実行結果に対する検証
        mockMvc.perform(
                post("/items/A0001/delete")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(content().json(expectedResponse, JsonCompareMode.STRICT));


        // 想定したパラメータでMock Serviceを呼び出したことを検証。
        verify(this.mockService).deleteOne("A0001", 1);
    }

    @Test
    @DisplayName("[deleteItem][異常系][パスのIDとリクエストボディのIDが不一致]")
    void testDeleteItem3() throws Exception {

        // コントローラが返す想定のJSON
        String expectedResponse = """
                {
                  "errorCode": "bs.error.business.InconsistentBodyWithPathVariable",
                  "message": "ボディの値がパス変数値と矛盾しています(項目=id)",
                  "detail": null
                }
                """;

        // リクエストBody（JSON）
        String requestJson = """
                {
                    "id": "A0001",
                    "version": 1
                }
                """;


        // リクエストの実行、実行結果に対する検証
        mockMvc.perform(
                post("/items/B0001/delete")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(expectedResponse, JsonCompareMode.STRICT));


        // Mock Serviceを呼び出していないことを検証。
        verifyNoInteractions(this.mockService);
    }

    @Test
    @DisplayName("[getItemImage][正常系][取得成功]")
    void testGetItemImage1() throws Exception {

        // 期待値ファイルを作成
        Path imagePath = Files.createTempFile("foodshop-test-image", ".jpg");

        try {
            // 期待値ファイルにデータを書き込み
            Files.write(imagePath, new byte[] {0, 0, 0, 0, 0});

            // MockServiceの挙動を設定
            doReturn(imagePath).when(mockService).getImage(any());


            // リクエストの実行、実行結果に対する検証
            mockMvc.perform(
                    get("/items/A0001/image"))
                    .andExpect(status().isOk())
                    .andExpect(content().bytes(new byte[] {0, 0, 0, 0, 0}));


            // 想定したリクエスト内容でMock Serviceを呼び出したことを検証
            verify(this.mockService).getImage("A0001");

        } finally {
            Files.delete(imagePath);
        }
    }

    @Test
    @DisplayName("[getItemImage][異常系][指定された商品画像が存在しない]")
    void testGetItemImageItem2() throws Exception {
        
        // Mockのふるまい定義
        ResourceNotFoundException exception = new ResourceNotFoundException(ErrorCode.ITEM_IMAGE_NOT_FOUND, new String[] {"A0001"});
        doThrow(exception).when(mockService).getImage(any());

        // コントローラが返す想定のJSON
        String expectedResponse = """
                {
                  "errorCode": "bs.error.business.ItemImageNotFound",
                  "message": "商品画像が見つかりませんでした。(商品ID=A0001)",
                  "detail": null
                }
                """;
        
        
        // リクエストの実行、実行結果に対する検証
        mockMvc.perform(
                get("/items/A0001/image"))
                .andExpect(status().isNotFound())
                .andExpect(content().json(expectedResponse, JsonCompareMode.STRICT));


        // 想定したリクエスト内容でMock Serviceを呼び出したことを検証
        verify(this.mockService).getImage("A0001");
    }

}
