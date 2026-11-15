package dao;

import java.util.*;
import modelo.*;

public class ProductoDAO {
  public List<Fila> listar(String q, String categoria, boolean todos) throws Exception {
    return DB.filas(
        "SELECT p.*,c.nombre categoria,i.stock FROM producto p JOIN categoria c ON"
            + " c.id=p.categoria_id JOIN inventario i ON i.producto_id=p.id WHERE (?=1 OR"
            + " (p.activo=1 AND c.activo=1)) AND (p.nombre LIKE ? OR p.codigo LIKE ?) AND (?='' OR"
            + " p.categoria_id=?) ORDER BY p.id",
        todos ? 1 : 0,
        "%" + q + "%",
        "%" + q + "%",
        categoria,
        categoria);
  }

  public Fila obtener(int id) throws Exception {
    List<Fila> r =
        DB.filas(
            "SELECT p.*,i.stock FROM producto p JOIN inventario i ON i.producto_id=p.id WHERE"
                + " p.id=?",
            id);
    if (r.isEmpty()) throw new IllegalArgumentException("Producto no encontrado");
    return r.get(0);
  }

  public List<Fila> categorias() throws Exception {
    return DB.filas("SELECT * FROM categoria WHERE activo=1 ORDER BY nombre");
  }

  public List<LineaCarrito> carrito(Map<Integer, Integer> cart) throws Exception {
    List<LineaCarrito> r = new ArrayList<>();
    for (Map.Entry<Integer, Integer> e : cart.entrySet()) {
      Fila p = obtener(e.getKey());
      r.add(new LineaCarrito(e.getKey(), e.getValue(), p.texto("nombre"), p.decimal("precio")));
    }
    return r;
  }
}
