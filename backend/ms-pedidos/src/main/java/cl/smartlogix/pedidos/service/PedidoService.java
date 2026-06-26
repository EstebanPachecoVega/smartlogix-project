package cl.smartlogix.pedidos.service;

import cl.smartlogix.pedidos.dto.request.CrearPedidoRequestDTO;
import cl.smartlogix.pedidos.entity.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PedidoService {
    Pedido crearPedido(CrearPedidoRequestDTO request, String usuarioId, String idempotencyKey);

    Pedido obtenerPedidoPorId(Long id);

    Pedido obtenerPedidoPorNumeroOrden(String numeroOrden);

    List<Pedido> listarPedidos();

    Page<Pedido> listarPedidos(Pageable pageable);

    List<Pedido> listarPedidosPorUsuario(String usuarioId);

    Page<Pedido> listarPedidosPorUsuario(String usuarioId, Pageable pageable);

    void actualizarEstadoPorEnvio(Long pedidoId, String estadoEnvio);
}