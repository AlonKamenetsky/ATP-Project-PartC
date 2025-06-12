package Model;



import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.MyMazeGenerator;
import algorithms.mazeGenerators.Position;
import algorithms.search.Solution;

import java.beans.PropertyChangeListener;
import java.util.Observable;
import java.util.Observer;

public class MyModel extends Observable implements IModel {
    private Maze maze;
    private Position playerPosition;
    private MyMazeGenerator myMazeGenerator;
    private Solution solution;
    private int playerRow;
    private int playerCol;

    public MyModel() {
        myMazeGenerator = new MyMazeGenerator();
    }


    public void generateMaze(int rows, int cols) {
        maze = myMazeGenerator.generate(rows, cols);
        playerPosition = maze.getStartPosition();
        playerRow = playerPosition.getRowIndex();
        playerCol = playerPosition.getColumnIndex();
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
            playerPosition = new Position(newRow, newCol);
            playerRow = newRow;
            playerCol = newCol;
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
        solution = new Solution();

    }
    @Override
    public void assignObserver(Observer o) {
        this.addObserver(o);
    }

    @Override
    public Solution getSolution() {
        return solution;
    }
}
