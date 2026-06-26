package cl.smartlogix.inventario.service;

import cl.smartlogix.inventario.dto.request.ProductoRequestDTO;
import cl.smartlogix.inventario.dto.response.ProductoResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductoService {
    ProductoResponseDTO createProducto(ProductoRequestDTO request);

    ProductoResponseDTO updateProducto(Long id, ProductoRequestDTO request);

    void deleteProducto(Long id);

    ProductoResponseDTO getProductoById(Long id);

    List<ProductoResponseDTO> getAllProductos();

    Page<ProductoResponseDTO> getAllProductos(Pageable pageable);

    ProductoResponseDTO getProductoBySlug(String slug);

    ProductoResponseDTO getProductoBySku(String sku);

    List<ProductoResponseDTO> getProductosByCategoria(Long categoriaId);

    List<ProductoResponseDTO> getProductosFiltrados(
            String nombre,
            Long categoriaId,
            Boolean conStock,
            Integer precioMin,
            Integer precioMax,
            Boolean destacado,
            Boolean novedad);

    Page<ProductoResponseDTO> getProductosFiltrados(
            String nombre,
            Long categoriaId,
            Boolean conStock,
            Integer precioMin,
            Integer precioMax,
            Boolean destacado,
            Boolean novedad,
            Pageable pageable);
}