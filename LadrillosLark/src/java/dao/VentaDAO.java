package dao;

import java.math.*;
import java.util.*;
import modelo.*;

public class VentaDAO {
  public int comprar(
      Usuario u,
      Map<Integer, Integer> cart,
      String direccion,
      String distrito,
      String referencia,
      String telefono,
      boolean envio,
      String metodo,
      BigDecimal porcentaje,
      String token)
      throws Exception {
    if (!u.es(1,2)) throw new IllegalArgumentException("Acceso denegado");
    if (!Arrays.asList("TARJETA", "YAPE", "PLIN", "CONTRA_ENTREGA").contains(metodo))
      throw new IllegalArgumentException("Método de pago inválido");
    if (porcentaje.signum() < 0
        || porcentaje.compareTo(BigDecimal.TEN) > 0
        || (!u.es(1, 2) && porcentaje.signum() != 0))
      throw new IllegalArgumentException("Descuento no permitido");
    if (cart.isEmpty() || cart.size() > 100)
      throw new IllegalArgumentException("Carrito vacío o demasiado grande");
    UUID.fromString(token);
    return DB.transaccion(
        c -> {
          // Serializa envíos de checkout del mismo usuario; el token cubre reintentos.
          DB.uno(c, "SELECT id FROM usuario WHERE id=? AND activo=1 FOR UPDATE", u.id);
          List<Fila> anterior =
              DB.filas(
                  c,
                  "SELECT p.id FROM venta v JOIN pedido p ON p.venta_id=v.id WHERE v.token=? AND"
                      + " v.usuario_id=?",
                  token,
                  u.id);
          if (!anterior.isEmpty()) return anterior.get(0).entero("id");
          BigDecimal sub = BigDecimal.ZERO;
          List<LineaCarrito> lineas = new ArrayList<>();
          for (Map.Entry<Integer, Integer> e : new TreeMap<>(cart).entrySet()) {
            int qty = e.getValue();
            if (qty < 1 || qty > 1000000)
              throw new IllegalArgumentException("Cantidad fuera de rango");
            Fila p =
                DB.uno(
                    c,
                    "SELECT p.nombre,p.precio,p.activo,c.activo categoria_activa,i.stock FROM"
                        + " producto p JOIN categoria c ON c.id=p.categoria_id JOIN inventario i ON"
                        + " i.producto_id=p.id WHERE p.id=? FOR UPDATE",
                    e.getKey());
            if (!p.texto("activo").matches("1|true")
                || !p.texto("categoria_activa").matches("1|true")
                || p.entero("stock") < qty)
              throw new IllegalArgumentException(
                  "Stock insuficiente o producto inactivo: " + p.texto("nombre"));
            LineaCarrito l =
                new LineaCarrito(e.getKey(), qty, p.texto("nombre"), p.decimal("precio"));
            lineas.add(l);
            sub = sub.add(l.subtotal());
          }
          BigDecimal costo = envio ? new BigDecimal("25.00") : BigDecimal.ZERO,
              desc =
                  sub.multiply(porcentaje).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP),
              total = sub.subtract(desc).add(costo);
          if (total.compareTo(new BigDecimal("9999999999.99")) > 0)
            throw new IllegalArgumentException("Importe excede el máximo");
          int v =
              DB.insertar(
                  c,
                  "INSERT INTO"
                      + " venta(usuario_id,canal,subtotal,descuento,costo_delivery,total,metodo,token)"
                      + " VALUES(?,?,?,?,?,?,?,?)",
                  u.id,
                  "PRESENCIAL",
                  sub,
                  desc,
                  costo,
                  total,
                  metodo,
                  token);
          int p =
              DB.insertar(
                  c,
                  "INSERT INTO pedido(codigo,venta_id,direccion,total,metodo)"
                      + " VALUES(?,?,?,?,?)",
                  "LARK-" + v,
                  v,
                  direccion,
                  total,
                  metodo);
          DB.ejecutar(
              c, "INSERT INTO pago(venta_id,metodo,importe) VALUES(?,?,?)", v, metodo, total);
          for (LineaCarrito l : lineas) {
            DB.ejecutar(
                c,
                "INSERT INTO detalle_venta(venta_id,producto_id,cantidad,precio,subtotal)"
                    + " VALUES(?,?,?,?,?)",
                v,
                l.producto,
                l.cantidad,
                l.precio,
                l.subtotal());
            DB.ejecutar(
                c,
                "INSERT INTO detalle_pedido(pedido_id,producto_id,cantidad,precio) VALUES(?,?,?,?)",
                p,
                l.producto,
                l.cantidad,
                l.precio);
            InventarioDAO.movimiento(
                c, l.producto, -l.cantidad, u.id, "VENTA", "Reserva por compra", null, v);
          }
          if (envio)
            DB.ejecutar(
                c,
                "INSERT INTO"
                    + " delivery(pedido_id,direccion,distrito,referencia,telefono,costo,fecha_estimada)"
                    + " VALUES(?,?,?,?,?,?,DATE_ADD(CURRENT_DATE,INTERVAL 2 DAY))",
                p,
                direccion,
                distrito,
                referencia,
                telefono,
                costo);
          return p;
        });
  }
}
