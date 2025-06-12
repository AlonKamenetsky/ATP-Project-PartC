package Model;

import java.beans.PropertyChangeListener;

public interface IModel {
    void generateMaze(int rows, int cols);
    int getPlayerRow();
    int getPlayerCol();
    void solveMaze();

}
