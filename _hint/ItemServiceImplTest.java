package jp.co.nekonet.foodshop.bs.item.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
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
import org.mockito.Mockito;
import jp.co.nekonet.foodshop.bs.common.ErrorCode;
import jp.co.nekonet.foodshop.bs.item.model.Item;
import jp.co.nekonet.foodshop.bs.item.model.ItemCategory;
import jp.co.nekonet.foodshop.bs.item.model.ItemSearchCondition;
import jp.co.nekonet.foodshop.bs.item.repository.ItemCategoryRepository;
import jp.co.nekonet.foodshop.bs.item.repository.ItemRepository;
import jp.co.nekonet.foodshop.bs.item.service.ItemService;
import jp.co.nekonet.foodshop.bs.maker.model.Maker;
import jp.co.nekonet.foodshop.bs.maker.repository.MakerRepository;
import jp.co.nekonet.foodshop.bs.review.repository.ItemReviewRepository;
import jp.co.nekonet.springer.exceptions.ApplicationException;
import jp.co.nekonet.springer.exceptions.ConflictException;
import jp.co.nekonet.springer.exceptions.ResourceNotFoundException;
import jp.co.nekonet.springer.exceptions.SystemException;

/**
 * ItemServiceImplのテストクラス
 * 
 * @author YAMATO SYSTEM DEVELOPMENT
 */
class ItemServiceImplTest {

    /** Mock商品リポジトリ */
    private ItemRepository itemRepository = Mockito.mock(ItemRepository.class);

    /** Mock商品レビューリポジトリ */
    private ItemReviewRepository itemReviewRepository = Mockito.mock(ItemReviewRepository.class);

    /** Mock商品カテゴリリポジトリ */
    private ItemCategoryRepository itemCategoryRepository = Mockito.mock(ItemCategoryRepository.class);

    /** Mockメーカーリポジトリ */
    private MakerRepository makerRepository = Mockito.mock(MakerRepository.class);

    /** 商品サービス */
    private ItemService target = new ItemServiceImpl(itemRepository, itemReviewRepository,
            itemCategoryRepository, makerRepository);

    /** テスト用商品１ */
    private Item item1;
    
    /** テスト用商品２ */
    private Item item2;
    
    /** テスト用商品３ */
    private Item item3;

    /**
     * 初期化処理を行います。
     */
    @BeforeEach
    void setUp() {
        // モックのふるまい初期化
        Mockito.reset(itemRepository, itemReviewRepository, itemCategoryRepository, makerRepository);

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
        item1.setPrice(1000);
        item1.setDescription("商品説明１");
        item1.setReviewAverageScore("3.10");
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
        item2.setPrice(1000);
        item2.setDescription("商品説明２");
        item2.setReviewAverageScore("3.20");
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
        item3.setMaker(maker1);
        item3.setStartDate(LocalDate.parse("2023-03-31"));
        item3.setEndDate(LocalDate.parse("2023-03-31"));
        item3.setPrice(1000);
        item3.setDescription("商品説明３");
        item3.setReviewAverageScore("3.30");
        item3.setVersion(1);
        item3.setCreatedAt(OffsetDateTime.parse("2023-04-01T10:15:30+09:00"));
        item3.setLastModifiedAt(OffsetDateTime.parse("2023-04-01T12:30:30+09:00"));
    }

