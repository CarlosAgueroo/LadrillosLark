# Sistema interno Ladrillos Lark
Objetivo: gestionar productos, ventas, pedidos e inventario con acceso exclusivo del personal.

| Función | Administrador | Cajero | Almacenero |
|---|---|---|---|
| Consultar productos y pedidos | Sí | Sí | Sí |
| Registrar ventas, cobrar, rechazar y cancelar | Sí | Sí | No |
| Preparar, despachar y entregar pedidos | Sí | No | Sí |
| Movimientos de inventario | Sí | No | Sí |
| Usuarios, categorías, productos y proveedores | Sí | No | No |
| Dashboard y reportes | Sí | No | No |

No existe autorregistro ni catálogo público. Se conserva el detalle temporal de venta en sesión.
Las ventas reservan stock; cancelar devuelve las unidades. Entregar requiere pago confirmado.
Delivery conserva dirección, teléfono y referencia de entrega, sin crear una cuenta externa.
Los pagos continúan siendo simulados, sin integración bancaria ni SUNAT.
