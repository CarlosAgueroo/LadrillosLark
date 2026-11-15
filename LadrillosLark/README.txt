Ladrillos Lark · Sistema interno de ventas e inventario

Ladrillos Lark es una aplicación web que permite al personal de la empresa registrar ventas, controlar el inventario y dar seguimiento a los pedidos hasta su entrega. El acceso requiere una cuenta interna. Los pagos se simulan con fines académicos.

##Roles y funciones

| Rol          | Funciones |

| Administrador | Gestiona las cuentas del personal, los productos, las categorías y los proveedores. Consulta reportes. |
| Cajero       | Registra ventas desde el catálogo, confirma pagos y gestiona el rechazo o la cancelación de ventas. |
| Almacenero    | Registra entradas y ajustes de inventario, prepara los pedidos y actualiza su estado hasta la entrega. |

Los tres roles pueden consultar productos y pedidos y actualizar sus datos de contacto.

## Requisitos

- JDK 17 o superior.
- NetBeans con soporte para Java Web.
- Apache Tomcat 9.
- MySQL o MariaDB. Para ejecutar el proyecto en Windows, puedes utilizar XAMPP con phpMyAdmin.

## Instalación y ejecución

1. Inicia MySQL desde el panel de XAMPP.
2. Abre phpMyAdmin e importa `database/bd_ladrillos_lark.sql`. Este script crea las tablas y las cuentas de demostración.
3. Revisa la conexión en `src/java/config/Conexion.java`. Por defecto, la aplicación utiliza `localhost:3306`, la base `bd_ladrillos_lark`, el usuario `root` y una contraseña vacía. Si tu base se llama `bd_ladrillos_lark_interno`, ajusta ese nombre en la URL de conexión. También puedes configurar `LARK_DB_URL`, `LARK_DB_USER` y `LARK_DB_PASSWORD` como variables de entorno.
4. En NetBeans, abre la carpeta `LadrillosLark` que contiene `build.xml` y selecciona Apache Tomcat 9 como servidor.
5. En **Propiedades → Ejecutar**, establece `/app/login` como URL relativa.
6. Selecciona **Limpiar y construir** y luego **Ejecutar**.

Si Tomcat utiliza el puerto `8082`, abre [Ladrillos Lark en tu equipo](http://localhost:8082/LadrillosLark/app/login). Si configuraste otro puerto, sustituye `8082` en la dirección.

## Cuentas de demostración local

| Rol | Usuario |
| --- | --- |
| Administrador | `admin` |
| Cajero | `cajero` |
| Almacenero | `almacenero` |

La contraseña inicial de las tres cuentas es `Lark2026!`. Cámbiala antes de publicar la aplicación en Internet.

## Cómo probar una venta

1. Ingresa como **Cajero** y abre el catálogo. Selecciona un producto y pulsa **Agregar a la venta**.
2. Abre **Nueva venta**, revisa los productos y selecciona **Registrar venta**.
3. En **Pedidos**, localiza la venta y confirma el pago simulado.
4. Ingresa como **Almacenero**, abre el pedido y actualiza su estado según la modalidad de entrega.

| Modalidad | Estados del pedido |
| --- | --- |
| Recojo | `PREPARANDO` → `ENTREGADO` |
| Delivery | `PREPARANDO` → `EN_CAMINO` → `ENTREGADO` |

Para marcar un pedido como `ENTREGADO`, el pago debe estar confirmado.
