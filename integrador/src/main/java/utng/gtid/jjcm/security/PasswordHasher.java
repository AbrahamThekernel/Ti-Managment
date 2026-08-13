package utng.gtid.jjcm.security;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Genera y verifica contraseñas con PBKDF2, sal aleatoria y comparación segura.
 * Nunca se guarda la contraseña escrita por el usuario en texto plano.
 */
public final class PasswordHasher {

    /** Algoritmo disponible en el JDK sin dependencias adicionales. */
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    /** Cantidad de iteraciones que vuelve costosos los intentos masivos. */
    private static final int ITERATIONS = 120_000;

    /** Longitud de la sal aleatoria expresada en bytes. */
    private static final int SALT_BYTES = 16;

    /** Longitud de la clave derivada expresada en bits. */
    private static final int KEY_BITS = 256;

    /** Constructor privado porque la clase solamente contiene utilidades. */
    private PasswordHasher() {
    }

    /**
     * Convierte una contraseña en el formato iteraciones:sal:hash.
     */
    public static String hash(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");
        }

        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        byte[] derivedKey = derive(password.toCharArray(), salt, ITERATIONS);

        Base64.Encoder encoder = Base64.getEncoder();
        return ITERATIONS + ":" + encoder.encodeToString(salt)
                + ":" + encoder.encodeToString(derivedKey);
    }

    /**
     * Comprueba una contraseña sin revelar ni recuperar el texto original.
     */
    public static boolean verify(String password, String storedHash) {
        if (password == null || storedHash == null || storedHash.isBlank()) {
            return false;
        }

        try {
            String[] parts = storedHash.split(":");
            if (parts.length != 3) {
                return false;
            }

            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expected = Base64.getDecoder().decode(parts[2]);
            byte[] actual = derive(password.toCharArray(), salt, iterations);

            return constantTimeEquals(expected, actual);
        } catch (IllegalArgumentException error) {
            // Un hash dañado se trata como contraseña no válida.
            return false;
        }
    }

    /** Ejecuta PBKDF2 y convierte errores del proveedor en estado inválido. */
    private static byte[] derive(char[] password, byte[] salt, int iterations) {
        PBEKeySpec specification = new PBEKeySpec(password, salt, iterations, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM)
                    .generateSecret(specification)
                    .getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException error) {
            throw new IllegalStateException("El JDK no admite el algoritmo de contraseñas.", error);
        } finally {
            specification.clearPassword();
        }
    }

    /**
     * Compara todos los bytes para reducir filtraciones por tiempo de respuesta.
     */
    private static boolean constantTimeEquals(byte[] first, byte[] second) {
        if (first.length != second.length) {
            return false;
        }
        int difference = 0;
        for (int index = 0; index < first.length; index++) {
            difference |= first[index] ^ second[index];
        }
        return difference == 0;
    }
}
