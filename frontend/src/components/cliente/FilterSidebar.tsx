'use client';

import { useState } from 'react';
import PriceRangeSlider from './PriceRangeSlider';
import { SlidersHorizontal, X } from 'lucide-react';

interface FilterSidebarProps {
  precioMin?: number;
  precioMax?: number;
  soloStock?: boolean;
  onPrecioChange: (min: number | undefined, max: number | undefined) => void;
  onStockChange: (solo: boolean) => void;
  onClear: () => void;
  minPrice: number;
  maxPrice: number;
}

export default function FilterSidebar({
  precioMin, precioMax, soloStock,
  onPrecioChange, onStockChange, onClear,
  minPrice, maxPrice,
}: FilterSidebarProps) {
  const [mobileOpen, setMobileOpen] = useState(false);

  const hasFilterCount = [precioMin !== undefined || precioMax !== undefined, !!soloStock].filter(Boolean).length;

  const filterContent = (
    <div className="space-y-6">
      <div>
        <h4 className="text-sm font-medium text-gray-700 mb-3">Precio</h4>
        <PriceRangeSlider
          min={minPrice}
          max={maxPrice}
          value={[precioMin ?? minPrice, precioMax ?? maxPrice]}
          onChange={([min, max]) => onPrecioChange(
            min === minPrice ? undefined : min,
            max === maxPrice ? undefined : max,
          )}
          step={100}
        />
      </div>

      <div className="border-t pt-4">
        <h4 className="text-sm font-medium text-gray-700 mb-3">Stock</h4>
        <label className="flex items-center gap-2 text-sm text-gray-600 cursor-pointer">
          <input
            type="checkbox"
            checked={!!soloStock}
            onChange={(e) => onStockChange(e.target.checked)}
            className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
          />
          Solo productos con stock
        </label>
      </div>

      {hasFilterCount > 0 && (
        <div className="border-t pt-4">
          <button
            onClick={onClear}
            className="text-sm text-blue-600 hover:text-blue-800 font-medium transition-colors"
          >
            Limpiar filtros
          </button>
        </div>
      )}
    </div>
  );

  return (
    <>
      <button
        onClick={() => setMobileOpen(!mobileOpen)}
        className="flex sm:hidden items-center gap-2 text-sm font-medium text-gray-700 mb-4"
      >
        <SlidersHorizontal className="h-4 w-4" />
        Filtros
        {hasFilterCount > 0 && (
          <span className="bg-blue-600 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
            {hasFilterCount}
          </span>
        )}
      </button>

      <aside className="hidden sm:block w-64 shrink-0">
        <div className="bg-white border rounded-lg p-4 sticky top-20">
          <h3 className="font-semibold text-sm text-gray-800 uppercase tracking-wide mb-5">Filtros</h3>
          {filterContent}
        </div>
      </aside>

      {mobileOpen && (
        <div className="fixed inset-0 z-50 sm:hidden">
          <div className="absolute inset-0 bg-black/30" onClick={() => setMobileOpen(false)} />
          <div className="absolute left-0 top-0 bottom-0 w-72 bg-white shadow-xl p-4 overflow-y-auto">
            <div className="flex items-center justify-between mb-5">
              <h3 className="font-semibold text-sm text-gray-800 uppercase tracking-wide">Filtros</h3>
              <button onClick={() => setMobileOpen(false)} className="p-1 hover:bg-gray-100 rounded-full transition-colors">
                <X className="h-5 w-5 text-gray-500" />
              </button>
            </div>
            {filterContent}
          </div>
        </div>
      )}
    </>
  );
}
