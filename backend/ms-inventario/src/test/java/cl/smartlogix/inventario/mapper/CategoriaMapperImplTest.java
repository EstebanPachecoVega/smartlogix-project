package cl.smartlogix.inventario.mapper;

import cl.smartlogix.inventario.dto.request.CategoriaRequestDTO;
import cl.smartlogix.inventario.dto.response.CategoriaResponseDTO;
import cl.smartlogix.inventario.entity.Categoria;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CategoriaMapperImplTest {

    private final CategoriaMapperImpl mapper = new CategoriaMapperImpl();

    @Test
    void toEntity_mapsAllFields() {
        CategoriaRequestDTO dto = new CategoriaRequestDTO();
        dto.setNombre("Electrónica");
        dto.setSlug("electronica");
        dto.setDescripcion("Productos electrónicos");
        dto.setPadreId(3L);

        Categoria entity = mapper.toEntity(dto);

        assertEquals("Electrónica", entity.getNombre());
        assertEquals("electronica", entity.getSlug());
        assertEquals("Productos electrónicos", entity.getDescripcion());
        assertNotNull(entity.getPadre());
        assertEquals(3L, entity.getPadre().getId());
    }

    @Test
    void toEntity_nullPadreId_setsNullPadre() {
        CategoriaRequestDTO dto = new CategoriaRequestDTO();
        dto.setNombre("Sin Padre");
        dto.setSlug("sin-padre");

        Categoria entity = mapper.toEntity(dto);

        assertNull(entity.getPadre());
    }

    @Test
    void toResponseDTO_mapsAllFields() {
        Categoria padre = new Categoria();
        padre.setId(1L);
        padre.setNombre("Categorías");

        Categoria entity = new Categoria();
        entity.setId(2L);
        entity.setNombre("Electrónica");
        entity.setSlug("electronica");
        entity.setDescripcion("Productos electrónicos");
        entity.setPadre(padre);
        entity.setOrdenVisual(1);
        entity.setActivo(true);

        CategoriaResponseDTO dto = mapper.toResponseDTO(entity);

        assertEquals(2L, dto.getId());
        assertEquals("Electrónica", dto.getNombre());
        assertEquals("electronica", dto.getSlug());
        assertEquals("Productos electrónicos", dto.getDescripcion());
        assertEquals(1L, dto.getPadreId());
        assertEquals("Categorías", dto.getPadreNombre());
        assertEquals(1, dto.getOrdenVisual());
        assertTrue(dto.getActivo());
    }

    @Test
    void toResponseDTO_nullPadre_returnsNullFields() {
        Categoria entity = new Categoria();
        entity.setId(1L);
        entity.setNombre("Sin Padre");
        entity.setSlug("sin-padre");

        CategoriaResponseDTO dto = mapper.toResponseDTO(entity);

        assertNull(dto.getPadreId());
        assertNull(dto.getPadreNombre());
    }

    @Test
    void updateEntity_updatesNonNullFields() {
        Categoria entity = new Categoria();
        entity.setId(1L);
        entity.setNombre("Original");
        entity.setSlug("original");

        CategoriaRequestDTO dto = new CategoriaRequestDTO();
        dto.setNombre("Actualizado");
        dto.setPadreId(5L);

        mapper.updateEntity(entity, dto);

        assertEquals("Actualizado", entity.getNombre());
        assertNotNull(entity.getPadre());
        assertEquals(5L, entity.getPadre().getId());

        assertEquals("original", entity.getSlug());
    }
}
