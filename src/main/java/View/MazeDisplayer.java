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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

public class MazeDisplayer extends Canvas {
    private Maze maze;
    private Solution solution;
    private int rows;
    private int cols;

    // player position:
    private int playerRow ;
    private int playerCol ;
    // wall and player images:
    StringProperty imageFileNameWall = new SimpleStringProperty();
    StringProperty imageFileNamePlayer = new SimpleStringProperty();
    // goal position
    private int goalRow;
    private int goalCol;


    public int getPlayerRow() {
        return playerRow;
    }

    public int getPlayerCol() {
        return playerCol;
    }

    private Position parsePosition(AState state) {
        String[] parts = state.getState().split(",");
        int row = Integer.parseInt(parts[0]);
        int col = Integer.parseInt(parts[1]);
        return new Position(row, col);
    }


    public void setPlayerPosition(int row, int col) {
        this.playerRow = row;
        this.playerCol = col;
        draw();
    }
    public void setEndPoint(int row, int col) {
        this.goalRow = row;
        this.goalCol = col;
        draw();
    }

    public void setSolution(Solution solution) {
        this.solution = solution;
        draw();
    }

    public String getImageFileNameWall() {
        return imageFileNameWall.get();
    }

    public String imageFileNameWallProperty() {
        return imageFileNameWall.get();
    }

    public void setImageFileNameWall(String imageFileNameWall) {
        this.imageFileNameWall.set(imageFileNameWall);
    }

    public String getImageFileNamePlayer() {
        return imageFileNamePlayer.get();
    }

    public String imageFileNamePlayerProperty() {
        return imageFileNamePlayer.get();
    }

    public void setImageFileNamePlayer(String imageFileNamePlayer) {
        this.imageFileNamePlayer.set(imageFileNamePlayer);
    }

    public void drawMaze(Maze maze) {
        this.maze = maze;
        this.rows = maze.getRows();
        this.cols = maze.getCols();
        draw();
    }


    private void draw() {
        if (rows > 0 && cols > 0) {
            double canvasHeight = getHeight();
            double canvasWidth = getWidth();

            double cellHeight = canvasHeight / rows;
            double cellWidth = canvasWidth / cols;

            GraphicsContext gc = getGraphicsContext2D();
            gc.clearRect(0, 0, canvasWidth, canvasHeight);

            drawMazeWalls(gc, cellHeight, cellWidth, rows, cols);
            if (solution != null)
                drawSolution(gc, cellHeight, cellWidth);
            drawPlayer(gc, cellHeight, cellWidth);
            drawEndPoint(gc, cellHeight, cellWidth);
        }
    }


    private void drawMazeWalls(GraphicsContext gc, double cellHeight, double cellWidth, int rows, int cols) {
        gc.setFill(Color.RED);

        Image wallImage = null;
        try {
          //  wallImage = new Image(new FileInputStream(getImageFileNameWall()));
            if (getImageFileNameWall() != null)
                wallImage = new Image(new FileInputStream(getImageFileNameWall()));

        } catch (FileNotFoundException e) {
            System.out.println("There is no wall image file");
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (maze.getCell(i, j) == 1) {
                    double x = j * cellWidth;
                    double y = i * cellHeight;
                    if (wallImage == null)
                        gc.fillRect(x, y, cellWidth, cellHeight);
                    else
                        gc.drawImage(wallImage, x, y, cellWidth, cellHeight);
                }
            }
        }
    }


    private void drawPlayer(GraphicsContext graphicsContext, double cellHeight, double cellWidth) {
        double x = getPlayerCol() * cellWidth;
        double y = getPlayerRow() * cellHeight;
        graphicsContext.setFill(Color.GREEN);

        Image playerImage = null;
        try {
          //  playerImage = new Image (new FileInputStream(getImageFileNamePlayer()));
            if (getImageFileNamePlayer() != null)
                playerImage = new Image(new FileInputStream(getImageFileNamePlayer()));

        } catch (FileNotFoundException e) {
            System.out.println("There is no player image file");
        }
        if(playerImage == null)
            graphicsContext.fillRect(x, y, cellWidth, cellHeight);
        else
            graphicsContext.drawImage(playerImage, x, y, cellWidth, cellHeight);
    }

    private void drawEndPoint(GraphicsContext graphicsContext, double cellHeight, double cellWidth) {
        double y = maze.getGoalPosition().getRowIndex() * cellHeight;
        double x = maze.getGoalPosition().getColumnIndex() * cellWidth;
        graphicsContext.setFill(Color.BLUE);

        Image endPointImage = null;
        try {
            //  playerImage = new Image (new FileInputStream(getImageFileNamePlayer()));
            if (getImageFileNamePlayer() != null)
                endPointImage = new Image(new FileInputStream(getImageFileNamePlayer()));

        } catch (FileNotFoundException e) {
            System.out.println("There is no player image file");
        }
        if(endPointImage == null)
            graphicsContext.fillRect(x, y, cellWidth, cellHeight);
        else
            graphicsContext.drawImage(endPointImage, x, y, cellWidth, cellHeight);

    }
    private void drawSolution(GraphicsContext gc, double cellHeight, double cellWidth) {
        if (solution == null || solution.getSolutionPath().size() < 2)
            return;

        gc.setStroke(Color.YELLOW);
        gc.setLineWidth(2);

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

        if (startIndex == -1 || startIndex == path.size() - 1) {

            return;
        }

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


}
