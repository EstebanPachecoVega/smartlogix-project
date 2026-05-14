package cl.smartlogix.pedidos.controller;

import cl.smartlogix.pedidos.dto.request.CrearPedidoRequestDTO;
import cl.smartlogix.pedidos.dto.response.PedidoResponseDTO;
import cl.smartlogix.pedidos.entity.Pedido;
import cl.smartlogix.pedidos.mapper.PedidoMapper;
import cl.smartlogix.pedidos.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {
    private final PedidoService pedidoService;
    private final PedidoMapper pedidoMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponseDTO crearPedido(@Valid @RequestBody CrearPedidoRequestDTO request) {
        Pedido pedido = pedidoService.crearPedido(request);
        return pedidoMapper.toResponseDTO(pedido);
    }
}