-- Solo para la base de la versión anterior. Detener Tomcat y hacer respaldo primero.
-- DDL confirma automáticamente en MySQL/MariaDB. Ejecutar una vez en phpMyAdmin.
USE bd_ladrillos_lark;
-- Conserva ventas, movimientos y cuentas históricas sin darles un rol interno.
ALTER TABLE usuario MODIFY rol_id INT NULL;
UPDATE usuario SET activo=0, rol_id=NULL WHERE rol_id=4;
UPDATE rol SET nombre='CAJERO' WHERE id=2;
-- Las claves foráneas se resuelven por metadatos para no depender de sus nombres.
SET @fk=(SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='venta' AND COLUMN_NAME='cliente_id' AND REFERENCED_TABLE_NAME='cliente' LIMIT 1);
SET @ddl=CONCAT('ALTER TABLE venta DROP FOREIGN KEY `',@fk,'`');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
ALTER TABLE venta DROP COLUMN cliente_id;
SET @fk=(SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='pedido' AND COLUMN_NAME='cliente_id' AND REFERENCED_TABLE_NAME='cliente' LIMIT 1);
SET @ddl=CONCAT('ALTER TABLE pedido DROP FOREIGN KEY `',@fk,'`');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
ALTER TABLE pedido DROP COLUMN cliente_id;
DROP TABLE cliente;
DELETE FROM rol WHERE id=4;
-- Se conserva el canal histórico de las ventas antiguas; las nuevas son presenciales.
-- Se conserva el nombre de acceso del antiguo vendedor. Su rol visible ya es Cajero.