    @Test
    @DisplayName("[getList][正常系][商品3件取得]")
    void testGetList1() {

        // Mockリポジトリの戻り値
        List<Item> mockReturn = Arrays.asList(item1, item2, item3);

        // Mockのふるまい定義
        doReturn(mockReturn).when(itemRepository).getList(any());
        doReturn(item1.getItemCategory(), item2.getItemCategory(), item3.getItemCategory())
                .when(itemCategoryRepository).getOne(any());

        // サービス引数の定義
        ItemSearchCondition condition = new ItemSearchCondition();
        condition.setName("商品");


        // サービス実行
        List<Item> result = target.getList(condition);


        // Mock商品リポジトリの呼び出し確認
        verify(itemRepository).getList(condition);

        // カテゴリリポジトリが呼び出されていないことの確認
        verifyNoInteractions(itemCategoryRepository);

        // 結果件数の検証
        assertEquals(3, result.size());

        // 商品ごとに内容を検証
        for (int i = 0; i < result.size(); i++) {
            assertEquals(mockReturn.get(i).getId(), result.get(i).getId());
            assertEquals(mockReturn.get(i).getName(), result.get(i).getName());
            assertEquals(mockReturn.get(i).getNameKana(), result.get(i).getNameKana());
            assertEquals(mockReturn.get(i).getItemCategory().getId(), result.get(i).getItemCategory().getId());
            assertEquals(mockReturn.get(i).getItemCategory().getName(), result.get(i).getItemCategory().getName());
            assertEquals(mockReturn.get(i).getItemCategory().getCreatedAt(), result.get(i).getItemCategory().getCreatedAt());
            assertEquals(mockReturn.get(i).getItemCategory().getLastModifiedAt(), result.get(i).getItemCategory().getLastModifiedAt());
            assertEquals(mockReturn.get(i).getMaker().getId(), result.get(i).getMaker().getId());
            assertEquals(mockReturn.get(i).getMaker().getName(), result.get(i).getMaker().getName());
            assertEquals(mockReturn.get(i).getMaker().getNameKana(), result.get(i).getMaker().getNameKana());
            assertEquals(mockReturn.get(i).getMaker().getCreatedAt(), result.get(i).getMaker().getCreatedAt());
            assertEquals(mockReturn.get(i).getMaker().getLastModifiedAt(), result.get(i).getMaker().getLastModifiedAt());
            assertEquals(mockReturn.get(i).getStartDate(), result.get(i).getStartDate());
            assertEquals(mockReturn.get(i).getEndDate(), result.get(i).getEndDate());
            assertEquals(mockReturn.get(i).getPrice(), result.get(i).getPrice());
            assertEquals(mockReturn.get(i).getDescription(), result.get(i).getDescription());
            assertEquals(mockReturn.get(i).getReviewAverageScore(), result.get(i).getReviewAverageScore());
            assertEquals(mockReturn.get(i).getVersion(), result.get(i).getVersion());
            assertEquals(mockReturn.get(i).getCreatedAt(), result.get(i).getCreatedAt());
            assertEquals(mockReturn.get(i).getLastModifiedAt(), result.get(i).getLastModifiedAt());
        }
    }

    @Test
    @DisplayName("[getList][異常系][カテゴリ存在チェックでエラー]")
    void testGetList2() {

        // Mock商品カテゴリリポジトリからスローされる例外
        ResourceNotFoundException exception = new ResourceNotFoundException(ErrorCode.ITEM_CATEGORY_NOT_FOUND, new String[] {"99"});

        // Mockのふるまい定義
        doThrow(exception).when(itemCategoryRepository).getOne(any());

        // サービス引数の定義
        ItemSearchCondition condition = new ItemSearchCondition();
        condition.setItemCategoryId("99");


        // サービス実行
        ApplicationException e = assertThrows(ApplicationException.class, () -> target.getList(condition));


        // Mock商品カテゴリリポジトリの呼び出し確認
        verify(itemCategoryRepository).getOne("99");
        // Mock商品リポジトリが呼び出されていないことの確認
        verifyNoInteractions(itemRepository);

        // メッセージコードとパラメータ―の検証
        assertEquals(ErrorCode.ITEM_CATEGORY_NOT_FOUND, e.getErrorCode());
        assertTrue(Arrays.equals(new String[] {"99"}, e.getParams()));
    }

    @Test
    @DisplayName("[getListCsv][正常系][パス取得成功]")
    void testGetListCsv1() throws Exception {

        Path mockReturnPath = null;

        try {
            // 期待値ファイルパス
            mockReturnPath = Files.createTempFile("testGetListCsv1", ".csv");

            // Mockリポジトリの戻り値
            List<Item> mockReturnList = Arrays.asList(item1, item2, item3);

            // Mockのふるまい定義
            doReturn(mockReturnPath).when(itemRepository).getListCsv(any());
            doReturn(mockReturnList.get(0).getItemCategory(),
                    mockReturnList.get(1).getItemCategory(),
                    mockReturnList.get(2).getItemCategory())
                            .when(itemCategoryRepository).getOne(any());

            // サービス引数の定義
            ItemSearchCondition condition = new ItemSearchCondition();
            condition.setName("商品");


            // サービス実行
            Path result = target.getListCsv(condition);


            // Mock商品リポジトリの呼び出し確認
            verify(itemRepository).getListCsv(condition);

            // カテゴリリポジトリが呼び出されていないことの確認
            verifyNoInteractions(itemCategoryRepository);

            // リポジトリが返したPathとサービス戻り値が一致することの確認
            assertEquals(mockReturnPath, result);

        } finally {
            if (mockReturnPath != null) {
                Files.delete(mockReturnPath);
            }
        } ;
    }

