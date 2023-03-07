package ai_csp;


import java.util.*;

/*
* The board from the context of the agent.
*/
public class ContextBoard {
    public static final byte UNKNOWN = -1;  // Sentinel Value
    public static final byte BOMB_SENTINEL = 10;

    private final Map<Location, ConstraintDetails> constraintLocations;
    private final byte[][] board;

    private final Set<Location> containsBombSet;
    private final Set<Location> removeSet;

    // Initializes the board with all squares set as unknown.
    public ContextBoard(int width, int height) {
        this.containsBombSet = new HashSet<>();
        this.removeSet = new HashSet<>();
        this.constraintLocations = new HashMap<>();
        board = new byte[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                board[i][j] = UNKNOWN;
            }
        }
    }

    /*
     * Update the knowledge. Place bomb at (x,y).
     * This variable will be eliminated from any constraint containing
     * this location along with a reduction of one bomb.
     * A helper method is used to investigate additional
     * simplification after receiving the neighboring constraint.
     *
     * x coordinate
     * y coordinate
     * grid pre-allocated memory for all possible locations
     * moves the set of pending moves for the agent
     * bombs a set of location for the agent to mark on GUI
     */
    public void setBombAt(int x, int y, LocationGrid grid, Set<Location> moves, Set<Location> bombs) {
        board[x][y] = BOMB_SENTINEL;
        bombs.add(grid.getVariable(x,y));
        for (Location location : grid.getNeighbours(x,y)) {
            ConstraintDetails detail;
            if ((detail = constraintLocations.get(location)) != null) {
                detail.decrementNeighbourBombs();
                detail.removeVariable(grid.getVariable(x, y));
                storeSimplifications(detail, location, moves);
            }
        }
    }

    // Same as setBombAt but empties temp sets when it's done.
    public void manualSetBombAt(int x, int y, LocationGrid grid, Set<Location> moves, Set<Location> bombs) {
        setBombAt(x, y, grid, moves, bombs);
        emptyTempSets(grid, moves, bombs);
    }

    /*
     * Update knowledge. Coordinate (x,y) to have no bomb. Each neighbour is explored and each unknown is
     * made into a variable. The neighbour number is decreased for each neighbour bomb we already know
     * of. If there is a constraint in any of the neighbouring location, (x,y) is removed from them
     * as a variable and the constraint is sent to a helper function for further simplification. If
     * the newly formed constraint is trivial, we add them to pending moves if =0 or temp bomb set
     * if all bombs, otherwise a new constraint is added to our system.
     *
     * x coordinate
     * y coordinate
     * neighbour number of neighbour bombs in the actual board
     * grid pre-allocated location memory
     * moves set of pending moves
     * bombs set of bombs to mark in GUI
     */
    public void setNeighbour(int x, int y, int neighbour, LocationGrid grid, Set<Location> moves, Set<Location> bombs) {
        board[x][y] = (byte)neighbour;
        Set<Location> newVariables = new HashSet<>();
        for (Location location : grid.getNeighbours(x,y)) {
            if (board[location.getX()][location.getY()] == UNKNOWN) {
                newVariables.add(location);
            } else if (board[location.getX()][location.getY()] == BOMB_SENTINEL) {
                neighbour--;
            } else {
                ConstraintDetails detail;
                if ((detail = constraintLocations.get(location)) != null) {
                    detail.removeVariable(grid.getVariable(x, y));
                    storeSimplifications(detail, location, moves);
                }
            }
        }
        if (newVariables.size() == neighbour) {
            containsBombSet.addAll(newVariables);
        }
        else if (neighbour == 0) moves.addAll(newVariables);
        else this.constraintLocations.put(grid.getVariable(x, y), new ConstraintDetails(newVariables, neighbour));

        // Handle all temps sets
        emptyTempSets(grid, moves, bombs);
    }

    //  mapping from board location to the constraint it forms
    public Map<Location, ConstraintDetails> getConstraintLocations() {
        return this.constraintLocations;
    }

    // the board from the perspective of the agent
    public byte[][] getBoard() {
        return this.board;
    }

    /*
     * When we are done updating, we update our knowledge for all bombs we discovered. We do not
     * update the non-bombs since they will be updated when we pop them from the pending moves.
     *
     * grid allocated memory for locations
     * moves pending moves
     * bombs unmarked bombs
     */
    private void emptyTempSets(LocationGrid grid, Set<Location> moves, Set<Location> bombs) {
        while (!containsBombSet.isEmpty()) {
            Iterator<Location> it = containsBombSet.iterator();
            Location bomb = it.next();
            it.remove();
            if (this.board[bomb.getX()][bomb.getY()] != ContextBoard.BOMB_SENTINEL) {
                setBombAt(bomb.getX(), bomb.getY(), grid, moves, bombs);
            }
        }
        for (Location pos : this.removeSet) {
            this.constraintLocations.remove(pos);
        }
    }

    /*
     * Any constraints that have a variable removed are passed to this method. It check if it has become
     * trivial or empty. If no bombs, we add all it's variables to the set of pending moves. If all bombs,
     * we add all variables to a temporary bomb list which will be explored later. For all these 3 cases,
     * we add the location to a remove temp set, which we later use to remove the constraint.
     *
     * detail constraint
     * location coordinates
     * moves set of pending moves
     */
    private void storeSimplifications( ConstraintDetails detail, Location location, Set<Location> moves) {
        if (detail.isEmpty()) {
            this.removeSet.add(location);
        } else if (detail.noBombs()) {
            moves.addAll(detail.getUnknownNeighbours());
            this.removeSet.add(location);
        } else if (detail.allBombs()) {
            this.containsBombSet.addAll(detail.getUnknownNeighbours());
            this.removeSet.add(location);
        }
    }
}
