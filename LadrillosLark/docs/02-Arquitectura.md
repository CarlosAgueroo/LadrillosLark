# Arquitectura actual
Presentación: JSP bajo WEB-INF, CSS y JavaScript locales.
Controlador: AppServlet valida parámetros y enruta solicitudes.
Acceso a datos: DAO con sentencias preparadas y transacciones JDBC.
Persistencia: MySQL/MariaDB; tablas rol, usuario, categoria, producto, proveedor,
producto_proveedor, inventario, movimiento_inventario, venta, detalle_venta,
pedido, detalle_pedido, pago y delivery.

SeguridadFiltro exige autenticación para /app/* salvo /login, verifica que la cuenta
siga activa, limita los módulos por rol y valida CSRF. UsuarioDAO solo autentica
roles 1, 2 y 3. GestionDAO y Validar impiden asignar roles externos.
PedidoDAO separa acciones de cobro de acciones de preparación y despacho.
Solo el Administrador crea cuentas del personal. Las cuentas históricas sin rol
quedan desactivadas, fuera del mantenimiento y sin posibilidad de iniciar sesión.
