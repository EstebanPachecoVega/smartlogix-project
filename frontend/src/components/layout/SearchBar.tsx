'use client';

import { useState, useEffect, useRef } from 'react';
import { useRouter } from 'next/navigation';
import { productosPublicApi } from '@/lib/api';
import { Producto } from '@/types';
import { Search, X, Loader2 } from 'lucide-react';

export default function SearchBar() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<Producto[]>([]);
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const router = useRouter();
  const ref = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (query.length < 2) {
      setResults([]);
      setOpen(false);
      return;
    }

    const timer = setTimeout(async () => {
      setLoading(true);
      try {
        const data = await productosPublicApi.listar({ nombre: query });
        setResults(data.slice(0, 5));
        setOpen(true);
      } catch {
        setResults([]);
      } finally {
        setLoading(false);
      }
    }, 300);

    return () => clearTimeout(timer);
  }, [query]);

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) {
        setOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleSelect = (slug: string) => {
    setOpen(false);
    setQuery('');
    router.push(`/productos/${slug}`);
  };

  const handleVerTodos = () => {
    setOpen(false);
    const q = query;
    setQuery('');
    router.push(`/?search=${encodeURIComponent(q)}`);
  };

  return (
    <div ref={ref} className="relative w-full max-w-lg mx-auto">
      <div className="relative">
        <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
        <input
          ref={inputRef}
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onFocus={() => { if (results.length > 0) setOpen(true); }}
          placeholder="Buscar productos..."
          className="w-full pl-10 pr-9 py-2 border rounded-full text-sm bg-gray-50 focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
        />
        {query && (
          <button
            type="button"
            onClick={() => { setQuery(''); setResults([]); setOpen(false); inputRef.current?.focus(); }}
            className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
          >
            <X className="h-4 w-4" />
          </button>
        )}
      </div>

      {open && (
        <div className="absolute top-full mt-1.5 w-full bg-white border rounded-xl shadow-lg z-50 overflow-hidden">
          {loading ? (
            <div className="flex items-center justify-center py-4">
              <Loader2 className="h-5 w-5 animate-spin text-gray-400" />
            </div>
          ) : results.length > 0 ? (
            <>
              {results.map((p) => (
                <button
                  key={p.id}
                  type="button"
                  onClick={() => handleSelect(p.slug || '')}
                  className="w-full flex items-center gap-3 px-3 py-2.5 hover:bg-gray-50 text-left transition-colors"
                >
                  {p.imagenPrincipal ? (
                    <img src={p.imagenPrincipal} alt="" className="w-10 h-10 rounded-md object-cover shrink-0" />
                  ) : (
                    <div className="w-10 h-10 rounded-md bg-gray-100 shrink-0" />
                  )}
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium truncate">{p.nombre}</p>
                    <p className="text-xs text-gray-500">${p.precio.toLocaleString()}</p>
                  </div>
                </button>
              ))}
              <button
                type="button"
                onClick={handleVerTodos}
                className="w-full px-3 py-2.5 text-sm text-blue-600 hover:bg-blue-50 border-t font-medium text-center transition-colors"
              >
                Ver todos los resultados para &ldquo;{query}&rdquo;
              </button>
            </>
          ) : (
            <p className="px-3 py-4 text-sm text-gray-500 text-center">
              No se encontraron productos para &ldquo;{query}&rdquo;
            </p>
          )}
        </div>
      )}
    </div>
  );
}
