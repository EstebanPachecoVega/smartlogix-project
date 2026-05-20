export interface Producto {
  id: number;
  sku: string;
  nombre: string;
  slug?: string;
  descripcion?: string;
  categoriaId: number;
  categoriaNombre?: string;
  precio: number;
  cantidad: number;
  imagenPrincipal?: string;
  imagenes?: string[];
  destacado?: boolean;
  novedad?: boolean;
  activo?: boolean;
  fechaCreacion?: string;
  fechaActualizacion?: string;
}

export interface Categoria {
  id: number;
  nombre: string;
  slug: string;
  descripcion?: string;
  padreId?: number;
  padreNombre?: string;
  ordenVisual?: number;
  activo: boolean;
  fechaCreacion?: string;
  fechaActualizacion?: string;
}

export interface ItemCarrito {
  producto: Producto;
  cantidad: number;
}

export interface PedidoRequest {
  usuarioId: number;
  destinatario: string;
  calle: string;
  numero: string;
  comuna: string;
  ciudad: string;
  codigoPostal?: string;
  metodoEnvio: string;
  pesoKg?: number;
  dimensiones?: string;
  items: {
    productoId: number;
    sku: string;
    nombreProducto: string;
    precioUnitario: number;
    cantidad: number;
  }[];
}

export interface PedidoResponse {
  id: number;
  numeroOrden: string;
  estado: string;
  totalCompra: number;
  fechaPedido?: string;
  detalles?: DetallePedido[];
}

export interface DetallePedido {
  id: number;
  productoId: number;
  sku: string;
  nombreProducto: string;
  precioUnitario: number;
  cantidad: number;
  subtotal: number;
}

export interface Envio {
  id: number;
  pedidoId: number;
  usuarioId: number;
  destinatario: string;
  calle: string;
  numero: string;
  comuna: string;
  ciudad: string;
  codigoPostal: string;
  metodoEnvio: string;
  empresaLogistica: string;
  numeroTracking: string;
  fechaEstimadaEntrega: string;
  estadoEnvio: string;
  pesoKg: number;
  dimensiones: string;
  fechaCreacion: string;
}