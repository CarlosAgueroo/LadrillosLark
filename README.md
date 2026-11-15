# Ladrillos Lark · Sistema interno de ventas e inventario

El proyecto Ladrillos Lark surgió de una necesidad de la ferretería Lark, ubicada en San Juan de Miraflores, Lima. El personal anotaba en papel las ventas y las existencias de materiales como ladrillos, cemento, arena y fierros. Para saber cuánto material quedaba disponible, debía revisar esos apuntes.

Como parte de un proyecto académico, se desarrolló una aplicación web para reunir las ventas, los pedidos y el inventario en un mismo sistema. El cajero registra la venta, el almacenero prepara el pedido para su recojo o despacho y el administrador consulta los reportes del negocio.
## Roles y funciones

| Rol | Funciones |
| --- | --- |
| Administrador | Gestiona las cuentas del personal, los productos, las categorías y los proveedores. Consulta reportes. |
| Cajero | Registra ventas desde el catálogo, confirma pagos y gestiona el rechazo o la cancelación de ventas. |
| Almacenero | Registra entradas y ajustes de inventario, prepara los pedidos y actualiza su estado hasta la entrega. |

Los tres roles pueden consultar productos y pedidos y actualizar sus datos de contacto.

## Requisitos

- JDK 17 o superior.
- NetBeans con soporte para Java Web.
- Apache Tomcat 9.
- MySQL o MariaDB. Para ejecutar el proyecto en Windows, puedes utilizar XAMPP con phpMyAdmin.
