# Verificación de esta entrega

Completado en el entorno de revisión:
- Compilación de todos los Java para Java 17 con API de Tomcat 9.
- Compilación de todas las páginas JSP con Jasper de Tomcat 9: cero errores.
- 18 pruebas Java: hash, escape HTML, cálculos, validación de entradas y roles,
  rechazo del módulo externo, bloqueo del cobro y venta para Almacenero y
  bloqueo del despacho para Cajero.

Pendiente en tu instalación Windows/XAMPP:
- Importar la base nueva o ejecutar la migración sobre una copia respaldada.
- Iniciar sesión con cada rol y comprobar sus menús y acceso por URL directa.
- Registrar una venta, confirmar pago, preparar y entregar.
- Cancelar una venta pendiente y comprobar devolución de stock.
- Consultar dashboard, reportes, movimientos y exportar CSV.
- Confirmar que el historial anterior se conserva después de migrar.

No se ejecutó MySQL/MariaDB en el entorno de revisión. La compilación no equivale
a una prueba integral contra tu base real. Los pagos son simulados.

Ejecutar las pruebas sin base: compilar test/Pruebas.java junto con src/java,
incluyendo servlet-api.jar de Tomcat 9; ejecutar la clase Pruebas.
