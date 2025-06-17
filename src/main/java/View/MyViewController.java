package View;

import ViewModel.MyViewModel;
import algorithms.mazeGenerators.Maze;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyEvent;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

public class MyViewController implements Initializable, Observer {
    public MyViewModel viewModel;

    public void setViewModel(MyViewModel viewModel) {
        this.viewModel = viewModel;
        this.viewModel.addObserver(this);
    }
    @FXML
    private TextField textField_mazeRows;
    @FXML
    private TextField textField_mazeColumns;
    @FXML
    private MazeDisplayer mazeDisplayer;
    @FXML
    private Label playerRow;
    @FXML
    private Label playerCol;
    @FXML
    private TextField mazeRows;

    @FXML
    private TextField mazeColumns;
    StringProperty updatePlayerRow = new SimpleStringProperty();
    StringProperty updatePlayerCol = new SimpleStringProperty();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        playerRow.textProperty().bind(updatePlayerRow);
        playerCol.textProperty().bind(updatePlayerCol);

        mazeDisplayer.setFocusTraversable(true);
        mazeDisplayer.setOnKeyPressed(this::handleKeyPress);
    }


    public void generateMaze(ActionEvent actionEvent) {
        int rows = Integer.valueOf(textField_mazeRows.getText());
        int cols = Integer.valueOf(textField_mazeColumns.getText());

        viewModel.generateMaze(rows, cols);
    }

    public void solveMaze(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText("Solving maze...");
        alert.show();
        viewModel.solveMaze();
    }


    public void handleKeyPress(KeyEvent event) {
        switch (event.getCode()) {
            case UP, DOWN, LEFT, RIGHT -> viewModel.movePlayer(event);
        }
        System.out.println("Key pressed: " + event.getCode());
    }


    @Override
    public void update(Observable o, Object arg) {
        String change = (String) arg;
        switch (change){
            case "mazeGenerated" -> mazeGenerated();
            case "playerMoved" -> playerMoved();
            case "mazeSolved" -> mazeSolved();
            default -> System.out.println("Not implemented change: " + change);
        }
    }

    private void mazeSolved() {
        mazeDisplayer.setSolution(viewModel.getSolution());
    }

    private void playerMoved() {
        setPlayerPosition(viewModel.getPlayerRow(), viewModel.getPlayerCol());
    }

    private void mazeGenerated() {
        mazeDisplayer.drawMaze(viewModel.getMaze());
    }
    public void setPlayerPosition(int row, int col){
        mazeDisplayer.setPlayerPosition(row, col);
        setUpdatePlayerRow(row);
        setUpdatePlayerCol(col);
    }
    public void setUpdatePlayerRow(int updatePlayerRow) {
        this.updatePlayerRow.set(updatePlayerRow + "");
    }
    public void setUpdatePlayerCol(int updatePlayerCol) {
        this.updatePlayerCol.set(updatePlayerCol + "");
    }
    @FXML
    private void startMaze() {
        try {
            int rows = Integer.parseInt(mazeRows.getText());
            int cols = Integer.parseInt(mazeColumns.getText());
            viewModel.generateMaze(rows, cols);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input for maze size.");
        }
    }
    @FXML
    public void onStartClicked() {
        try {
            int rows = Integer.parseInt(mazeRows.getText());
            int cols = Integer.parseInt(mazeColumns.getText());

            viewModel.generateMaze(rows, cols);
            Maze maze = viewModel.getMaze();

            mazeDisplayer.drawMaze(maze);
            mazeDisplayer.setPlayerPosition(0, 0);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input: rows and columns must be integers.");
        }
    }
}