package ai_csp;

// Game ending

import java.util.HashSet;
import java.util.Set;

public class FinalStageConstraint {
    private final Set<ConstraintDetails> constraints;
    private final Set<Location> variables;
    private int bombsRemaining;

    public FinalStageConstraint(ContextBoard board, int totalBombs, int w, int h, LocationGrid grid) {
        this.constraints = new HashSet<>();
        this.variables = new HashSet<>();
        for (ConstraintDetails constraint : board.getConstraintLocations().values()) {
            this.constraints.add(constraint);
            this.variables.addAll(constraint.getUnknownNeighbours());
        }
        this.bombsRemaining = totalBombs;
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                if (board.getBoard()[i][j] == ContextBoard.BOMB_SENTINEL) {
                    this.bombsRemaining--;
                } else if (board.getBoard()[i][j] == ContextBoard.UNKNOWN) {
                    this.variables.add(grid.getVariable(i, j));
                }
            }
        }
    }

    public Set<Location> getVariables() {
        return this.variables;
    }

    public Set<ConstraintDetails> getConstraints() {
        return this.constraints;
    }

    public int getBombsRemaining() {
        return this.bombsRemaining;
    }
}
