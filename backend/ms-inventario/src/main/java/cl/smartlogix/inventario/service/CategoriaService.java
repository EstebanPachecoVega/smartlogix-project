package cl.smartlogix.inventario.service;

import cl.smartlogix.inventario.dto.request.CategoriaRequestDTO;
import cl.smartlogix.inventario.dto.request.ReordenarCategoriaDTO;
import cl.smartlogix.inventario.dto.response.CategoriaResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoriaService {
    CategoriaResponseDTO createCategoria(CategoriaRequestDTO request);
    CategoriaResponseDTO updateCategoria(Long id, CategoriaRequestDTO request);
    void deleteCategoria(Long id);
    CategoriaResponseDTO getCategoriaById(Long id);
    CategoriaResponseDTO getCategoriaBySlug(String slug);
    List<CategoriaResponseDTO> getAllCategorias();
    Page<CategoriaResponseDTO> getAllCategorias(Pageable pageable);
    List<CategoriaResponseDTO> getCategoriasPrincipales();
    void reordenar(List<ReordenarCategoriaDTO> ordenes);
}