'use client';

import { useRef, useCallback } from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';
import { Producto } from '@/types';
import ProductCard from './ProductCard';

interface ProductCarouselProps {
  productos: Producto[];
  title: string;
}

export default function ProductCarousel({ productos, title }: ProductCarouselProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const isDraggingRef = useRef(false);
  const hasDraggedRef = useRef(false);
  const startXRef = useRef(0);
  const scrollLeftRef = useRef(0);

  const DRAG_THRESHOLD = 5;

  const enablePointerEvents = useCallback(() => {
    if (!containerRef.current) return;
    const cards = containerRef.current.querySelectorAll<HTMLElement>('[data-carousel-card]');
    cards.forEach((card) => { card.style.pointerEvents = ''; });
  }, []);

  const disablePointerEvents = useCallback(() => {
    if (!containerRef.current) return;
    const cards = containerRef.current.querySelectorAll<HTMLElement>('[data-carousel-card]');
    cards.forEach((card) => { card.style.pointerEvents = 'none'; });
  }, []);

  const handleMouseDown = useCallback((e: React.MouseEvent) => {
    if (!containerRef.current) return;
    isDraggingRef.current = true;
    hasDraggedRef.current = false;
    startXRef.current = e.pageX - containerRef.current.offsetLeft;
    scrollLeftRef.current = containerRef.current.scrollLeft;
    containerRef.current.style.cursor = 'grabbing';
    containerRef.current.style.userSelect = 'none';
  }, []);

  const handleMouseMove = useCallback((e: React.MouseEvent) => {
    if (!isDraggingRef.current || !containerRef.current) return;
    const x = e.pageX - containerRef.current.offsetLeft;
    const walk = x - startXRef.current;
    if (Math.abs(walk) > DRAG_THRESHOLD) {
      hasDraggedRef.current = true;
      disablePointerEvents();
    }
    if (hasDraggedRef.current) {
      e.preventDefault();
      containerRef.current.scrollLeft = scrollLeftRef.current - walk;
    }
  }, [disablePointerEvents]);

  const handleMouseUp = useCallback(() => {
    if (!containerRef.current) return;
    containerRef.current.style.cursor = '';
    containerRef.current.style.userSelect = '';
    enablePointerEvents();
    isDraggingRef.current = false;
    hasDraggedRef.current = false;
  }, [enablePointerEvents]);

  const handleMouseLeave = useCallback(() => {
    handleMouseUp();
  }, [handleMouseUp]);

  const scrollBy = useCallback((direction: 'left' | 'right') => {
    if (!containerRef.current) return;
    const cardWidth = 280;
    const gap = 16;
    const scrollAmount = cardWidth + gap;
    containerRef.current.scrollBy({
      left: direction === 'left' ? -scrollAmount : scrollAmount,
      behavior: 'smooth',
    });
  }, []);

  if (productos.length === 0) return null;

  return (
    <section className="mb-10">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-xl font-bold">{title}</h2>
      </div>
      <div className="relative px-1">
        <button
          onClick={() => scrollBy('left')}
          className="absolute left-0 top-1/2 -translate-y-1/2 z-10 bg-white/80 hover:bg-white shadow-md rounded-full p-2 transition-colors cursor-pointer"
          aria-label="Anterior"
        >
          <ChevronLeft className="h-5 w-5" />
        </button>
        <button
          onClick={() => scrollBy('right')}
          className="absolute right-0 top-1/2 -translate-y-1/2 z-10 bg-white/80 hover:bg-white shadow-md rounded-full p-2 transition-colors cursor-pointer"
          aria-label="Siguiente"
        >
          <ChevronRight className="h-5 w-5" />
        </button>
        <div
          ref={containerRef}
          onMouseDown={handleMouseDown}
          onMouseMove={handleMouseMove}
          onMouseUp={handleMouseUp}
          onMouseLeave={handleMouseLeave}
          className="flex gap-4 overflow-x-auto scrollbar-hide select-none [&_img]:pointer-events-none pb-2 cursor-grab active:cursor-grabbing"
          style={{ scrollbarWidth: 'none', msOverflowStyle: 'none' }}
        >
          {productos.map((prod) => (
            <div
              key={prod.id}
              data-carousel-card
              className="flex-shrink-0 w-[220px] sm:w-[250px] md:w-[280px]"
            >
              <ProductCard producto={prod} />
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
