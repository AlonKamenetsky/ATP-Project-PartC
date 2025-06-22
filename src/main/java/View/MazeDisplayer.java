package View;

import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.Position;
import algorithms.search.AState;
import algorithms.search.Solution;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

import java.util.List;

/**
 * A custom JavaFX Canvas responsible for visually rendering a maze,
 * including the player, walls, background, solution path, goal, and next step hint.
 */
public class MazeDisplayer extends Canvas {

    private Maze maze;
    private Solution solution;
    private Maze currentMaze;
    private int rows;
    private int cols;

    // Player position
    private int playerRow;
    private int playerCol;

    // Goal position
    private int goalRow;
    private int goalCol;

    // Next step hint (used for solution preview)
    private Position nextStepPosition;
    private Image nextStepImage;

    // Dynamic wall/player image paths
    StringProperty imageFileNameWall = new SimpleStringProperty();
    StringProperty imageFileNamePlayer = new SimpleStringProperty();

    /**
     * Constructor loads the "next step" hint image if available.
     */
    public MazeDisplayer() {
        try {
            nextStepImage = new Image(getClass().getResourceAsStream("/Go+Here.png"));
        } catch (Exception e) {
            System.out.println("Couldn't load next step image");
        }
    }

    public int getPlayerRow() {
        return playerRow;
    }

    public int getPlayerCol() {
        return playerCol;
    }

    /**
     * Parses a string-based AState to a Position object.
     * @param state the AState to parse
     * @return parsed Position
     */
    private Position parsePosition(AState state) {
        String[] parts = state.getState().split(",");
        int row = Integer.parseInt(parts[0]);
        int col = Integer.parseInt(parts[1]);
        return new Position(row, col);
    }

    /**
     * Sets the player's current position and redraws the maze.
     */
    public void setPlayerPosition(int row, int col) {
        this.playerRow = row;
        this.playerCol = col;
        draw();
    }

    /**
     * Sets the goal position and redraws the maze.
     */
    public void setEndPoint(int row, int col) {
        this.goalRow = row;
        this.goalCol = col;
        draw();
    }

    /**
     * Sets the maze solution path and triggers redraw.
     */
    public void setSolution(Solution solution) {
        this.solution = solution;
        draw();
    }

    /**
     * Sets the maze object and triggers drawing it.
     * @param maze the maze to draw
     */
    public void drawMaze(Maze maze) {
        this.currentMaze = maze;
        this.maze = maze;
        this.rows = maze.getRows();
        this.cols = maze.getCols();
        draw();
    }

    /**
     * Main draw method that renders the entire maze UI components.
     */
    private void draw() {
        if (rows > 0 && cols > 0) {
            double canvasHeight = getHeight();
            double canvasWidth = getWidth();
            double cellHeight = canvasHeight / rows;
            double cellWidth = canvasWidth / cols;

            GraphicsContext gc = getGraphicsContext2D();
            gc.clearRect(0, 0, canvasWidth, canvasHeight);

            drawMazeBackground(gc, cellHeight, cellWidth, rows, cols);
            drawMazeWalls(gc, cellHeight, cellWidth, rows, cols);
            if (solution != null)
                drawSolution(gc, cellHeight, cellWidth);
            drawPlayer(gc, cellHeight, cellWidth);
            drawEndPoint(gc, cellHeight, cellWidth);
        }
    }

