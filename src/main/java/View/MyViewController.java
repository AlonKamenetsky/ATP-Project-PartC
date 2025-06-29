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
import javafx.scene.media.MediaPlayer;
import java.io.*;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import javafx.scene.media.Media;

/**
 * Controller class for the JavaFX Maze Game.
 * Acts as the View in MVVM, responding to UI events and updating visuals via the ViewModel.
 * Handles maze display, player movement, audio feedback, solution display, and dialog interaction.
 */
public class MyViewController implements Initializable, Observer {

    public MyViewModel viewModel;

    private boolean nextStepVisible = false;
    private Position highlightedPosition = null;

    private MediaPlayer clickSound;
    private MediaPlayer gameAudio;
    private MediaPlayer winAudio;

    // FXML-injected components
    @FXML private MazeDisplayer mazeDisplayer;
    @FXML private Label playerRow;
    @FXML private Label playerCol;
    @FXML private TextField mazeRows;
    @FXML private TextField mazeColumns;
    @FXML private ImageView victoryGif;
    @FXML private BorderPane rootPane;
    @FXML private StackPane mazeContainer;
    @FXML private Label stepCounter;
    @FXML private Label timeCounter;

    private Timeline timer;

    StringProperty updatePlayerRow = new SimpleStringProperty();
    StringProperty updatePlayerCol = new SimpleStringProperty();

    /**
     * Sets the ViewModel and registers the controller as an observer.
     * @param viewModel the shared ViewModel instance
     */
    public void setViewModel(MyViewModel viewModel) {
        this.viewModel = viewModel;
        this.viewModel.addObserver(this);
    }

    /**
     * Called once upon UI initialization.
     * Sets up bindings, event listeners, audio, and timer logic.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mazeDisplayer.widthProperty().bind(mazeContainer.widthProperty());
        mazeDisplayer.heightProperty().bind(mazeContainer.heightProperty());
        playerRow.textProperty().bind(updatePlayerRow);
        playerCol.textProperty().bind(updatePlayerCol);

        mazeDisplayer.setFocusTraversable(true);
        mazeDisplayer.setOnKeyPressed(this::handleKeyPress);

        mazeRows.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode().isArrowKey()) event.consume();
        });
        mazeColumns.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode().isArrowKey()) event.consume();
        });

        timer = new Timeline(new KeyFrame(Duration.seconds(1), e ->
                timeCounter.setText("⏱ Time: " + viewModel.getElapsedTimeInSeconds() + "s")));
        timer.setCycleCount(Timeline.INDEFINITE);

        mazeDisplayer.widthProperty().bind(mazeContainer.widthProperty());
        mazeDisplayer.heightProperty().bind(mazeContainer.heightProperty());
        mazeDisplayer.widthProperty().addListener((obs, oldVal, newVal) -> mazeDisplayer.redraw());
        mazeDisplayer.heightProperty().addListener((obs, oldVal, newVal) -> mazeDisplayer.redraw());

        // Load sounds
        try {
            clickSound = new MediaPlayer(new Media(getClass().getResource("/Sounds/movement.mp3").toExternalForm()));
            gameAudio = new MediaPlayer(new Media(getClass().getResource("/Sounds/gameAudio.mp3").toExternalForm()));
            winAudio = new MediaPlayer(new Media(getClass().getResource("/Sounds/win.mp3").toExternalForm()));
        } catch (Exception e) {
            System.out.println("Error loading sound: " + e.getMessage());
        }
    }

    /**
     * Handles keyboard movement (WASD + diagonals).
     * @param event the key event
     */
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

    /**
     * Observer update method. Reacts to changes from the ViewModel.
     */
    @Override
    public void update(Observable o, Object arg) {
        String change = (String) arg;
        switch (change) {
            case "mazeGenerated" -> mazeGenerated();
            case "playerMoved" -> playerMoved();
            case "mazeSolved" -> mazeSolved();
            default -> System.out.println("Unhandled update: " + change);
        }
    }

    /**
     * Called when a new maze is generated.
     */
    private void mazeGenerated() {
        mazeDisplayer.drawMaze(viewModel.getMaze());
        timer.playFromStart();
        stepCounter.setText("🚶 Steps: 0");
        timeCounter.setText("⏱ Time: 0s");
    }

    /**
     * Updates the maze view and UI after a player move.
     */
    private void playerMoved() {
        setPlayerPosition(viewModel.getPlayerRow(), viewModel.getPlayerCol());
        clickSound.stop();
        clickSound.play();
        nextStepVisible = false;
        highlightedPosition = null;

        if (viewModel.shouldShowVictorySequence()) {
            showVictoryGIFThenDialog();
            timer.stop();
        }

        stepCounter.setText("🚶 Steps: " + viewModel.getStepCount());
    }

    /**
     * Called when the maze is solved and updates the solution view.
     */
    private void mazeSolved() {
        mazeDisplayer.setSolution(viewModel.getSolution());
    }

    /**
     * Binds UI player row label.
     */
    public void setUpdatePlayerRow(int updatePlayerRow) {
        this.updatePlayerRow.set(String.valueOf(updatePlayerRow));
    }

    /**
     * Binds UI player column label.
     */
    public void setUpdatePlayerCol(int updatePlayerCol) {
        this.updatePlayerCol.set(String.valueOf(updatePlayerCol));
    }

    /**
     * Sets and draws player position.
     */
    public void setPlayerPosition(int row, int col) {
        mazeDisplayer.setPlayerPosition(row, col);
        setUpdatePlayerRow(row);
        setUpdatePlayerCol(col);
    }

