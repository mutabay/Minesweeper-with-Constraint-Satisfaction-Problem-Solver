package ai_csp;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CSPModel {

    private final Model model;
    private final Map<Location, IntVar> varMap;

    // initialize model with final stage constraint,sum of all unknowns = remaining bombs
    public CSPModel(FinalStageConstraint constraint) throws ContradictionException {
        this.model = new Model();
        this.varMap = new HashMap<>();

        // Map each variable to a Choco variable
        for (Location loc : constraint.getVariables()) this.varMap.put(loc, this.model.intVar(loc.toString(), 0, 1));

        // Create Choco constraints from our constraints
        for (ConstraintDetails c : constraint.getConstraints()) {
            IntVar[] con = new IntVar[c.getUnknownNeighbours().size()];
            int index = 0;
            for (Location loc : c.getUnknownNeighbours()) {
                con[index] = this.varMap.get(loc);
                index++;
            }
            this.model.sum(con, "=", c.getNeighbourBombs()).post();
        }

        IntVar[] con = new IntVar[constraint.getVariables().size()];
        int index = 0;
        for (Location loc : constraint.getVariables()) {
            con[index] = varMap.get(loc);
            index++;
        }
        this.model.sum(con, "=", constraint.getBombsRemaining());

        // constraint propagation
        this.model.getSolver().propagate();
    }

    // Creates a Choco model using the constraint in a given constraint group.
    public CSPModel(Set<ConstraintDetails> constraints, Set<Location> variables) throws ContradictionException {
        this.model = new Model();
        this.varMap = new HashMap<>();

        // Map each variable to a Choco variable
        for (Location loc : variables) this.varMap.put(loc, this.model.intVar(loc.toString(), 0, 1));

        // Create Choco constraints from our constraints
        for (ConstraintDetails c : constraints) {
            IntVar[] con = new IntVar[c.getUnknownNeighbours().size()];
            int index = 0;
            for (Location loc : c.getUnknownNeighbours()) {
                con[index] = this.varMap.get(loc);
                index++;
            }
            this.model.sum(con, "=", c.getNeighbourBombs()).post();
        }
        // constraint propagation
        this.model.getSolver().propagate();
    }

    // A location does not contain a bomb, check if it leads to a contradiction. If so, it must contain a bomb.
    public boolean hasBomb(Location location) {
        return containsContradiction(model.arithm(varMap.get(location), "=", 0));
    }

    // A location contains a bomb, check if it leads to a contradiction. If so, it must not contain a bomb.
    public boolean hasNoBombs(Location location) {
        return containsContradiction(model.arithm(varMap.get(location), "=", 1));
    }

    // Use of Choco solver to see if we can find a solution given an assumption.
    private boolean containsContradiction(Constraint assumption) {
        model.getEnvironment().worldPush();
        model.post(assumption);
        Solution sol = model.getSolver().findSolution();
        model.getEnvironment().worldPop();
        model.unpost(assumption);
        model.getSolver().hardReset();
        return sol == null;
    }
}
