package jp.co.nekonet.foodshop.bs.item.repository.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import jp.co.nekonet.foodshop.bs.common.ErrorCode;
import jp.co.nekonet.foodshop.bs.common.file.TempFileUtil;
import jp.co.nekonet.foodshop.bs.item.model.Item;
import jp.co.nekonet.foodshop.bs.item.model.ItemCategory;
import jp.co.nekonet.foodshop.bs.item.model.ItemSearchCondition;
import jp.co.nekonet.foodshop.bs.item.repository.ItemImageRepository;
import jp.co.nekonet.foodshop.bs.maker.model.Maker;
import jp.co.nekonet.springer.exceptions.ConflictException;
import jp.co.nekonet.springer.exceptions.ResourceNotFoundException;
import jp.co.nekonet.springer.exceptions.SystemException;

/**
 * ItemRepositoryImplのテストクラス
 * 
 * @author YAMATO SYSTEM DEVELOPMENT
 */
@SpringBootTest
@Transactional
@Sql(scripts = {"classpath:data/common/delete-all.sql", "classpath:data/item/ItemRepositoryImplTest.sql"})
class ItemRepositoryImplTest {

    /**
     * 商品画像リポジトリモック
     */
    @MockitoBean
    private ItemImageRepository itemImageRepository;

    /** テスト対象リポジトリ */
    @Autowired
    private ItemRepositoryImpl target;

    /** DB検証用 */
    @Autowired
    private JdbcTemplate jdbctemplate;
    
    /** テスト用商品 */
    private Item item;
    
    @BeforeEach
    void setup() {
        item = new Item();
        item.setId("T0001");
        item.setName("商品１");
        item.setNameKana("ショウヒンイチ");
        ItemCategory itemCategory = new ItemCategory();
        itemCategory.setId("01");
        item.setItemCategory(itemCategory);
        Maker maker1 = new Maker();
        maker1.setId("M0001");
        item.setMaker(maker1);
        item.setStartDate(LocalDate.parse("2021-03-01"));
        item.setEndDate(LocalDate.parse("2021-03-31"));
        item.setPrice(1000);
        item.setDescription("商品説明１");
        item.setVersion(1);
    }

