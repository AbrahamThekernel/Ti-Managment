package utng.gtid.jjcm.service;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import utng.gtid.jjcm.database.DatabaseConfig;
import utng.gtid.jjcm.database.DatabaseException;
import utng.gtid.jjcm.model.BackupView;
import utng.gtid.jjcm.repository.BackupRepository;
import utng.gtid.jjcm.repository.CatalogRepository;

/**
 * Ejecuta pg_dump y pg_restore sin colocar la contraseña en la línea de comandos.
 */
public final class BackupService {

    /** Repositorio del historial de archivos. */
    private final BackupRepository backupRepository = new BackupRepository();

    /** Repositorio que identifica al administrador que inició la operación. */
    private final CatalogRepository catalogRepository = new CatalogRepository();

    /** Devuelve el historial completo para la pantalla. */
    public List<BackupView> findAll() {
        return backupRepository.findAll();
    }

    /**
     * Genera un respaldo PostgreSQL en formato custom, apropiado para pg_restore.
     */
    public void createManualBackup(Path destination) {
        DatabaseTarget target = DatabaseTarget.from(DatabaseConfig.load());
        long administratorId = catalogRepository.findDefaultAdministratorId();
        String name = removeExtension(destination.getFileName().toString());
        long backupId = backupRepository.insertStarted(
                name,
                destination.toAbsolutePath().toString(),
                "MANUAL",
                administratorId
        );

        List<String> command = new ArrayList<>();
        command.add(findPostgreSqlTool("pg_dump"));
        command.add("--host");
        command.add(target.host);
        command.add("--port");
        command.add(String.valueOf(target.port));
        command.add("--username");
        command.add(target.user);
        command.add("--format=custom");
        command.add("--no-owner");
        command.add("--file");
        command.add(destination.toAbsolutePath().toString());
        command.add(target.database);

        try {
            String output = runPostgreSqlTool(command, target.password);
            long size = Files.size(destination);
            backupRepository.finish(backupId, "COMPLETADO", size,
                    output.isBlank() ? "Respaldo creado correctamente." : output);
        } catch (IOException | DatabaseException error) {
            backupRepository.finish(backupId, "FALLIDO", null, safeMessage(error));
            throw new DatabaseException("No se pudo crear el respaldo: " + safeMessage(error), error);
        }
    }

    /**
     * Restaura un archivo custom en la base configurada. La confirmación de esta
     * operación destructiva se solicita en el controlador antes de llegar aquí.
     */
    public void restore(BackupView backup) {
        Path source = Path.of(backup.getPath()).toAbsolutePath().normalize();
        if (!Files.isRegularFile(source)) {
            throw new DatabaseException("El archivo del respaldo ya no existe en la ruta registrada.");
        }

        DatabaseTarget target = DatabaseTarget.from(DatabaseConfig.load());
        List<String> command = new ArrayList<>();
        command.add(findPostgreSqlTool("pg_restore"));
        command.add("--host");
        command.add(target.host);
        command.add("--port");
        command.add(String.valueOf(target.port));
        command.add("--username");
        command.add(target.user);
        command.add("--clean");
        command.add("--if-exists");
        command.add("--no-owner");
        command.add("--dbname");
        command.add(target.database);
        command.add(source.toString());

        runPostgreSqlTool(command, target.password);

        // El dump capturó este registro como EN_PROCESO; se corrige tras restaurar.
        backupRepository.finish(backup.getId(), "COMPLETADO", backup.getSizeBytes(),
                "Base restaurada correctamente desde este archivo.");
    }

