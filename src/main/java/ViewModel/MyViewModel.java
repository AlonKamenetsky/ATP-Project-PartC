package ViewModel;

import Model.IModel;
import Model.MovementDirection;
import algorithms.mazeGenerators.Maze;
import algorithms.search.Solution;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.input.KeyEvent;

import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;


public class MyViewModel extends Observable implements Observer {
    private IModel model;

    public MyViewModel(IModel model) {
        this.model = model;
        this.model.assignObserver(this); //Observe the Model for it's changes
    }

    @Override
    public void update(Observable o, Object arg) {
        setChanged();
        notifyObservers(arg);
    }

    public void movePlayer(KeyEvent keyEvent) {
        MovementDirection direction = null;

        switch (keyEvent.getCode()) {
            case UP -> direction = MovementDirection.UP;
            case DOWN -> direction = MovementDirection.DOWN;
            case LEFT -> direction = MovementDirection.LEFT;
            case RIGHT -> direction = MovementDirection.RIGHT;
            default -> {
                return;
            }
        }
        setChanged();
        notifyObservers("playerMoved");
        model.updatePlayerLocation(direction);


    }

    public void movePlayer(MovementDirection direction) {
        model.updatePlayerLocation(direction);
        setChanged();
        notifyObservers("playerMoved");
    }

    public int getPlayerRow() {
        return model.getPlayerRow();
    }

    public int getPlayerCol() {
        return model.getPlayerCol();
    }

    public Solution getSolution() {
        return model.getSolution();
    }

    public void generateMaze(int rows, int cols) {
        model.generateMaze(rows, cols);
    }

    public void solveMaze() {
        model.solveMaze();
    }

    public Maze getMaze() {
        return model.getMaze();
    }

    public int getEndPointRow() {
        return model.getEndPoint().getRowIndex();
    }

    public int getEndPointCol() {
        return model.getEndPoint().getColumnIndex();
    }

    public void saveMaze(File file) {
        try {
            model.saveMazeToFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadMaze(File file) {
        try {
            model.loadMazeFromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public boolean shouldShowVictorySequence() {
        return model.shouldShowVictorySequence();
    }
    public int getStepCount() {
        return model.getStepCount();
    }

    public long getElapsedTimeInSeconds() {
        return model.getElapsedTimeInSeconds();
    }
}
