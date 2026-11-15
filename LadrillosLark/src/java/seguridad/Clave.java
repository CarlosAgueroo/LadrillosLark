package seguridad;

import java.security.*;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class Clave {
  public static String hash(String s) {
    byte[] salt = new byte[16];
    new SecureRandom().nextBytes(salt);
    return "600000:"
        + Base64.getEncoder().encodeToString(salt)
        + ":"
        + Base64.getEncoder().encodeToString(derive(s, salt, 600000));
  }

  private static byte[] derive(String s, byte[] salt, int n) {
    try {
      PBEKeySpec p = new PBEKeySpec(s.toCharArray(), salt, n, 256);
      try {
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(p).getEncoded();
      } finally {
        p.clearPassword();
      }
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public static boolean verificar(String s, String h) {
    try {
      String[] a = h.split(":");
      return MessageDigest.isEqual(
          derive(s, Base64.getDecoder().decode(a[1]), Integer.parseInt(a[0])),
          Base64.getDecoder().decode(a[2]));
    } catch (RuntimeException e) {
      return false;
    }
  }
}
