package ai_csp;

import java.util.Set;

// A data structure for a single constraint.

public class ConstraintDetails {
    private final Set<Location> unknownNeighbours;
    private int neighbourBombs;

    // Constructs the constraint sum(unknownNeighbours) = adjacentBombs
    public ConstraintDetails(Set<Location> unknownNeighbours, int neighbourBombs) {
        this.unknownNeighbours = unknownNeighbours;
        this.neighbourBombs = neighbourBombs;
    }

    // Sum of variables
    public int getNeighbourBombs() {
        return this.neighbourBombs;
    }

    public void decrementNeighbourBombs() {this.neighbourBombs--;}

    public Set<Location> getUnknownNeighbours() {
        return this.unknownNeighbours;
    }

    public void removeVariable(Location position) {
        this.unknownNeighbours.remove(position);
    }

    // Check if sum(variables) = count(variables) which means all must be bombs.
    public boolean allBombs() {
        return this.neighbourBombs == unknownNeighbours.size();
    }

    // Checks sum(var) = 0, which means no bombs
    public boolean noBombs() {
        return this.neighbourBombs == 0;
    }

    // Checks constraint has variables
    public boolean isEmpty() {
        return this.unknownNeighbours.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        return this.unknownNeighbours.equals(((ConstraintDetails)o).unknownNeighbours);
    }

    @Override
    public int hashCode() {
        return this.unknownNeighbours.hashCode();
    }

    @Override
    public String toString() {
        return "[" + neighbourBombs + ", " + unknownNeighbours.toString() + "]";
    }
}
