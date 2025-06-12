package Model;



import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.MyMazeGenerator;
import algorithms.mazeGenerators.Position;
import algorithms.search.Solution;

import java.beans.PropertyChangeListener;

public class MyModel implements IModel {
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
    }

    public Maze getMaze() {
        return maze;
    }

    public Position getPlayerPosition() {
        return playerPosition;
    }

    public boolean isWalkable(int row, int col) {
        if (row < 0 || row >= maze.getRows() || col < 0 || col >= maze.getCols())
            return false;
        return maze.getCell(row, col) == 0;
    }

    public void movePlayer(int dRow, int dCol) {
        int newRow = playerPosition.getRowIndex() + dRow;
        int newCol = playerPosition.getColumnIndex() + dCol;

        if (isWalkable(newRow, newCol)) {
            playerPosition = new Position(newRow, newCol);
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

    public Position getGoalPosition() {
        return maze.getGoalPosition();
    }

}
