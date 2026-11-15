package dao;

import java.util.*;
import modelo.*;

public class PedidoDAO {
  public List<Fila> listar(Usuario u) throws Exception {
    return DB.filas(
        "SELECT p.*,u.nombres operador,pa.estado pago_estado,COALESCE(d.estado,'RECOJO')"
            + " delivery_estado,d.distrito,d.referencia,d.telefono,d.fecha_estimada,d.fecha_entrega"
            + " FROM pedido p JOIN venta v ON v.id=p.venta_id JOIN usuario u ON u.id=v.usuario_id JOIN pago pa ON"
            + " pa.venta_id=p.venta_id LEFT JOIN delivery d ON d.pedido_id=p.id ORDER BY p.id DESC");
  }

  public List<Fila> detalles(int id, Usuario u) throws Exception {
    return DB.filas(
        "SELECT pr.nombre,d.cantidad,d.precio FROM detalle_pedido d JOIN pedido p ON"
            + " p.id=d.pedido_id JOIN producto pr ON"
            + " pr.id=d.producto_id WHERE p.id=?",
        id);
  }

  public void cambiar(int id, String accion, Usuario u) throws Exception {
    if (!u.es(1,2,3) || (u.rol==2 && !Arrays.asList("PAGAR","RECHAZAR","CANCELAR").contains(accion))
        || (u.rol==3 && !Arrays.asList("PREPARANDO","EN_CAMINO","ENTREGADO").contains(accion)))
      throw new IllegalArgumentException("Acceso denegado");
    DB.transaccion(
        c -> {
          Fila p =
              DB.uno(
                  c,
                  "SELECT p.*,pa.estado pago_estado FROM pedido p JOIN pago pa ON pa.venta_id=p.venta_id"
                      + " WHERE p.id=? FOR UPDATE",
                  id);
          String estado = p.texto("estado"), pago = p.texto("pago_estado");
          int v = p.entero("venta_id");
          if (estado.equals("CANCELADO") || estado.equals("ENTREGADO"))
            throw new IllegalArgumentException("Pedido finalizado");
          if (accion.equals("CANCELAR") || accion.equals("RECHAZAR")) {
            if (accion.equals("RECHAZAR") && !pago.equals("PENDIENTE"))
              throw new IllegalArgumentException("Solo se rechaza un pago pendiente");
            List<Fila> lineas =
                DB.filas(
                    c,
                    "SELECT producto_id,cantidad FROM detalle_venta WHERE venta_id=? ORDER BY"
                        + " producto_id",
                    v);
            for (Fila l : lineas) {
              DB.uno(
                  c,
                  "SELECT stock FROM inventario WHERE producto_id=? FOR UPDATE",
                  l.entero("producto_id"));
              InventarioDAO.movimiento(
                  c,
                  l.entero("producto_id"),
                  l.entero("cantidad"),
                  u.id,
                  "DEVOLUCION",
                  "Cancelación pedido " + id,
                  null,
                  v);
            }
            String np =
                accion.equals("RECHAZAR")
                    ? "RECHAZADO"
                    : pago.equals("PAGADO") ? "REEMBOLSADO" : "CANCELADO";
            DB.ejecutar(c, "UPDATE pago SET estado=? WHERE venta_id=?", np, v);
            DB.ejecutar(c, "UPDATE venta SET estado='CANCELADO' WHERE id=?", v);
            DB.ejecutar(c, "UPDATE pedido SET estado='CANCELADO' WHERE id=?", id);
            DB.ejecutar(c, "UPDATE delivery SET estado='CANCELADO' WHERE pedido_id=?", id);
          } else if (accion.equals("PAGAR")) {
            if (!pago.equals("PENDIENTE")) throw new IllegalArgumentException("Pago ya procesado");
            DB.ejecutar(c, "UPDATE pago SET estado='PAGADO' WHERE venta_id=?", v);
            DB.ejecutar(c, "UPDATE venta SET estado='PAGADO' WHERE id=?", v);
            if (estado.equals("PENDIENTE"))
              DB.ejecutar(c, "UPDATE pedido SET estado='PAGADO' WHERE id=?", id);
          } else {
            boolean envio = !DB.filas(c, "SELECT id FROM delivery WHERE pedido_id=?", id).isEmpty();
            boolean valido =
                (accion.equals("PREPARANDO")
                        && (estado.equals("PAGADO")
                            || (estado.equals("PENDIENTE")
                                && p.texto("metodo").equals("CONTRA_ENTREGA"))))
                    || (accion.equals("EN_CAMINO") && envio && estado.equals("PREPARANDO"))
                    || (accion.equals("ENTREGADO")
                        && pago.equals("PAGADO")
                        && (estado.equals("EN_CAMINO") || (!envio && estado.equals("PREPARANDO"))));
            if (!valido)
              throw new IllegalArgumentException(
                  "Transición inválida. Respete el orden y cobre antes de entregar.");
            DB.ejecutar(c, "UPDATE pedido SET estado=? WHERE id=?", accion, id);
            DB.ejecutar(
                c,
                "UPDATE delivery SET estado=?,fecha_entrega=CASE WHEN ?='ENTREGADO' THEN NOW() ELSE"
                    + " fecha_entrega END WHERE pedido_id=?",
                accion,
                accion,
                id);
          }
          return null;
        });
  }
}
