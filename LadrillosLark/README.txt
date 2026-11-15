LADRILLOS LARK — SISTEMA INTERNO
Roles: Administrador, Cajero y Almacenero.

REQUISITOS
JDK 17 o superior, NetBeans Java Web, Tomcat 9 y MySQL/MariaDB (XAMPP).
Se conserva la conexión de tu proyecto: localhost:3306/bd_ladrillos_lark,
usuario root y contraseña vacía. Se puede cambiar en src/java/config/Conexion.java
o con LARK_DB_URL, LARK_DB_USER y LARK_DB_PASSWORD.

SI YA TIENES LA BASE DEL PROYECTO ANTERIOR
1. Detén Tomcat y exporta un respaldo completo desde phpMyAdmin.
2. En phpMyAdmin importa SOLO database/actualizar_sistema_interno.sql, una vez.
   Conserva productos, existencias, ventas, pedidos y movimientos.
   Elimina la tabla Cliente y sus vínculos con ventas y pedidos.
   Desactiva las antiguas cuentas externas y les quita el rol; conserva sus filas
   únicamente para mantener las referencias del historial. No aparecen en Usuarios.
   No convierte ninguna cuenta externa en personal de la empresa.
   El antiguo usuario vendedor conserva su contraseña y nombre de acceso,
   pero ahora pertenece al rol Cajero. El Administrador puede renombrarlo.
   El SQL cambia el esquema: si falla, detente y restaura el respaldo antes de repetir.
3. Abre esta carpeta LadrillosLark en NetBeans. Selecciona tu Tomcat 9 y JDK.
4. Propiedades > Ejecutar > URL relativa: /app/login.
5. Limpiar y construir; luego Ejecutar. No reutilices el WAR anterior.
   También puedes desplegar dist/LadrillosLark.war en Tomcat 9.
6. Abre http://localhost:8082/LadrillosLark/app/login si tu Tomcat usa 8082.
   Si usa otro puerto, reemplaza 8082 por ese puerto.

SI EMPIEZAS CON UNA BASE VACÍA
Importa SOLO database/bd_ladrillos_lark.sql. No ejecutes la migración después.
Cuentas: admin / cajero / almacenero. Contraseña de las tres: Lark2026!

RECORRIDO
Administrador: cuentas del personal, productos, categorías, proveedores y reportes.
Cajero: catálogo interno > producto > Agregar a la venta > Nueva venta >
Registrar venta > Pedidos > confirmar pago. También rechaza/cancela ventas.
Almacenero: Inventario para entradas/ajustes; Pedidos para preparar y despachar.
Recojo: PREPARANDO > ENTREGADO. Delivery: PREPARANDO > EN_CAMINO > ENTREGADO.
La entrega exige pago confirmado. Los pagos siguen siendo simulaciones académicas.
Todos pueden consultar productos, pedidos y actualizar sus datos de contacto.
No existe registro público, módulo Clientes ni compras desde cuentas externas.

PRUEBAS
Consultar docs/Checklist-pruebas.md para las verificaciones y sus límites.
Las capturas y diagramas antiguos se retiraron por corresponder al alcance anterior.
