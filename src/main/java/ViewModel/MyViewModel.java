package ViewModel;

import Model.IModel;
import Model.MovementDirection;

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
        MovementDirection direction = switch (keyEvent.getCode()) {
            case UP -> MovementDirection.UP;
            case DOWN -> MovementDirection.DOWN;
            case LEFT -> MovementDirection.LEFT;
            case RIGHT -> MovementDirection.RIGHT;
            default -> null;
        };

        if (direction != null) {
            model.updatePlayerLocation(direction);
        }
    }



}

