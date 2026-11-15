package dao;

import config.Conexion;
import java.sql.*;
import java.util.*;
import modelo.Fila;

/** Utilidades internas DAO. Todas las consultas se definen en este paquete. */
public final class DB {
  public interface Trabajo<T> {
    T ejecutar(Connection c) throws Exception;
  }

  public static <T> T transaccion(Trabajo<T> t) throws Exception {
    try (Connection c = Conexion.abrir()) {
      c.setAutoCommit(false);
      try {
        T v = t.ejecutar(c);
        c.commit();
        return v;
      } catch (Exception e) {
        c.rollback();
        throw e;
      }
    }
  }

  private static void parametros(PreparedStatement p, Object... a) throws SQLException {
    for (int i = 0; i < a.length; i++) p.setObject(i + 1, a[i]);
  }

  public static List<Fila> filas(Connection c, String sql, Object... a) throws SQLException {
    try (PreparedStatement p = c.prepareStatement(sql)) {
      parametros(p, a);
      try (ResultSet r = p.executeQuery()) {
        List<Fila> v = new ArrayList<>();
        while (r.next()) {
          Fila f = new Fila();
          for (int i = 1; i <= r.getMetaData().getColumnCount(); i++)
            f.put(r.getMetaData().getColumnLabel(i), r.getObject(i));
          v.add(f);
        }
        return v;
      }
    }
  }

  public static List<Fila> filas(String sql, Object... a) throws SQLException {
    try (Connection c = Conexion.abrir()) {
      return filas(c, sql, a);
    }
  }

  public static Fila uno(Connection c, String sql, Object... a) throws SQLException {
    List<Fila> r = filas(c, sql, a);
    if (r.isEmpty()) throw new IllegalArgumentException("Registro no encontrado");
    return r.get(0);
  }

  public static int ejecutar(Connection c, String sql, Object... a) throws SQLException {
    try (PreparedStatement p = c.prepareStatement(sql)) {
      parametros(p, a);
      return p.executeUpdate();
    }
  }

  public static int insertar(Connection c, String sql, Object... a) throws SQLException {
    try (PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      parametros(p, a);
      p.executeUpdate();
      try (ResultSet r = p.getGeneratedKeys()) {
        if (!r.next()) throw new SQLException("Sin identificador generado");
        return r.getInt(1);
      }
    }
  }
}
