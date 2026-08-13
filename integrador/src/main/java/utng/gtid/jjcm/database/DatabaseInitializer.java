package utng.gtid.jjcm.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Comprueba la conexión y crea el esquema requerido por la aplicación.
 */
public final class DatabaseInitializer {

    /** Ruta del esquema empaquetado dentro de src/main/resources. */
    private static final String SCHEMA_RESOURCE = "/database/schema.sql";

    /** Impide crear instancias del inicializador. */
    private DatabaseInitializer() {
    }

    /**
     * Prueba PostgreSQL y, cuando está habilitado, ejecuta schema.sql.
     *
     * Toda la creación ocurre en una transacción: si una instrucción falla,
     * PostgreSQL revierte los cambios de esa ejecución.
     */
    public static void initialize() {
        Database.testConnection();

        // Permite desactivar la creación automática en ambientes administrados.
        if (!Database.getConfig().isInitializeSchema()) {
            return;
        }

        List<String> statements = readStatements();

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                for (String sql : statements) {
                    statement.execute(sql);
                }
                connection.commit();
            } catch (SQLException error) {
                rollbackQuietly(connection);
                throw error;
            }
        } catch (SQLException error) {
            throw new DatabaseException("No se pudo crear o actualizar el esquema PostgreSQL.", error);
        }
    }

    /**
     * Lee schema.sql, elimina comentarios de línea y separa sus instrucciones.
     */
    private static List<String> readStatements() {
        InputStream stream = DatabaseInitializer.class.getResourceAsStream(SCHEMA_RESOURCE);
        if (stream == null) {
            throw new DatabaseException("No se encontró el recurso " + SCHEMA_RESOURCE + ".");
        }

        StringBuilder sql = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                // Los comentarios empiezan con -- y no deben enviarse al servidor.
                if (!trimmed.startsWith("--")) {
                    sql.append(line).append('\n');
                }
            }
        } catch (IOException error) {
            throw new DatabaseException("No se pudo leer el esquema SQL.", error);
        }

        return splitSql(sql.toString());
    }

    /**
     * Separa por punto y coma, respetando textos SQL escritos entre comillas.
     */
    private static List<String> splitSql(String script) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean insideString = false;

        for (int index = 0; index < script.length(); index++) {
            char character = script.charAt(index);

            if (character == '\'') {
                // Dos comillas consecutivas representan una comilla dentro del texto.
                boolean escapedQuote = insideString
                        && index + 1 < script.length()
                        && script.charAt(index + 1) == '\'';
                current.append(character);
                if (escapedQuote) {
                    current.append(script.charAt(++index));
                    continue;
                }
                insideString = !insideString;
                continue;
            }

            if (character == ';' && !insideString) {
                String statement = current.toString().trim();
                if (!statement.isEmpty()) {
                    statements.add(statement);
                }
                current.setLength(0);
            } else {
                current.append(character);
            }
        }

        String remaining = current.toString().trim();
        if (!remaining.isEmpty()) {
            statements.add(remaining);
        }
        return statements;
    }

    /** Intenta revertir una transacción sin ocultar el error original. */
    private static void rollbackQuietly(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // El error principal se conserva; este fallo secundario no lo reemplaza.
        }
    }
}