    @Test
    @DisplayName("[getListCsv][異常系][カテゴリ存在チェックでエラー]")
    void testGetListCsv2() throws Exception {

        // Mock商品カテゴリリポジトリからスローされる例外
        ResourceNotFoundException exception = new ResourceNotFoundException(ErrorCode.ITEM_CATEGORY_NOT_FOUND, new String[] {"99"});

        // Mockのふるまい定義
        doThrow(exception).when(itemCategoryRepository).getOne(any());

        // サービス引数の定義
        ItemSearchCondition condition = new ItemSearchCondition();
        condition.setItemCategoryId("99");


        // サービス実行
        ApplicationException e = assertThrows(ApplicationException.class, () -> target.getListCsv(condition));


        // Mock商品カテゴリリポジトリの呼び出し確認
        verify(itemCategoryRepository).getOne("99");
        // Mock商品リポジトリが呼び出されていないことの確認
        verifyNoInteractions(itemRepository);

        // メッセージコードとパラメータ―の検証
        assertEquals(ErrorCode.ITEM_CATEGORY_NOT_FOUND, e.getErrorCode());
        assertTrue(Arrays.equals(new String[] {"99"}, e.getParams()));
    }

    @Test
    @DisplayName("[getOne][正常系][商品取得成功]")
    void testGetOne1() throws Exception {

        // Mockリポジトリの戻り値
        List<Item> testItemList = Arrays.asList(item1);

        // Mockのふるまい定義
        doReturn(testItemList).when(itemRepository).getList(any());


        // サービス実行
        Item result = target.getOne("A0001");


        // Mock商品リポジトリの呼び出し確認
        verify(itemRepository).getList(argThat(condition -> {
            assertEquals("A0001", condition.getId());
            return true;
        }));

        // 商品情報の検証
        assertEquals(item1.getId(), result.getId());
        assertEquals(item1.getName(), result.getName());
        assertEquals(item1.getNameKana(), result.getNameKana());
        assertEquals(item1.getItemCategory().getId(), result.getItemCategory().getId());
        assertEquals(item1.getItemCategory().getName(), result.getItemCategory().getName());
        assertEquals(item1.getItemCategory().getCreatedAt(), result.getItemCategory().getCreatedAt());
        assertEquals(item1.getItemCategory().getLastModifiedAt(), result.getItemCategory().getLastModifiedAt());
        assertEquals(item1.getMaker().getId(), result.getMaker().getId());
        assertEquals(item1.getMaker().getName(), result.getMaker().getName());
        assertEquals(item1.getMaker().getNameKana(), result.getMaker().getNameKana());
        assertEquals(item1.getMaker().getCreatedAt(), result.getMaker().getCreatedAt());
        assertEquals(item1.getMaker().getLastModifiedAt(), result.getMaker().getLastModifiedAt());
        assertEquals(item1.getStartDate(), result.getStartDate());
        assertEquals(item1.getEndDate(), result.getEndDate());
        assertEquals(item1.getPrice(), result.getPrice());
        assertEquals(item1.getDescription(), result.getDescription());
        assertEquals(item1.getReviewAverageScore(), result.getReviewAverageScore());
        assertEquals(item1.getVersion(), result.getVersion());
        assertEquals(item1.getCreatedAt(), result.getCreatedAt());
        assertEquals(item1.getLastModifiedAt(), result.getLastModifiedAt());

    }

    @Test
    @DisplayName("[getOne][異常系][指定された商品が存在しない]")
    void testGetOne2() throws Exception {

        // Mockのふるまい定義
        doReturn(Collections.emptyList()).when(itemRepository).getList(any());


        // サービス実行
        ResourceNotFoundException result = assertThrows(ResourceNotFoundException.class, () -> target.getOne("X0001"));


        // Mock商品リポジトリの呼び出し確認
        verify(itemRepository).getList(argThat(condition -> {
            assertEquals("X0001", condition.getId());
            return true;
        }));

        // メッセージコードとパラメータ―の検証
        assertEquals(ErrorCode.ITEM_NOT_FOUND, result.getErrorCode());
        assertTrue(Arrays.equals(new String[] {"X0001"}, result.getParams()));
    }

    @Test
    @DisplayName("[getImage][正常系][商品画像取得成功]")
    void testGetImage1() throws Exception {

        Path mockReturn = null;

        try {
            // 期待値ファイルパス
            mockReturn = Files.createTempFile("testGetImage1", ".jpg");

            // Mockのふるまい定義
            doReturn(mockReturn).when(itemRepository).getImage(any());


            // サービス実行
            Path result = target.getImage("A0001");


            // Mock商品リポジトリの呼び出し確認
            verify(itemRepository).getImage("A0001");

            // リポジトリが返したPathとサービス戻り値が一致することの確認
            assertEquals(mockReturn, result);

        } finally {
            if (mockReturn != null) {
                Files.delete(mockReturn);
            }
        } ;
    }

