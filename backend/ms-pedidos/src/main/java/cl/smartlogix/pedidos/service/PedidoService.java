package cl.smartlogix.pedidos.service;

import cl.smartlogix.pedidos.dto.request.CrearPedidoRequestDTO;
import cl.smartlogix.pedidos.entity.Pedido;

public interface PedidoService {
    Pedido crearPedido(CrearPedidoRequestDTO request);
}