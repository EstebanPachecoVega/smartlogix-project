package cl.smartlogix.inventario.service.impl;

import cl.smartlogix.inventario.dto.request.CategoriaRequestDTO;
import cl.smartlogix.inventario.dto.request.ReordenarCategoriaDTO;
import cl.smartlogix.inventario.dto.response.CategoriaResponseDTO;
import cl.smartlogix.inventario.entity.Categoria;
import cl.smartlogix.inventario.entity.Producto;
import cl.smartlogix.inventario.exception.DomainException;
import cl.smartlogix.inventario.exception.DuplicateResourceException;
import cl.smartlogix.inventario.exception.ResourceNotFoundException;
import cl.smartlogix.inventario.mapper.CategoriaMapper;
import cl.smartlogix.inventario.repository.CategoriaRepository;
import cl.smartlogix.inventario.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceImplTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaMapper categoriaMapper;

    private CategoriaServiceImpl categoriaService;

    @BeforeEach
    void setUp() {
        categoriaService = new CategoriaServiceImpl(categoriaRepository, productoRepository, categoriaMapper);
    }

    @Test
    void createCategoria_ok() {
        CategoriaRequestDTO request = new CategoriaRequestDTO();
        request.setNombre("Electrónica");
        request.setSlug("electronica");

        when(categoriaRepository.existsByNombre("Electrónica")).thenReturn(false);
        when(categoriaRepository.existsBySlug("electronica")).thenReturn(false);

        Categoria categoria = new Categoria();
        when(categoriaMapper.toEntity(request)).thenReturn(categoria);
        when(categoriaRepository.save(categoria)).thenReturn(categoria);

        CategoriaResponseDTO expectedDto = new CategoriaResponseDTO();
        when(categoriaMapper.toResponseDTO(categoria)).thenReturn(expectedDto);

        CategoriaResponseDTO result = categoriaService.createCategoria(request);

        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    void createCategoria_nombreDuplicado_lanzaDuplicateResourceException() {
        CategoriaRequestDTO request = new CategoriaRequestDTO();
        request.setNombre("Electrónica");
        request.setSlug("electronica");

        when(categoriaRepository.existsByNombre("Electrónica")).thenReturn(true);

        assertThatThrownBy(() -> categoriaService.createCategoria(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("nombre");
    }

    @Test
    void createCategoria_slugDuplicado_lanzaDuplicateResourceException() {
        CategoriaRequestDTO request = new CategoriaRequestDTO();
        request.setNombre("Electrónica");
        request.setSlug("electronica");

        when(categoriaRepository.existsByNombre("Electrónica")).thenReturn(false);
        when(categoriaRepository.existsBySlug("electronica")).thenReturn(true);

        assertThatThrownBy(() -> categoriaService.createCategoria(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("slug");
    }

    @Test
    void createCategoria_padreNoExiste_lanzaResourceNotFoundException() {
        CategoriaRequestDTO request = new CategoriaRequestDTO();
        request.setNombre("Subcategoría");
        request.setSlug("subcategoria");
        request.setPadreId(999L);

        when(categoriaRepository.existsByNombre("Subcategoría")).thenReturn(false);
        when(categoriaRepository.existsBySlug("subcategoria")).thenReturn(false);
        when(categoriaRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> categoriaService.createCategoria(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("categoría padre");
    }

    @Test
    void updateCategoria_ok() {
        CategoriaRequestDTO request = new CategoriaRequestDTO();
        request.setNombre("Nuevo nombre");
        request.setSlug("nuevo-slug");

        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Nombre original");
        categoria.setSlug("slug-original");

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsByNombreAndIdNot("Nuevo nombre", 1L)).thenReturn(false);
        when(categoriaRepository.existsBySlugAndIdNot("nuevo-slug", 1L)).thenReturn(false);
        when(categoriaRepository.save(categoria)).thenReturn(categoria);

        CategoriaResponseDTO expectedDto = new CategoriaResponseDTO();
        when(categoriaMapper.toResponseDTO(categoria)).thenReturn(expectedDto);

        CategoriaResponseDTO result = categoriaService.updateCategoria(1L, request);

        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    void updateCategoria_noExiste_lanzaResourceNotFoundException() {
        when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoriaService.updateCategoria(999L, new CategoriaRequestDTO()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateCategoria_cicloJerarquico_lanzaDomainException() {
        CategoriaRequestDTO request = new CategoriaRequestDTO();
        request.setNombre("Nuevo nombre");
        request.setSlug("nuevo-slug");
        request.setPadreId(1L);

        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Nombre original");
        categoria.setSlug("slug-original");

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsByNombreAndIdNot("Nuevo nombre", 1L)).thenReturn(false);
        when(categoriaRepository.existsBySlugAndIdNot("nuevo-slug", 1L)).thenReturn(false);

        // Set padre mismo id -> self-reference cycle
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));

        assertThatThrownBy(() -> categoriaService.updateCategoria(1L, request))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("padre de sí misma");
    }

    @Test
    void deleteCategoria_ok() {
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Electrónica");

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(productoRepository.findByCategoriaIdIn(anyList())).thenReturn(List.of());

        categoriaService.deleteCategoria(1L);

        verify(categoriaRepository).delete(categoria);
    }

    @Test
    void deleteCategoria_conProductos_lanzaDomainException() {
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Electrónica");

        Producto p1 = new Producto();
        p1.setNombre("Producto 1");

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(productoRepository.findByCategoriaIdIn(anyList())).thenReturn(List.of(p1));

        assertThatThrownBy(() -> categoriaService.deleteCategoria(1L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("No se puede eliminar");
    }

    @Test
    void deleteCategoria_noExiste_lanzaResourceNotFoundException() {
        when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoriaService.deleteCategoria(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getCategoriaById_ok() {
        Categoria categoria = new Categoria();
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        CategoriaResponseDTO expectedDto = new CategoriaResponseDTO();
        when(categoriaMapper.toResponseDTO(categoria)).thenReturn(expectedDto);

        CategoriaResponseDTO result = categoriaService.getCategoriaById(1L);

        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    void getCategoriaById_noExiste_lanzaResourceNotFoundException() {
        when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoriaService.getCategoriaById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getCategoriaBySlug_noExiste_lanzaResourceNotFoundException() {
        when(categoriaRepository.findBySlug("no-existe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoriaService.getCategoriaBySlug("no-existe"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllCategorias_retornaLista() {
        Categoria c1 = new Categoria();
        Categoria c2 = new Categoria();
        when(categoriaRepository.findAllByOrderByOrdenVisualAsc()).thenReturn(List.of(c1, c2));
        when(categoriaMapper.toResponseDTO(c1)).thenReturn(new CategoriaResponseDTO());
        when(categoriaMapper.toResponseDTO(c2)).thenReturn(new CategoriaResponseDTO());

        List<CategoriaResponseDTO> result = categoriaService.getAllCategorias();

        assertThat(result).hasSize(2);
    }

    @Test
    void getCategoriasPrincipales_retornaLista() {
        Categoria c = new Categoria();
        when(categoriaRepository.findByPadreIsNullOrderByOrdenVisualAsc()).thenReturn(List.of(c));
        when(categoriaMapper.toResponseDTO(c)).thenReturn(new CategoriaResponseDTO());

        List<CategoriaResponseDTO> result = categoriaService.getCategoriasPrincipales();

        assertThat(result).hasSize(1);
    }

    @Test
    void reordenar_ok() {
        List<ReordenarCategoriaDTO> ordenes = List.of(
                new ReordenarCategoriaDTO(1L, 1),
                new ReordenarCategoriaDTO(2L, 2)
        );
        Categoria c1 = new Categoria();
        Categoria c2 = new Categoria();
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(c1));
        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(c2));

        categoriaService.reordenar(ordenes);

        verify(categoriaRepository, times(2)).save(any(Categoria.class));
    }

    @Test
    void reordenar_categoriaNoExiste_lanzaResourceNotFoundException() {
        List<ReordenarCategoriaDTO> ordenes = List.of(
                new ReordenarCategoriaDTO(999L, 1)
        );
        when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoriaService.reordenar(ordenes))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
