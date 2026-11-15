package modelo;

import java.io.Serializable;

public final class Usuario implements Serializable {
  public final int id, rol;
  public final String nombre;

  public Usuario(int id, int rol, String nombre) {
    this.id = id;
    this.rol = rol;
    this.nombre = nombre;
  }

  public boolean es(int... roles) {
    for (int r : roles) if (rol == r) return true;
    return false;
  }
}
