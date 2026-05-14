package cl.smartlogix.pedidos.mapper;

import cl.smartlogix.pedidos.dto.response.PedidoResponseDTO;
import cl.smartlogix.pedidos.entity.Pedido;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PedidoMapper {
    PedidoResponseDTO toResponseDTO(Pedido pedido);
}