    @Test
    @DisplayName("[getList][正常系][商品3件取得、ID低い順]")
    void testGetList1() {

        // 検索条件
        ItemSearchCondition condition = new ItemSearchCondition();


        // 検索を実施
        List<Item> result = target.getList(condition);


        // 結果件数の検証
        assertEquals(3, result.size());

        // レコードごとに内容を検証
        assertEquals("A0001", result.get(0).getId());
        assertEquals("テストスナックA", result.get(0).getName());
        assertEquals("テストスナックエー", result.get(0).getNameKana());
        assertEquals("01", result.get(0).getItemCategory().getId());
        assertEquals("カテゴリ1", result.get(0).getItemCategory().getName());
        assertNull(result.get(0).getItemCategory().getCreatedAt());
        assertNull(result.get(0).getItemCategory().getLastModifiedAt());
        assertEquals("M0001", result.get(0).getMaker().getId());
        assertEquals("テスト食品", result.get(0).getMaker().getName());
        assertNull(result.get(0).getMaker().getCreatedAt());
        assertNull(result.get(0).getMaker().getLastModifiedAt());
        assertEquals(LocalDate.parse("2019-01-01"), result.get(0).getStartDate());
        assertEquals(LocalDate.parse("2020-01-02"), result.get(0).getEndDate());
        assertEquals(129, result.get(0).getPrice());
        assertEquals("商品説明１", result.get(0).getDescription());
        assertEquals("4.00", result.get(0).getReviewAverageScore());
        assertEquals(1, result.get(0).getVersion());
        assertEquals(OffsetDateTime.parse("2019-09-12T15:53:00.000+09:00"), result.get(0).getCreatedAt());
        assertEquals(OffsetDateTime.parse("2020-01-03T03:15:00.000+09:00"), result.get(0).getLastModifiedAt());

        assertEquals("B0001", result.get(1).getId());
        assertEquals("テスト飲料A", result.get(1).getName());
        assertEquals("テストインリョウエー", result.get(1).getNameKana());
        assertEquals("02", result.get(1).getItemCategory().getId());
        assertEquals("カテゴリ2", result.get(1).getItemCategory().getName());
        assertNull(result.get(1).getItemCategory().getCreatedAt());
        assertNull(result.get(1).getItemCategory().getLastModifiedAt());
        assertEquals("M0002", result.get(1).getMaker().getId());
        assertEquals("テスト水産", result.get(1).getMaker().getName());
        assertNull(result.get(1).getMaker().getCreatedAt());
        assertNull(result.get(1).getMaker().getLastModifiedAt());
        assertEquals(LocalDate.parse("2019-01-02"), result.get(1).getStartDate());
        assertNull(result.get(1).getEndDate());
        assertEquals(98, result.get(1).getPrice());
        assertEquals("商品説明２", result.get(1).getDescription());
        assertEquals("3.50", result.get(1).getReviewAverageScore());
        assertEquals(2, result.get(1).getVersion());
        assertEquals(OffsetDateTime.parse("2019-09-13T12:00:00.000+09:00"), result.get(1).getCreatedAt());
        assertEquals(OffsetDateTime.parse("2019-09-14T00:00:00.000+09:00"), result.get(1).getLastModifiedAt());

        assertEquals("C0001", result.get(2).getId());
        assertEquals("テストアイスA", result.get(2).getName());
        assertEquals("テストアイスエー", result.get(2).getNameKana());
        assertEquals("03", result.get(2).getItemCategory().getId());
        assertEquals("カテゴリ3", result.get(2).getItemCategory().getName());
        assertNull(result.get(2).getItemCategory().getCreatedAt());
        assertNull(result.get(2).getItemCategory().getLastModifiedAt());
        assertEquals("M0003", result.get(2).getMaker().getId());
        assertEquals("テストフーズ", result.get(2).getMaker().getName());
        assertNull(result.get(2).getMaker().getCreatedAt());
        assertNull(result.get(2).getMaker().getLastModifiedAt());
        assertEquals(LocalDate.parse("2019-01-03"), result.get(2).getStartDate());
        assertNull(result.get(2).getEndDate());
        assertEquals(180, result.get(2).getPrice());
        assertEquals("商品説明３", result.get(2).getDescription());
        assertNull(result.get(2).getReviewAverageScore());
        assertEquals(3, result.get(2).getVersion());
        assertEquals(OffsetDateTime.parse("2019-09-14T12:00:00.000+09:00"), result.get(2).getCreatedAt());
        assertEquals(OffsetDateTime.parse("2020-01-02T00:00:00.000+09:00"), result.get(2).getLastModifiedAt());
    }

    @ParameterizedTest
    @CsvSource({"12, A0001, B0001, C0001", "11, C0001, B0001, A0001", "22, C0001, A0001, B0001", "21, B0001, A0001, C0001"})
    @DisplayName("[getList][正常系][商品3件取得、ソート順テスト]")
    void testGetList2(String order, String id1st, String id2nd, String id3rd) {

        // 検索条件
        ItemSearchCondition condition = new ItemSearchCondition();
        condition.setOrder(order);

        // 検索を実施
        List<Item> result = target.getList(condition);

        // 結果件数の検証
        assertEquals(3, result.size());

        // レコードごとに内容を検証（オーダー順の検証ケースのため商品IDのみ検証対象とする。
        assertEquals(id1st, result.get(0).getId());
        assertEquals(id2nd, result.get(1).getId());
        assertEquals(id3rd, result.get(2).getId());
    }

    @Test
    @DisplayName("[getList][正常系][商品1件取得]")
    void testGetList3() {

        // 検索条件
        ItemSearchCondition condition = new ItemSearchCondition();
        condition.setId("C0001");
        condition.setItemCategoryId("03");
        condition.setName("テストアイスA");
        condition.setNameKana("テストアイスエー");
        condition.setOrder("12");


        // 検索を実施
        List<Item> result = target.getList(condition);


        // 結果件数の検証
        assertEquals(1, result.size());

        // レコードごとに内容を検証
        assertEquals("C0001", result.get(0).getId());
        assertEquals("テストアイスA", result.get(0).getName());
        assertEquals("テストアイスエー", result.get(0).getNameKana());
        assertEquals("03", result.get(0).getItemCategory().getId());
        assertEquals("カテゴリ3", result.get(0).getItemCategory().getName());
        assertNull(result.get(0).getItemCategory().getCreatedAt());
        assertNull(result.get(0).getItemCategory().getLastModifiedAt());
        assertEquals("M0003", result.get(0).getMaker().getId());
        assertEquals("テストフーズ", result.get(0).getMaker().getName());
        assertNull(result.get(0).getMaker().getCreatedAt());
        assertNull(result.get(0).getMaker().getLastModifiedAt());
        assertEquals(LocalDate.parse("2019-01-03"), result.get(0).getStartDate());
        assertNull(result.get(0).getEndDate());
        assertEquals(180, result.get(0).getPrice());
        assertEquals("商品説明３", result.get(0).getDescription());
        assertNull(result.get(0).getReviewAverageScore());
        assertEquals(3, result.get(0).getVersion());
        assertEquals(OffsetDateTime.parse("2019-09-14T12:00:00.000+09:00"), result.get(0).getCreatedAt());
        assertEquals(OffsetDateTime.parse("2020-01-02T00:00:00.000+09:00"), result.get(0).getLastModifiedAt());
    }

