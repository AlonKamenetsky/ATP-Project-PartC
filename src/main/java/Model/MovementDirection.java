package Model;

/**
 * Enum representing possible movement directions in the maze.
 * Includes both cardinal (up, down, left, right) and diagonal directions.
 */
public enum MovementDirection {
    /**
     * Move up (row - 1, same column)
     */
    UP,

    /**
     * Move down (row + 1, same column)
     */
    DOWN,

    /**
     * Move left (same row, column - 1)
     */
    LEFT,

    /**
     * Move right (same row, column + 1)
     */
    RIGHT,

    /**
     * Move diagonally up-left (row - 1, column - 1)
     */
    UP_LEFT,

    /**
     * Move diagonally up-right (row - 1, column + 1)
     */
    UP_RIGHT,

    /**
     * Move diagonally down-left (row + 1, column - 1)
     */
    DOWN_LEFT,

    /**
     * Move diagonally down-right (row + 1, column + 1)
     */
    DOWN_RIGHT
}
