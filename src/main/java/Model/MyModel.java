package Model;

import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.MyMazeGenerator;
import algorithms.mazeGenerators.Position;
import algorithms.search.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.io.*;
import java.util.Observable;
import java.util.Observer;

/**
 * Model implementation for maze generation, navigation, solving, and state management.
 * Implements the IModel interface and uses Observer pattern to notify ViewModel.
 */
public class MyModel extends Observable implements IModel {

    private Maze maze;
    private Position playerPosition;
    private Position endPoint;
    private MyMazeGenerator myMazeGenerator;
    private Solution solution;
    private int playerRow;
    private int playerCol;
    private boolean showVictorySequence = false;
    private int stepCount = 0;
    private long startTime = 0;
    private String lastUsedSolver = "None";
    private static final Logger logger = LogManager.getLogger(MyModel.class);

    /**
     * Constructor initializes the maze generator.
     */
    public MyModel() {
        myMazeGenerator = new MyMazeGenerator();
    }

    /**
     * Generates a new maze with specified dimensions and initializes player state.
     * @param rows number of rows
     * @param cols number of columns
     */
    public void generateMaze(int rows, int cols) {
        logger.info("Generating maze with size {}x{}", rows, cols);
        maze = myMazeGenerator.generate(rows, cols);
        playerPosition = maze.getStartPosition();
        playerRow = playerPosition.getRowIndex();
        playerCol = playerPosition.getColumnIndex();
        stepCount = 0;
        startTime = System.currentTimeMillis();
        setChanged();
        notifyObservers("mazeGenerated");
        movePlayer(playerRow, playerCol);
    }

    /**
     * Updates the player's location based on the direction input.
     * @param direction movement direction (UP, DOWN, etc.)
     */
    public void updatePlayerLocation(MovementDirection direction) {
        int newRow = playerRow;
        int newCol = playerCol;

        switch (direction) {
            case UP -> newRow--;
            case DOWN -> newRow++;
            case LEFT -> newCol--;
            case RIGHT -> newCol++;
            case UP_LEFT -> { newRow--; newCol--; }
            case UP_RIGHT -> { newRow--; newCol++; }
            case DOWN_LEFT -> { newRow++; newCol--; }
            case DOWN_RIGHT -> { newRow++; newCol++; }
        }

        movePlayer(newRow, newCol);
    }

    /**
     * @return current player position object
     */
    public Position getPlayerPosition() {
        return playerPosition;
    }

    /**
     * Checks if the target cell is within bounds and walkable (value = 0).
     * @param row target row
     * @param col target column
     * @return true if cell is walkable
     */
    public boolean isWalkable(int row, int col) {
        if (row < 0 || row >= maze.getRows() || col < 0 || col >= maze.getCols())
            return false;
        return maze.getCell(row, col) == 0;
    }

    /**
     * Moves the player to a specified cell if it is valid and updates observers.
     * @param newRow target row
     * @param newCol target column
     */
    public void movePlayer(int newRow, int newCol) {
        if (isWalkable(newRow, newCol)) {
            logger.debug("Moving player to {}, {}", newRow, newCol);
            playerRow = newRow;
            playerCol = newCol;
            showVictorySequence = playerRow == maze.getGoalPosition().getRowIndex()
                    && playerCol == maze.getGoalPosition().getColumnIndex();
            stepCount++;
            setChanged();
            notifyObservers("playerMoved");
            if (showVictorySequence) {
                logger.info("Player reached the goal!");
            }
        } else {
            logger.warn("Attempted to move to invalid cell {}, {}", newRow, newCol);
        }
    }

    /**
     * Checks if the player is at the goal position.
     * @return true if player is at goal
     */
    public boolean isAtGoal() {
        return playerPosition.equals(maze.getGoalPosition());
    }

    /**
     * @return number of rows in the maze
     */
    public int getMazeRows() {
        return maze.getRows();
    }

