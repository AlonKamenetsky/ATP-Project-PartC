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

        // Set up the stage
        primaryStage.setTitle("Maze App");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
