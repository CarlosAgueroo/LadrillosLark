package dao;

import java.sql.Connection;
import modelo.*;

public class InventarioDAO {
  public void mover(
      int producto, int valor, String tipo, String motivo, Integer proveedor, int usuario)
      throws Exception {
    if (!java.util.Arrays.asList("ENTRADA", "SALIDA", "AJUSTE", "REPOSICION").contains(tipo))
      throw new IllegalArgumentException("Tipo inválido");
    if (valor < 0 || (!tipo.equals("AJUSTE") && valor == 0) || motivo.trim().isEmpty())
      throw new IllegalArgumentException("Cantidad o motivo inválido");
    DB.transaccion(
        c -> {
          Fila i =
              DB.uno(c, "SELECT stock FROM inventario WHERE producto_id=? FOR UPDATE", producto);
          int actual = i.entero("stock");
          int delta =
              tipo.equals("AJUSTE") ? valor - actual : tipo.equals("SALIDA") ? -valor : valor;
          if ((long) actual + delta < 0 || (long) actual + delta > 100000000)
            throw new IllegalArgumentException("Stock fuera de rango");
          if (tipo.equals("REPOSICION")) {
            if (proveedor == null) throw new IllegalArgumentException("Seleccione proveedor");
            DB.uno(
                c,
                "SELECT pp.producto_id FROM producto_proveedor pp JOIN proveedor p ON"
                    + " p.id=pp.proveedor_id WHERE pp.producto_id=? AND pp.proveedor_id=? AND"
                    + " p.activo=1",
                producto,
                proveedor);
          }
          movimiento(c, producto, delta, usuario, tipo, motivo, proveedor, null);
          return null;
        });
  }

  static void movimiento(
      Connection c,
      int p,
      int delta,
      int u,
      String tipo,
      String motivo,
      Integer prov,
      Integer venta)
      throws Exception {
    DB.ejecutar(c, "UPDATE inventario SET stock=stock+? WHERE producto_id=?", delta, p);
    int s = DB.uno(c, "SELECT stock FROM inventario WHERE producto_id=?", p).entero("stock");
    DB.ejecutar(
        c,
        "INSERT INTO"
            + " movimiento_inventario(producto_id,usuario_id,proveedor_id,venta_id,tipo,cantidad,stock_resultante,motivo)"
            + " VALUES(?,?,?,?,?,?,?,?)",
        p,
        u,
        prov,
        venta,
        tipo,
        delta,
        s,
        motivo);
  }
}
