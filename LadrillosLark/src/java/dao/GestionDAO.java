package dao;

import java.util.*;
import modelo.*;
import seguridad.Clave;

public class GestionDAO {
  public List<Fila> listar(Entidad e) throws Exception {
    return DB.filas(
        "SELECT id," + String.join(",", e.campos) + " FROM " + e.nombre + (e.nombre.equals("usuario") ? " WHERE rol_id IN (1,2,3)" : "") + " ORDER BY id DESC");
  }

  public Fila obtener(Entidad e, int id) throws Exception {
    List<Fila> f =
        DB.filas(
            "SELECT id," + String.join(",", e.campos) + " FROM " + e.nombre + " WHERE id=?" + (e.nombre.equals("usuario") ? " AND rol_id IN (1,2,3)" : ""), id);
    if (f.isEmpty()) throw new IllegalArgumentException("Registro no encontrado");
    return f.get(0);
  }

  public void guardar(Entidad e, int id, Map<String, String> m, String clave, int actor)
      throws Exception {
    DB.transaccion(
        c -> {
          // Editar usuarios se serializa para no desactivar al último administrador.
          if (e.nombre.equals("usuario")) {
            seguridad.Validar.campo("rol_id", m.get("rol_id"));
            DB.filas(c, "SELECT id FROM usuario ORDER BY id FOR UPDATE");
            if (id > 0) {
              Fila old = DB.uno(c, "SELECT rol_id,activo FROM usuario WHERE id=? AND rol_id IN (1,2,3)", id);
              if ((id == actor) && (!m.get("rol_id").equals("1") || !m.get("activo").equals("1")))
                throw new IllegalArgumentException(
                    "No puede quitarse su propio acceso administrador");
              if (old.entero("rol_id") == 1
                  && (!m.get("rol_id").equals("1") || !m.get("activo").equals("1"))
                  && DB.uno(c, "SELECT COUNT(*) n FROM usuario WHERE rol_id=1 AND activo=1")
                          .entero("n")
                      <= 1)
                throw new IllegalArgumentException("Debe quedar un administrador activo");
            }
          }
          List<Object> vals = new ArrayList<>();
          for (String f : e.campos) vals.add(m.get(f));
          int key = id;
          if (id == 0) {
            String cols = String.join(",", e.campos);
            if (e.nombre.equals("usuario")) {
              cols += ",clave";
              vals.add(Clave.hash(clave));
            }
            key =
                DB.insertar(
                    c,
                    "INSERT INTO "
                        + e.nombre
                        + "("
                        + cols
                        + ") VALUES("
                        + String.join(",", Collections.nCopies(vals.size(), "?"))
                        + ")",
                    vals.toArray());
            if (e.nombre.equals("producto"))
              DB.ejecutar(c, "INSERT INTO inventario(producto_id,stock) VALUES(?,0)", key);
          } else {
            List<String> sets = new ArrayList<>();
            for (String f : e.campos) sets.add(f + "=?");
            if (e.nombre.equals("usuario") && !clave.isEmpty()) {
              sets.add("clave=?");
              vals.add(Clave.hash(clave));
            }
            vals.add(id);
            DB.ejecutar(
                c,
                "UPDATE " + e.nombre + " SET " + String.join(",", sets) + " WHERE id=?",
                vals.toArray());
          }
          return null;
        });
  }

  public void asociar(int producto, int proveedor) throws Exception {
    DB.transaccion(
        c -> {
          DB.ejecutar(
              c,
              "INSERT IGNORE INTO producto_proveedor(producto_id,proveedor_id) VALUES(?,?)",
              producto,
              proveedor);
          return null;
        });
  }

  public List<Fila> suministros() throws Exception {
    return DB.filas(
        "SELECT pp.producto_id,pp.proveedor_id,p.nombre producto,v.razon_social proveedor FROM"
            + " producto_proveedor pp JOIN producto p ON p.id=pp.producto_id JOIN proveedor v ON"
            + " v.id=pp.proveedor_id");
  }
}
