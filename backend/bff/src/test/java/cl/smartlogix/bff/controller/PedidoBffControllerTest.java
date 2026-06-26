package cl.smartlogix.bff.controller;

import cl.smartlogix.bff.client.GatewayClient;
import cl.smartlogix.bff.config.TestSecurityConfig;
import cl.smartlogix.bff.dto.request.CrearPedidoRequestDTO;
import cl.smartlogix.bff.dto.response.PagedResponse;
import cl.smartlogix.bff.dto.response.PedidoResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebFluxTest(PedidoBffController.class)
@Import(TestSecurityConfig.class)
class PedidoBffControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private GatewayClient gatewayClient;

    @Test
    void crear_returnsCreated() {
        var request = new CrearPedidoRequestDTO();
        request.setUsuarioId(1L);
        request.setDestinatario("Test");
        request.setCalle("Calle");
        request.setNumero("123");
        request.setComuna("Comuna");
        request.setCiudad("Ciudad");
        request.setMetodoEnvio("standard");
        request.setItems(List.of(new CrearPedidoRequestDTO.DetalleDTO(
                1L, "SKU-001", "Producto", 1000, 1, null
        )));

        var response = new PedidoResponseDTO();
        when(gatewayClient.crearPedido(any(), eq("mock-jwt"), any(), any()))
                .thenReturn(Mono.just(response));

        webTestClient.post().uri("/bff/pedidos")
                .header("Authorization", "Bearer mock-jwt")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void listar_returnsOrders() {
        PagedResponse<PedidoResponseDTO> paged = new PagedResponse<>();
        paged.setContent(List.of(new PedidoResponseDTO()));
        when(gatewayClient.listarPedidos(eq("mock-jwt"), any(), anyInt(), anyInt()))
                .thenReturn(Mono.just(paged));

        webTestClient.get().uri("/bff/pedidos")
                .header("Authorization", "Bearer mock-jwt")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void obtener_returnsOrder() {
        when(gatewayClient.obtenerPedido(eq(1L), eq("mock-jwt"), any()))
                .thenReturn(Mono.just(new PedidoResponseDTO()));

        webTestClient.get().uri("/bff/pedidos/1")
                .header("Authorization", "Bearer mock-jwt")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void listar_withoutAuth_returnsUnauthorized() {
        webTestClient.get().uri("/bff/pedidos")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void listar_withInvalidAuth_returnsUnauthorized() {
        webTestClient.get().uri("/bff/pedidos")
                .header("Authorization", "InvalidToken")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
