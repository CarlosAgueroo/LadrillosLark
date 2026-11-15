package controlador;

import dao.*;
import java.io.*;
import java.math.*;
import java.time.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import modelo.*;
import seguridad.*;

/** Enrutador MVC: valida entradas y delega operaciones en DAO; no contiene SQL. */
@WebServlet(urlPatterns = "/app/*")
public class AppServlet extends HttpServlet {
  private String param(HttpServletRequest r, String k) {
    String v = r.getParameter(k);
    return v == null ? "" : v.trim();
  }

  private Usuario usuario(HttpServletRequest r) {
    return (Usuario) r.getSession().getAttribute("usuario");
  }

  @SuppressWarnings("unchecked")
  private Map<Integer, Integer> cart(HttpServletRequest r) {
    HttpSession s = r.getSession();
    Map<Integer, Integer> c = (Map<Integer, Integer>) s.getAttribute("carrito");
    if (c == null) {
      c = new TreeMap<>();
      s.setAttribute("carrito", c);
    }
    return c;
  }

  private void vista(HttpServletRequest r, HttpServletResponse s, String v) throws Exception {
    r.setAttribute("vista", v);
    r.getRequestDispatcher("/WEB-INF/vistas/" + v + ".jsp").forward(r, s);
  }

  private void redirect(HttpServletRequest r, HttpServletResponse s, String p) throws IOException {
    s.sendRedirect(r.getContextPath() + "/app/" + p);
  }

  private Map<String, String> campos(HttpServletRequest r, String... fs) {
    Map<String, String> m = new LinkedHashMap<>();
    for (String f : fs) {
      String v = param(r, f);
      Validar.campo(f, v);
      m.put(f, v);
    }
    return m;
  }

  protected void doGet(HttpServletRequest r, HttpServletResponse s)
      throws IOException, ServletException {
    String p = r.getPathInfo() == null ? "/catalogo" : r.getPathInfo();
    try {
      switch (p) {
        case "/":
        case "/catalogo":
          r.setAttribute(
              "productos",
              new ProductoDAO()
                  .listar(
                      param(r, "q"),
                      param(r, "categoria"),
                      usuario(r) != null && usuario(r).es(1, 2, 3)));
          r.setAttribute("categorias", new ProductoDAO().categorias());
          vista(r, s, "catalogo");
          break;
        case "/producto":
          Fila prod =
              new ProductoDAO().obtener(Validar.numero(param(r, "id"), 1, Integer.MAX_VALUE));
          if (!prod.texto("activo").matches("1|true")
              && usuario(r) == null) {
            s.sendError(404);
            return;
          }
          r.setAttribute("producto", prod);
          vista(r, s, "producto");
          break;
        case "/login":
          vista(r, s, p.substring(1));
          break;
        case "/perfil":
          r.setAttribute("perfil", new UsuarioDAO().perfil(usuario(r).id));
          vista(r, s, "perfil");
          break;
        case "/carrito":
        case "/checkout":
          synchronized (r.getSession()) {
            r.setAttribute("lineas", new ProductoDAO().carrito(new TreeMap<>(cart(r))));
            if (r.getSession().getAttribute("compraToken") == null)
              r.getSession().setAttribute("compraToken", UUID.randomUUID().toString());
          }
          vista(r, s, p.substring(1));
          break;
        case "/gestion":
          Entidad e = Entidad.obtener(param(r, "entidad"));
          r.setAttribute("entidad", e);
          r.setAttribute("filas", new GestionDAO().listar(e));
          r.setAttribute(
              "edicion",
              param(r, "id").isEmpty()
                  ? new Fila()
                  : new GestionDAO()
                      .obtener(e, Validar.numero(param(r, "id"), 1, Integer.MAX_VALUE)));
          r.setAttribute("categorias", new ProductoDAO().categorias());
          if (e.nombre.equals("proveedor")) {
            r.setAttribute("productos", new ProductoDAO().listar("", "", true));
            r.setAttribute("suministros", new GestionDAO().suministros());
          }
          vista(r, s, "gestion");
          break;
        case "/inventario":
          r.setAttribute("productos", new ProductoDAO().listar("", "", true));
          r.setAttribute("proveedores", new GestionDAO().listar(Entidad.obtener("proveedor")));
          r.setAttribute(
              "filas", new ReporteDAO().reporte("inventario", "2000-01-01", "2100-01-01"));
          vista(r, s, "inventario");
          break;
        case "/pedidos":
          r.setAttribute("pedidos", new PedidoDAO().listar(usuario(r)));
          vista(r, s, "pedidos");
          break;
        case "/detalle":
          r.setAttribute(
              "filas",
              new PedidoDAO()
                  .detalles(Validar.numero(param(r, "id"), 1, Integer.MAX_VALUE), usuario(r)));
          vista(r, s, "detalle");
          break;
        case "/dashboard":
          r.setAttribute("metricas", new ReporteDAO().dashboard());
          r.setAttribute(
              "bajo", new ReporteDAO().reporte("stock_bajo", "2000-01-01", "2100-01-01"));
          r.setAttribute(
              "top", new ReporteDAO().reporte("mas_vendidos", "2000-01-01", "2100-01-01"));
          vista(r, s, "dashboard");
          break;
        case "/reportes":
          String tipo = param(r, "tipo").isEmpty() ? "ventas" : param(r, "tipo"),
              desde =
                  param(r, "desde").isEmpty()
                      ? LocalDate.now().withDayOfMonth(1).toString()
                      : LocalDate.parse(param(r, "desde")).toString(),
              hasta =
                  param(r, "hasta").isEmpty()
                      ? LocalDate.now().toString()
                      : LocalDate.parse(param(r, "hasta")).toString();
          if (desde.compareTo(hasta) > 0)
            throw new IllegalArgumentException("Rango de fechas inválido");
          List<Fila> rows = new ReporteDAO().reporte(tipo, desde, hasta);
          if ("csv".equals(param(r, "formato"))) {
            csv(s, rows, tipo);
            return;
          }
          r.setAttribute("filas", rows);
          r.setAttribute("tipo", tipo);
          r.setAttribute("desde", desde);
          r.setAttribute("hasta", hasta);
          vista(r, s, "reportes");
          break;
        default:
          s.sendError(404);
      }
    } catch (Exception ex) {
      error(r, s, ex);
    }
  }

