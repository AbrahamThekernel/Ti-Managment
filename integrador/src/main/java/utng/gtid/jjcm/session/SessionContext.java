package utng.gtid.jjcm.session;

import java.util.Locale;

/**
 * Conserva la identidad del usuario autenticado mientras la aplicación está
 * abierta. No guarda la contraseña ni el hash de contraseña.
 */
public final class SessionContext {

    /** Identificador PostgreSQL del usuario que inició sesión. */
    private static Long userId;

    /** Nombre completo que se mostrará en el menú lateral. */
    private static String fullName;

    /** Rol legible que se mostrará debajo del nombre. */
    private static String role;

    /** Constructor privado porque la sesión se utiliza de forma estática. */
    private SessionContext() {
    }

    /** Inicia una sesión después de validar correctamente las credenciales. */
    public static void start(long id, String name, String lastName, String databaseRole) {
        userId = id;
        fullName = (safe(name) + " " + safe(lastName)).trim();
        role = formatRole(databaseRole);
    }

    /** Actualiza el nombre visible cuando el usuario modifica Mi perfil. */
    public static void updateIdentity(String name, String lastName, String databaseRole) {
        if (userId == null) {
            return;
        }
        fullName = (safe(name) + " " + safe(lastName)).trim();
        role = formatRole(databaseRole);
    }

    /** Elimina la identidad al presionar Cerrar sesión. */
    public static void clear() {
        userId = null;
        fullName = null;
        role = null;
    }

    /** @return true cuando existe un usuario autenticado. */
    public static boolean isAuthenticated() {
        return userId != null;
    }

    /** @return id del usuario o null antes de iniciar sesión. */
    public static Long getUserId() {
        return userId;
    }

    /** @return nombre completo de la cuenta actual. */
    public static String getFullName() {
        return fullName == null || fullName.isBlank() ? "Sin sesión" : fullName;
    }

    /** @return rol legible de la cuenta actual. */
    public static String getRole() {
        return role == null || role.isBlank() ? "Usuario" : role;
    }

    /** Convierte null en texto vacío. */
    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    /** Convierte ADMINISTRADOR o SOPORTE_TECNICO en texto legible. */
    private static String formatRole(String databaseRole) {
        String normalized = safe(databaseRole).replace('_', ' ').toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return "Usuario";
        }
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }
}
