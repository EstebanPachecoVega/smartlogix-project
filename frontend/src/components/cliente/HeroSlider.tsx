'use client';

import { useEffect, useCallback, useState } from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';

const slides = [
  {
    gradient: 'from-blue-600 to-blue-800',
    title: 'Tecnología de punta',
    subtitle: 'Los últimos lanzamientos en un solo lugar',
  },
  {
    gradient: 'from-purple-600 to-purple-800',
    title: 'Ofertas exclusivas',
    subtitle: 'Precios imperdibles por tiempo limitado',
  },
  {
    gradient: 'from-emerald-600 to-emerald-800',
    title: 'Envío rápido y seguro',
    subtitle: 'Recibe tus productos en la puerta de tu casa',
  },
];

export default function HeroSlider() {
  const [current, setCurrent] = useState(0);

  const goTo = useCallback((index: number) => {
    setCurrent(index);
  }, []);

  const next = useCallback(() => {
    setCurrent((prev) => (prev + 1) % slides.length);
  }, []);

  const prev = useCallback(() => {
    setCurrent((prev) => (prev - 1 + slides.length) % slides.length);
  }, []);

  useEffect(() => {
    const timer = setInterval(next, 5000);
    return () => clearInterval(timer);
  }, [next]);

  return (
    <div className="relative w-full overflow-hidden rounded-xl mb-8 select-none">
      <div
        className="flex transition-transform duration-500 ease-in-out"
        style={{ transform: `translateX(-${current * 100}%)` }}
      >
        {slides.map((slide, i) => (
          <div
            key={i}
            className={`w-full shrink-0 bg-gradient-to-r ${slide.gradient} px-8 py-12 md:py-16 text-white`}
          >
            <h2 className="text-3xl md:text-4xl font-bold mb-3">{slide.title}</h2>
            <p className="text-blue-100 text-lg max-w-xl">{slide.subtitle}</p>
          </div>
        ))}
      </div>

      {slides.length > 1 && (
        <>
          <button
            onClick={prev}
            className="absolute left-2 top-1/2 -translate-y-1/2 z-10 bg-black/30 hover:bg-black/50 text-white rounded-full p-2 transition-colors cursor-pointer"
            aria-label="Anterior"
          >
            <ChevronLeft className="h-6 w-6" />
          </button>
          <button
            onClick={next}
            className="absolute right-2 top-1/2 -translate-y-1/2 z-10 bg-black/30 hover:bg-black/50 text-white rounded-full p-2 transition-colors cursor-pointer"
            aria-label="Siguiente"
          >
            <ChevronRight className="h-6 w-6" />
          </button>

          <div className="absolute bottom-3 left-1/2 -translate-x-1/2 z-10 flex gap-2">
            {slides.map((_, i) => (
              <button
                key={i}
                onClick={() => goTo(i)}
                className={`w-2.5 h-2.5 rounded-full transition-all cursor-pointer ${
                  i === current ? 'bg-white w-6' : 'bg-white/50 hover:bg-white/70'
                }`}
                aria-label={`Slide ${i + 1}`}
              />
            ))}
          </div>
        </>
      )}
    </div>
  );
}
