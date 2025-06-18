package View;

import Model.MovementDirection;
import ViewModel.MyViewModel;
import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.Position;
import algorithms.search.Solution;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import java.io.*;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.ResourceBundle;

public class MyViewController implements Initializable, Observer {
    public MyViewModel viewModel;
    private boolean solutionShown = false;
    private boolean nextStepVisible = false;
    private Position highlightedPosition = null;

    public void setViewModel(MyViewModel viewModel) {
        this.viewModel = viewModel;
        this.viewModel.addObserver(this);
    }

    @FXML private MazeDisplayer mazeDisplayer;
    @FXML private Label playerRow;
    @FXML private Label playerCol;
    @FXML private TextField mazeRows;
    @FXML private ImageView victoryGif;
    @FXML private BorderPane rootPane;
    @FXML private StackPane mazeContainer;
    @FXML private Label stepCounter;
    @FXML private Label timeCounter;
    @FXML private TextField mazeColumns;
    private Timeline timer;
    StringProperty updatePlayerRow = new SimpleStringProperty();
    StringProperty updatePlayerCol = new SimpleStringProperty();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        playerRow.textProperty().bind(updatePlayerRow);
        playerCol.textProperty().bind(updatePlayerCol);

        mazeDisplayer.setFocusTraversable(true);
        mazeDisplayer.setOnKeyPressed(this::handleKeyPress);

