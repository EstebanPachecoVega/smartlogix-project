'use client';

import { CldUploadWidget } from 'next-cloudinary';
import { Button } from './button';
import { X, Upload, Loader2 } from 'lucide-react';
import { useState } from 'react';

interface ImageUploaderProps {
  mode: 'single' | 'multiple';
  value: string | string[];
  onChange: (value: string | string[]) => void;
  label?: string;
}

export default function ImageUploader({ mode, value, onChange, label }: ImageUploaderProps) {
  const [uploading, setUploading] = useState(false);
  const images = mode === 'single' ? (value ? [value as string] : []) : (value as string[]);

  const handleSuccess = (result: any) => {
    const url = result?.info?.secure_url || result?.info?.url;
    if (!url) return;
    setUploading(false);
    if (mode === 'single') {
      onChange(url);
    } else {
      onChange([...images, url]);
    }
  };

  const removeImage = (index: number) => {
    if (mode === 'single') {
      onChange('');
    } else {
      const updated = images.filter((_, i) => i !== index);
      onChange(updated);
    }
  };

  return (
    <div className="space-y-3">
      {label && <p className="text-sm font-medium">{label}</p>}

      {images.length > 0 && (
        <div className={`grid ${mode === 'single' ? 'grid-cols-1' : 'grid-cols-2 sm:grid-cols-3 md:grid-cols-4'} gap-3`}>
          {images.map((url, index) => (
            <div key={url} className="relative group aspect-square rounded-md overflow-hidden border bg-gray-50">
              <img
                src={url}
                alt={`Imagen ${index + 1}`}
                className="w-full h-full object-cover"
              />
              <button
                type="button"
                onClick={() => removeImage(index)}
                className="absolute top-1 right-1 bg-black/60 text-white rounded-full p-1 opacity-0 group-hover:opacity-100 transition-opacity"
              >
                <X className="h-3 w-3" />
              </button>
            </div>
          ))}
        </div>
      )}

      <CldUploadWidget
        uploadPreset={process.env.NEXT_PUBLIC_CLOUDINARY_UPLOAD_PRESET!}
        onSuccess={handleSuccess}
        options={{
          maxFiles: mode === 'single' ? 1 : 10,
          multiple: mode === 'multiple',
          folder: 'smartlogix/productos',
        }}
      >
        {({ open }) => (
          <Button
            type="button"
            variant="outline"
            size="sm"
            disabled={uploading}
            onClick={() => {
              setUploading(true);
              open();
            }}
          >
            {uploading ? (
              <Loader2 className="h-4 w-4 mr-2 animate-spin" />
            ) : (
              <Upload className="h-4 w-4 mr-2" />
            )}
            {uploading ? 'Subiendo...' : 'Subir imagen'}
          </Button>
        )}
      </CldUploadWidget>

      {mode === 'multiple' && images.length > 0 && (
        <p className="text-xs text-gray-500">{images.length} imagen(es) seleccionada(s)</p>
      )}
    </div>
  );
}
