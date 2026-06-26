'use client';

import { useEffect, useCallback, useState, memo } from 'react';
import Link from 'next/link';
import { ChevronLeft, ChevronRight } from 'lucide-react';

interface Slide {
  gradient: string;
  title: string;
  subtitle: string;
  image?: string;
  cta?: { label: string; href: string };
}

const slides: Slide[] = [
  {
    gradient: 'from-blue-600 to-blue-800',
    title: 'Novedades',
    subtitle: 'Descubre los últimos productos agregados',
    cta: { label: 'Ver novedades', href: '/?novedad=true' },
  },
  {
    gradient: 'from-orange-600 to-red-600',
    title: 'Componentes para PC',
    subtitle: 'Todo lo que necesitas para armar tu computadora de escritorio',
    cta: { label: 'Ver componentes', href: '/?cat=componentes' },
  },
  {
    gradient: 'from-emerald-600 to-emerald-800',
    title: 'Envío rápido y seguro',
    subtitle: 'Recibe tus productos en la puerta de tu casa',
    cta: { label: 'Más información', href: '/entrega-y-envios' },
  },
];

const HeroSlider = memo(function HeroSlider() {
  const [current, setCurrent] = useState(0);

  const goTo = useCallback((index: number) => setCurrent(index), []);
  const next = useCallback(() => setCurrent((prev) => (prev + 1) % slides.length), []);
  const prev = useCallback(() => setCurrent((prev) => (prev - 1 + slides.length) % slides.length), []);

  useEffect(() => {
    const timer = setInterval(next, 5000);
    return () => clearInterval(timer);
  }, [next]);

  return (
    <section
      className="relative overflow-hidden"
      style={{ width: '100dvw', marginLeft: 'calc(-50dvw + 50%)' }}
    >
      <div
        className="flex transition-transform duration-500 ease-in-out"
        style={{ transform: `translateX(-${current * 100}%)` }}
      >
        {slides.map((slide, i) => (
          <div
            key={i}
            className="relative w-full shrink-0 flex items-center justify-center min-h-[30vh] sm:min-h-[40vh] md:min-h-[40vh] lg:min-h-[50vh]"
          >
            {slide.image ? (
              <>
                <div
                  className="absolute inset-0 bg-cover bg-center"
                  style={{ backgroundImage: `url(${slide.image})` }}
                />
                <div className={`absolute inset-0 bg-gradient-to-r ${slide.gradient} opacity-85`} />
              </>
            ) : (
              <div className={`absolute inset-0 bg-gradient-to-r ${slide.gradient}`} />
            )}

            <div className="relative z-10 text-center px-4 sm:px-6 lg:px-8 max-w-4xl mx-auto">
              <h2 className="text-3xl sm:text-4xl md:text-5xl lg:text-6xl font-bold text-white mb-3 sm:mb-4 leading-tight">
                {slide.title}
              </h2>
              <p className="text-sm sm:text-base md:text-lg lg:text-xl text-white/80 mb-5 sm:mb-6 md:mb-8 max-w-2xl mx-auto">
                {slide.subtitle}
              </p>
              {slide.cta && (
                <Link
                  href={slide.cta.href}
                  className="inline-block bg-white text-gray-900 font-semibold px-5 sm:px-6 md:px-8 py-2.5 sm:py-3 rounded-lg hover:bg-gray-100 transition-colors text-sm sm:text-base shadow-lg"
                >
                  {slide.cta.label}
                </Link>
              )}
            </div>
          </div>
        ))}
      </div>

      {slides.length > 1 && (
        <>
          <button
            onClick={prev}
            className="absolute left-2 sm:left-4 top-1/2 -translate-y-1/2 z-20 bg-black/30 hover:bg-black/50 text-white rounded-full p-2 sm:p-3 transition-colors cursor-pointer"
            aria-label="Anterior"
          >
            <ChevronLeft className="h-5 w-5 sm:h-6 sm:w-6" />
          </button>

          <button
            onClick={next}
            className="absolute right-2 sm:right-4 top-1/2 -translate-y-1/2 z-20 bg-black/30 hover:bg-black/50 text-white rounded-full p-2 sm:p-3 transition-colors cursor-pointer"
            aria-label="Siguiente"
          >
            <ChevronRight className="h-5 w-5 sm:h-6 sm:w-6" />
          </button>

          <div className="absolute bottom-4 sm:bottom-6 left-1/2 -translate-x-1/2 z-20 flex gap-2 sm:gap-3">
            {slides.map((_, i) => (
              <button
                key={i}
                onClick={() => goTo(i)}
                className={`rounded-full transition-all cursor-pointer ${
                  i === current
                    ? 'bg-white w-6 sm:w-8 h-2.5 sm:h-3'
                    : 'bg-white/50 hover:bg-white/70 w-2.5 sm:w-3 h-2.5 sm:h-3'
                }`}
                aria-label={`Slide ${i + 1}`}
              />
            ))}
          </div>
        </>
      )}
    </section>
  );
});
export default HeroSlider;
