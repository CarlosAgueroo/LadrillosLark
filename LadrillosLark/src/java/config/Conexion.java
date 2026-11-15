package config;

import java.sql.*;

/** Valores locales XAMPP; sobreescribibles por variables de entorno. */
public final class Conexion {
  private Conexion() {}

  public static Connection abrir() throws SQLException {
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      throw new SQLException("Falta MySQL Connector/J en lib", e);
    }
    Connection c =
        DriverManager.getConnection(
            env(
                "LARK_DB_URL",
                "jdbc:mysql://localhost:3306/bd_ladrillos_lark?useSSL=false&serverTimezone=America/Lima"),
            env("LARK_DB_USER", "root"),
            env("LARK_DB_PASSWORD", ""));
    return c;
  }

  private static String env(String k, String d) {
    String v = System.getenv(k);
    return v == null ? d : v;
  }
}