    /**
     * @return number of columns in the maze
     */
    public int getMazeCols() {
        return maze.getCols();
    }

    /**
     * Returns the value of a specific cell in the maze.
     * @param row row index
     * @param col column index
     * @return 0 for path, 1 for wall
     */
    public int getCell(int row, int col) {
        return maze.getCell(row, col);
    }

    @Override
    public int getPlayerRow() {
        return playerRow;
    }

    @Override
    public int getPlayerCol() {
        return playerCol;
    }

    /**
     * Solves the current maze using a randomly selected search algorithm (BFS, DFS, BestFS).
     * Notifies observers with the solution.
     */
    @Override
    public void solveMaze() {
        if (maze == null) {
            logger.error("solveMaze() called but maze is null.");
            return;
        }

        Position current = new Position(playerRow, playerCol);
        Maze dynamicMaze = new Maze(maze.toByteArray());
        dynamicMaze.setStartPosition(current);
        ISearchable searchableMaze = new SearchableMaze(dynamicMaze);

        ISearchingAlgorithm[] solvers = {
                new BreadthFirstSearch(),
                new DepthFirstSearch(),
                new BestFirstSearch()
        };

        Random rand = new Random();
        int index = rand.nextInt(solvers.length);
        ISearchingAlgorithm solver = solvers[index];
        this.lastUsedSolver = solver.getClass().getSimpleName();

        logger.info("Solving maze using {}", lastUsedSolver);

        this.solution = solver.solve(searchableMaze);

        setChanged();
        notifyObservers("mazeSolved");
    }

    /**
     * Registers an observer for model state changes.
     * @param o the observer to be added
     */
    @Override
    public void assignObserver(Observer o) {
        this.addObserver(o);
    }

    /**
     * @return the computed solution of the maze
     */
    @Override
    public Solution getSolution() {
        return solution;
    }

    /**
     * @return the current maze
     */
    @Override
    public Maze getMaze() {
        return maze;
    }

    /**
     * @return the goal position in the maze
     */
    @Override
    public Position getEndPoint() {
        return endPoint;
    }

    /**
     * Saves the maze to a given file in byte array format.
     * @param file the destination file
     * @throws FileNotFoundException if the file cannot be written
     */
    public void saveMazeToFile(File file) throws FileNotFoundException {
        if (this.maze == null) {
            logger.error("Attempted to save null maze.");
            throw new IllegalStateException("Maze is null");
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            logger.info("Saving maze to file: {}", file.getName());
            byte[] data = maze.toByteArray();
            fos.write(data);
            fos.flush();
        } catch (IOException e) {
            logger.error("Error saving maze to file", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads a maze from a file and updates the player and goal positions.
     * @param file the source file
     * @throws FileNotFoundException if the file doesn't exist
     */
    @Override
    public void loadMazeFromFile(File file) throws FileNotFoundException {
        logger.info("Loading maze from file: {}", file.getName());
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] bytes = fis.readAllBytes();
            this.maze = new Maze(bytes);
            this.playerPosition = maze.getStartPosition();
            this.playerRow = playerPosition.getRowIndex();
            this.playerCol = playerPosition.getColumnIndex();
            this.endPoint = maze.getGoalPosition();
            setChanged();
            notifyObservers("maze loaded");
        } catch (IOException e) {
            logger.error("Error loading maze from file", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * @return true if the player has reached the goal
     */
    @Override
    public boolean shouldShowVictorySequence() {
        return showVictorySequence;
    }

    /**
     * @return number of steps taken by the player
     */
    @Override
    public int getStepCount() {
        return stepCount;
    }

    /**
     * @return elapsed time since maze generation, in seconds
     */
    @Override
    public long getElapsedTimeInSeconds() {
        return (System.currentTimeMillis() - startTime) / 1000;
    }

    /**
     * @return the name of the last algorithm used to solve the maze
     */
    @Override
    public String getLastUsedSolver() {
        return lastUsedSolver;
    }
}
