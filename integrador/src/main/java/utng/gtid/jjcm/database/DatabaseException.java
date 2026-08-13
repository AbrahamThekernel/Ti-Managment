package utng.gtid.jjcm.database;

/**
 * Excepción propia de la aplicación para errores de persistencia.
 *
 * Convertimos las excepciones verificadas de JDBC en este tipo para que los
 * controladores puedan mostrar mensajes comprensibles sin mezclar SQL con UI.
 */
public class DatabaseException extends RuntimeException {

    /** Identificador de versión requerido por Serializable. */
    private static final long serialVersionUID = 1L;

    /**
     * Crea una excepción con un mensaje y conserva la causa técnica original.
     *
     * @param message explicación legible de la operación que falló.
     * @param cause excepción SQLException o IOException que originó el problema.
     */
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Crea una excepción para una validación de datos o regla de negocio.
     *
     * @param message explicación legible del problema.
     */
    public DatabaseException(String message) {
        super(message);
    }
}
