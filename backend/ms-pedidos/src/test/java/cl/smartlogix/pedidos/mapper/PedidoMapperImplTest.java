package cl.smartlogix.pedidos.mapper;

import cl.smartlogix.pedidos.dto.response.PedidoResponseDTO;
import cl.smartlogix.pedidos.entity.DetallePedido;
import cl.smartlogix.pedidos.entity.EstadoPedido;
import cl.smartlogix.pedidos.entity.Pedido;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PedidoMapperImplTest {

    private final PedidoMapperImpl mapper = new PedidoMapperImpl();

    @Test
    void toResponseDTO_mapsAllFields() {
        Pedido pedido = Pedido.builder()
                .id(1L)
                .numeroOrden("ORD-001")
                .fechaPedido(LocalDateTime.of(2026, 6, 22, 10, 0))
                .estado(EstadoPedido.PENDIENTE)
                .totalCompra(25000)
                .destinatario("Juan Pérez")
                .calle("Av. Siempre Viva")
                .numero("742")
                .comuna("Santiago")
                .ciudad("Santiago")
                .codigoPostal("12345")
                .metodoEnvio("Despacho")
                .plataforma("DESKTOP")
                .build();

        DetallePedido detalle = DetallePedido.builder()
                .id(10L)
                .productoId(100L)
                .sku("SKU-001")
                .nombreProducto("Producto Test")
                .precioUnitario(12500)
                .cantidad(2)
                .subtotal(25000)
                .imagenPrincipal("img.jpg")
                .build();
        pedido.agregarDetalle(detalle);

        PedidoResponseDTO dto = mapper.toResponseDTO(pedido);

        assertEquals(1L, dto.getId());
        assertEquals("ORD-001", dto.getNumeroOrden());
        assertEquals("PENDIENTE", dto.getEstado());
        assertEquals(25000, dto.getTotalCompra());
        assertEquals("Juan Pérez", dto.getDestinatario());
        assertEquals("Av. Siempre Viva", dto.getCalle());
        assertEquals("742", dto.getNumero());
        assertEquals("Santiago", dto.getComuna());
        assertEquals("Santiago", dto.getCiudad());
        assertEquals("12345", dto.getCodigoPostal());
        assertEquals("Despacho", dto.getMetodoEnvio());
        assertEquals("DESKTOP", dto.getPlataforma());
        assertNotNull(dto.getFechaPedido());

        assertNotNull(dto.getDetalles());
        assertEquals(1, dto.getDetalles().size());
        PedidoResponseDTO.DetalleResponseDTO detalleDto = dto.getDetalles().get(0);
        assertEquals(10L, detalleDto.getId());
        assertEquals(100L, detalleDto.getProductoId());
        assertEquals("SKU-001", detalleDto.getSku());
        assertEquals("Producto Test", detalleDto.getNombreProducto());
        assertEquals(12500, detalleDto.getPrecioUnitario());
        assertEquals(2, detalleDto.getCantidad());
        assertEquals(25000, detalleDto.getSubtotal());
        assertEquals("img.jpg", detalleDto.getImagenPrincipal());
    }

    @Test
    void toResponseDTO_noDetalles_returnsEmptyList() {
        Pedido pedido = Pedido.builder()
                .id(1L)
                .numeroOrden("ORD-001")
                .estado(EstadoPedido.PENDIENTE)
                .totalCompra(10000)
                .build();

        PedidoResponseDTO dto = mapper.toResponseDTO(pedido);

        assertNotNull(dto.getDetalles());
        assertTrue(dto.getDetalles().isEmpty());
    }

    @Test
    void toResponseDTO_nullEstado_returnsNull() {
        Pedido pedido = Pedido.builder()
                .id(1L)
                .numeroOrden("ORD-001")
                .totalCompra(5000)
                .build();

        PedidoResponseDTO dto = mapper.toResponseDTO(pedido);

        assertNull(dto.getEstado());
    }

    @Test
    void toResponseDTO_nullPedido_returnsNull() {
        assertNull(mapper.toResponseDTO(null));
    }

    @Test
    void detallePedidoToDetalleResponseDTO_null_returnsNull() {
        assertNull(mapper.detallePedidoToDetalleResponseDTO(null));
    }

    @Test
    void detallePedidoListToDetalleResponseDTOList_null_returnsNull() {
        assertNull(mapper.detallePedidoListToDetalleResponseDTOList(null));
    }
}
