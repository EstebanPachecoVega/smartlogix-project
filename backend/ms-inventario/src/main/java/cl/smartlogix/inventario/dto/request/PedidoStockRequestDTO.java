package cl.smartlogix.inventario.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PedidoStockRequestDTO {
    
    @NotEmpty(message = "La lista de ítems no puede estar vacía")
    @Valid
    private List<ReservarStockRequestDTO> items;

    private String reservaId;
}