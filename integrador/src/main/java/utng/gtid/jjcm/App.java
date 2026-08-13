package utng.gtid.jjcm;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.io.IOException;
import utng.gtid.jjcm.database.DatabaseException;
import utng.gtid.jjcm.database.DatabaseInitializer;

/**
 * Punto de entrada de la aplicación JavaFX.
 */
public class App extends Application {

    // Todas las pantallas se muestran dentro de esta misma escena.
    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        try {
            /*
             * Antes de mostrar la interfaz comprobamos credenciales y tablas.
             * schema.sql es repetible, por lo que no duplica los datos iniciales.
             */
            DatabaseInitializer.initialize();
        } catch (DatabaseException error) {
            // Un mensaje claro evita que la aplicación falle sin explicar la causa.
            showDatabaseError(error);
            return;
        }

        // Inventario funciona actualmente como la pantalla inicial del sistema.
        scene = new Scene(loadFXML("primary"), 1280, 720);

        stage.setTitle("UTNG - Gestión Institucional");
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    /**
     * Sustituye la pantalla actual sin crear otra ventana.
     * NavigationController utiliza este método desde el menú lateral.
     */
    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    /**
     * Recibe el nombre del archivo sin extensión; por ejemplo, "equipos".
     */
    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        Parent root = fxmlLoader.load();

        /*
         * Todas las pantallas del menú heredan NavigationController. Después de
         * inyectar sus Labels, se coloca la identidad de la sesión actual.
         */
        Object controller = fxmlLoader.getController();
        if (controller instanceof NavigationController) {
            ((NavigationController) controller).actualizarIdentidadSesion();
        }
        return root;
    }

    /**
     * Explica cómo corregir una conexión fallida sin mostrar la contraseña.
     */
    private void showDatabaseError(DatabaseException error) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("No fue posible conectar con PostgreSQL");
        alert.setHeaderText("Revisa la configuración de la base gestion_utng");
        alert.setContentText(
                error.getMessage()
                + "\n\nConfigura la contraseña en config/database.properties "
                + "o en la variable UTNG_DB_PASSWORD."
        );
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }

}