    @Test
    @DisplayName("[getImage][異常系][指定された商品画像が存在しない]")
    void testGetImage2() throws Exception {

        // Mock商品カテゴリリポジトリからスローされる例外
        ResourceNotFoundException exception = new ResourceNotFoundException(ErrorCode.ITEM_IMAGE_NOT_FOUND, new String[] {"X0001"});

        // Mockのふるまい定義
        doThrow(exception).when(itemRepository).getImage(any());


        // サービス実行
        ApplicationException result = assertThrows(ApplicationException.class, () -> target.getImage("X0001"));


        // Mock商品リポジトリの呼び出し確認
        verify(itemRepository).getImage("X0001");

        // メッセージコードとパラメータ―の検証
        assertEquals(ErrorCode.ITEM_IMAGE_NOT_FOUND, result.getErrorCode());
        assertTrue(Arrays.equals(new String[] {"X0001"}, result.getParams()));
    }

    @Test
    @DisplayName("[createOne][正常系][商品登録成功]")
    void testCreateOne1() throws Exception {

        // Mockのふるまい定義
        doReturn(item1.getItemCategory()).when(itemCategoryRepository).getOne(any());
        doReturn(item1.getMaker()).when(makerRepository).getOne(any());
        doNothing().when(itemRepository).createOne(any(), any());
        doNothing().when(itemReviewRepository).createSummary(any());

        // サービス引数定義
        Path path = Path.of("temp");

        
        // サービス実行
        target.createOne(item1, path);


        // Mockカテゴリリポジトリの呼び出し確認
        verify(itemCategoryRepository).getOne("01");
        // Mockメーカーリポジトリの呼び出し確認
        verify(makerRepository).getOne("M0001");
        // Mock商品リポジトリの呼び出し確認
        verify(itemRepository).createOne(argThat(condition -> {
            assertEquals(item1.getId(), condition.getId());
            assertEquals(item1.getName(), condition.getName());
            assertEquals(item1.getNameKana(), condition.getNameKana());
            assertEquals(item1.getItemCategory().getId(), condition.getItemCategory().getId());
            assertEquals(item1.getItemCategory().getName(), condition.getItemCategory().getName());
            assertEquals(item1.getItemCategory().getCreatedAt(), condition.getItemCategory().getCreatedAt());
            assertEquals(item1.getItemCategory().getLastModifiedAt(), condition.getItemCategory().getLastModifiedAt());
            assertEquals(item1.getMaker().getId(), condition.getMaker().getId());
            assertEquals(item1.getMaker().getName(), condition.getMaker().getName());
            assertEquals(item1.getMaker().getNameKana(), condition.getMaker().getNameKana());
            assertEquals(item1.getMaker().getCreatedAt(), condition.getMaker().getCreatedAt());
            assertEquals(item1.getMaker().getLastModifiedAt(), condition.getMaker().getLastModifiedAt());
            assertEquals(item1.getStartDate(), condition.getStartDate());
            assertEquals(item1.getEndDate(), condition.getEndDate());
            assertEquals(item1.getPrice(), condition.getPrice());
            assertEquals(item1.getDescription(), condition.getDescription());
            assertEquals(item1.getReviewAverageScore(), condition.getReviewAverageScore());
            assertEquals(item1.getVersion(), condition.getVersion());
            assertEquals(item1.getCreatedAt(), condition.getCreatedAt());
            assertEquals(item1.getLastModifiedAt(), condition.getLastModifiedAt());
            return true;
        }), eq(path));
        
        // Mock商品レビューリポジトリの呼び出し確認
        verify(itemReviewRepository).createSummary("A0001");
    }

