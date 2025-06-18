package View;

import Model.MyModel;
import ViewModel.MyViewModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.scene.input.KeyEvent;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/MyView.fxml"));
        Parent root = loader.load();

        MyModel model = new MyModel();
        MyViewModel viewModel = new MyViewModel(model);

        MyViewController controller = loader.getController();
        controller.setViewModel(viewModel);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/View/MainStyle.css").toExternalForm());

        // ✅ Global key handler that PREVENTS TextField interference
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
                case UP, DOWN, LEFT, RIGHT -> {
                    controller.handleKeyPress(event); // your method already expects a KeyEvent
                    event.consume(); // block TextField default behavior
                }
            }
        });

        primaryStage.setTitle("Maze App");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
