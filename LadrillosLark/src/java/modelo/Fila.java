package modelo;

import java.math.BigDecimal;
import java.util.*;

/** DTO de lectura para formularios y reportes; sin acceso JDBC. */
public class Fila extends LinkedHashMap<String, Object> {
  public String texto(String k) {
    Object v = get(k);
    return v == null ? "" : v.toString();
  }

  public int entero(String k) {
    return Integer.parseInt(texto(k));
  }

  public BigDecimal decimal(String k) {
    return new BigDecimal(texto(k));
  }
}
