package View;

/**
 * Interface for the View component in MVVM architecture.
 * The View is responsible for rendering the user interface and reacting to updates from the ViewModel.
 */
public interface IView {

    /**
     * Initializes the view.
     * Called during the application startup to set up UI elements and bindings.
     */
    void initialize();

    /**
     * Updates the view when notified of changes from the ViewModel.
     * Typically called in response to observer notifications.
     */
    void update();
}
