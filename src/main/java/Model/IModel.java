package Model;

import algorithms.mazeGenerators.Position;

import java.beans.PropertyChangeListener;
import java.util.Observer;

public interface IModel {
    void generateMaze(int rows, int cols);
    int getPlayerRow();
    int getPlayerCol();
    void solveMaze();
    void updatePlayerLocation(MovementDirection direction);
    Position getPlayerPosition();
    int getMazeRows();
    int getMazeCols();
    int getCell(int row, int col);
    void movePlayer(int newRow, int newCol);
    void assignObserver(Observer o);
     Solution getSolution();
}
