package cl.smartlogix.pedidos.service;

import cl.smartlogix.pedidos.dto.request.CrearPedidoRequestDTO;
import cl.smartlogix.pedidos.entity.Pedido;
import java.util.List;

public interface PedidoService {
    Pedido crearPedido(CrearPedidoRequestDTO request);

    Pedido obtenerPedidoPorId(Long id);

    Pedido obtenerPedidoPorNumeroOrden(String numeroOrden);

    List<Pedido> listarPedidos();
}