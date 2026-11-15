package seguridad;

import dao.UsuarioDAO;
import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;
import modelo.Usuario;

@WebFilter("/*")
public class SeguridadFiltro implements Filter {
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest r = (HttpServletRequest) req;
    HttpServletResponse s = (HttpServletResponse) res;
    r.setCharacterEncoding("UTF-8");
    s.setCharacterEncoding("UTF-8");
    s.setHeader("X-Content-Type-Options", "nosniff");
    s.setHeader("X-Frame-Options", "DENY");
    s.setHeader("Referrer-Policy", "same-origin");
    s.setHeader(
        "Content-Security-Policy",
        "default-src 'self'; style-src 'self'; script-src 'self'; img-src 'self'; form-action"
            + " 'self'; frame-ancestors 'none'; base-uri 'self'");
    String path = r.getServletPath();
    if (path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/img/")) {
      chain.doFilter(r, s);
      return;
    }
    s.setHeader("Cache-Control", "no-store");
    HttpSession session = r.getSession();
    if (session.getAttribute("csrf") == null)
      session.setAttribute("csrf", UUID.randomUUID().toString());
    Usuario u = (Usuario) session.getAttribute("usuario");
    if (u != null) {
      try {
        u = new UsuarioDAO().activo(u.id);
        if (u == null || !u.es(1,2,3)) {
          session.invalidate();
          s.sendRedirect(r.getContextPath() + "/app/login");
          return;
        }
        session.setAttribute("usuario", u);
      } catch (Exception e) {
        r.getServletContext().log("Verificación de sesión", e);
        s.sendError(503, "No se pudo conectar con la base de datos");
        return;
      }
    }
    if (r.getMethod().equals("POST")
        && !Objects.equals(session.getAttribute("csrf"), r.getParameter("csrf"))) {
      s.sendError(403, "Formulario expirado. Recargue la página.");
      return;
    }
    if (!r.getMethod().equals("GET")
        && !r.getMethod().equals("POST")
        && !r.getMethod().equals("HEAD")) {
      s.sendError(405);
      return;
    }
    if (path.startsWith("/app")) {
      String p = r.getPathInfo() == null ? "/catalogo" : r.getPathInfo();
      boolean publico =
          p.equals("/login");
      if (!publico && u == null) {
        s.sendRedirect(r.getContextPath() + "/app/login");
        return;
      }
      if (u != null) {
        boolean ok = true;
        if (Arrays.asList("/dashboard", "/gestion", "/guardar", "/asociar", "/reportes")
            .contains(p))
          ok = u.es(1);
        if (p.equals("/inventario") || p.equals("/movimiento")) ok = u.es(1, 3);
        if (Arrays.asList("/carrito", "/checkout", "/comprar").contains(p)) ok = u.es(1, 2);
        if (p.equals("/pedidos") || p.equals("/detalle")) ok = u.es(1, 2, 3);
        if (p.equals("/estado")) ok = u.es(1, 2, 3);
        if (!ok) {
          s.sendError(403, "Su rol no tiene acceso");
          return;
        }
      }
    }
    chain.doFilter(r, s);
  }
}
