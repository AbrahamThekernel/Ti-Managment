package utng.gtid.jjcm.database;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Lee la configuración necesaria para conectarse a PostgreSQL.
 *
 * El orden de prioridad es:
 * 1. Variables de entorno, útiles al publicar la aplicación.
 * 2. config/database.properties, útil durante el desarrollo local.
 * 3. database-defaults.properties, que contiene valores sin contraseña.
 */
public final class DatabaseConfig {

    /** Nombre de la propiedad que almacena la URL JDBC. */
    private static final String URL_PROPERTY = "db.url";

    /** Nombre de la propiedad que almacena el usuario PostgreSQL. */
    private static final String USER_PROPERTY = "db.user";

    /** Nombre de la propiedad que almacena la contraseña PostgreSQL. */
    private static final String PASSWORD_PROPERTY = "db.password";

    /** Nombre de la propiedad que activa la creación automática de tablas. */
    private static final String INITIALIZE_PROPERTY = "db.initialize";

    /** Nombre relativo del archivo local que contiene la conexión privada. */
    private static final Path RELATIVE_EXTERNAL_FILE =
            Paths.get("config", "database.properties");

    /**
     * Propiedad opcional para indicar una ruta diferente al iniciar Java.
     * Ejemplo: -Dutng.db.config=C:\\ruta\\database.properties
     */
    private static final String CONFIG_PATH_SYSTEM_PROPERTY = "utng.db.config";

    /** URL final que utilizará DriverManager. */
    private final String url;

    /** Usuario final que utilizará DriverManager. */
    private final String user;

    /** Contraseña final que utilizará DriverManager. */
    private final String password;

    /** Indica si las tablas deben revisarse al iniciar. */
    private final boolean initializeSchema;

    /**
     * Construye una configuración inmutable.
     */
    private DatabaseConfig(String url, String user, String password, boolean initializeSchema) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.initializeSchema = initializeSchema;
    }

    /**
     * Carga la configuración completa respetando el orden de prioridad.
     *
     * @return configuración lista para abrir conexiones.
     */
    public static DatabaseConfig load() {
        Properties properties = new Properties();

        // Primero cargamos valores seguros incluidos dentro de la aplicación.
        try (InputStream defaults = DatabaseConfig.class.getResourceAsStream(
                "/database-defaults.properties")) {
            if (defaults != null) {
                properties.load(defaults);
            }
        } catch (IOException error) {
            throw new DatabaseException("No se pudo leer database-defaults.properties.", error);
        }

        /*
         * Después buscamos el archivo local. No se usa solamente
         * Paths.get("config", ...), porque VS Code puede iniciar el programa
         * desde C:\integradoraa aunque el proyecto esté en la subcarpeta
         * C:\integradoraa\integrador.
         */
        Path externalFile = findExternalFile();
        if (externalFile != null) {
            try (InputStream external = Files.newInputStream(externalFile)) {
                properties.load(external);
            } catch (IOException error) {
                throw new DatabaseException(
                        "No se pudo leer " + externalFile.toAbsolutePath() + ".",
                        error
                );
            }
        }

        // Finalmente las variables de entorno tienen la prioridad más alta.
        String url = environmentOrProperty("UTNG_DB_URL", properties, URL_PROPERTY);
        String user = environmentOrProperty("UTNG_DB_USER", properties, USER_PROPERTY);
        String password = environmentOrProperty(
                "UTNG_DB_PASSWORD",
                properties,
                PASSWORD_PROPERTY
        );
        String initialize = environmentOrProperty(
                "UTNG_DB_INITIALIZE",
                properties,
                INITIALIZE_PROPERTY
        );

        // La URL y el usuario siempre son obligatorios.
        if (url == null || url.isBlank()) {
            throw new DatabaseException("Falta configurar db.url o UTNG_DB_URL.");
        }
        if (user == null || user.isBlank()) {
            throw new DatabaseException("Falta configurar db.user o UTNG_DB_USER.");
        }

        return new DatabaseConfig(
                url.trim(),
                user.trim(),
                password == null ? "" : password,
                Boolean.parseBoolean(initialize)
        );
    }

    /**
     * Localiza database.properties sin depender del directorio elegido por el IDE.
     *
     * @return ruta existente o null cuando debe usarse la configuración segura
     *         incluida en resources.
     */
    private static Path findExternalFile() {
        // Una ruta indicada explícitamente siempre tiene la prioridad más alta.
        String explicitPath = System.getProperty(CONFIG_PATH_SYSTEM_PROPERTY);
        if (explicitPath != null && !explicitPath.isBlank()) {
            Path configuredPath = Paths.get(explicitPath).toAbsolutePath().normalize();
            if (!Files.isRegularFile(configuredPath)) {
                throw new DatabaseException(
                        "No existe el archivo indicado en -D"
                        + CONFIG_PATH_SYSTEM_PROPERTY + ": " + configuredPath
                );
            }
            return configuredPath;
        }

        /*
         * Primera opción: se ejecutó dentro de C:\integradoraa\integrador.
         * Segunda opción: se ejecutó desde C:\integradoraa, como en la consola
         * mostrada por el usuario.
         */
        List<Path> candidates = Arrays.asList(
                RELATIVE_EXTERNAL_FILE,
                Paths.get("integrador").resolve(RELATIVE_EXTERNAL_FILE)
        );

        for (Path candidate : candidates) {
            Path absoluteCandidate = candidate.toAbsolutePath().normalize();
            if (Files.isRegularFile(absoluteCandidate)) {
                return absoluteCandidate;
            }
        }
        return null;
    }

    /**
     * Devuelve la variable de entorno cuando existe; de lo contrario usa Properties.
     */
    private static String environmentOrProperty(
            String environmentName,
            Properties properties,
            String propertyName
    ) {
        String environmentValue = System.getenv(environmentName);
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue;
        }
        return properties.getProperty(propertyName, "");
    }

    /** @return URL JDBC, por ejemplo jdbc:postgresql://localhost:5432/gestion_utng. */
    public String getUrl() {
        return url;
    }

    /** @return nombre del usuario PostgreSQL. */
    public String getUser() {
        return user;
    }

    /** @return contraseña PostgreSQL; nunca debe imprimirse en registros. */
    public String getPassword() {
        return password;
    }

    /** @return true cuando debe ejecutarse schema.sql al iniciar. */
    public boolean isInitializeSchema() {
        return initializeSchema;
    }

    /**
     * Devuelve información segura para mensajes de diagnóstico.
     * La contraseña se omite intencionalmente.
     */
    public String describeSafely() {
        return "url=" + url + ", user=" + user;
    }
}
