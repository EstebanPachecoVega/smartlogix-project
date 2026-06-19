package cl.smartlogix.inventario.controller;

import cl.smartlogix.inventario.dto.request.ProductoRequestDTO;
import cl.smartlogix.inventario.dto.response.MapaCategoriaResponseDTO;
import cl.smartlogix.inventario.dto.response.ProductoResponseDTO;
import cl.smartlogix.inventario.repository.ProductoRepository;
import cl.smartlogix.inventario.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;
    private final ProductoRepository productoRepository;

    // Crear un nuevo producto con validación de SKU único
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoResponseDTO createProducto(@Valid @RequestBody ProductoRequestDTO request) {
        return productoService.createProducto(request);
    }

    // Actualizar un producto por ID con validación de SKU único
    @PutMapping("/{id}")
    public ProductoResponseDTO updateProducto(@PathVariable Long id, @Valid @RequestBody ProductoRequestDTO request) {
        return productoService.updateProducto(id, request);
    }

    // Eliminar un producto por ID con manejo de excepciónes
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProducto(@PathVariable Long id) {
        productoService.deleteProducto(id);
    }

    // Obtener un producto por ID con manejo de excepciones
    @GetMapping("/{id}")
    public ProductoResponseDTO getProductoById(@PathVariable Long id) {
        return productoService.getProductoById(id);
    }

    // Obtener todos los productos con manejo de excepciones y filtrado dinámico
    @GetMapping
    public List<ProductoResponseDTO> getAllProductos(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Boolean conStock,
            @RequestParam(required = false) Integer precioMin,
            @RequestParam(required = false) Integer precioMax,
            @RequestParam(required = false) Boolean destacado,
            @RequestParam(required = false) Boolean novedad) {
        
        if (nombre != null || categoriaId != null || conStock != null || precioMin != null || precioMax != null || destacado != null || novedad != null) {
            return productoService.getProductosFiltrados(nombre, categoriaId, conStock, precioMin, precioMax, destacado, novedad);
        }
        return productoService.getAllProductos();
    }

    // Método para buscar por slug
    @GetMapping("/slug/{slug}")
    public ProductoResponseDTO getProductoBySlug(@PathVariable String slug) {
        return productoService.getProductoBySlug(slug);
    }

    // Método para buscar por SKU
    @GetMapping("/sku/{sku}")
    public ProductoResponseDTO getProductoBySku(@PathVariable String sku) {
        return productoService.getProductoBySku(sku);
    }

    // Obtener productos por categoría (incluyendo subcategorías directas)
    @GetMapping("/categoria/{categoriaId}")
    public List<ProductoResponseDTO> getProductosByCategoria(@PathVariable Long categoriaId) {
        return productoService.getProductosByCategoria(categoriaId);
    }

    @GetMapping("/mapa-categorias")
    public List<MapaCategoriaResponseDTO> getMapaCategorias() {
        return productoRepository.findMapaCategorias();
    }
}