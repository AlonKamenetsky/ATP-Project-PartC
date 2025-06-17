package View;

import Model.MovementDirection;
import ViewModel.MyViewModel;
import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.Position;
import algorithms.search.Solution;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyEvent;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;
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

    @FXML
    private TextField textField_mazeRows;
    @FXML
    private TextField textField_mazeColumns;
    @FXML
    private MazeDisplayer mazeDisplayer;
    @FXML
    private Label playerRow;
    @FXML
    private Label playerCol;
    @FXML
    private TextField mazeRows;

    @FXML
    private TextField mazeColumns;
    StringProperty updatePlayerRow = new SimpleStringProperty();
    StringProperty updatePlayerCol = new SimpleStringProperty();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        playerRow.textProperty().bind(updatePlayerRow);
        playerCol.textProperty().bind(updatePlayerCol);

        mazeDisplayer.setFocusTraversable(true);
        mazeDisplayer.setOnKeyPressed(this::handleKeyPress);

        // Prevent arrow keys from moving the caret in text fields
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
    }


    public void generateMaze(ActionEvent actionEvent) {
        int rows = Integer.valueOf(textField_mazeRows.getText());
        int cols = Integer.valueOf(textField_mazeColumns.getText());

        viewModel.generateMaze(rows, cols);
    }

    public void handleKeyPress(KeyEvent event) {
        System.out.println("Pressed: " + event.getCode());
        switch (event.getCode()) {
            case W -> viewModel.movePlayer(MovementDirection.UP);
            case S -> viewModel.movePlayer(MovementDirection.DOWN);
            case A -> viewModel.movePlayer(MovementDirection.LEFT);
            case D -> viewModel.movePlayer(MovementDirection.RIGHT);
            case Q -> viewModel.movePlayer(MovementDirection.UP_LEFT);
            case E -> viewModel.movePlayer(MovementDirection.UP_RIGHT);
            case Z -> viewModel.movePlayer(MovementDirection.DOWN_LEFT);
            case C -> viewModel.movePlayer(MovementDirection.DOWN_RIGHT);
            default -> {
                return;
            }
        }

        mazeDisplayer.requestFocus(); // refocus canvas
        System.out.println("Key pressed: " + event.getCode());
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

        // Clear highlighted next step
        nextStepVisible = false;
        highlightedPosition = null;

        // Check win
        if (viewModel.getPlayerRow() == viewModel.getMaze().getGoalPosition().getRowIndex() &&
                viewModel.getPlayerCol() == viewModel.getMaze().getGoalPosition().getColumnIndex()) {

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Maze Completed");
            alert.setHeaderText(null);
            alert.setContentText("🎉 You reached the goal! Well done!");
            alert.showAndWait();
            mazeDisplayer.getGraphicsContext2D().clearRect(0, 0,
                    mazeDisplayer.getWidth(), mazeDisplayer.getHeight());
        }
    }



    private void mazeGenerated() {
        mazeDisplayer.drawMaze(viewModel.getMaze());
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
    private void startMaze() {
        try {
            int rows = Integer.parseInt(mazeRows.getText());
            int cols = Integer.parseInt(mazeColumns.getText());
            viewModel.generateMaze(rows, cols);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input for maze size.");
        }
    }
    // make sure this import exists

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

            // Clear focus from text fields
            mazeRows.getParent().requestFocus(); // parent = AnchorPane, it eats the focus

            // Now give focus to canvas
            Platform.runLater(() -> mazeDisplayer.requestFocus());

        } catch (NumberFormatException e) {
            System.out.println("Invalid input: rows and columns must be integers.");
        }
    }


    @FXML
    public void solveMaze() {
        viewModel.solveMaze();
        mazeDisplayer.setSolution(viewModel.getSolution());

        // Reset next step state if it was active
        nextStepVisible = false;
        highlightedPosition = null;

        Platform.runLater(() -> mazeDisplayer.requestFocus());
    }

    @FXML
    public void showNextStep() {
        // Always regenerate solution based on current player position
        viewModel.solveMaze();

        Solution sol = viewModel.getSolution();
        if (sol == null || sol.getSolutionPath().isEmpty()) return;

        int playerRow = viewModel.getPlayerRow();
        int playerCol = viewModel.getPlayerCol();

        // Find next step from current position
        for (int i = 0; i < sol.getSolutionPath().size() - 1; i++) {
            Position pos = parsePosition(sol.getSolutionPath().get(i).getState());
            if (pos.getRowIndex() == playerRow && pos.getColumnIndex() == playerCol) {
                Position next = parsePosition(sol.getSolutionPath().get(i + 1).getState());
                mazeDisplayer.highlightCell(next.getRowIndex(), next.getColumnIndex(), Color.YELLOW);
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
        // Assumes format: "{row,col}"
        String[] parts = state.replaceAll("[{}]", "").split(",");
        int row = Integer.parseInt(parts[0]);
        int col = Integer.parseInt(parts[1]);
        return new Position(row, col);
    }

    @FXML
    public void removeSolution() {
        if (viewModel.getMaze() == null)
            return;

        mazeDisplayer.clearSolution(); // this is the key part
        nextStepVisible = false;
        highlightedPosition = null;

        Platform.runLater(() -> {
            mazeDisplayer.setFocusTraversable(true);
            mazeDisplayer.requestFocus();
        });
    }



    @FXML
    public void removeNextStep() {
        // Remove focus from canvas temporarily
        mazeDisplayer.getParent().requestFocus();

        mazeDisplayer.drawMaze(viewModel.getMaze());
        mazeDisplayer.setPlayerPosition(viewModel.getPlayerRow(), viewModel.getPlayerCol());

        // Prevent NullPointerException from uninitialized end point
        if (viewModel.getMaze() != null && viewModel.getMaze().getGoalPosition() != null) {
            mazeDisplayer.setEndPoint(viewModel.getEndPointRow(), viewModel.getEndPointCol());
        }

        nextStepVisible = false;
        highlightedPosition = null;

        // Delay focus return to canvas
        PauseTransition pause = new PauseTransition(Duration.millis(50));
        pause.setOnFinished(e -> mazeDisplayer.requestFocus());
        pause.play();
    }










}