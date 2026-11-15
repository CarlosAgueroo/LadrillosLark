import java.math.BigDecimal;
import modelo.*;
import seguridad.*;

/** Pruebas sin base de datos: ant test */
public class Pruebas {
  static int n = 0;

  static void ok(boolean c, String m) {
    if (!c) throw new AssertionError(m);
    System.out.println("PASS " + m);
    n++;
  }

  public static void main(String[] a) throws Exception {
    String h = Clave.hash("Lark2026!");
    ok(Clave.verificar("Lark2026!", h), "hash válido");
    ok(!Clave.verificar("incorrecta", h), "rechaza contraseña");
    ok(!Clave.hash("Lark2026!").equals(h), "sal aleatoria");
    ok(!Clave.verificar("x", "malformado"), "hash malformado");
    ok(Validar.html("<script>\"&'").equals("&lt;script&gt;&quot;&amp;&#39;"), "escape HTML");
    ok(
        new LineaCarrito(1, 3, "Prueba", new BigDecimal("1.40"))
            .subtotal()
            .equals(new BigDecimal("4.20")),
        "aritmética decimal");
    ok(new Usuario(1, 3, "Almacenero").es(3) && !new Usuario(1, 3, "Almacenero").es(1, 2), "roles");
    try {
      Validar.campo("dni", "123");
      throw new AssertionError();
    } catch (IllegalArgumentException e) {
      ok(true, "DNI inválido");
    }
    try {
      Entidad.obtener("usuario; DROP TABLE usuario");
      throw new AssertionError();
    } catch (IllegalArgumentException e) {
      ok(true, "lista cerrada entidades");
    }
    for (String rol : new String[]{"0", "4", "99"}) {
      try { Validar.campo("rol_id", rol); throw new AssertionError(); }
      catch (IllegalArgumentException e) { ok(true, "rechaza rol " + rol); }
    }
    for (int rol : new int[]{1,2,3}) Validar.campo("rol_id", String.valueOf(rol));
    ok(true, "acepta los tres roles internos");
    try { Entidad.obtener("cliente"); throw new AssertionError(); }
    catch (IllegalArgumentException e) { ok(true, "módulo externo retirado"); }
    try { new dao.PedidoDAO().cambiar(1,"PAGAR",new Usuario(3,3,"Almacenero")); throw new AssertionError(); }
    catch (IllegalArgumentException e) { ok(true, "Almacenero no cobra"); }
    try { new dao.PedidoDAO().cambiar(1,"PREPARANDO",new Usuario(2,2,"Cajero")); throw new AssertionError(); }
    catch (IllegalArgumentException e) { ok(true, "Cajero no despacha"); }
    try { new dao.VentaDAO().comprar(new Usuario(3,3,"Almacenero"),new java.util.TreeMap<>(),"","","","",false,"YAPE",BigDecimal.ZERO,""); throw new AssertionError(); }
    catch (IllegalArgumentException e) { ok(true, "Almacenero no registra ventas"); }
    Validar.campo("correo", "demo@lark.test");
    ok(true, "correo válido");
    System.out.println(n + " pruebas completadas");
  }
}
