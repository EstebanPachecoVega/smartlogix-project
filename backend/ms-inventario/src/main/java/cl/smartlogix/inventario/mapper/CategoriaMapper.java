package cl.smartlogix.inventario.mapper;

import cl.smartlogix.inventario.dto.request.CategoriaRequestDTO;
import cl.smartlogix.inventario.dto.response.CategoriaResponseDTO;
import cl.smartlogix.inventario.entity.Categoria;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CategoriaMapper {

    @Mapping(source = "padreId", target = "padre", qualifiedByName = "idToPadre")
    Categoria toEntity(CategoriaRequestDTO dto);

    @Mapping(source = "padre.id", target = "padreId")
    @Mapping(source = "padre.nombre", target = "padreNombre")
    CategoriaResponseDTO toResponseDTO(Categoria categoria);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "padreId", target = "padre", qualifiedByName = "idToPadre")
    void updateEntity(@MappingTarget Categoria categoria, CategoriaRequestDTO dto);

    @Named("idToPadre")
    default Categoria idToPadre(Long id) {
        if (id == null) {
            return null;
        }
        Categoria padre = new Categoria();
        padre.setId(id);
        return padre;
    }
}