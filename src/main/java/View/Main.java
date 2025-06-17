package View;

import Model.MyModel;
import ViewModel.MyViewModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/MyView.fxml"));
        Parent root = loader.load();

        // Initialize Model and ViewModel
        MyModel model = new MyModel();
        MyViewModel viewModel = new MyViewModel(model);

        // Inject ViewModel into Controller
        MyViewController controller = loader.getController();
        controller.setViewModel(viewModel);
        Scene scene = new Scene(root);
        scene.setOnKeyPressed(event -> {
            controller.handleKeyPress(event); // Delegate to your controller
        });
        scene.getStylesheets().add(getClass().getResource("/View/MainStyle.css").toExternalForm());
        primaryStage.setTitle("Maze App");
        primaryStage.setScene(scene); // ✅ reuse the same Scene
        primaryStage.show();
        root.requestFocus();


    }

    public static void main(String[] args) {
        launch(args);
    }
}
