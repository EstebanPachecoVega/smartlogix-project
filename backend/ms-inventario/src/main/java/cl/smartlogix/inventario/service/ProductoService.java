package cl.smartlogix.inventario.service;

import cl.smartlogix.inventario.dto.request.ProductoRequestDTO;
import cl.smartlogix.inventario.dto.response.ProductoResponseDTO;

import java.util.List;

public interface ProductoService {
    ProductoResponseDTO createProducto(ProductoRequestDTO request);

    ProductoResponseDTO updateProducto(Long id, ProductoRequestDTO request);

    void deleteProducto(Long id);

    ProductoResponseDTO getProductoById(Long id);

    List<ProductoResponseDTO> getAllProductos();

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
}