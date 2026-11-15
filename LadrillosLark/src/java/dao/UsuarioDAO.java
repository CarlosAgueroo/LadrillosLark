package dao;

import java.util.*;
import modelo.*;
import seguridad.Clave;

public class UsuarioDAO {
  public Usuario autenticar(String login, String clave) throws Exception {
    List<Fila> r = DB.filas("SELECT * FROM usuario WHERE usuario=? AND activo=1 AND rol_id IN (1,2,3)", login);
    if (r.isEmpty() || !Clave.verificar(clave, r.get(0).texto("clave"))) return null;
    return usuario(r.get(0));
  }

  public Usuario activo(int id) throws Exception {
    List<Fila> r = DB.filas("SELECT id,rol_id,nombres FROM usuario WHERE id=? AND activo=1 AND rol_id IN (1,2,3)", id);
    return r.isEmpty() ? null : usuario(r.get(0));
  }

  private Usuario usuario(Fila f) {
    return new Usuario(f.entero("id"), f.entero("rol_id"), f.texto("nombres"));
  }

  public Fila perfil(int id) throws Exception {
    return DB.filas(
            "SELECT nombres,apellidos,dni,correo,telefono,direccion,usuario FROM usuario WHERE"
                + " id=?",
            id)
        .get(0);
  }

  public void perfil(int id, Map<String, String> m) throws Exception {
    DB.transaccion(
        c -> {
          DB.ejecutar(
              c,
              "UPDATE usuario SET telefono=?,direccion=? WHERE id=?",
              m.get("telefono"),
              m.get("direccion"),
              id);
          return null;
        });
  }
}
