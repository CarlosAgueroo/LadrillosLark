package modelo;

import java.math.BigDecimal;

public class LineaCarrito {
  public final int producto, cantidad;
  public final String nombre;
  public final BigDecimal precio;

  public LineaCarrito(int p, int c, String n, BigDecimal v) {
    producto = p;
    cantidad = c;
    nombre = n;
    precio = v;
  }

  public BigDecimal subtotal() {
    return precio.multiply(BigDecimal.valueOf(cantidad));
  }
}