    @Test
    @DisplayName("[createOne][異常系][指定された商品カテゴリが存在しない]")
    void testCreateOne2() throws Exception {
        
        // Mock商品カテゴリリポジトリからスローされる例外
        ResourceNotFoundException exception = new ResourceNotFoundException(ErrorCode.ITEM_CATEGORY_NOT_FOUND, new String[] {"01"});

        // Mockのふるまい定義
        doThrow(exception).when(itemCategoryRepository).getOne(any());


        // サービス実行
        ApplicationException e = assertThrows(ApplicationException.class, () -> target.createOne(item1, Path.of("temp")));


        // Mock商品カテゴリリポジトリの呼び出し確認
        verify(itemCategoryRepository).getOne("01");
        // Mockメーカーリポジトリが呼び出されていないことの確認
        verifyNoInteractions(makerRepository);
        // Mock商品リポジトリが呼び出されていないことの確認
        verifyNoInteractions(itemRepository);
        // Mock商品レビューリポジトリが呼び出されていないことの確認
        verifyNoInteractions(itemReviewRepository);

        // メッセージコードとパラメータ―の検証
        assertEquals(ErrorCode.ITEM_CATEGORY_NOT_FOUND, e.getErrorCode());
        assertTrue(Arrays.equals(new String[] {"01"}, e.getParams()));
    }

    @Test
    @DisplayName("[createOne][異常系][指定されたメーカーが存在しない]")
    void testCreateOne3() throws Exception {

        // Mock商品カテゴリリポジトリからスローされる例外
        ResourceNotFoundException exception = new ResourceNotFoundException(ErrorCode.MAKER_NOT_FOUND, new String[] {"M0001"});

        // Mockのふるまい定義
        doReturn(item1.getItemCategory()).when(itemCategoryRepository).getOne(any());
        doThrow(exception).when(makerRepository).getOne(any());


        // サービス実行
        ApplicationException e = assertThrows(ApplicationException.class, () -> target.createOne(item1, Path.of("temp")));


        // Mock商品カテゴリリポジトリの呼び出し確認
        verify(itemCategoryRepository).getOne("01");
        // Mockメーカーリポジトリの呼び出し確認
        verify(makerRepository).getOne("M0001");
        // Mock商品リポジトリが呼び出されていないことの確認
        verifyNoInteractions(itemRepository);
        // Mock商品レビューリポジトリが呼び出されていないことの確認
        verifyNoInteractions(itemReviewRepository);

        // メッセージコードとパラメータ―の検証
        assertEquals(ErrorCode.MAKER_NOT_FOUND, e.getErrorCode());
        assertTrue(Arrays.equals(new String[] {"M0001"}, e.getParams()));
    }

    @Test
    @DisplayName("[createOne][異常系][商品がすでに存在する]")
    void testCreateOne4() throws Exception {

        // Mock商品リポジトリからスローされる例外
        ConflictException exception = new ConflictException(ErrorCode.ITEM_ALREADY_EXISTS, new String[] {"A0001"});

        // Mockのふるまい定義
        doReturn(item1.getItemCategory()).when(itemCategoryRepository).getOne(any());
        doReturn(item1.getMaker()).when(makerRepository).getOne(any());
        doThrow(exception).when(itemRepository).createOne(any(), any());

        // サービス引数定義
        Path path = Path.of("temp");
        

        // サービス実行
        ConflictException e = assertThrows(ConflictException.class, () -> target.createOne(item1, path));


        // Mock商品カテゴリリポジトリの呼び出し確認
        verify(itemCategoryRepository).getOne("01");
        // Mockメーカーリポジトリの呼び出し確認
        verify(makerRepository).getOne("M0001");
        // Mock商品リポジトリの呼び出し確認
        verify(itemRepository).createOne(argThat(condition -> {
            assertEquals(item1.getId(), condition.getId());
            assertEquals(item1.getName(), condition.getName());
            assertEquals(item1.getNameKana(), condition.getNameKana());
            assertEquals(item1.getItemCategory().getId(), condition.getItemCategory().getId());
            assertEquals(item1.getItemCategory().getName(), condition.getItemCategory().getName());
            assertEquals(item1.getItemCategory().getCreatedAt(), condition.getItemCategory().getCreatedAt());
            assertEquals(item1.getItemCategory().getLastModifiedAt(), condition.getItemCategory().getLastModifiedAt());
            assertEquals(item1.getMaker().getId(), condition.getMaker().getId());
            assertEquals(item1.getMaker().getName(), condition.getMaker().getName());
            assertEquals(item1.getMaker().getNameKana(), condition.getMaker().getNameKana());
            assertEquals(item1.getMaker().getCreatedAt(), condition.getMaker().getCreatedAt());
            assertEquals(item1.getMaker().getLastModifiedAt(), condition.getMaker().getLastModifiedAt());
            assertEquals(item1.getStartDate(), condition.getStartDate());
            assertEquals(item1.getEndDate(), condition.getEndDate());
            assertEquals(item1.getPrice(), condition.getPrice());
            assertEquals(item1.getDescription(), condition.getDescription());
            assertEquals(item1.getReviewAverageScore(), condition.getReviewAverageScore());
            assertEquals(item1.getVersion(), condition.getVersion());
            assertEquals(item1.getCreatedAt(), condition.getCreatedAt());
            assertEquals(item1.getLastModifiedAt(), condition.getLastModifiedAt());
            return true;
        }), eq(path));
        // Mock商品レビューリポジトリが呼び出されていないことの確認
        verifyNoInteractions(itemReviewRepository);

        // メッセージコードとパラメータ―の検証
        assertEquals(ErrorCode.ITEM_ALREADY_EXISTS, e.getErrorCode());
        assertTrue(Arrays.equals(new String[] {"A0001"}, e.getParams()));
    }

