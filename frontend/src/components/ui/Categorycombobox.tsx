'use client';

import { useState, useMemo } from 'react';
import {
    Combobox,
    ComboboxContent,
    ComboboxInput,
    ComboboxItem,
    ComboboxList,
} from '@/components/ui/combobox';
import { Categoria } from '@/types';

interface CategoryComboboxProps {
    categorias: Categoria[];
    value: number | undefined;
    onChange: (value: number | undefined) => void;
    placeholder?: string;
    disabled?: boolean;
}

/**
* Combobox de categorías con función de búsqueda, basado en @base-ui/react.
*
* ¿Por qué Base UI en lugar de Radix Select o Popover + Command?
* El Positioner de Base UI reconoce el desplazamiento del contenedor y NO depende de
* @radix-ui/react-remove-scroll, por lo que abrir este combobox nunca congela
* el desplazamiento del contenedor .app, la causa principal del error original.
*
* Características:
* - Navegación nativa con teclado (↑ ↓ Enter Escape)
* - Filtra mientras escribes, sin tener en cuenta los acentos mediante filterFn
* - "Sin categoría" siempre en la parte superior, separado de la lista
* - El botón Borrar (×) aparece una vez que se selecciona un valor
* - Funciona con más de 20 elementos (se puede virtualizar posteriormente si es necesario)
*/
export function CategoryCombobox({
    categorias,
    value,
    onChange,
    placeholder = 'Seleccionar categoría...',
    disabled = false,
}: CategoryComboboxProps) {
    const [query, setQuery] = useState('');

    const normalise = (s: string) =>
        s.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');

    const showSinCategoria = !query || normalise('Sin categoría').includes(normalise(query));

    const filteredCategorias = useMemo(() => {
        if (!query) return categorias;
        const nq = normalise(query);
        return categorias.filter(
            (c) => normalise(c.nombre).includes(nq) || normalise(c.slug).includes(nq),
        );
    }, [categorias, query]);

    const isEmpty = !showSinCategoria && filteredCategorias.length === 0;

    return (
        <Combobox
            value={value ?? null}
            onValueChange={(newValue: number | null) =>
                onChange(newValue === null ? undefined : newValue)
            }
            onInputValueChange={(q) => setQuery(q)}
            itemToStringLabel={(item) => {
                if (item === null) return 'Sin categoría';
                const cat = categorias.find((c) => c.id === item);
                return cat?.nombre ?? String(item);
            }}
        >
            <ComboboxInput
                className="w-full"
                placeholder={placeholder}
                disabled={disabled}
                showTrigger
                showClear={value !== undefined}
            />

            <ComboboxContent>
                {isEmpty && (
                    <p className="flex w-full justify-center py-2 text-center text-sm text-muted-foreground">
                        No se encontró categoría{query ? ` "${query}"` : ''}.
                    </p>
                )}

                <ComboboxList>
                    {showSinCategoria && (
                        <ComboboxItem
                            value={null}
                            className="data-highlighted:bg-primary/10 data-highlighted:ring-1 data-highlighted:ring-inset data-highlighted:ring-border"
                        >
                            Sin categoría
                        </ComboboxItem>
                    )}

                    {showSinCategoria && filteredCategorias.length > 0 && (
                        <div className="-mx-1 my-1 h-px bg-border" />
                    )}

                    {filteredCategorias.map((cat) => (
                        <ComboboxItem
                            key={cat.id}
                            value={cat.id}
                            className="data-highlighted:bg-primary/10 data-highlighted:ring-1 data-highlighted:ring-inset data-highlighted:ring-border"
                        >
                            {cat.nombre}
                        </ComboboxItem>
                    ))}
                </ComboboxList>
            </ComboboxContent>
        </Combobox>
    );
}