    /** Copia el archivo exacto a una nueva ruta elegida por el usuario. */
    public void copyTo(BackupView backup, Path destination) {
        Path source = Path.of(backup.getPath()).toAbsolutePath().normalize();
        try {
            Files.copy(source, destination.toAbsolutePath().normalize(),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException error) {
            throw new DatabaseException("No se pudo copiar el archivo de respaldo.", error);
        }
    }

    /**
     * Elimina el archivo exacto y después sus metadatos. El controlador solicita
     * confirmación antes de invocar este método.
     */
    public void delete(BackupView backup) {
        Path file = Path.of(backup.getPath()).toAbsolutePath().normalize();
        try {
            Files.deleteIfExists(file);
            backupRepository.delete(backup.getId());
        } catch (IOException error) {
            throw new DatabaseException("No se pudo eliminar el archivo de respaldo.", error);
        }
    }

    /** Ejecuta el proceso y devuelve su salida combinada. */
    private String runPostgreSqlTool(List<String> command, String password) {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);

        // PGPASSWORD se entrega solamente al proceso hijo y nunca se imprime.
        builder.environment().put("PGPASSWORD", password);
        try {
            Process process = builder.start();
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new DatabaseException(
                        "La herramienta PostgreSQL terminó con código " + exitCode
                        + (output.isBlank() ? "." : ": " + output.trim())
                );
            }
            return output.trim();
        } catch (IOException error) {
            throw new DatabaseException(
                    "No se encontró o no se pudo ejecutar la herramienta PostgreSQL.", error
            );
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new DatabaseException("La operación de respaldo fue interrumpida.", error);
        }
    }

    /**
     * Localiza pg_dump.exe o pg_restore.exe en instalaciones comunes de Windows.
     */
    private String findPostgreSqlTool(String toolName) {
        String executable = isWindows() ? toolName + ".exe" : toolName;
        String configuredDirectory = System.getenv("UTNG_PG_BIN");
        if (configuredDirectory != null && !configuredDirectory.isBlank()) {
            Path configured = Path.of(configuredDirectory, executable);
            if (Files.isRegularFile(configured)) return configured.toString();
        }

        String programFiles = System.getenv("ProgramFiles");
        if (programFiles != null) {
            Path root = Path.of(programFiles, "PostgreSQL");
            if (Files.isDirectory(root)) {
                try (Stream<Path> versions = Files.list(root)) {
                    Path found = versions
                            .sorted(Comparator.reverseOrder())
                            .map(version -> version.resolve("bin").resolve(executable))
                            .filter(Files::isRegularFile)
                            .findFirst()
                            .orElse(null);
                    if (found != null) return found.toString();
                } catch (IOException error) {
                    throw new DatabaseException("No se pudo revisar la instalación de PostgreSQL.", error);
                }
            }
        }

        // Como último recurso se permite que ProcessBuilder utilice el PATH.
        return executable;
    }

    /** Devuelve true en sistemas Windows. */
    private boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    /** Quita la extensión solamente para el nombre mostrado en la tabla. */
    private String removeExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }

    /** Evita mensajes null en la interfaz. */
    private String safeMessage(Throwable error) {
        return error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
    }

    /**
     * Datos de conexión adaptados desde la URL JDBC a argumentos de consola.
     */
    private static final class DatabaseTarget {
        private final String host;
        private final int port;
        private final String database;
        private final String user;
        private final String password;

        /** Construye un destino inmutable. */
        private DatabaseTarget(String host, int port, String database,
                               String user, String password) {
            this.host = host;
            this.port = port;
            this.database = database;
            this.user = user;
            this.password = password;
        }

        /** Analiza jdbc:postgresql://host:puerto/base. */
        private static DatabaseTarget from(DatabaseConfig configuration) {
            try {
                String uriText = configuration.getUrl().substring("jdbc:".length());
                URI uri = URI.create(uriText);
                String database = uri.getPath().replaceFirst("^/", "");
                int port = uri.getPort() < 0 ? 5432 : uri.getPort();
                if (uri.getHost() == null || database.isBlank()) {
                    throw new IllegalArgumentException("URL incompleta");
                }
                return new DatabaseTarget(uri.getHost(), port, database,
                        configuration.getUser(), configuration.getPassword());
            } catch (RuntimeException error) {
                throw new DatabaseException(
                        "db.url debe tener el formato jdbc:postgresql://host:puerto/base.", error
                );
            }
        }
    }
}