    @Test
    @DisplayName("[createOne][異常系][商品レビュー要約がすでに存在する]")
    void testCreateOne5() throws Exception {

        // Mock商品レビューリポジトリからスローされる例外
        ConflictException exception = new ConflictException(ErrorCode.ITEM_REVIEW_SUMMARY_ALREADY_EXISTS, new String[] {"A0001"});

        // Mockのふるまい定義
        doReturn(item1.getItemCategory()).when(itemCategoryRepository).getOne(any());
        doReturn(item1.getMaker()).when(makerRepository).getOne(any());
        doNothing().when(itemRepository).createOne(any(), any());
        doThrow(exception).when(itemReviewRepository).createSummary(any());

        // サービス引数定義
        Path path = Path.of("temp");
        
        
        // サービス実行
        SystemException e = assertThrows(SystemException.class, () -> target.createOne(item1, path));


        // Mock商品カテゴリリポジトリの呼び出し確認
        verify(itemCategoryRepository).getOne("01");
        // Mockメーカーリポジトリの呼び出し確認
        verify(makerRepository).getOne("M0001");
        // Mock商品リポジトリの呼び出し確認
        verify(itemRepository).createOne(argThat(condition -> {
            assertEquals(item1.getId(), condition.getId());
            assertEquals(item1.getName(), condition.getName());
            assertEquals(item1.getNameKana(), condition.getNameKana());
            assertEquals(item1.getItemCategory().getId(), condition.getItemCategory().getId());
            assertEquals(item1.getItemCategory().getName(), condition.getItemCategory().getName());
            assertEquals(item1.getItemCategory().getCreatedAt(), condition.getItemCategory().getCreatedAt());
            assertEquals(item1.getItemCategory().getLastModifiedAt(), condition.getItemCategory().getLastModifiedAt());
            assertEquals(item1.getMaker().getId(), condition.getMaker().getId());
            assertEquals(item1.getMaker().getName(), condition.getMaker().getName());
            assertEquals(item1.getMaker().getNameKana(), condition.getMaker().getNameKana());
            assertEquals(item1.getMaker().getCreatedAt(), condition.getMaker().getCreatedAt());
            assertEquals(item1.getMaker().getLastModifiedAt(), condition.getMaker().getLastModifiedAt());
            assertEquals(item1.getStartDate(), condition.getStartDate());
            assertEquals(item1.getEndDate(), condition.getEndDate());
            assertEquals(item1.getPrice(), condition.getPrice());
            assertEquals(item1.getDescription(), condition.getDescription());
            assertEquals(item1.getReviewAverageScore(), condition.getReviewAverageScore());
            assertEquals(item1.getVersion(), condition.getVersion());
            assertEquals(item1.getCreatedAt(), condition.getCreatedAt());
            assertEquals(item1.getLastModifiedAt(), condition.getLastModifiedAt());
            return true;
        }), eq(path));
        // Mock商品レビューリポジトリの呼び出し確認
        verify(itemReviewRepository).createSummary(item1.getId());
        
        // メッセージコードとパラメータ―の検証
        assertEquals(ErrorCode.ITEM_REVIEW_SUMMARY_ALREADY_EXISTS, e.getErrorCode());
        assertTrue(Arrays.equals(new String[] {"A0001"}, e.getParams()));
    }

