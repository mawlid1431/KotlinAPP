import { useCallback, useEffect, useRef, useState } from 'react';
import { ApiError, apiFetch, hasApi } from './apiClient';

/**
 * Data hooks over REST.
 *
 * REST has no push channel, so live-feeling screens are served by polling plus
 * a revalidation bus that re-runs every mounted query after any write.
 *
 *   undefined → still loading
 *   null      → unauthorised or not found
 *   T         → data
 *
 * Pages branch on `undefined` to render their loading state, so that tri-state
 * is part of the contract.
 */

type Listener = () => void;
const listeners = new Set<Listener>();

/** Re-runs every mounted query. Called automatically after any mutation. */
export function revalidateAll(): void {
  for (const listener of listeners) listener();
}

export type QueryOptions = {
  /** Poll interval in ms. Use for screens that must feel live. */
  refetchInterval?: number;
};

/** Poll cadences, chosen per screen rather than globally. */
export const POLL = {
  /** Order queues — an admin advancing a step should show up promptly. */
  orders: 5_000,
  /** Dashboard KPIs — cheaper, and nobody watches them second by second. */
  dashboard: 30_000,
} as const;

export function useApiQuery<T>(
  path: string | null,
  options: QueryOptions = {},
): T | null | undefined {
  const { refetchInterval } = options;
  const [data, setData] = useState<T | null | undefined>(undefined);
  const pathRef = useRef(path);
  pathRef.current = path;

  const load = useCallback(async (signal?: AbortSignal) => {
    const current = pathRef.current;
    if (!current || !hasApi()) {
      setData(null);
      return;
    }

    try {
      const result = await apiFetch<T>(current, { signal });
      if (!signal?.aborted) setData(result);
    } catch (error) {
      if (signal?.aborted) return;
      // 401/404 are legitimate "nothing here" answers, not failures.
      if (error instanceof ApiError && (error.status === 401 || error.status === 404)) {
        setData(null);
        return;
      }
      console.error(`[api] ${current} failed:`, error);
      setData(null);
    }
  }, []);

  useEffect(() => {
    if (path === null) {
      setData(undefined);
      return;
    }

    const controller = new AbortController();
    void load(controller.signal);

    const listener = () => void load();
    listeners.add(listener);

    const timer = refetchInterval
      ? setInterval(() => {
          if (document.visibilityState === 'visible') void load();
        }, refetchInterval)
      : undefined;

    return () => {
      controller.abort();
      listeners.delete(listener);
      if (timer) clearInterval(timer);
    };
  }, [path, refetchInterval, load]);

  return data;
}

/**
 * Returns a callable that performs a write and then revalidates every mounted
 * query, so the UI reflects the change without a manual refresh.
 */
export function useApiMutation() {
  return useCallback(
    async <T>(
      path: string,
      init: { method?: 'POST' | 'PATCH' | 'DELETE'; body?: unknown } = {},
    ): Promise<T> => {
      const result = await apiFetch<T>(path, { method: init.method ?? 'POST', body: init.body });
      revalidateAll();
      return result;
    },
    [],
  );
}
