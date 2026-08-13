package utng.gtid2.inicio;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

   @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader =
         new FXMLLoader(App.class.getResource("login.fxml"));

        scene = new Scene(fxmlLoader.load());

        scene.getStylesheets().add(
            App.class.getResource("/css/styles.css").toExternalForm());
        System.out.println(
        App.class.getResource("/css/styles.css")
        );
    
        stage.setScene(scene);
        stage.show();

    }  

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
    var url = App.class.getResource(fxml + ".fxml");

    if (url == null) {
        throw new IllegalArgumentException(
            "No se encontró el archivo: " + fxml + ".fxml"
        );
    }

    FXMLLoader loader = new FXMLLoader(url);
    return loader.load();
}

    public static void main(String[] args) {
        launch();
    }

    public static void navigateTo(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'navigateTo'");
    }

    
}
