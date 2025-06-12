package Model;

import java.beans.PropertyChangeListener;

public interface IModel {
    void start();
    void stop();
    void doAction(String input);
    void addListener(PropertyChangeListener listener);
}
