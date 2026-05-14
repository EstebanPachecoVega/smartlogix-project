package cl.smartlogix.envios.mapper;

import cl.smartlogix.envios.dto.response.EnvioResponseDTO;
import cl.smartlogix.envios.entity.Envio;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EnvioMapper {
    EnvioResponseDTO toResponseDTO(Envio envio);
}