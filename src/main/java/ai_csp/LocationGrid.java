package ai_csp;

import java.util.ArrayList;
import java.util.stream.IntStream;

/*
 *
 * Allocation of all position for a given board. They
 * are then re-used for anything that uses positions.
 *
 */
public class LocationGrid {

    private final Location[][] board;

    // Constructs a 2d array of all possible locations.
    public LocationGrid(int width, int height) {
        // The IntStream.range() method is used to create a stream of integer values from 0 to width-1
        // (exclusive) and another stream from 0 to height-1 (exclusive).
        this.board = IntStream.range(0, width).mapToObj(x ->
                        IntStream.range(0, height).mapToObj(y ->
                                new Location(x, y)).toArray(Location[]::new)).
                toArray(Location[][]::new);
    }

    // Getter for allocated position, given coordinates.
    public Location getVariable(int x, int y) {
        return this.board[x][y];
    }

    // Returns a list of all neighbours for a given location.
    public ArrayList<Location> getNeighbours(int x, int y) {
        ArrayList<Location> returnValue = new ArrayList<>();
        for (int i = x - 1; i < x + 2; i++) {
            for (int j = y - 1; j < y + 2; j++) {
                if ((i != x || j != y) && j >= 0 && i >= 0 && i < this.board.length && j < this.board[0].length) {
                    returnValue.add(this.board[i][j]);
                }
            }
        }
        return returnValue;
    }

}
