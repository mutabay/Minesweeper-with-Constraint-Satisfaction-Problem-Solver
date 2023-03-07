package main;

// Represents the actual board and its bombs.
public class Board {
    private final boolean[][] board;
    private int bombCount;

    public Board(int width, int height) {
        this.bombCount = 0;
        this.board = new boolean[height][width];
    }

    public int getWidth() {
        return this.board[0].length;
    }

    public int getHeight() {
        return this.board.length;
    }

    public int getBombCount() {
        return this.bombCount;
    }

    public boolean containsBomb(int x, int y) {
        return board[y][x];
    }

    // true iff (x,y) is outside the board
    public boolean outOfBoard(int x, int y) {
        return x >= 0 && y >= 0 && x < this.getWidth() && y < this.getHeight();
    }

    public void addBomb(int x, int y) {
        if (!board[y][x]) bombCount++;
        board[y][x] = true;
    }

    public int neighbourBombsCount(int x, int y) {
        int counter = 0;
        for (int i = x-1; i < x+2; i++) {
            for (int j = y-1; j < y+2; j++) {
                if (outOfBoard(i, j) && containsBomb(i,j)) {
                    counter++;
                }
            }
        }
        return counter;
    }
}