  protected void doPost(HttpServletRequest r, HttpServletResponse s)
      throws IOException, ServletException {
    String p = r.getPathInfo();
    try {
      switch (p) {
        case "/login":
          Long until = (Long) r.getSession().getAttribute("bloqueo");
          if (until != null && until > System.currentTimeMillis())
            throw new IllegalArgumentException("Espere un minuto antes de reintentar");
          Usuario u =
              new UsuarioDAO()
                  .autenticar(Validar.texto(param(r, "usuario"), 50), r.getParameter("clave"));
          if (u == null) {
            Integer n = (Integer) r.getSession().getAttribute("intentos");
            n = n == null ? 1 : n + 1;
            r.getSession().setAttribute("intentos", n);
            if (n >= 5) {
              r.getSession().setAttribute("bloqueo", System.currentTimeMillis() + 60000);
              r.getSession().setAttribute("intentos", 0);
            }
            throw new IllegalArgumentException("Usuario o contraseña incorrectos");
          }
          r.getSession().invalidate();
          r.getSession(true).setAttribute("usuario", u);
          r.getSession().setAttribute("csrf", UUID.randomUUID().toString());
          redirect(r, s, u.rol == 1 ? "dashboard" : u.rol == 3 ? "inventario" : "catalogo");
          break;
        case "/logout":
          r.getSession().invalidate();
          redirect(r, s, "login");
          break;
        case "/perfil":
          new UsuarioDAO().perfil(usuario(r).id, campos(r, "telefono", "direccion"));
          r.getSession().setAttribute("mensaje", "Perfil actualizado");
          redirect(r, s, "perfil");
          break;
        case "/carrito":
          synchronized (r.getSession()) {
            int id = Validar.numero(param(r, "producto"), 1, Integer.MAX_VALUE),
                qty = Validar.numero(param(r, "cantidad"), 0, 1000000);
            Map<Integer, Integer> c = cart(r);
            if (qty == 0) c.remove(id);
            else {
              Fila pr = new ProductoDAO().obtener(id);
              int n = "sumar".equals(param(r, "accion")) ? c.getOrDefault(id, 0) + qty : qty;
              if (n > pr.entero("stock") || !pr.texto("activo").matches("1|true"))
                throw new IllegalArgumentException("Producto inactivo o stock insuficiente");
              if (c.size() >= 100 && !c.containsKey(id))
                throw new IllegalArgumentException("Máximo 100 productos diferentes");
              c.put(id, n);
            }
            r.getSession().removeAttribute("compraToken");
          }
          redirect(r, s, "carrito");
          break;
        case "/comprar":
          synchronized (r.getSession()) {
            String token = param(r, "token");
            if (!Objects.equals(token, r.getSession().getAttribute("compraToken")))
              throw new IllegalArgumentException(
                  "Compra ya enviada o formulario expirado. Revise sus pedidos.");
            boolean envio = "1".equals(param(r, "envio"));
            String dir = envio ? Validar.texto(param(r, "direccion"), 250) : "Recojo en tienda",
                distrito = envio ? Validar.texto(param(r, "distrito"), 100) : "",
                tel = Validar.texto(param(r, "telefono"), 20);
            Validar.campo("telefono", tel);
            String ref = param(r, "referencia");
            if (ref.length() > 250)
              throw new IllegalArgumentException("Referencia demasiado larga");
            BigDecimal desc =
                usuario(r).es(1, 2) ? new BigDecimal(param(r, "descuento")) : BigDecimal.ZERO;
            new VentaDAO()
                .comprar(
                    usuario(r),
                    new TreeMap<>(cart(r)),
                    dir,
                    distrito,
                    ref,
                    tel,
                    envio,
                    param(r, "metodo"),
                    desc,
                    token);
            cart(r).clear();
            r.getSession().removeAttribute("compraToken");
          }
          r.getSession()
              .setAttribute(
                  "mensaje",
                  "Pedido registrado. Pago en modo simulación; no se ha realizado ningún cargo.");
          redirect(r, s, "pedidos");
          break;
        case "/guardar":
          Entidad ent = Entidad.obtener(param(r, "entidad"));
          int id =
              Validar.numero(param(r, "id").isEmpty() ? "0" : param(r, "id"), 0, Integer.MAX_VALUE);
          Map<String, String> valores = campos(r, ent.campos);
          String clave = r.getParameter("clave") == null ? "" : r.getParameter("clave");
          if (ent.nombre.equals("usuario") && (id == 0 || !clave.isEmpty())) Validar.clave(clave);
          new GestionDAO().guardar(ent, id, valores, clave, usuario(r).id);
          r.getSession().setAttribute("mensaje", "Registro guardado");
          redirect(r, s, "gestion?entidad=" + ent.nombre);
          break;
        case "/asociar":
          new GestionDAO()
              .asociar(
                  Validar.numero(param(r, "producto"), 1, Integer.MAX_VALUE),
                  Validar.numero(param(r, "proveedor"), 1, Integer.MAX_VALUE));
          redirect(r, s, "gestion?entidad=proveedor");
          break;
        case "/movimiento":
          new InventarioDAO()
              .mover(
                  Validar.numero(param(r, "producto"), 1, Integer.MAX_VALUE),
                  Validar.numero(param(r, "cantidad"), 0, 100000000),
                  param(r, "tipo"),
                  Validar.texto(param(r, "motivo"), 250),
                  param(r, "proveedor").isEmpty()
                      ? null
                      : Validar.numero(param(r, "proveedor"), 1, Integer.MAX_VALUE),
                  usuario(r).id);
          r.getSession().setAttribute("mensaje", "Movimiento registrado");
          redirect(r, s, "inventario");
          break;
        case "/estado":
          new PedidoDAO()
              .cambiar(
                  Validar.numero(param(r, "id"), 1, Integer.MAX_VALUE),
                  param(r, "accion"),
                  usuario(r));
          r.getSession().setAttribute("mensaje", "Estado actualizado");
          redirect(r, s, "pedidos");
          break;
        default:
          s.sendError(404);
      }
    } catch (Exception ex) {
      error(r, s, ex);
    }
  }

