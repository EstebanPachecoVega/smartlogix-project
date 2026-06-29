package cl.smartlogix.envios.entity;

public enum EstadoEnvio {
    PENDIENTE("Pendiente"),              // 0: Pedido registrado pero aún no procesado
    PREPARANDO("Preparando"),            // 1: Empaquetado o preparación en almacén
    ENVIADO("Enviado"),                  // 2: Salida del almacén / en manos del transportista
    EN_TRANSITO("En Tránsito"),          // 3: En ruta hacia el destino (seguimiento activo)
    EN_REPARTO("En Reparto"),            // 4: Con el repartidor para entrega final
    ENTREGADO("Entregado"),              // 5: Recibido por el destinatario
    INTENTO_FALLIDO("Intento Fallido"),  // 6: No se pudo entregar
    RETRASADO("Retrasado"),              // 7: Por clima, aduanas, logística, etc.
    DEVUELTO("Devuelto"),                // 8: Vuelve al remitente
    CANCELADO("Cancelado");              // 9: Anulado antes de ser enviado

    private String texto;

    EstadoEnvio(String texto) {
        this.texto = texto;
    }

    public String getTexto() {
        return texto;
    }
}