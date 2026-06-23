package cl.smartlogix.pedidos.client;

import cl.smartlogix.pedidos.exception.DomainException;
import cl.smartlogix.pedidos.exception.ResourceNotFoundException;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FeignErrorDecoderTest {

    private final FeignErrorDecoder decoder = new FeignErrorDecoder();

    private Response createMockResponse(int status, String body) {
        return Response.builder()
                .status(status)
                .reason("Mock")
                .headers(Collections.emptyMap())
                .body(body.getBytes(StandardCharsets.UTF_8))
                .request(createMockRequest())
                .build();
    }

    private feign.Request createMockRequest() {
        return feign.Request.create(
                feign.Request.HttpMethod.GET,
                "http://localhost",
                Collections.emptyMap(),
                (byte[]) null,
                null,
                null
        );
    }

    @Test
    void decode_notFound_returnsResourceNotFoundException() {
        String body = "{\"status\":404,\"detail\":\"Producto no encontrado\"}";
        Response response = createMockResponse(404, body);

        Exception result = decoder.decode("InventarioClient#metodo()", response);

        assertInstanceOf(ResourceNotFoundException.class, result);
        assertEquals("Producto no encontrado", result.getMessage());
    }

    @Test
    void decode_unprocessableEntity_returnsDomainException() {
        String body = "{\"status\":422,\"detail\":\"Stock insuficiente\"}";
        Response response = createMockResponse(422, body);

        Exception result = decoder.decode("InventarioClient#metodo()", response);

        assertInstanceOf(DomainException.class, result);
        assertEquals("Stock insuficiente", result.getMessage());
    }

    @Test
    void decode_otherStatus_returnsResponseStatusException() {
        String body = "{\"status\":500,\"detail\":\"Error interno\"}";
        Response response = createMockResponse(500, body);

        Exception result = decoder.decode("InventarioClient#metodo()", response);

        assertInstanceOf(ResponseStatusException.class, result);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ((ResponseStatusException) result).getStatusCode());
    }

    @Test
    void decode_nullBody_usesDefaultDecoder() {
        Response response = Response.builder()
                .status(500)
                .reason("Mock")
                .headers(Collections.emptyMap())
                .body((byte[]) null)
                .request(createMockRequest())
                .build();

        Exception result = decoder.decode("InventarioClient#metodo()", response);

        assertNotNull(result);
    }

    @Test
    void decode_blankBody_usesDefaultDecoder() {
        Response response = createMockResponse(500, "");

        Exception result = decoder.decode("InventarioClient#metodo()", response);

        assertNotNull(result);
    }
}
