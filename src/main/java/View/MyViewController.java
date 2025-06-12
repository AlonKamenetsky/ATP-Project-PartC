package View;

import ViewModel.MyViewModel;
import algorithms.mazeGenerators.Position;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

public class MyViewController {

    @FXML
    private Canvas mazeCanvas;

    private final int cellSize = 25; // adjust as needed
    private final MyViewModel viewModel = new MyViewModel();

    @FXML
    public void initialize() {
        mazeCanvas.setFocusTraversable(true); // allow key events
        mazeCanvas.setOnKeyPressed(this::handleKeyPress);
    }

    @FXML
    private void onGenerateMazeClicked() {
        viewModel.generateMaze(20, 20); // fixed size for now
        drawMaze();
    }

    private void handleKeyPress(KeyEvent event) {
        switch (event.getCode()) {
            case UP -> viewModel.movePlayer(-1, 0);
            case DOWN -> viewModel.movePlayer(1, 0);
            case LEFT -> viewModel.movePlayer(0, -1);
            case RIGHT -> viewModel.movePlayer(0, 1);
            default -> { return; }
        }

        drawMaze();

        if (viewModel.isAtGoal()) {
            System.out.println("🎉 Goal reached!");
        }
    }

    private void drawMaze() {
        GraphicsContext gc = mazeCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, mazeCanvas.getWidth(), mazeCanvas.getHeight());

        int rows = viewModel.getMazeRows();
        int cols = viewModel.getMazeCols();

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int val = viewModel.getCellValue(row, col);

                gc.setFill(val == 1 ? Color.BLACK : Color.WHITE);
                gc.fillRect(col * cellSize, row * cellSize, cellSize, cellSize);
            }
        }

        // Draw goal
        Position goal = viewModel.getGoalPosition();
        gc.setFill(Color.GREEN);
        gc.fillOval(goal.getColumnIndex() * cellSize, goal.getRowIndex() * cellSize, cellSize, cellSize);

        // Draw player
        Position player = viewModel.getPlayerPosition();
        gc.setFill(Color.BLUE);
        gc.fillOval(player.getColumnIndex() * cellSize, player.getRowIndex() * cellSize, cellSize, cellSize);
    }
}
