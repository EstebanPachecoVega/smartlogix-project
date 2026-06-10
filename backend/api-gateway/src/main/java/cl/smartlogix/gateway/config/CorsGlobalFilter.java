package cl.smartlogix.gateway.config;

/**
 * La configuración CORS del api-gateway ahora se maneja exclusivamente en
 * {@link SecurityConfig#corsConfigurationSource()} para evitar conflictos
 * entre CorsWebFilter y la configuración de Spring Security WebFlux.
 *
 * @deprecated Eliminada en favor de corsConfigurationSource() en SecurityConfig
 */
@Deprecated
public class CorsGlobalFilter {
    // Intencionalmente vacía — ver SecurityConfig
}