    @Test
    @DisplayName("[updateOne][正常系][商品更新成功]")
    void testUpdateOne1() throws Exception {

        // Mockのふるまい定義
        doReturn(item1.getItemCategory()).when(itemCategoryRepository).getOne(any());
        doReturn(item1.getMaker()).when(makerRepository).getOne(any());
        doNothing().when(itemRepository).updateOne(any(), any());

        // サービス引数定義
        Path path = Path.of("temp");

        
        // サービス実行
        target.updateOne(item1, path);


        // Mockカテゴリリポジトリの呼び出し確認
        verify(itemCategoryRepository).getOne("01");
        // Mockメーカーリポジトリの呼び出し確認
        verify(makerRepository).getOne("M0001");
        // Mock商品リポジトリの呼び出し確認
        verify(itemRepository).updateOne(argThat(condition -> {
            assertEquals(item1.getId(), condition.getId());
            assertEquals(item1.getName(), condition.getName());
            assertEquals(item1.getNameKana(), condition.getNameKana());
            assertEquals(item1.getItemCategory().getId(), condition.getItemCategory().getId());
            assertEquals(item1.getItemCategory().getName(), condition.getItemCategory().getName());
            assertEquals(item1.getItemCategory().getCreatedAt(), condition.getItemCategory().getCreatedAt());
            assertEquals(item1.getItemCategory().getLastModifiedAt(), condition.getItemCategory().getLastModifiedAt());
            assertEquals(item1.getMaker().getId(), condition.getMaker().getId());
            assertEquals(item1.getMaker().getName(), condition.getMaker().getName());
            assertEquals(item1.getMaker().getNameKana(), condition.getMaker().getNameKana());
            assertEquals(item1.getMaker().getCreatedAt(), condition.getMaker().getCreatedAt());
            assertEquals(item1.getMaker().getLastModifiedAt(), condition.getMaker().getLastModifiedAt());
            assertEquals(item1.getStartDate(), condition.getStartDate());
            assertEquals(item1.getEndDate(), condition.getEndDate());
            assertEquals(item1.getPrice(), condition.getPrice());
            assertEquals(item1.getDescription(), condition.getDescription());
            assertEquals(item1.getReviewAverageScore(), condition.getReviewAverageScore());
            assertEquals(item1.getVersion(), condition.getVersion());
            assertEquals(item1.getCreatedAt(), condition.getCreatedAt());
            assertEquals(item1.getLastModifiedAt(), condition.getLastModifiedAt());
            return true;
        }), eq(path));
    }

    @Test
    @DisplayName("[updateOne][異常系][指定された商品カテゴリが存在しない]")
    void testUpdateOne2() throws Exception {

        // Mock商品カテゴリリポジトリからスローされる例外
        ResourceNotFoundException exception = new ResourceNotFoundException(ErrorCode.ITEM_CATEGORY_NOT_FOUND, new String[] {"01"});

        // Mockのふるまい定義
        doThrow(exception).when(itemCategoryRepository).getOne(any());


        // サービス実行
        ApplicationException e = assertThrows(ApplicationException.class, () -> target.updateOne(item1, Path.of("temp")));


        // Mock商品カテゴリリポジトリの呼び出し確認
        verify(itemCategoryRepository).getOne("01");
        // Mockメーカーリポジトリが呼び出されていないことの確認
        verifyNoInteractions(makerRepository);
        // Mock商品リポジトリが呼び出されていないことの確認
        verifyNoInteractions(itemRepository);
        // Mock商品レビューリポジトリが呼び出されていないことの確認
        verifyNoInteractions(itemReviewRepository);

        // メッセージコードとパラメータ―の検証
        assertEquals(ErrorCode.ITEM_CATEGORY_NOT_FOUND, e.getErrorCode());
        assertTrue(Arrays.equals(new String[] {"01"}, e.getParams()));
    }

    @Test
    @DisplayName("[updateOne][異常系][指定されたメーカーが存在しない]")
    void testUpdateOne3() throws Exception {

        // Mock商品カテゴリリポジトリからスローされる例外
        ResourceNotFoundException exception = new ResourceNotFoundException(ErrorCode.MAKER_NOT_FOUND, new String[] {"M0001"});

        // Mockのふるまい定義
        doReturn(item1.getItemCategory()).when(itemCategoryRepository).getOne(any());
        doThrow(exception).when(makerRepository).getOne(any());


        // サービス実行
        ApplicationException e = assertThrows(ApplicationException.class, () -> target.updateOne(item1, Path.of("temp")));


        // Mock商品カテゴリリポジトリの呼び出し確認
        verify(itemCategoryRepository).getOne("01");
        // Mockメーカーリポジトリの呼び出し確認
        verify(makerRepository).getOne("M0001");
        // Mock商品リポジトリが呼び出されていないことの確認
        verifyNoInteractions(itemRepository);

        // メッセージコードとパラメータ―の検証
        assertEquals(ErrorCode.MAKER_NOT_FOUND, e.getErrorCode());
        assertTrue(Arrays.equals(new String[] {"M0001"}, e.getParams()));
    }

