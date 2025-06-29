package View;

import Model.MyModel;
import ViewModel.MyViewModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.input.KeyEvent;

/**
 * Entry point for the JavaFX Maze Game application.
 * Initializes the MVVM components, loads the FXML layout, and starts the main UI scene.
 */
public class Main extends Application {

    /**
     * JavaFX lifecycle method - called when the application starts.
     * Sets up the Model, ViewModel, ViewController, and the scene.
     * @param primaryStage the primary window of the application
     * @throws Exception if the FXML cannot be loaded
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the FXML layout for the main view
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/MyView.fxml"));
        Parent root = loader.load();
        System.out.println(System.getProperty("javafx.runtime.version"));


        // Initialize the MVVM structure
        MyModel model = new MyModel();
        MyViewModel viewModel = new MyViewModel(model);
        MyViewController controller = loader.getController();
        controller.setViewModel(viewModel);

        // Create and style the main scene
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/View/MainStyle.css").toExternalForm());

        /**
         * Global key press filter to handle arrow keys even when focus is on a TextField.
         * This prevents TextField arrow navigation and ensures consistent player movement.
         */
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
                case UP, DOWN, LEFT, RIGHT -> {
                    controller.handleKeyPress(event); // Delegate to ViewController
                    event.consume(); // Prevent default TextField behavior
                }
            }
        });

        // Setup stage
        primaryStage.setTitle("Maze App");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Launches the JavaFX application.
     * @param args CLI arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
