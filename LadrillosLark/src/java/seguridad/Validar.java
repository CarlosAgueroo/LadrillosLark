package seguridad;

import java.math.*;
import java.util.*;

public final class Validar {
  public static String texto(String s, int max) {
    if (s == null || s.trim().isEmpty() || s.trim().length() > max)
      throw new IllegalArgumentException("Complete los campos (máximo " + max + " caracteres)");
    return s.trim();
  }

  public static void campo(String f, String v) {
    texto(v, f.equals("descripcion") ? 1000 : f.equals("direccion") ? 250 : 160);
    switch (f) {
      case "dni":
        if (!v.matches("[0-9]{8}")) throw new IllegalArgumentException("DNI: 8 dígitos");
        break;
      case "ruc":
        if (!v.matches("[0-9]{11}")) throw new IllegalArgumentException("RUC: 11 dígitos");
        break;
      case "correo":
        if (v.length() > 120 || !v.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))
          throw new IllegalArgumentException("Correo inválido");
        break;
      case "telefono":
        if (!v.matches("[+0-9 ()-]{7,20}")) throw new IllegalArgumentException("Teléfono inválido");
        break;
      case "usuario":
        if (!v.matches("[a-zA-Z0-9_.-]{3,50}"))
          throw new IllegalArgumentException("Usuario: 3 a 50 letras, números, puntos o guiones");
        break;
      case "precio":
        BigDecimal p = new BigDecimal(v);
        if (p.signum() <= 0 || p.scale() > 2 || p.compareTo(new BigDecimal("9999999999.99")) > 0)
          throw new IllegalArgumentException("Precio inválido");
        break;
      case "stock_minimo":
        numero(v, 0, 100000000);
        break;
      case "categoria_id":
        numero(v, 1, Integer.MAX_VALUE);
        break;
      case "rol_id":
        numero(v, 1, 3);
        break;
      case "activo":
        numero(v, 0, 1);
        break;
      case "imagen":
        if (!Arrays.asList("ladrillo.svg", "cemento.svg", "arena.svg", "herramienta.svg")
            .contains(v)) throw new IllegalArgumentException("Imagen inválida");
        break;
    }
  }

  public static int numero(String v, int min, int max) {
    int n;
    try {
      n = Integer.parseInt(v);
    } catch (Exception e) {
      throw new IllegalArgumentException("Número inválido");
    }
    if (n < min || n > max) throw new IllegalArgumentException("Número fuera de rango");
    return n;
  }

  public static void clave(String v) {
    if (v == null || v.length() < 8 || v.length() > 128)
      throw new IllegalArgumentException("Contraseña: entre 8 y 128 caracteres");
  }

  public static String html(Object v) {
    return v == null
        ? ""
        : v.toString()
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
  }
}
