package ViewModel;

import Model.IModel;
import Model.MovementDirection;
import algorithms.mazeGenerators.Maze;
import algorithms.search.Solution;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyEvent;

import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/**
 * ViewModel class for the MVVM architecture.
 * Acts as an intermediary between the View and the Model.
 * Listens to changes from the Model and notifies the View.
 */
public class MyViewModel extends Observable implements Observer {

    private final IModel model;

    /**
     * Constructor initializes the ViewModel with a given model and sets up observation.
     * @param model the model to observe
     */
    public MyViewModel(IModel model) {
        this.model = model;
        this.model.assignObserver(this); // Observe model updates
    }

    /**
     * Called when the Model changes. Forwards the change to View observers.
     * @param o   the observable object
     * @param arg argument indicating what changed
     */
    @Override
    public void update(Observable o, Object arg) {
        setChanged();
        notifyObservers(arg);
    }

    /**
     * Handles key presses and converts them into movement commands.
     * Supports arrow key navigation.
     * @param keyEvent the key event from the user
     */
    public void movePlayer(KeyEvent keyEvent) {
        MovementDirection direction = null;

        switch (keyEvent.getCode()) {
            case UP -> direction = MovementDirection.UP;
            case DOWN -> direction = MovementDirection.DOWN;
            case LEFT -> direction = MovementDirection.LEFT;
            case RIGHT -> direction = MovementDirection.RIGHT;
            default -> {
                return; // Ignore other keys
            }
        }

        setChanged();
        notifyObservers("playerMoved");
        model.updatePlayerLocation(direction);
    }

    /**
     * Moves the player using the given direction enum.
     * @param direction movement direction
     */
    public void movePlayer(MovementDirection direction) {
        model.updatePlayerLocation(direction);
        setChanged();
        notifyObservers("playerMoved");
    }

    /**
     * @return current player row index
     */
    public int getPlayerRow() {
        return model.getPlayerRow();
    }

    /**
     * @return current player column index
     */
    public int getPlayerCol() {
        return model.getPlayerCol();
    }

    /**
     * @return the solution path for the current maze, if available
     */
    public Solution getSolution() {
        return model.getSolution();
    }

    /**
     * Triggers maze generation via the model.
     * @param rows number of rows
     * @param cols number of columns
     */
    public void generateMaze(int rows, int cols) {
        model.generateMaze(rows, cols);
    }

    /**
     * Solves the current maze using the model’s algorithm.
     */
    public void solveMaze() {
        model.solveMaze();
    }

    /**
     * @return the current maze object
     */
    public Maze getMaze() {
        return model.getMaze();
    }

    /**
     * @return the goal row index of the maze
     */
    public int getEndPointRow() {
        return model.getEndPoint().getRowIndex();
    }

    /**
     * @return the goal column index of the maze
     */
    public int getEndPointCol() {
        return model.getEndPoint().getColumnIndex();
    }

    /**
     * Saves the maze to the specified file. Alerts the user if an error occurs.
     * @param file destination file
     */
    public void saveMaze(File file) {
        try {
            model.saveMazeToFile(file);
        } catch (IllegalStateException e) {
            showAlert("Save Failed", "Maze is not initialized. Please generate or load a maze first.");
        } catch (IOException e) {
            showAlert("Save Failed", "An error occurred while saving the maze.");
            e.printStackTrace();
        }
    }

    /**
     * Displays an error alert popup.
     * @param title   the title of the alert
     * @param content the message to display
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Loads a maze from a file. Errors are printed to the console.
     * @param file file to load the maze from
     */
    public void loadMaze(File file) {
        try {
            model.loadMazeFromFile(file);
        } catch (IOException e) {
            e.printStackTrace(); // Optionally, show alert like in saveMaze()
        }
    }

    /**
     * @return true if the player has reached the goal
     */
    public boolean shouldShowVictorySequence() {
        return model.shouldShowVictorySequence();
    }

    /**
     * @return number of steps the player has taken
     */
    public int getStepCount() {
        return model.getStepCount();
    }

    /**
     * @return time passed since maze generation, in seconds
     */
    public long getElapsedTimeInSeconds() {
        return model.getElapsedTimeInSeconds();
    }

    /**
     * @return the name of the last algorithm used to solve the maze
     */
    public String getLastUsedSolverName() {
        return model.getLastUsedSolver();
    }
}
