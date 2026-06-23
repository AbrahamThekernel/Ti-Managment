package utng.gtid232.jjcm;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Clase utilitaria encargada de gestionar la navegación entre pantallas
 * (cambio de escenas) manteniendo siempre el mismo Stage principal.
 *
 * Ubicación: src/main/java/utng/gtid232/jjcm/Navegacion.java
 */
public class Navegacion {

    private static Stage stagePrincipal;

    /** Debe llamarse una sola vez desde App.start(). */
    public static void setStagePrincipal(Stage stage) {
        stagePrincipal = stage;
    }

    public static Stage getStagePrincipal() {
        return stagePrincipal;
    }

    /**
     * Navega a una pantalla FXML reemplazando el contenido del Stage actual.
     *
     * @param fxmlFile nombre del archivo FXML (ej. "ListaOrdenes.fxml")
     * @return el FXMLLoader usado, por si se necesita acceder al controlador
     */
    public static FXMLLoader navegarA(String fxmlFile) {
        try {
            URL resource = Navegacion.class.getResource("/utng/gtid232/jjcm/" + fxmlFile);
            if (resource == null) {
                throw new IOException("No se encontró el recurso FXML: " + fxmlFile
                        + " en /utng/gtid232/jjcm/");
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            if (stagePrincipal.getScene() == null) {
                stagePrincipal.setScene(new Scene(root));
            } else {
                stagePrincipal.getScene().setRoot(root);
            }
            return loader;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Cierra la aplicación. */
    public static void cerrarAplicacion() {
        if (stagePrincipal != null) {
            stagePrincipal.close();
        }
    }
}
