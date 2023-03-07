package main;

import java.util.Random;

public class RandomGenerator {
    private static final int SPLIT_BOUNDARY = 5;
    private final Random random;

    public RandomGenerator() {
        this.random = new Random();
    }

    // Creates a random board with given parameters. There are 4 different options,
    // set by the possible values of the boolean parameters.
    // noRingedBomb true : No bomb has just bombs around it.
    // equalDistributed true: Recursively, divide the board and produce separate pieces.
    public Board create(int width, int height, int bombs, boolean equalDistributed) {
        return (equalDistributed ?  createEqualDistributed(width, height, bombs) : create(width, height, bombs));
    }

    // Create without restriction
    private Board create(int width, int height, int bombs) {
        Board board = new Board(width, height);
        while (board.getBombCount() < bombs) {
            int x = this.random.nextInt(width);
            int y = this.random.nextInt(height);
            board.addBomb(x, y);
        }
        return board;
    }


    private Board createEqualDistributed(int width, int height, int bombs) {
        Board board = new Board(width, height);
        distributeEqualHelper(board, 0, width - 1, 0, height - 1, bombs, false);
        return board;
    }

    /* Recursive helper function that calls self with a new boundary value as a parameter.
         minX: Lower horizontal bound
         minX lower horizontal bound
         maxX upper horizontal bound
         minY lower vertical bound
         maxY upper vertical bound
    */
    private void distributeEqualHelper(Board board, int minX, int maxX, int minY, int maxY, int bombs, boolean noRingedBomb) {
        if ((maxX - minY > SPLIT_BOUNDARY) && (maxY - minY > SPLIT_BOUNDARY)) {
            // Split both horizontally and vertically

            int halfX = (maxX - minX) >> 1;
            int halfY = (maxY - minY) >> 1;
            int quartBombs = bombs >> 2;
            int bombReminder = bombs % 4; // reminder, if any, is spread to first, second and/or third

            // Each quadrant
            distributeEqualHelper(board, minX, minX + halfX, minY, minY + halfY,
                    bombReminder > 0 ? quartBombs + 1: quartBombs, noRingedBomb);
            distributeEqualHelper(board, minX, minX + halfX , minY + halfY + 1, maxY,
                    bombReminder > 1 ? quartBombs + 1 : quartBombs, noRingedBomb);
            distributeEqualHelper(board, minX + halfX + 1, maxX, minY, minY + halfY,
                    bombReminder > 2 ? quartBombs + 1 : quartBombs, noRingedBomb);
            distributeEqualHelper(board, minX + halfX + 1, maxX, minY + halfY + 1, maxY,
                    quartBombs, noRingedBomb);

        } else if (maxX - minX > SPLIT_BOUNDARY) {
            // Split horizontally

            int half = (maxX - minX) >> 1;
            int halfBombs = bombs >> 1;
            distributeEqualHelper(board, minX, minX + half, minY, maxY, bombs - halfBombs, noRingedBomb);
            distributeEqualHelper(board, minX + half + 1, maxX, minY, maxY, halfBombs, noRingedBomb);

        } else if (maxY - minY > SPLIT_BOUNDARY) {
            // Split vertically

            int half = (maxY - minY) >> 1;
            int halfBombs = bombs >> 1;
            distributeEqualHelper(board, minX, maxX, minY, minY + half, bombs - halfBombs, noRingedBomb);
            distributeEqualHelper(board, minX, maxX, minY + half + 1, maxY, halfBombs, noRingedBomb);

        } else {
            // Split neither horizontally and vertically

            int goalBombs = board.getBombCount() + bombs;
            if (noRingedBomb) {
                while (board.getBombCount() < goalBombs) {
                    int x = intervalRandom(minX, maxX);
                    int y = intervalRandom(minY, maxY);
                    if (ringed(board, x, y)) continue;
                    board.addBomb(x, y);
                }
            } else {
                while (board.getBombCount() < goalBombs) {
                    board.addBomb(intervalRandom(minX, maxX), intervalRandom(minY, maxY));
                }
            }
        }
    }
    
    // Returns false iff at least one of (x,y)'s neighbours is not a bomb
    private boolean ringed(Board board, int x, int y) {
        for (int i = x - 1; i < x + 2; i++) {
            for (int j = y - 1; j < y + 2; j++) {
                if (board.outOfBoard(i,j) && !board.containsBomb(i,j)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private int intervalRandom(int a, int b) {
        return this.random.nextInt(b-a+1)+a;
    }
}
