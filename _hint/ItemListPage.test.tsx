/**
 * ItemListPage のユニットテスト見本（/reacter-unit-test-gen が体裁を揃えるための参照ファイル）
 *
 * 観点:
 *   1. 表示要素検証（一覧表示・0件表示）
 *   2. Props/State変化検証（検索フォーム入力）
 *   3. ユーザ操作検証（検索・登録ボタン）
 *   4. 入力バリデーション（必須項目）
 *   5. API通信（MSWによるモック）
 *
 * 🚫 このファイルは見本コードです。実装コードとしてビルド対象に含めないでください
 *    （import先のコンポーネント・型は架空のため）。
 */
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { MemoryRouter } from 'react-router-dom';
import { describe, it, expect, beforeAll, afterAll, afterEach } from 'vitest';

import { ItemListPage } from './ItemListPage';

const TEST_HANDLERS = [
  http.get('/api/items', ({ request }) => {
    const url = new URL(request.url);
    const name = url.searchParams.get('name');
    if (name === 'テスト食品') {
      return HttpResponse.json({ items: [] });
    }
    return HttpResponse.json({
      items: [
        { id: 'M0001', name: 'アイテムA' },
        { id: 'M0002', name: 'アイテムB' },
        { id: 'M0003', name: 'アイテムC' },
      ],
    });
  }),
];

const server = setupServer(...TEST_HANDLERS);

beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('ItemListPage', () => {
  describe('表示要素検証', () => {
    it('一覧表示成功時に3件のアイテム名が表示される', async () => {
      // given: 3件のアイテムを返すAPIモックが設定済み
      render(
        <MemoryRouter>
          <ItemListPage />
        </MemoryRouter>,
      );

      // when: コンポーネントがマウントされAPIレスポンスを待つ
      await waitFor(() => {
        expect(screen.getByText('アイテムA')).toBeInTheDocument();
      });

      // then: 3件すべてのアイテム名が表示される
      expect(screen.getByText('アイテムA')).toBeInTheDocument();
      expect(screen.getByText('アイテムB')).toBeInTheDocument();
      expect(screen.getByText('アイテムC')).toBeInTheDocument();
    });

    it('検索結果が0件の場合「該当するデータがありません」が表示される', async () => {
      // given: name=テスト食品 で検索すると0件を返すAPIモック
      render(
        <MemoryRouter>
          <ItemListPage />
        </MemoryRouter>,
      );

      // when: 検索フォームに「テスト食品」を入力して検索ボタンを押す
      fireEvent.change(screen.getByRole('textbox', { name: '名称' }), {
        target: { value: 'テスト食品' },
      });
      fireEvent.click(screen.getByRole('button', { name: '検索' }));

      // then: 0件表示メッセージが表示される
      await waitFor(() => {
        expect(screen.getByText('該当するデータがありません')).toBeInTheDocument();
      });
    });
  });

  describe('Props/State変化検証', () => {
    it('検索フォーム入力時にstateが更新される', () => {
      // given
      render(
        <MemoryRouter>
          <ItemListPage />
        </MemoryRouter>,
      );
      const input = screen.getByRole('textbox', { name: '名称' }) as HTMLInputElement;

      // when
      fireEvent.change(input, { target: { value: 'アイテムA' } });

      // then
      expect(input.value).toBe('アイテムA');
    });
  });

  describe('ユーザ操作検証', () => {
    it('登録ボタンクリックで登録画面へ遷移する', async () => {
      // given
      render(
        <MemoryRouter>
          <ItemListPage />
        </MemoryRouter>,
      );

      // when
      fireEvent.click(screen.getByRole('button', { name: '登録' }));

      // then: ルーティング遷移後の画面見出しが表示される（実装に応じて調整すること）
      await waitFor(() => {
        expect(screen.getByRole('heading', { name: 'アイテム登録' })).toBeInTheDocument();
      });
    });
  });

  describe('入力バリデーション', () => {
    it('名称未入力で検索すると「名称は必須です」が表示される', async () => {
      // given
      render(
        <MemoryRouter>
          <ItemListPage />
        </MemoryRouter>,
      );

      // when: 名称を空のまま検索ボタンを押す
      fireEvent.click(screen.getByRole('button', { name: '検索' }));

      // then
      await waitFor(() => {
        expect(screen.getByText('名称は必須です')).toBeInTheDocument();
      });
    });
  });
});
