/**
 * useItem カスタムフックのユニットテスト見本（/reacter-unit-test-gen が体裁を揃えるための参照ファイル）
 *
 * 観点:
 *   1. 取得値の検証（正常時・エラー時）
 *   2. 引数の利用検証
 *   3. API通信のモック化（MSW）
 *
 * 🚫 このファイルは見本コードです。実装コードとしてビルド対象に含めないでください
 *    （import先のフック・型は架空のため）。
 */
import { renderHook, waitFor } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { describe, it, expect, beforeAll, afterAll, afterEach } from 'vitest';

import { useItem } from './useItem';

const TEST_HANDLERS = [
  http.get('/api/items/:id', ({ params }) => {
    if (params.id === 'X9999') {
      return new HttpResponse(null, { status: 404 });
    }
    return HttpResponse.json({ id: params.id, name: 'アイテムA' });
  }),
];

const server = setupServer(...TEST_HANDLERS);

beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('useItem', () => {
  describe('取得値の検証', () => {
    it('正常時にitem情報を返す', async () => {
      // given: id=M0001 のアイテムを返すAPIモック
      const { result } = renderHook(() => useItem('M0001'));

      // when: 非同期取得が完了するまで待つ
      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
      });

      // then
      expect(result.current.item).toEqual({ id: 'M0001', name: 'アイテムA' });
      expect(result.current.error).toBeNull();
    });

    it('該当なし（404）の場合error状態を返す', async () => {
      // given: id=X9999 は404を返すAPIモック
      const { result } = renderHook(() => useItem('X9999'));

      // when
      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
      });

      // then
      expect(result.current.item).toBeNull();
      expect(result.current.error).not.toBeNull();
    });
  });

  describe('引数の利用検証', () => {
    it('渡したidがAPIリクエストのパスに反映される', async () => {
      // given
      const { result } = renderHook(() => useItem('M0002'));

      // when
      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
      });

      // then: id=M0002に対応するアイテムが取得される
      expect(result.current.item?.id).toBe('M0002');
    });
  });
});
