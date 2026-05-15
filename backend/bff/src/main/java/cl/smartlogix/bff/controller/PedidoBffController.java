package cl.smartlogix.bff.controller;

import cl.smartlogix.bff.dto.request.CrearPedidoRequestDTO;
import cl.smartlogix.bff.dto.response.PedidoResponseDTO;
import cl.smartlogix.bff.service.PedidoBffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/bff/pedidos")
@RequiredArgsConstructor
public class PedidoBffController {
    private final PedidoBffService pedidoBffService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<PedidoResponseDTO> crearPedido(@Valid @RequestBody CrearPedidoRequestDTO request) {
        return pedidoBffService.crearPedido(request);
    }

    @GetMapping("/{id}")
    public Mono<PedidoResponseDTO> obtenerPedido(@PathVariable Long id) {
        return pedidoBffService.obtenerPedidoPorId(id);
    }
}