    /**
     * Generates and starts a new maze from user input.
     */
    @FXML
    public void onStartClicked() {
        try {
            int rows = Integer.parseInt(mazeRows.getText());
            int cols = Integer.parseInt(mazeColumns.getText());

            viewModel.generateMaze(rows, cols);
            Maze maze = viewModel.getMaze();
            mazeDisplayer.drawMaze(maze);
            mazeDisplayer.setPlayerPosition(maze.getStartPosition().getRowIndex(), maze.getStartPosition().getColumnIndex());
            mazeDisplayer.setEndPoint(maze.getGoalPosition().getRowIndex(), maze.getGoalPosition().getColumnIndex());

            gameAudio.setCycleCount(MediaPlayer.INDEFINITE);
            gameAudio.play();
            winAudio.stop();
            mazeRows.getParent().requestFocus();
            Platform.runLater(() -> mazeDisplayer.requestFocus());

        } catch (NumberFormatException e) {
            System.out.println("Invalid input: must be integers.");
        }
    }

    /**
     * Triggers maze solution and displays the full path.
     */
    @FXML
    public void solveMaze() {
        viewModel.solveMaze();
        mazeDisplayer.setSolution(viewModel.getSolution());
        nextStepVisible = false;
        highlightedPosition = null;
        Platform.runLater(() -> mazeDisplayer.requestFocus());
    }

    /**
     * Shows only the next suggested move in the solution.
     */
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
        Platform.runLater(() -> mazeDisplayer.requestFocus());
    }

    /**
     * Removes the full solution display from the maze.
     */
    @FXML
    public void removeSolution() {
        if (viewModel.getMaze() == null) return;
        mazeDisplayer.clearSolution();
        nextStepVisible = false;
        highlightedPosition = null;
        Platform.runLater(() -> mazeDisplayer.requestFocus());
    }

    /**
     * Removes the highlighted next step image.
     */
    @FXML
    public void removeNextStep() {
        mazeDisplayer.removeNextStepImage();
        nextStepVisible = false;
        highlightedPosition = null;

        PauseTransition pause = new PauseTransition(Duration.millis(50));
        pause.setOnFinished(e -> mazeDisplayer.requestFocus());
        pause.play();
    }

    /**
     * Parses a string position format like "3,4" into a Position.
     */
    private Position parsePosition(String state) {
        String[] parts = state.replaceAll("[{}]", "").split(",");
        int row = Integer.parseInt(parts[0]);
        int col = Integer.parseInt(parts[1]);
        return new Position(row, col);
    }

    /**
     * Saves the current maze to a file using FileChooser.
     */
    public void handleSaveMaze(javafx.event.ActionEvent actionEvent) {
        if (viewModel.getMaze() == null) {
            showAlert("Save Failed", "Maze is not initialized.");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Maze");
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            viewModel.saveMaze(file);
        }
    }

    /**
     * Loads a maze from a selected file and redraws it.
     */
    public void handleLoadMaze(javafx.event.ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Maze");
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            viewModel.loadMaze(file);
            mazeDisplayer.drawMaze(viewModel.getMaze());
            mazeDisplayer.setPlayerPosition(viewModel.getPlayerRow(), viewModel.getPlayerCol());
            mazeDisplayer.setEndPoint(viewModel.getEndPointRow(), viewModel.getEndPointCol());
            Platform.runLater(() -> mazeDisplayer.requestFocus());
        }
    }

    /**
     * Opens a dialog to enter new maze dimensions and generates it.
     */
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
                showAlert("Invalid Input", "Please enter a valid format: 10x10");
            }
        });
    }

    /**
     * Displays current maze properties including size and algorithm used.
     */
    @FXML
    private void onPropertiesClicked() {
        String solverName = viewModel.getLastUsedSolverName();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Maze Properties");
        alert.setHeaderText("Current Maze Properties");
        alert.setContentText(
                "🧠 Algorithm used: " + solverName + "\n" +
                        "📐 Size: " + viewModel.getMaze().getRows() + " x " + viewModel.getMaze().getCols() + "\n" +
                        "🚩 Start: " + viewModel.getMaze().getStartPosition() + "\n" +
                        "🏁 Goal: " + viewModel.getMaze().getGoalPosition()
        );
        alert.showAndWait();
    }

    /**
     * Displays help dialog with movement keys and feature usage.
     */
    @FXML
    private void onHelpClicked() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Help");
        alert.setHeaderText("Game Help");
        alert.setContentText("Use W/A/S/D or Q/E/Z/C to move.\nUse buttons to show or clear solutions.");
        alert.showAndWait();
    }

    /**
     * Displays the about dialog with project information.
     */
    @FXML
    private void onAboutClicked() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Maze App");
        alert.setContentText("Maze App was created by Shay Smertenko and Alon Kamenetsky.\nVersion 1.0");
        alert.showAndWait();
    }

    /**
     * Quits the application.
     */
    @FXML
    private void onExitClicked() {
        System.exit(0);
    }

    /**
     * Displays a GIF animation followed by a win dialog.
     */
    private void showVictoryGIFThenDialog() {
        mazeDisplayer.setVisible(false);
        gameAudio.stop();
        winAudio.setCycleCount(MediaPlayer.INDEFINITE);
        winAudio.play();

        try (InputStream gifStream = getClass().getResourceAsStream("/victory.gif")) {
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

    /**
     * Shows a confirmation dialog after winning the maze.
     */
    private void showVictoryDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Victory!");
        alert.setHeaderText("You solved the maze!");
        alert.setContentText("Would you like to play again or exit?");
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

    /**
     * Utility method to show an alert with custom title and content.
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
