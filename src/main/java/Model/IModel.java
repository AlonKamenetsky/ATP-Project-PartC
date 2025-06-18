package Model;

import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.Position;
import algorithms.search.Solution;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Observer;

public interface IModel {
    public void generateMaze(int rows, int cols);

    public int getPlayerRow();

    public int getPlayerCol();

    public void solveMaze();

    public void updatePlayerLocation(Model.MovementDirection direction);

    public Position getPlayerPosition();

    public int getMazeRows();

    public int getMazeCols();

    public int getCell(int row, int col);

    public void movePlayer(int newRow, int newCol);

    public void assignObserver(Observer o);

    public Solution getSolution();

    public Maze getMaze();

    public Position getEndPoint();

    public void loadMazeFromFile(File file) throws FileNotFoundException;

    public void saveMazeToFile(File file) throws FileNotFoundException;


}
