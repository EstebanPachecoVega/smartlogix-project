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
import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping
    public List<PedidoResponseDTO> listarPedidos() {
        return pedidoService.listarPedidos()
                .stream()
                .map(pedidoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public PedidoResponseDTO obtenerPedidoPorId(@PathVariable Long id) {
        Pedido pedido = pedidoService.obtenerPedidoPorId(id);
        return pedidoMapper.toResponseDTO(pedido);
    }

    @GetMapping("/orden/{numeroOrden}")
    public PedidoResponseDTO obtenerPedidoPorNumeroOrden(@PathVariable String numeroOrden) {
        Pedido pedido = pedidoService.obtenerPedidoPorNumeroOrden(numeroOrden);
        return pedidoMapper.toResponseDTO(pedido);
    }
}