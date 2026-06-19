package cl.smartlogix.inventario.controller;

import cl.smartlogix.inventario.dto.request.CategoriaRequestDTO;
import cl.smartlogix.inventario.dto.request.ReordenarCategoriaDTO;
import cl.smartlogix.inventario.dto.response.CategoriaResponseDTO;
import cl.smartlogix.inventario.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaResponseDTO createCategoria(@Valid @RequestBody CategoriaRequestDTO request) {
        return categoriaService.createCategoria(request);
    }

    @PutMapping("/{id}")
    public CategoriaResponseDTO updateCategoria(@PathVariable Long id, @Valid @RequestBody CategoriaRequestDTO request) {
        return categoriaService.updateCategoria(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategoria(@PathVariable Long id) {
        categoriaService.deleteCategoria(id);
    }

    @GetMapping("/{id}")
    public CategoriaResponseDTO getCategoriaById(@PathVariable Long id) {
        return categoriaService.getCategoriaById(id);
    }

    @GetMapping("/slug/{slug}")
    public CategoriaResponseDTO getCategoriaBySlug(@PathVariable String slug) {
        return categoriaService.getCategoriaBySlug(slug);
    }

    @GetMapping
    public List<CategoriaResponseDTO> getAllCategorias() {
        return categoriaService.getAllCategorias();
    }

    @GetMapping("/principales")
    public List<CategoriaResponseDTO> getCategoriasPrincipales() {
        return categoriaService.getCategoriasPrincipales();
    }

    @PatchMapping("/reordenar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reordenar(@Valid @RequestBody List<ReordenarCategoriaDTO> ordenes) {
        categoriaService.reordenar(ordenes);
    }
}