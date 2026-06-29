import axios from 'axios';

const BFF_URL = 'http://localhost:8084/bff/pedidos';

async function crearPedido(usuarioId, productoId, cantidad, idempotencyKey) {
  try {
    const payload = {
      usuarioId,
      destinatario: "Test " + usuarioId,
      calle: "Calle Falsa",
      numero: "123",
      comuna: "Santiago",
      ciudad: "Santiago",
      metodoEnvio: "standard",
      items: [
        {
          productoId,
          sku: "SKU" + productoId,
          nombreProducto: "Producto " + productoId,
          precioUnitario: 100,
          cantidad
        }
      ]
    };
    const headers = {
      'Content-Type': 'application/json',
      'Idempotency-Key': idempotencyKey,
      'Authorization': 'Bearer dev-token'  // Token mock
    };
    const res = await axios.post(BFF_URL, payload, { headers });
    console.log(`✅ Usuario ${usuarioId} (${idempotencyKey}) éxito - Pedido ID: ${res.data.id}, Estado: ${res.data.estado}`);
    return res.data;
  } catch (err) {
    console.error(`❌ Usuario ${usuarioId} (${idempotencyKey}) falló:`, err.response?.data?.detail || err.message);
  }
}

async function simularConcurrencia() {
  console.log("🚀 Simulando dos compras simultáneas con stock inicial 5, ambos piden 5...\n");
  const p1 = crearPedido(1, 1, 5, "idemp-001");
  const p2 = crearPedido(2, 1, 5, "idemp-002");
  await Promise.allSettled([p1, p2]);
  console.log("\n✅ Simulación completada.");
}

simularConcurrencia();