    @Test
    @DisplayName("[updateOne][異常系][指定されたバージョンの商品が存在しない]")
    void testUpdateOne4() throws Exception {

        // Mock商品リポジトリからスローされる例外
        ConflictException exception = new ConflictException(ErrorCode.ITEM_WAS_UPDATED_BY_ANYONE, new String[] {"A0001"});

        // Mockのふるまい定義
        doReturn(item1.getItemCategory()).when(itemCategoryRepository).getOne(any());
        doReturn(item1.getMaker()).when(makerRepository).getOne(any());
        doThrow(exception).when(itemRepository).updateOne(any(), any());

        // サービス引数定義
        Path path = Path.of("temp");
        

        // サービス実行
        ConflictException e = assertThrows(ConflictException.class, () -> target.updateOne(item1, path));


        // Mock商品カテゴリリポジトリの呼び出し確認
        verify(itemCategoryRepository).getOne("01");
        // Mockメーカーリポジトリの呼び出し確認
        verify(makerRepository).getOne("M0001");
        // Mock商品リポジトリの呼び出し確認
        verify(itemRepository).updateOne(argThat(condition -> {
            assertEquals(item1.getId(), condition.getId());
            assertEquals(item1.getName(), condition.getName());
            assertEquals(item1.getNameKana(), condition.getNameKana());
            assertEquals(item1.getItemCategory().getId(), condition.getItemCategory().getId());
            assertEquals(item1.getItemCategory().getName(), condition.getItemCategory().getName());
            assertEquals(item1.getItemCategory().getCreatedAt(), condition.getItemCategory().getCreatedAt());
            assertEquals(item1.getItemCategory().getLastModifiedAt(), condition.getItemCategory().getLastModifiedAt());
            assertEquals(item1.getMaker().getId(), condition.getMaker().getId());
            assertEquals(item1.getMaker().getName(), condition.getMaker().getName());
            assertEquals(item1.getMaker().getNameKana(), condition.getMaker().getNameKana());
            assertEquals(item1.getMaker().getCreatedAt(), condition.getMaker().getCreatedAt());
            assertEquals(item1.getMaker().getLastModifiedAt(), condition.getMaker().getLastModifiedAt());
            assertEquals(item1.getStartDate(), condition.getStartDate());
            assertEquals(item1.getEndDate(), condition.getEndDate());
            assertEquals(item1.getPrice(), condition.getPrice());
            assertEquals(item1.getDescription(), condition.getDescription());
            assertEquals(item1.getReviewAverageScore(), condition.getReviewAverageScore());
            assertEquals(item1.getVersion(), condition.getVersion());
            assertEquals(item1.getCreatedAt(), condition.getCreatedAt());
            assertEquals(item1.getLastModifiedAt(), condition.getLastModifiedAt());
            return true;
        }), eq(path));

        // メッセージコードとパラメータ―の検証
        assertEquals(ErrorCode.ITEM_WAS_UPDATED_BY_ANYONE, e.getErrorCode());
        assertTrue(Arrays.equals(new String[] {"A0001"}, e.getParams()));
    }


    @Test
    @DisplayName("[deleteOne][正常系][商品削除成功]")
    void testDeleteOne1() throws Exception {

        // Mockのふるまい定義
        doNothing().when(itemRepository).deleteOne(any(), anyInt());

        
        // サービス実行
        target.deleteOne("A0001", 1);


        // Mock商品リポジトリの呼び出し確認
        verify(itemRepository).deleteOne("A0001", 1);
    }

    @Test
    @DisplayName("[deleteOne][異常系][指定されたバージョンの商品が存在しない]")
    void testDeleteOne2() throws Exception {

        // Mock商品リポジトリからスローされる例外
        ConflictException exception = new ConflictException(ErrorCode.ITEM_WAS_UPDATED_BY_ANYONE, new String[] {"A0001"});

        // Mockのふるまい定義
        doThrow(exception).when(itemRepository).deleteOne(any(), anyInt());
        

        // サービス実行
        ConflictException e = assertThrows(ConflictException.class, () -> target.deleteOne("A0001", 1));


        // Mock商品リポジトリの呼び出し確認
        verify(itemRepository).deleteOne("A0001", 1);

        // メッセージコードとパラメータ―の検証
        assertEquals(ErrorCode.ITEM_WAS_UPDATED_BY_ANYONE, e.getErrorCode());
        assertTrue(Arrays.equals(new String[] {"A0001"}, e.getParams()));
    }

}
