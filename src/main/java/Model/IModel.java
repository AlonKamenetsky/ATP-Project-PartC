package Model;

import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.Position;
import algorithms.search.Solution;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Observer;

/**
 * Interface for the Model layer in the MVVM architecture.
 * Handles the logic and data for maze generation, player movement, solution computation, and game state tracking.
 */
public interface IModel {

    /**
     * Generates a new maze with the given dimensions.
     * @param rows number of rows in the maze
     * @param cols number of columns in the maze
     */
    void generateMaze(int rows, int cols);

    /**
     * @return the current row index of the player
     */
    int getPlayerRow();

    /**
     * @return the current column index of the player
     */
    int getPlayerCol();

    /**
     * Solves the current maze using a predefined or random algorithm.
     */
    void solveMaze();

    /**
     * Updates the player's position based on the given movement direction.
     * @param direction the direction to move the player
     */
    void updatePlayerLocation(Model.MovementDirection direction);

    /**
     * @return the current position of the player
     */
    Position getPlayerPosition();

    /**
     * @return the number of rows in the maze
     */
    int getMazeRows();

    /**
     * @return the number of columns in the maze
     */
    int getMazeCols();

    /**
     * Returns the value at the specified cell in the maze.
     * @param row the row index
     * @param col the column index
     * @return cell value (0 = path, 1 = wall)
     */
    int getCell(int row, int col);

    /**
     * Moves the player to the specified cell directly.
     * Intended for manual movement or animation steps.
     * @param newRow the target row
     * @param newCol the target column
     */
    void movePlayer(int newRow, int newCol);

    /**
     * Assigns an observer to the model for update notifications.
     * @param o the observer to be assigned
     */
    void assignObserver(Observer o);

    /**
     * @return the solution path to the maze, if one has been computed
     */
    Solution getSolution();

    /**
     * @return the current maze object
     */
    Maze getMaze();

    /**
     * @return the target (end) position in the maze
     */
    Position getEndPoint();

    /**
     * Loads a maze from a given file.
     * @param file the file containing the maze data
     * @throws FileNotFoundException if the file does not exist
     */
    void loadMazeFromFile(File file) throws FileNotFoundException;

    /**
     * Saves the current maze to a specified file.
     * @param file the file to save the maze into
     * @throws FileNotFoundException if the file cannot be created or accessed
     */
    void saveMazeToFile(File file) throws FileNotFoundException;

    /**
     * @return true if the victory sequence (e.g. animation or dialog) should be shown
     */
    boolean shouldShowVictorySequence();

    /**
     * @return the number of steps the player has taken in the maze
     */
    int getStepCount();

    /**
     * @return the elapsed time since the start of the maze in seconds
     */
    long getElapsedTimeInSeconds();

    /**
     * @return the name of the last algorithm used to solve the maze
     */
    String getLastUsedSolver();
}
