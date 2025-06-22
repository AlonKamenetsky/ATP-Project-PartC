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





    public MyModel() {
        myMazeGenerator = new MyMazeGenerator();
    }


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



    public void updatePlayerLocation(MovementDirection direction) {
        int newRow = playerRow;
        int newCol = playerCol;

        switch (direction) {
            case UP -> newRow--;
            case DOWN -> newRow++;
            case LEFT -> newCol--;
            case RIGHT -> newCol++;
            case UP_LEFT -> {
                newRow--;
                newCol--;
            }
            case UP_RIGHT -> {
                newRow--;
                newCol++;
            }
            case DOWN_LEFT -> {
                newRow++;
                newCol--;
            }
            case DOWN_RIGHT -> {
                newRow++;
                newCol++;
            }
        }

        movePlayer(newRow, newCol);
    }


    public Position getPlayerPosition() {
        return playerPosition;
    }

    public boolean isWalkable(int row, int col) {
        if (row < 0 || row >= maze.getRows() || col < 0 || col >= maze.getCols())
            return false;
        return maze.getCell(row, col) == 0;
    }

    public void movePlayer(int newRow, int newCol) {
        if (isWalkable(newRow, newCol)) {
            logger.debug("Moving player to {}, {}", newRow, newCol);
            playerRow = newRow;
            playerCol = newCol;
            showVictorySequence = playerRow == maze.getGoalPosition().getRowIndex() &&
                    playerCol == maze.getGoalPosition().getColumnIndex();
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




    public boolean isAtGoal() {
        return playerPosition.equals(maze.getGoalPosition());
    }

    public int getMazeRows() {
        return maze.getRows();
    }

    public int getMazeCols() {
        return maze.getCols();
    }

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



    @Override
    public void assignObserver(Observer o) {
        this.addObserver(o);
    }

    @Override
    public Solution getSolution() {
        return solution;
    }

    @Override
    public Maze getMaze() {
        return maze;
    }

    @Override
    public Position getEndPoint() {
        return endPoint;
    }

    public void saveMazeToFile(File file) throws FileNotFoundException {
        if(this.maze == null){
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

    @Override
    public boolean shouldShowVictorySequence() {
        return showVictorySequence;
    }
    @Override
    public int getStepCount() {
        return stepCount;
    }

    @Override
    public long getElapsedTimeInSeconds() {
        return (System.currentTimeMillis() - startTime) / 1000;
    }


    @Override
    public String getLastUsedSolver() {
        return lastUsedSolver;
    }


}
