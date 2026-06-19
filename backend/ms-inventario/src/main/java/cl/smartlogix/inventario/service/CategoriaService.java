package cl.smartlogix.inventario.service;

import cl.smartlogix.inventario.dto.request.CategoriaRequestDTO;
import cl.smartlogix.inventario.dto.request.ReordenarCategoriaDTO;
import cl.smartlogix.inventario.dto.response.CategoriaResponseDTO;

import java.util.List;

public interface CategoriaService {
    CategoriaResponseDTO createCategoria(CategoriaRequestDTO request);
    CategoriaResponseDTO updateCategoria(Long id, CategoriaRequestDTO request);
    void deleteCategoria(Long id);
    CategoriaResponseDTO getCategoriaById(Long id);
    CategoriaResponseDTO getCategoriaBySlug(String slug);
    List<CategoriaResponseDTO> getAllCategorias();
    List<CategoriaResponseDTO> getCategoriasPrincipales();
    void reordenar(List<ReordenarCategoriaDTO> ordenes);
}