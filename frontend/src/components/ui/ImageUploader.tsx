'use client';

import { useState, useRef } from 'react';
import Image from 'next/image';
import { X, Upload, Loader2 } from 'lucide-react';

interface ImageUploaderProps {
  mode: 'single' | 'multiple';
  value: string | string[];
  onChange: (value: string | string[]) => void;
  label?: string;
}

const CLOUDINARY_URL = `https://api.cloudinary.com/v1_1/${process.env.NEXT_PUBLIC_CLOUDINARY_CLOUD_NAME}/image/upload`;

async function uploadFile(file: File): Promise<string> {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('upload_preset', process.env.NEXT_PUBLIC_CLOUDINARY_UPLOAD_PRESET!);
  formData.append('folder', 'smartlogix/productos');
  const res = await fetch(CLOUDINARY_URL, { method: 'POST', body: formData });
  if (!res.ok) throw new Error(`Upload failed: ${res.statusText}`);
  const data = await res.json();
  return data.secure_url;
}

export default function ImageUploader({ mode, value, onChange, label }: ImageUploaderProps) {
  const [uploading, setUploading] = useState(false);
  const [dragOver, setDragOver] = useState(false);
  const [dragIndex, setDragIndex] = useState<number | null>(null);
  const [dropIndex, setDropIndex] = useState<number | null>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  const images = mode === 'single' ? (value ? [value as string] : []) : (value as string[]);

  const uploadFiles = async (files: FileList) => {
    setUploading(true);
    try {
      if (mode === 'single') {
        const url = await uploadFile(files[0]);
        onChange(url);
      } else {
        const fileArray = Array.from(files);
        const urls = await Promise.all(fileArray.map(uploadFile));
        onChange([...images, ...urls]);
      }
    } catch {
      // error silently
    } finally {
      setUploading(false);
    }
  };

  const handleFileDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setDragOver(false);
    if (e.dataTransfer.files.length > 0) {
      uploadFiles(e.dataTransfer.files);
    }
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    setDragOver(true);
  };

  const handleDragLeave = () => {
    setDragOver(false);
  };

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      uploadFiles(e.target.files);
      e.target.value = '';
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

  const handleReorderStart = (e: React.DragEvent, index: number) => {
    e.dataTransfer.effectAllowed = 'move';
    setDragIndex(index);
  };

  const handleReorderOver = (e: React.DragEvent, index: number) => {
    if (e.dataTransfer.types.includes('Files')) return;
    e.preventDefault();
    e.dataTransfer.dropEffect = 'move';
    setDropIndex(index);
  };

  const handleReorderLeave = () => {
    setDropIndex(null);
  };

  const handleReorderDrop = (index: number) => {
    setDropIndex(null);
    if (dragIndex === null || dragIndex === index) {
      setDragIndex(null);
      return;
    }
    const updated = [...images];
    const [moved] = updated.splice(dragIndex, 1);
    updated.splice(index, 0, moved);
    onChange(updated);
    setDragIndex(null);
  };

  const handleReorderEnd = () => {
    setDragIndex(null);
    setDropIndex(null);
  };

  return (
    <div className="space-y-3">
      {label && <p className="text-sm font-medium">{label}</p>}

      {mode === 'single' ? (
        <div
          onDrop={handleFileDrop}
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          onClick={() => inputRef.current?.click()}
          className={`relative aspect-square rounded-lg overflow-hidden border-2 transition-all cursor-pointer group
            ${dragOver ? 'border-primary bg-accent' : 'border-dashed border-border hover:border-primary/60'}
            ${images.length > 0 ? '' : 'flex items-center justify-center'}`}
        >
          {images.length > 0 ? (
            <>
              <Image
                src={images[0]}
                alt="Imagen principal"
                fill
                sizes="400px"
                className="object-cover pointer-events-none"
              />
              <div className="absolute inset-0 flex items-center justify-center bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity">
                <div className="text-center text-white">
                  <Upload className="h-8 w-8 mx-auto" />
                  <p className="text-sm mt-1">Arrastra para reemplazar</p>
                </div>
              </div>
              <button
                type="button"
                onClick={(e) => { e.stopPropagation(); removeImage(0); }}
                className="absolute top-2 right-2 bg-black/60 text-white rounded-full p-1.5 opacity-0 group-hover:opacity-100 transition-opacity z-10"
              >
                <X className="h-4 w-4" />
              </button>
            </>
          ) : uploading ? (
            <div className="text-center">
              <Loader2 className="h-10 w-10 mx-auto animate-spin text-muted-foreground/70" />
              <p className="text-sm text-muted-foreground mt-2">Subiendo...</p>
            </div>
          ) : (
            <div className="text-center px-4">
              <Upload className="h-10 w-10 mx-auto text-muted-foreground/40" />
              <p className="text-sm text-muted-foreground mt-2">Arrastra la imagen aquí o haz clic</p>
              <p className="text-xs text-muted-foreground/70 mt-1">JPG, PNG, WebP</p>
            </div>
          )}
          <input ref={inputRef} type="file" accept="image/*" className="hidden" onChange={handleFileSelect} />
        </div>
      ) : (
        <>
          {images.length > 0 && (
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
              {images.map((url, index) => (
                <div
                  key={url}
                  draggable
                  onDragStart={(e) => handleReorderStart(e, index)}
                  onDragOver={(e) => handleReorderOver(e, index)}
                  onDragLeave={handleReorderLeave}
                  onDrop={() => handleReorderDrop(index)}
                  onDragEnd={handleReorderEnd}
                  className={`relative group aspect-square rounded-md overflow-hidden border bg-transparent transition-all cursor-grab active:cursor-grabbing
                    ${dragIndex === index ? 'opacity-50 scale-95' : ''}
                    ${dropIndex === index && dragIndex !== index ? 'border-primary ring-2 ring-primary/30' : 'border-border'}`}
                >
                  <Image
                    src={url}
                    alt={`Imagen ${index + 1}`}
                    fill
                    sizes="(max-width: 768px) 50vw, 25vw"
                    className="object-cover pointer-events-none"
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

          <div
            onDrop={handleFileDrop}
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onClick={() => inputRef.current?.click()}
            className={`border-2 rounded-lg py-6 px-4 text-center cursor-pointer transition-all
              ${dragOver ? 'border-primary bg-accent' : 'border-dashed border-border hover:border-primary/60'}`}
          >
            {uploading ? (
              <>
                <Loader2 className="h-8 w-8 mx-auto animate-spin text-muted-foreground/70" />
                <p className="text-sm text-muted-foreground mt-2">Subiendo imágenes...</p>
              </>
            ) : (
              <>
                <Upload className="h-8 w-8 mx-auto text-muted-foreground/40" />
                <p className="text-sm text-muted-foreground mt-2">
                  Arrastra las imágenes aquí o haz clic
                </p>
                <p className="text-xs text-muted-foreground/70 mt-1">JPG, PNG, WebP • Puedes seleccionar varias</p>
              </>
            )}
            <input
              ref={inputRef}
              type="file"
              accept="image/*"
              multiple
              className="hidden"
              onChange={handleFileSelect}
            />
          </div>

          {images.length > 0 && (
            <p className="text-xs text-muted-foreground">{images.length} imagen(es) seleccionada(s)</p>
          )}
        </>
      )}
    </div>
  );
}
