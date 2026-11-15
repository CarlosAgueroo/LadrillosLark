package dao;

import java.util.*;
import modelo.*;

public class ReporteDAO {
  public Fila dashboard() throws Exception {
    return DB.filas(
            "SELECT (SELECT COALESCE(SUM(total),0) FROM venta WHERE estado='PAGADO' AND"
                + " DATE(fecha)=CURRENT_DATE) ventas_dia,(SELECT COALESCE(SUM(total),0) FROM venta"
                + " WHERE estado='PAGADO' AND fecha>=DATE_SUB(CURRENT_DATE,INTERVAL"
                + " WEEKDAY(CURRENT_DATE) DAY)) ventas_semana,(SELECT COALESCE(SUM(total),0) FROM"
                + " venta WHERE estado='PAGADO' AND YEAR(fecha)=YEAR(CURRENT_DATE) AND"
                + " MONTH(fecha)=MONTH(CURRENT_DATE)) ventas_mes,(SELECT COALESCE(SUM(total),0)"
                + " FROM venta WHERE estado='PAGADO') ingresos,(SELECT COUNT(*) FROM usuario WHERE activo=1 AND rol_id IN (1,2,3))"
                + " personal,(SELECT COUNT(*) FROM producto WHERE activo=1) productos,(SELECT"
                + " COUNT(*) FROM pedido WHERE estado NOT IN ('ENTREGADO','CANCELADO'))"
                + " pendientes,(SELECT COUNT(*) FROM pedido WHERE estado='ENTREGADO') entregados")
        .get(0);
  }

  public List<Fila> reporte(String tipo, String desde, String hasta) throws Exception {
    switch (tipo) {
      case "ventas":
        return DB.filas(
            "SELECT v.id,u.nombres"
                + " operador,v.canal,v.subtotal,v.descuento,v.costo_delivery,v.total,v.metodo,v.estado,v.fecha"
                + " FROM venta v JOIN usuario u ON u.id=v.usuario_id WHERE DATE(v.fecha) BETWEEN ?"
                + " AND ? ORDER BY v.id DESC",
            desde,
            hasta);
      case "inventario":
        return DB.filas(
            "SELECT m.id,p.nombre"
                + " producto,m.tipo,m.cantidad,m.stock_resultante,m.motivo,u.usuario,COALESCE(pr.razon_social,'')"
                + " proveedor,m.venta_id,m.fecha FROM movimiento_inventario m JOIN producto p ON"
                + " p.id=m.producto_id JOIN usuario u ON u.id=m.usuario_id LEFT JOIN proveedor pr"
                + " ON pr.id=m.proveedor_id WHERE DATE(m.fecha) BETWEEN ? AND ? ORDER BY m.id DESC",
            desde,
            hasta);
      case "stock":
      case "stock_bajo":
        return DB.filas(
            "SELECT p.id,p.codigo,p.nombre,i.stock,p.stock_minimo,p.precio,p.activo FROM producto p"
                + " JOIN inventario i ON i.producto_id=p.id WHERE (?='stock' OR"
                + " i.stock<=p.stock_minimo) ORDER BY i.stock",
            tipo);
      case "pedidos":
        return DB.filas(
            "SELECT p.codigo,u.nombres operador,p.direccion,p.total,p.metodo,p.estado,p.fecha FROM"
                + " pedido p JOIN venta v ON v.id=p.venta_id JOIN usuario u ON u.id=v.usuario_id WHERE DATE(p.fecha) BETWEEN ? AND"
                + " ? ORDER BY p.id DESC",
            desde,
            hasta);
      case "delivery":
        return DB.filas(
            "SELECT"
                + " p.codigo,d.direccion,d.distrito,d.referencia,d.telefono,d.costo,d.fecha_estimada,d.fecha_entrega,d.estado"
                + " FROM delivery d JOIN pedido p ON p.id=d.pedido_id WHERE DATE(p.fecha) BETWEEN ?"
                + " AND ? ORDER BY d.id DESC",
            desde,
            hasta);
      case "mas_vendidos":
      case "menos_vendidos":
        return DB.filas(
            "SELECT p.codigo,p.nombre,COALESCE(SUM(CASE WHEN v.estado='PAGADO' AND DATE(v.fecha)"
                + " BETWEEN ? AND ? THEN d.cantidad ELSE 0 END),0) unidades FROM producto p LEFT"
                + " JOIN detalle_venta d ON d.producto_id=p.id LEFT JOIN venta v ON v.id=d.venta_id"
                + " GROUP BY p.id,p.codigo,p.nombre ORDER BY unidades "
                + (tipo.equals("mas_vendidos") ? "DESC" : "ASC")
                + ",p.id",
            desde,
            hasta);
      default:
        throw new IllegalArgumentException("Reporte no válido");
    }
  }
}
