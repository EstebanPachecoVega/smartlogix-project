'use client';

import { useState } from 'react';
import { ImageIcon } from 'lucide-react';

interface ImageGalleryProps {
  imagenPrincipal?: string;
  imagenes?: string[];
  nombre: string;
}

export default function ImageGallery({ imagenPrincipal, imagenes, nombre }: ImageGalleryProps) {
  const allImages = [
    ...(imagenPrincipal ? [imagenPrincipal] : []),
    ...(imagenes || []),
  ];

  const [selectedIndex, setSelectedIndex] = useState(0);
  const [erroredImages, setErroredImages] = useState<string[]>([]);

  const handleImgError = (url: string) => {
    if (!erroredImages.includes(url)) {
      setErroredImages([...erroredImages, url]);
    }
  };

  const validImages = allImages.filter(url => !erroredImages.includes(url));
  const currentImage = validImages[selectedIndex];

  if (!currentImage) {
    return (
      <div className="aspect-square bg-transparent rounded-lg flex items-center justify-center">
        <ImageIcon className="h-16 w-16 text-gray-300" />
      </div>
    );
  }

  return (
    <div className="flex flex-col-reverse md:flex-row gap-3">
      {validImages.length > 1 && (
        <div className="flex md:flex-col gap-2 overflow-x-auto md:overflow-y-auto md:max-h-[500px] scrollbar-hide" style={{ scrollbarWidth: 'none', msOverflowStyle: 'none' }}>
          {validImages.map((url, i) => (
            <button
              key={url}
              type="button"
              onClick={() => setSelectedIndex(i)}
              className={`shrink-0 w-14 h-14 md:w-16 md:h-16 rounded-md overflow-hidden border-2 transition-colors ${
                i === selectedIndex ? 'border-blue-500' : 'border-transparent hover:border-gray-300'
              }`}
            >
              <img
                src={url}
                alt={`${nombre} - miniatura ${i + 1}`}
                onError={() => handleImgError(url)}
                className="w-full h-full object-cover"
              />
            </button>
          ))}
        </div>
      )}
      <div className="flex-1 aspect-square bg-transparent rounded-lg overflow-hidden">
        <img
          src={currentImage}
          alt={`${nombre} - imagen ${selectedIndex + 1}`}
          onError={() => handleImgError(currentImage)}
          className="w-full h-full object-cover"
        />
      </div>
    </div>
  );
}