        mazeRows.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode().isArrowKey()) {
                event.consume();
            }
        });

        mazeColumns.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode().isArrowKey()) {
                event.consume();
            }
        });

        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeCounter.setText("⏱ Time: " + viewModel.getElapsedTimeInSeconds() + "s");
        }));
        timer.setCycleCount(Timeline.INDEFINITE);

        mazeContainer.setMinWidth(0);
        mazeContainer.setMinHeight(0);

        mazeDisplayer.widthProperty().bind(mazeContainer.widthProperty());
        mazeDisplayer.heightProperty().bind(mazeContainer.heightProperty());

        mazeDisplayer.widthProperty().addListener((obs, oldVal, newVal) -> mazeDisplayer.redraw());
        mazeDisplayer.heightProperty().addListener((obs, oldVal, newVal) -> mazeDisplayer.redraw());
    }

    public void handleKeyPress(KeyEvent event) {
        switch (event.getCode()) {
            case W -> viewModel.movePlayer(MovementDirection.UP);
            case S -> viewModel.movePlayer(MovementDirection.DOWN);
            case A -> viewModel.movePlayer(MovementDirection.LEFT);
            case D -> viewModel.movePlayer(MovementDirection.RIGHT);
            case Q -> viewModel.movePlayer(MovementDirection.UP_LEFT);
            case E -> viewModel.movePlayer(MovementDirection.UP_RIGHT);
            case Z -> viewModel.movePlayer(MovementDirection.DOWN_LEFT);
            case C -> viewModel.movePlayer(MovementDirection.DOWN_RIGHT);
            default -> { return; }
        }

        mazeDisplayer.requestFocus();
    }

    @Override
    public void update(Observable o, Object arg) {
        String change = (String) arg;
        switch (change) {
            case "mazeGenerated" -> mazeGenerated();
            case "playerMoved" -> playerMoved();
            case "mazeSolved" -> mazeSolved();
            default -> System.out.println("Not implemented change: " + change);
        }
    }

    private void mazeSolved() {
        mazeDisplayer.setSolution(viewModel.getSolution());
    }

    private void playerMoved() {
        setPlayerPosition(viewModel.getPlayerRow(), viewModel.getPlayerCol());

        nextStepVisible = false;
        highlightedPosition = null;

        if (viewModel.shouldShowVictorySequence()) {
            showVictoryGIFThenDialog();
            timer.stop();
        }
        stepCounter.setText("🚶 Steps: " + viewModel.getStepCount());
    }

    private void mazeGenerated() {
        mazeDisplayer.drawMaze(viewModel.getMaze());
        timer.playFromStart();
        stepCounter.setText("🚶 Steps: 0");
        timeCounter.setText("⏱ Time: 0s");
    }

    public void setPlayerPosition(int row, int col) {
        mazeDisplayer.setPlayerPosition(row, col);
        setUpdatePlayerRow(row);
        setUpdatePlayerCol(col);
    }

    public void setUpdatePlayerRow(int updatePlayerRow) {
        this.updatePlayerRow.set(updatePlayerRow + "");
    }

    public void setUpdatePlayerCol(int updatePlayerCol) {
        this.updatePlayerCol.set(updatePlayerCol + "");
    }

    @FXML
    public void onStartClicked() {
        try {
            int rows = Integer.parseInt(mazeRows.getText());
            int cols = Integer.parseInt(mazeColumns.getText());

            viewModel.generateMaze(rows, cols);
            Maze maze = viewModel.getMaze();

            mazeDisplayer.drawMaze(maze);
            Position playerPosition = maze.getStartPosition();
            Position goalPosition = maze.getGoalPosition();
            mazeDisplayer.setPlayerPosition(playerPosition.getRowIndex(), playerPosition.getColumnIndex());
            mazeDisplayer.setEndPoint(goalPosition.getRowIndex(), goalPosition.getColumnIndex());

            mazeRows.getParent().requestFocus();
            Platform.runLater(() -> mazeDisplayer.requestFocus());

        } catch (NumberFormatException e) {
            System.out.println("Invalid input: rows and columns must be integers.");
        }
    }

    @FXML
    public void solveMaze() {
        viewModel.solveMaze();
        mazeDisplayer.setSolution(viewModel.getSolution());

        nextStepVisible = false;
        highlightedPosition = null;

        Platform.runLater(() -> mazeDisplayer.requestFocus());
    }

    @FXML
    public void showNextStep() {
        viewModel.solveMaze();
        Solution sol = viewModel.getSolution();
        if (sol == null || sol.getSolutionPath().isEmpty()) return;

        int playerRow = viewModel.getPlayerRow();
        int playerCol = viewModel.getPlayerCol();

        for (int i = 0; i < sol.getSolutionPath().size() - 1; i++) {
            Position pos = parsePosition(sol.getSolutionPath().get(i).getState());
            if (pos.getRowIndex() == playerRow && pos.getColumnIndex() == playerCol) {
                Position next = parsePosition(sol.getSolutionPath().get(i + 1).getState());
                mazeDisplayer.showNextStepImage(next.getRowIndex(), next.getColumnIndex());
                nextStepVisible = true;
                highlightedPosition = next;
                break;
            }
        }

        mazeDisplayer.getParent().requestFocus();
        Platform.runLater(() -> {
            mazeDisplayer.setFocusTraversable(true);
            mazeDisplayer.requestFocus();
        });
    }

    private Position parsePosition(String state) {
        String[] parts = state.replaceAll("[{}]", "").split(",");
        int row = Integer.parseInt(parts[0]);
        int col = Integer.parseInt(parts[1]);
        return new Position(row, col);
    }

    @FXML
    public void removeSolution() {
        if (viewModel.getMaze() == null) return;
        mazeDisplayer.clearSolution();
        nextStepVisible = false;
        highlightedPosition = null;

        Platform.runLater(() -> {
            mazeDisplayer.setFocusTraversable(true);
            mazeDisplayer.requestFocus();
        });
    }

    @FXML
    public void removeNextStep() {
        mazeDisplayer.getParent().requestFocus();
        mazeDisplayer.removeNextStepImage();
        nextStepVisible = false;
        highlightedPosition = null;

        PauseTransition pause = new PauseTransition(Duration.millis(50));
        pause.setOnFinished(e -> mazeDisplayer.requestFocus());
        pause.play();
    }

    public void handleSaveMaze(javafx.event.ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Maze");
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            viewModel.saveMaze(file);
        }
    }

    public void handleLoadMaze(javafx.event.ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Maze");
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            viewModel.loadMaze(file);
        }
    }

    @FXML
    private void onNewClicked() {
        TextInputDialog dialog = new TextInputDialog("10x10");
        dialog.setTitle("New Maze");
        dialog.setHeaderText("Create a New Maze");
        dialog.setContentText("Enter number of rows and columns (e.g., 10x10):");

        dialog.showAndWait().ifPresent(input -> {
            try {
                String[] parts = input.toLowerCase().split("x");
                int rows = Integer.parseInt(parts[0].trim());
                int cols = Integer.parseInt(parts[1].trim());

                mazeRows.setText(String.valueOf(rows));
                mazeColumns.setText(String.valueOf(cols));
                onStartClicked();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Input");
                alert.setHeaderText(null);
                alert.setContentText("Please enter a valid format: 10x10");
                alert.showAndWait();
            }
        });
    }

    @FXML
    private void onPropertiesClicked() {
        System.out.println("Properties clicked - Opening settings window");
    }

    @FXML
    private void onExitClicked() {
        System.exit(0);
    }

    @FXML
    private void onHelpClicked() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Help");
        alert.setHeaderText("Game Help");
        alert.setContentText("Use the following keys to move your player:\n" +
                "W – Up\t\tA – Left\t\tS – Down\t\tD – Right\n" +
                "Q – Up-Left\tE – Up-Right\tZ – Down-Left\tC – Down-Right\n\n" +
                "Click 'Solve Maze' to display the optimal path to the goal (without moving the player).\n" +
                "Click 'Show Next Step' to highlight the next move you should take.\n" +
                "You can clear the displayed solution using 'Remove Solution' or 'Remove Next Step'.");
        alert.showAndWait();
    }

    @FXML
    private void onAboutClicked() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Maze App");
        alert.setContentText("Maze App was created as a demo project by Shay Smertenko and Alon Kamenetsky. Version 1.0");
        alert.showAndWait();
    }

    private void showVictoryGIFThenDialog() {
        mazeDisplayer.setVisible(false);
        try {
            InputStream gifStream = getClass().getResourceAsStream("/victory.gif");
            if (gifStream == null) {
                System.out.println("GIF not found!");
                return;
            }
            Image gif = new Image(gifStream);
            victoryGif.setImage(gif);
            victoryGif.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        PauseTransition pause = new PauseTransition(Duration.seconds(5));
        pause.setOnFinished(e -> {
            victoryGif.setVisible(false);
            mazeDisplayer.setVisible(true);
            Platform.runLater(this::showVictoryDialog);
        });
        pause.play();
    }

    private void showVictoryDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Victory!");
        alert.setHeaderText("You solved the maze!");
        alert.setContentText("Would you like to play again or exit the game?");
        ButtonType newGame = new ButtonType("New Game");
        ButtonType exit = new ButtonType("Exit");
        alert.getButtonTypes().setAll(newGame, exit);

        alert.showAndWait().ifPresent(response -> {
            if (response == newGame) {
                onNewClicked();
            } else if (response == exit) {
                Platform.exit();
            }
        });
    }
}
