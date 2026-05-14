package cl.smartlogix.inventario.mapper;

import cl.smartlogix.inventario.dto.response.StockResponseDTO;
import cl.smartlogix.inventario.entity.Producto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductoMapper {
    @Mapping(source = "id", target = "productoId")
    StockResponseDTO toStockResponseDTO(Producto producto);
}