    /**
     * Draws maze walls using either an image or fallback color.
     */
    private void drawMazeWalls(GraphicsContext gc, double cellHeight, double cellWidth, int rows, int cols) {
        Image wallImage = null;
        try {
            wallImage = new Image(getClass().getResourceAsStream("/grass.png"));
        } catch (Exception e) {
            System.out.println("Couldn't load wall image");
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (maze.getCell(i, j) == 1) {
                    double x = j * cellWidth;
                    double y = i * cellHeight;
                    if (wallImage == null)
                        gc.fillRect(x, y, cellWidth, cellHeight);
                    else {
                        gc.setFill(Color.BLACK);
                        gc.drawImage(wallImage, x, y, cellWidth, cellHeight);
                    }
                }
            }
        }

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(5.0);
        gc.strokeRect(0, 0, cols * cellWidth, rows * cellHeight);
    }

    /**
     * Draws the player's current position.
     */
    private void drawPlayer(GraphicsContext gc, double cellHeight, double cellWidth) {
        double x = getPlayerCol() * cellWidth;
        double y = getPlayerRow() * cellHeight;

        Image playerImage = null;
        try {
            playerImage = new Image(getClass().getResourceAsStream("/player.png"));
        } catch (Exception e) {
            System.out.println("There is no player image file");
        }

        if (playerImage == null) {
            gc.setFill(Color.GREEN);
            gc.fillRect(x, y, cellWidth, cellHeight);
        } else {
            gc.drawImage(playerImage, x, y, cellWidth, cellHeight);
        }
    }

    /**
     * Draws the goal (end point) position.
     */
    private void drawEndPoint(GraphicsContext gc, double cellHeight, double cellWidth) {
        double y = maze.getGoalPosition().getRowIndex() * cellHeight;
        double x = maze.getGoalPosition().getColumnIndex() * cellWidth;

        Image endPointImage = null;
        try {
            endPointImage = new Image(getClass().getResourceAsStream("/goal.png"));
        } catch (Exception e) {
            System.out.println("There is no goal image file");
        }

        if (endPointImage == null) {
            gc.setFill(Color.BLUE);
            gc.fillRect(x, y, cellWidth, cellHeight);
        } else {
            gc.drawImage(endPointImage, x, y, cellWidth, cellHeight);
        }
    }

    /**
     * Draws the solution path as a yellow line from the player to the goal.
     */
    private void drawSolution(GraphicsContext gc, double cellHeight, double cellWidth) {
        if (solution == null || solution.getSolutionPath().size() < 2)
            return;

        List<AState> path = solution.getSolutionPath();

        int playerRow = getPlayerRow();
        int playerCol = getPlayerCol();

        int startIndex = -1;
        for (int i = 0; i < path.size(); i++) {
            Position pos = parsePosition(path.get(i));
            if (pos.getRowIndex() == playerRow && pos.getColumnIndex() == playerCol) {
                startIndex = i;
                break;
            }
        }

        if (startIndex == -1 || startIndex >= path.size() - 1)
            return;

        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);
        gc.setLineWidth(5);
        gc.setStroke(new Color(1.0, 1.0, 0.0, 0.6));

        for (int i = startIndex; i < path.size() - 1; i++) {
            Position from = parsePosition(path.get(i));
            Position to = parsePosition(path.get(i + 1));

            double x1 = from.getColumnIndex() * cellWidth + cellWidth / 2;
            double y1 = from.getRowIndex() * cellHeight + cellHeight / 2;
            double x2 = to.getColumnIndex() * cellWidth + cellWidth / 2;
            double y2 = to.getRowIndex() * cellHeight + cellHeight / 2;

            gc.strokeLine(x1, y1, x2, y2);
        }
    }

    /**
     * Clears the displayed solution and redraws the maze without it.
     */
    public void clearSolution() {
        this.solution = null;
        drawMaze(currentMaze);
        setPlayerPosition(playerRow, playerCol);
        setEndPoint(goalRow, goalCol);
    }

    /**
     * Draws the background for each maze cell, using an image or fallback color.
     */
    private void drawMazeBackground(GraphicsContext gc, double cellHeight, double cellWidth, int rows, int cols) {
        Image backgroundImage = null;
        try {
            backgroundImage = new Image(getClass().getResourceAsStream("/background.png"));
        } catch (Exception e) {
            System.out.println("Couldn't load background image");
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double x = j * cellWidth;
                double y = i * cellHeight;
                if (backgroundImage != null)
                    gc.drawImage(backgroundImage, x, y, cellWidth, cellHeight);
                else {
                    gc.setFill(Color.WHITE); // fallback
                    gc.fillRect(x, y, cellWidth, cellHeight);
                }
            }
        }
    }

    /**
     * Redraws the entire canvas based on the current maze and state.
     */
    public void redraw() {
        if (maze == null)
            return;

        GraphicsContext gc = getGraphicsContext2D();
        double cellHeight = getHeight() / maze.getRows();
        double cellWidth = getWidth() / maze.getCols();

        gc.clearRect(0, 0, getWidth(), getHeight());

        drawMazeBackground(gc, cellHeight, cellWidth, maze.getRows(), maze.getCols());
        drawMazeWalls(gc, cellHeight, cellWidth, maze.getRows(), maze.getCols());

        if (nextStepPosition != null && nextStepImage != null) {
            double x = nextStepPosition.getColumnIndex() * cellWidth;
            double y = nextStepPosition.getRowIndex() * cellHeight;
            gc.drawImage(nextStepImage, x, y, cellWidth, cellHeight);
        }

        drawSolution(gc, cellHeight, cellWidth);
        drawPlayer(gc, cellHeight, cellWidth);
        drawEndPoint(gc, cellHeight, cellWidth);
    }

    /**
     * Displays the "next step" hint image at a specific cell.
     * @param row row index of the hint
     * @param col column index of the hint
     */
    public void showNextStepImage(int row, int col) {
        this.nextStepPosition = new Position(row, col);
        redraw();
    }

    /**
     * Removes the "next step" image hint from the display.
     */
    public void removeNextStepImage() {
        this.nextStepPosition = null;
        redraw();
    }
}
