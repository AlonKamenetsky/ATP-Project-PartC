package Model;

import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.MyMazeGenerator;
import algorithms.mazeGenerators.Position;
import algorithms.search.*;
import javafx.application.Platform;

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



    public MyModel() {
        myMazeGenerator = new MyMazeGenerator();
    }


    public void generateMaze(int rows, int cols) {
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
            playerRow = newRow;
            playerCol = newCol;
            showVictorySequence = playerRow == maze.getGoalPosition().getRowIndex() &&
                    playerCol == maze.getGoalPosition().getColumnIndex();
            stepCount++;
            setChanged();
            notifyObservers("playerMoved");
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
        if (maze == null)
            return;

        // Use current player position
        Position current = new Position(playerRow, playerCol);

        Maze dynamicMaze = new Maze(maze.toByteArray()); // Make a copy
        dynamicMaze.setStartPosition(current);

        ISearchable searchableMaze = new SearchableMaze(dynamicMaze);
        ISearchingAlgorithm solver = new BreadthFirstSearch();
        this.solution = solver.solve(searchableMaze);
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
            throw new IllegalStateException("Maze is null");
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] data = maze.toByteArray();
            fos.write(data);
            fos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void loadMazeFromFile(File file) throws FileNotFoundException {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] bytes = fis.readAllBytes();
            this.maze = new Maze(bytes);
            this.playerPosition = maze.getStartPosition();
            this.playerRow = playerPosition.getRowIndex();
            this.playerCol = playerPosition.getColumnIndex();
            this.endPoint = maze.getGoalPosition();
            notifyObservers("maze loaded");
        } catch (IOException e) {
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


}
