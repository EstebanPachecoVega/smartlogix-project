package cl.smartlogix.inventario.mapper;

import cl.smartlogix.inventario.dto.request.ProductoRequestDTO;
import cl.smartlogix.inventario.dto.response.ProductoResponseDTO;
import cl.smartlogix.inventario.dto.response.StockResponseDTO;
import cl.smartlogix.inventario.entity.Categoria;
import cl.smartlogix.inventario.entity.Producto;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductoMapper {

    @Mapping(source = "categoriaId", target = "categoria", qualifiedByName = "idToCategoria")
    Producto toEntity(ProductoRequestDTO dto);

    @Mapping(source = "categoria.id", target = "categoriaId")
    @Mapping(source = "categoria.nombre", target = "categoriaNombre")
    ProductoResponseDTO toResponseDTO(Producto producto);

    List<ProductoResponseDTO> toResponseDTOList(List<Producto> productos);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "categoriaId", target = "categoria", qualifiedByName = "idToCategoria")
    void updateEntity(@MappingTarget Producto producto, ProductoRequestDTO dto);

    @Named("idToCategoria")
    default Categoria idToCategoria(Long id) {
        if (id == null)
            return null;
        Categoria categoria = new Categoria();
        categoria.setId(id);
        return categoria;
    }

    @Mapping(source = "id", target = "productoId")
    @Mapping(source = "cantidad", target = "stockDisponible")
    StockResponseDTO toStockResponseDTO(Producto producto);
}