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
  const currentImage = allImages[selectedIndex];

  if (!currentImage) {
    return (
      <div className="aspect-square bg-gray-100 rounded-lg flex items-center justify-center">
        <ImageIcon className="h-16 w-16 text-gray-300" />
      </div>
    );
  }

  return (
    <div className="space-y-3">
      <div className="aspect-square bg-gray-100 rounded-lg overflow-hidden">
        <img
          src={currentImage}
          alt={`${nombre} - imagen ${selectedIndex + 1}`}
          className="w-full h-full object-cover"
        />
      </div>
      {allImages.length > 1 && (
        <div className="grid grid-cols-5 gap-2">
          {allImages.map((url, i) => (
            <button
              key={url}
              type="button"
              onClick={() => setSelectedIndex(i)}
              className={`aspect-square rounded-md overflow-hidden border-2 transition-colors ${
                i === selectedIndex ? 'border-blue-500' : 'border-transparent hover:border-gray-300'
              }`}
            >
              <img
                src={url}
                alt={`${nombre} - miniatura ${i + 1}`}
                className="w-full h-full object-cover"
              />
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
