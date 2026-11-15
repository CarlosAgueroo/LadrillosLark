package modelo;

import java.util.*;

/** Lista cerrada de entidades y campos editables. Nunca acepta tablas arbitrarias. */
public final class Entidad {
  public final String nombre, titulo;
  public final String[] campos;

  private Entidad(String n, String t, String... c) {
    nombre = n;
    titulo = t;
    campos = c;
  }

  private static final Map<String, Entidad> TODAS = new LinkedHashMap<>();

  static {
    agregar(
        new Entidad(
            "producto",
            "Productos",
            "codigo",
            "nombre",
            "descripcion",
            "categoria_id",
            "precio",
            "stock_minimo",
            "imagen",
            "activo"));
    agregar(new Entidad("categoria", "Categorías", "nombre", "activo"));
    agregar(
        new Entidad(
            "proveedor",
            "Proveedores",
            "ruc",
            "razon_social",
            "telefono",
            "correo",
            "direccion",
            "activo"));
    agregar(
        new Entidad(
            "usuario",
            "Usuarios",
            "nombres",
            "apellidos",
            "dni",
            "correo",
            "telefono",
            "direccion",
            "usuario",
            "rol_id",
            "activo"));
  }

  private static void agregar(Entidad e) {
    TODAS.put(e.nombre, e);
  }

  public static Entidad obtener(String n) {
    Entidad e = TODAS.get(n);
    if (e == null) throw new IllegalArgumentException("Módulo desconocido");
    return e;
  }
}
