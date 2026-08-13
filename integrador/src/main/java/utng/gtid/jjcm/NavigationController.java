package utng.gtid.jjcm;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import utng.gtid.jjcm.session.SessionContext;

/**
 * Controlador compartido por todas las pantallas principales.
 *
 * Usamos una sola clase porque todos los menús hacen exactamente lo mismo:
 * leer el nombre de una vista y pedirle a App que muestre su archivo FXML.
 */
public class NavigationController {

    /** Nombre de la cuenta actual mostrado en la esquina inferior izquierda. */
    @FXML
    private Label lblNombreSesion;

    /** Rol de la cuenta actual mostrado debajo del nombre. */
    @FXML
    private Label lblRolSesion;

    /**
     * App ejecuta este método después de cargar cada FXML para evitar alias
     * escritos de forma fija en las pantallas.
     */
    void actualizarIdentidadSesion() {
        if (lblNombreSesion != null) {
            lblNombreSesion.setText(SessionContext.getFullName());
        }
        if (lblRolSesion != null) {
            lblRolSesion.setText(SessionContext.getRole());
        }
    }

    /**
     * Se ejecuta al presionar cualquier botón del menú lateral.
     *
     * En cada FXML, el atributo userData contiene el nombre del archivo que se
     * abrirá. Por ejemplo: userData="equipos" abre equipos.fxml.
     */
    @FXML
    protected void cambiarVista(ActionEvent event) {
        // El botón presionado es el origen del evento.
        Button botonPresionado = (Button) event.getSource();

        // userData guarda el nombre del FXML sin la extensión ".fxml".
        String nombreVista = botonPresionado.getUserData().toString();

        try {
            // Cambiamos solamente el contenido de la escena; la ventana se conserva.
            App.setRoot(nombreVista);
        } catch (IOException error) {
            // Si el archivo no existe o contiene un error, mostramos qué vista falló.
            throw new IllegalStateException(
                    "No se pudo abrir la pantalla: " + nombreVista,
                    error
            );
        }
    }

    /**
     * Cierra la sesión regresando al formulario funcional primary.fxml.
     */
    @FXML
    protected void cerrarSesion() {
        try {
            // Se borra la identidad antes de regresar al formulario de acceso.
            SessionContext.clear();
            App.setRoot("primary");
        } catch (IOException error) {
            throw new IllegalStateException(
                    "No se pudo regresar a la pantalla de inicio de sesión.",
                    error
            );
        }
    }
}