  private void error(HttpServletRequest r, HttpServletResponse s, Exception ex)
      throws ServletException, IOException {
    getServletContext().log("Operación Lark", ex);
    String msg =
        ex instanceof IllegalArgumentException
            ? ex.getMessage()
            : ex instanceof java.sql.SQLIntegrityConstraintViolationException
                ? "Dato duplicado o relación inválida. Revise DNI, usuario, correo, código y"
                      + " referencias."
                : "No se pudo completar la operación. Compruebe la base de datos y consulte el"
                      + " registro de Tomcat.";
    s.setStatus(ex instanceof IllegalArgumentException ? 400 : 500);
    r.setAttribute("error", msg);
    r.getRequestDispatcher("/WEB-INF/vistas/error.jsp").forward(r, s);
  }

  private void csv(HttpServletResponse s, List<Fila> rows, String tipo) throws IOException {
    s.setContentType("text/csv;charset=UTF-8");
    s.setHeader("Content-Disposition", "attachment; filename=\"lark-" + tipo + ".csv\"");
    PrintWriter w = s.getWriter();
    w.write('\ufeff');
    if (rows.isEmpty()) {
      w.println("Sin registros");
      return;
    }
    w.println(String.join(";", rows.get(0).keySet()));
    for (Fila f : rows) {
      List<String> a = new ArrayList<>();
      for (Object value : f.values()) {
        String v = value == null ? "" : value.toString();
        if (v.matches("^[=+@\\-\\t\\r].*")) v = "'" + v;
        a.add("\"" + v.replace("\"", "\"\"") + "\"");
      }
      w.println(String.join(";", a));
    }
  }
}
