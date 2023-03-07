package ai_csp;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// Sets up a Choco constraint model to find probability of variables.
public class Probability {
    private final Model model;
    private final Map<Location, IntVar> varMap;

    /*
     * Set up Choco model for the constraint group.
     *
     * detail constraint group
     * variables all variables in the constraint group
     * varCollection a collection to store any variable for outside use
     * @throws ContradictionException should never happen
     */
    public Probability(Set<ConstraintDetails> detail, Set<Location> variables, Set<Location> varCollection) throws ContradictionException {
        this.model = new Model();
        this.varMap = new HashMap<>();

        for (Location pos : variables) {
            this.varMap.put(pos, this.model.intVar(pos.toString(), 0, 1));
            varCollection.add(pos);
        }
        for (ConstraintDetails c : detail) {
            IntVar[] con = new IntVar[c.getUnknownNeighbours().size()];
            int index = 0;
            for (Location pos : c.getUnknownNeighbours()) {
                con[index] = this.varMap.get(pos);
                index++;
            }
            this.model.sum(con, "=", c.getNeighbourBombs()).post();
        }
        this.model.getSolver().propagate();
    }

    /*
     * Updates a map for probabilities for all variables in a constraint group.
     *
     * probabilityMap a map to update
     * @return minimum number of bombs for the constraint group
     */
    public int getProbabilities(Map<Location, Double> probabilityMap) {
        int minBombs = Integer.MAX_VALUE;

        // Initialize all probabilities as 0.0
        for (Location position : this.varMap.keySet()) probabilityMap.put(position, 0.0);

        // For a solution in the group of all solutions for the constraint group
        for (Solution solution : this.model.getSolver().findAllSolutions()) {
            int bombsSolution = 0;
            for (Location position : this.varMap.keySet()) {
                int value = solution.getIntVal(this.varMap.get(position));
                // If the position contains a bomb in this solution,
                // we add one to the probability map
                // The map works as a counter at this point.
                if (value == 1) {
                    probabilityMap.put(position, probabilityMap.get(position) + 1);
                    bombsSolution++;
                }
            }
            if (minBombs > bombsSolution) minBombs = bombsSolution;
        }
        // Convert counter to probabilities.
        long totalSolutions = this.model.getSolver().getSolutionCount();
        for (Location position : this.varMap.keySet()) {
            probabilityMap.put(position, 100.0 * probabilityMap.get(position) / totalSolutions);
        }
        return minBombs;
    }


}
