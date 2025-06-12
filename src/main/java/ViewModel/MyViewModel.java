package ViewModel;

import Model.MyModel;
import algorithms.mazeGenerators.Position;

public class MyViewModel {

    private final MyModel model;

    public MyViewModel() {
        model = new MyModel();
    }

    public void start() {
        model.start();
    }

    public void stop() {
        model.stop();
    }

    public void generateMaze(int rows, int cols) {
        model.generateMaze(rows, cols);
    }

    public int getMazeRows() {
        return model.getMazeRows();
    }

    public int getMazeCols() {
        return model.getMazeCols();
    }

    public int getCellValue(int row, int col) {
        return model.getCell(row, col);
    }

    public Position getPlayerPosition() {
        return model.getPlayerPosition();
    }

    public Position getGoalPosition() {
        return model.getGoalPosition();
    }

    public void movePlayer(int dRow, int dCol) {
        model.movePlayer(dRow, dCol);
    }

    public boolean isAtGoal() {
        return model.isAtGoal();
    }
}
