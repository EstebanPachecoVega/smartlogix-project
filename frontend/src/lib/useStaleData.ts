"use client";

import { useEffect, useRef, useState } from "react";

interface CacheEntry<T> {
  data: T;
  timestamp: number;
}

const cache = new Map<string, CacheEntry<unknown>>();

export function useStaleData<T>(
  key: string,
  fetcher: () => Promise<T>,
  ttlMs: number = 30000
) {
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);
  const inflightRef = useRef<Map<string, Promise<T>>>(new Map());

  useEffect(() => {
    let cancelled = false;

    async function load() {
      setLoading(true);
      setError(null);

      const cached = cache.get(key);
      if (cached && Date.now() - cached.timestamp < ttlMs) {
        if (!cancelled) {
          setData(cached.data as T);
          setLoading(false);
        }
        return;
      }

      if (cached) {
        if (!cancelled) setData(cached.data as T);
      }

      const inflight = inflightRef.current.get(key);
      if (inflight) {
        try {
          const result = await inflight;
          if (!cancelled) setData(result);
        } catch (e) {
          if (!cancelled && !data) setError(e as Error);
        }
        if (!cancelled) setLoading(false);
        return;
      }

      const promise = fetcher();
      inflightRef.current.set(key, promise);

      try {
        const result = await promise;
        cache.set(key, { data: result, timestamp: Date.now() });
        if (!cancelled) setData(result);
      } catch (e) {
        if (!cancelled) setError(e as Error);
      } finally {
        inflightRef.current.delete(key);
        if (!cancelled) setLoading(false);
      }
    }

    load();
    return () => { cancelled = true; };
  }, [key, ttlMs]);

  const revalidate = async () => {
    cache.delete(key);
    setLoading(true);
    try {
      const result = await fetcher();
      cache.set(key, { data: result, timestamp: Date.now() });
      setData(result);
    } catch (e) {
      setError(e as Error);
    } finally {
      setLoading(false);
    }
  };

  return { data, loading, error, revalidate };
}

export function clearCacheByPrefix(prefix: string) {
  for (const key of cache.keys()) {
    if (key.startsWith(prefix)) cache.delete(key);
  }
}