    @Test
    @DisplayName("[getList][正常系][該当商品なし]")
    void testGetList4() {

        // 検索条件
        ItemSearchCondition condition = new ItemSearchCondition();
        condition.setId("X0001");
        condition.setOrder("11");

        // 検索を実施
        List<Item> result = target.getList(condition);

        // 結果件数の検証
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("[getListCsv][正常系][パス取得成功]")
    void testGetListCsv1() throws Exception {

        // 検索条件
        ItemSearchCondition condition = new ItemSearchCondition();


        // 検索を実施
        Path result = target.getListCsv(condition);


        // ファイルが存在することの検証
        assertTrue(Files.exists(result));

        List<String> lines = Files.readAllLines(result);

        // 結果件数の検証
        assertEquals(4, lines.size());

        // レコード内容の検証
        assertEquals("商品ID,商品名,商品名カナ,商品カテゴリID,商品カテゴリ名,メーカーID,メーカー名,発売日,販売終了日,価格,説明,レビュー平均得点", lines.get(0));
        assertEquals("A0001,テストスナックA,テストスナックエー,01,カテゴリ1,M0001,テスト食品,2019-01-01,2020-01-02,129,商品説明１,4.00", lines.get(1));
        assertEquals("B0001,テスト飲料A,テストインリョウエー,02,カテゴリ2,M0002,テスト水産,2019-01-02,,98,商品説明２,3.50", lines.get(2));
        assertEquals("C0001,テストアイスA,テストアイスエー,03,カテゴリ3,M0003,テストフーズ,2019-01-03,,180,商品説明３,", lines.get(3));
    }

    @Test
    @DisplayName("[getListCsv][異常系][一時ファイル操作時にエラー発生]")
    void testGetListCsv2() throws Exception {
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class);
                MockedStatic<TempFileUtil> mockedTempFileUtil = mockStatic(TempFileUtil.class)) {

            // モック化したFiles.newBufferedWriterのふるまい設定
            IOException e = new IOException("Failed to create writer.");
            mockedFiles.when(() -> Files.newBufferedWriter(any())).thenThrow(e);

            // モック化したTempFileUtil.createのふるまい設定
            Path path = Path.of(URI.create("file:/dummy.csv"));
            mockedTempFileUtil.when(() -> TempFileUtil.create()).thenReturn(path);

            // 検索条件
            ItemSearchCondition condition = new ItemSearchCondition();


            // 検索を実施
            SystemException result = assertThrows(SystemException.class, () -> target.getListCsv(condition));

            
            // モックの呼び出し確認
            mockedTempFileUtil.verify(() -> TempFileUtil.create());
            mockedFiles.verify(() -> Files.newBufferedWriter(path));
            
            // 例外内容の検証
            assertEquals(ErrorCode.FILE_CREATION_FAILURE, result.getErrorCode());
            assertEquals(e, result.getCause());
        }
    }

    @Test
    @DisplayName("[createOne][正常系][登録成功]")
    void testCreateOne1() throws Exception {

        // Mockのふるまい定義
        doNothing().when(itemImageRepository).createOne(any(), any());

        // 商品画像パス
        Path path = Paths.get("dummy/test_image1.jpg");

        
        // 登録実行
        target.createOne(item, path);
        

        // モックの呼び出し確認
        verify(itemImageRepository).createOne("T0001", path);

        // 登録した商品をselectしてレコード内容を検証
        Map<String, Object> result = jdbctemplate.queryForMap("select * from m_item where id = 'T0001';");
        assertEquals("T0001", result.get("id"));
        assertEquals("商品１", result.get("name"));
        assertEquals("ショウヒンイチ", result.get("name_kana"));
        assertEquals("01", result.get("item_category_id"));
        assertEquals("M0001", result.get("maker_id"));
        assertEquals("2021-03-01", result.get("start_date").toString());
        assertEquals("2021-03-31", result.get("end_date").toString());
        assertEquals("1000", result.get("price").toString());
        assertEquals("商品説明１", result.get("description"));
        assertEquals(1, result.get("version"));
        assertEquals(LocalDate.now(), ((OffsetDateTime) result.get("created_at")).toLocalDate());
        assertEquals(LocalDate.now(), ((OffsetDateTime) result.get("last_modified_at")).toLocalDate());
    }

    @Test
    @DisplayName("[createOne][異常系][指定された商品がすでに存在する]")
    void testCreateOne2() throws Exception {

        // 登録商品
        item.setId("A0001");
        
        // 商品画像パス
        Path path = Paths.get("dummy/test_image1.jpg");

        
        // 登録実行
        ConflictException result = assertThrows(ConflictException.class, () -> target.createOne(item, path));


        // モックを呼び出していないことの検証
        verifyNoInteractions(itemImageRepository);

        // 例外内容の検証
        assertEquals(ErrorCode.ITEM_ALREADY_EXISTS, result.getErrorCode());
        assertTrue(Arrays.equals(new String[] {"A0001"}, result.getParams()));
    }
    
    @Test
    @DisplayName("[createOne][正常系][指定された商品画像がすでに存在する]")
    void testCreateOne3() throws Exception {

        // Mock商品画像リポジトリからスローされる例外
        ConflictException exception = new ConflictException(ErrorCode.ITEM_IMAGE_ALREADY_EXISTS, new String[] {"A0001"});

        // Mockのふるまい定義
        doThrow(exception).when(itemImageRepository).createOne(any(), any());
        doNothing().when(itemImageRepository).updateOne(any(), any());

        // 登録商品
        item.setId("X0001");
        
        // 商品画像パス
        Path path = Paths.get("dummy/test_image1.jpg");

        
        // 登録実行
        target.createOne(item, path);


        // モックの呼び出し確認
        verify(itemImageRepository).createOne("X0001", path);
        verify(itemImageRepository).updateOne("X0001", path);
        
        // 登録した商品をselectしてレコード内容を検証
        Map<String, Object> result = jdbctemplate.queryForMap("select * from m_item where id = 'X0001';");
        assertEquals("X0001", result.get("id"));
        assertEquals("商品１", result.get("name"));
        assertEquals("ショウヒンイチ", result.get("name_kana"));
        assertEquals("01", result.get("item_category_id"));
        assertEquals("M0001", result.get("maker_id"));
        assertEquals("2021-03-01", result.get("start_date").toString());
        assertEquals("2021-03-31", result.get("end_date").toString());
        assertEquals("1000", result.get("price").toString());
        assertEquals("商品説明１", result.get("description"));
        assertEquals(1, result.get("version"));
        assertEquals(LocalDate.now(), ((OffsetDateTime) result.get("created_at")).toLocalDate());
        assertEquals(LocalDate.now(), ((OffsetDateTime) result.get("last_modified_at")).toLocalDate());
    }
    
    @Test
    @DisplayName("[createOne][異常系][商品画像が競合し、画像の更新で競合が発生した場合]")
    void testCreateOne4() throws Exception {

        // Mock商品画像リポジトリからスローされる例外
        ConflictException conflictEx = new ConflictException(ErrorCode.ITEM_IMAGE_ALREADY_EXISTS, new String[] {"A0001"});
        ResourceNotFoundException notFoundEx = new ResourceNotFoundException(ErrorCode.ITEM_IMAGE_NOT_FOUND, new String[] {"A0001"});

        // Mockのふるまい定義
        doThrow(conflictEx).when(itemImageRepository).createOne(any(), any());
        doThrow(notFoundEx).when(itemImageRepository).updateOne(any(), any());

        // 登録商品
        item.setId("Z0001");

        // 商品画像パス
        Path path = Paths.get("dummy/test_image1.jpg");

        
        // 登録実行
        SystemException result = assertThrows(SystemException.class, () -> target.createOne(item, path));


        // モックの呼び出し確認
        verify(itemImageRepository).createOne("Z0001", path);
        verify(itemImageRepository).updateOne("Z0001", path);

        // 例外内容の検証
        assertEquals(ErrorCode.UNEXPECTED_ERROR, result.getErrorCode());
        assertEquals(conflictEx, result.getCause());
        
        // テストメソッドが終わるまでトランザクション完了しないのでロールバック検証はUT対象外。
    }    

    @Test
    @DisplayName("[updateOne][正常系][更新成功（画像なし）]")
    void testUpdateOne1() throws Exception {
        
        // 更新商品
        item.setId("A0001");

        
        // 更新実行
        target.updateOne(item, null);
        

        // 商品画像リポジトリを呼び出していないことを検証
        verifyNoInteractions(itemImageRepository);

        // 更新した商品をselectしてレコード内容を検証
        Map<String, Object> result = jdbctemplate.queryForMap("select * from m_item where id = 'A0001';");
        assertEquals("A0001", result.get("id"));
        assertEquals("商品１", result.get("name"));
        assertEquals("ショウヒンイチ", result.get("name_kana"));
        assertEquals("01", result.get("item_category_id"));
        assertEquals("M0001", result.get("maker_id"));
        assertEquals("2021-03-01", result.get("start_date").toString());
        assertEquals("2021-03-31", result.get("end_date").toString());
        assertEquals("1000", result.get("price").toString());
        assertEquals("商品説明１", result.get("description"));
        assertEquals(2, result.get("version"));
        assertEquals("2019-09-12T15:53+09:00", result.get("created_at").toString());
        assertEquals(LocalDate.now(), ((OffsetDateTime) result.get("last_modified_at")).toLocalDate());
    }

    @Test
    @DisplayName("[updateOne][正常系][更新成功（画像あり）]")
    void testUpdateOne2() throws Exception {

        // Mockのふるまい定義
        doNothing().when(itemImageRepository).updateOne(any(), any());

        // 更新商品
        item.setId("A0001");
        
        // 商品画像パス
        Path path = Paths.get("dummy/test_image1.jpg");

                
        // 更新実行
        target.updateOne(item, path);
        

        // モックの呼び出し確認
        verify(itemImageRepository).updateOne("A0001", path);

        // 更新した商品をselectしてレコード内容を検証
        Map<String, Object> result = jdbctemplate.queryForMap("select * from m_item where id = 'A0001';");
        assertEquals("A0001", result.get("id"));
        assertEquals("商品１", result.get("name"));
        assertEquals("ショウヒンイチ", result.get("name_kana"));
        assertEquals("01", result.get("item_category_id"));
        assertEquals("M0001", result.get("maker_id"));
        assertEquals("2021-03-01", result.get("start_date").toString());
        assertEquals("2021-03-31", result.get("end_date").toString());
        assertEquals("1000", result.get("price").toString());
        assertEquals("商品説明１", result.get("description"));
        assertEquals(2, result.get("version"));
        assertEquals("2019-09-12T15:53+09:00", result.get("created_at").toString());
        assertEquals(LocalDate.now(), ((OffsetDateTime) result.get("last_modified_at")).toLocalDate());
 
    }

    @Test
    @DisplayName("[updateOne][異常系][更新結果件数が１ではない]")
    void testUpdateOne3() throws Exception {

        // 更新商品
        item.setId("A0001");
        item.setVersion(2);
        

        // 更新実行
        ConflictException result = assertThrows(ConflictException.class, () -> target.updateOne(item, null));


        // 商品画像リポジトリを呼び出していないことを検証
        verifyNoInteractions(itemImageRepository);

        // 例外内容の検証
        assertEquals(ErrorCode.ITEM_WAS_UPDATED_BY_ANYONE, result.getErrorCode());
        assertTrue(Arrays.equals(new String[] {"A0001"}, result.getParams()));
    }

    @Test
    @DisplayName("[updateOne][正常系][指定された商品画像が存在しない]")
    void testUpdateOne4() throws Exception {

        // Mock商品カテゴリリポジトリからスローされる例外
        ResourceNotFoundException exception = new ResourceNotFoundException(ErrorCode.ITEM_IMAGE_NOT_FOUND, new String[] {"A0001"});

        // Mockのふるまい定義
        doThrow(exception).when(itemImageRepository).updateOne(any(), any());
        doNothing().when(itemImageRepository).createOne(any(), any());

        // 更新商品
        item.setId("A0001");
        
        // 商品画像パス
        Path path = Paths.get("dummy/test_image1.jpg");

        
        // 更新実行
        target.updateOne(item, path);


        // モックの呼び出し確認
        verify(itemImageRepository).createOne("A0001", path);
        verify(itemImageRepository).updateOne("A0001", path);
        
        // 更新した商品をselectしてレコード内容を検証
        Map<String, Object> result = jdbctemplate.queryForMap("select * from m_item where id = 'A0001';");
        assertEquals("A0001", result.get("id"));
        assertEquals("商品１", result.get("name"));
        assertEquals("ショウヒンイチ", result.get("name_kana"));
        assertEquals("01", result.get("item_category_id"));
        assertEquals("M0001", result.get("maker_id"));
        assertEquals("2021-03-01", result.get("start_date").toString());
        assertEquals("2021-03-31", result.get("end_date").toString());
        assertEquals("1000", result.get("price").toString());
        assertEquals("商品説明１", result.get("description"));
        assertEquals(2, result.get("version"));
        assertEquals("2019-09-12T15:53+09:00", result.get("created_at").toString());
        assertEquals(LocalDate.now(), ((OffsetDateTime) result.get("last_modified_at")).toLocalDate());
    }

    @Test
    @DisplayName("[updateOne][異常系][商品画像が競合し、画像の更新で競合が発生した場合]")
    void testUpdateOne5() throws Exception {

        // Mock商品画像リポジトリからスローされる例外
        ConflictException conflictEx = new ConflictException(ErrorCode.ITEM_IMAGE_ALREADY_EXISTS, new String[] {"A0001"});
        ResourceNotFoundException notFoundEx = new ResourceNotFoundException(ErrorCode.ITEM_IMAGE_NOT_FOUND, new String[] {"A0001"});

        // Mockのふるまい定義
        doThrow(conflictEx).when(itemImageRepository).createOne(any(), any());
        doThrow(notFoundEx).when(itemImageRepository).updateOne(any(), any());

        // 更新商品
        item.setId("A0001");
        
        // 商品画像パス
        Path path = Paths.get("dummy/test_image1.jpg");

        
        // 更新実行
        SystemException result = assertThrows(SystemException.class, () -> target.updateOne(item, path));


        // モックの呼び出し確認
        verify(itemImageRepository).createOne("A0001", path);
        verify(itemImageRepository).updateOne("A0001", path);

        // 例外内容の検証
        assertEquals(ErrorCode.UNEXPECTED_ERROR, result.getErrorCode());
        assertEquals(notFoundEx, result.getCause());
    }

    @Test
    @DisplayName("[deleteOne][正常系][削除成功]")
    void testDeleteOne1() throws Exception {
        
        // 削除実行
        target.deleteOne("A0001", 1);
        
        
        // 削除結果の検証
        List<Map<String, Object>> record = jdbctemplate.queryForList("select * from m_item where id = 'A0001';");
        assertTrue(record.isEmpty());
    }
    
    @Test
    @DisplayName("[deleteOne][異常系][削除結果件数が１ではない]")
    void testDeleteOne2() throws Exception {

        // 削除実行
        ConflictException result = assertThrows(ConflictException.class, () -> target.deleteOne("A0001", 2));


        // 商品画像リポジトリを呼び出していないことを検証
        verifyNoInteractions(itemImageRepository);

        // 例外内容の検証
        assertEquals(ErrorCode.ITEM_WAS_UPDATED_BY_ANYONE, result.getErrorCode());
        assertTrue(Arrays.equals(new String[] {"A0001"}, result.getParams()));
    }
    
    @Test
    @DisplayName("[deleteOne][正常系][指定された商品画像が存在しない]")
    void testDeleteOne3() throws Exception {

        // Mock商品画像リポジトリからスローされる例外
        ResourceNotFoundException exceptipn = new ResourceNotFoundException(ErrorCode.ITEM_IMAGE_NOT_FOUND, new String[] {"A0001"});

        // Mockのふるまい定義
        doThrow(exceptipn).when(itemImageRepository).updateOne(any(), any());
        
        
        // 削除実行
        target.deleteOne("A0001", 1);


        // 商品画像リポジトリを呼び出していないことを検証
        verify(itemImageRepository).deleteOne("A0001");

        // 削除結果の検証
        List<Map<String, Object>> record = jdbctemplate.queryForList("select * from m_item where id = 'A0001';");
        assertTrue(record.isEmpty());
    }
}
