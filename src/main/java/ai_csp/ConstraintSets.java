package ai_csp;
import java.util.*;

/*
*
* A collection of constraints' data structure.
* Every constraint in a set shares a certain
* variable with another constraint in that set.
* There aren't any shared characteristics between
* any two groupings. As a result, we may search
* one without relying on the variables of the other.
* With n boolean variables, we have 2n potential
* assignments (by brute force).
* However, if we divide them into 4 equal and
* independent sets, each group would have 2*(n/4)
* possible assignments, of which there are 4 in total,
* for a total of 4*2*(n/4), which is a significant improvement.
*
* */
public class ConstraintSets {
    // To make using the Choco framework simpler, a map to every variable in the set is used.
    private final Map<Set<ConstraintDetails>, Set<Location>> sets;

    // Init Constraints sets for knowledge
    public ConstraintSets(ContextBoard board) {
        this.sets = new HashMap<>();
        for (ConstraintDetails detail : board.getConstraintLocations().values()) add(detail);

    }

    public Map<Set<ConstraintDetails>, Set<Location>> getSets() {
        return sets;
    }

    // Returns true if agent's current knowledge do not result in any constraints.
    public boolean isEmpty() {
        return this.sets.isEmpty();
    }

    /*
     * Adds a new constraint to the sets of constraint set.
     * Any set that contains any of its variable is added to
     * a stack and passed to another method to merge with all
     * sets in the stack, into which we add the new constraint.
     * If no such set exist, we create a new set.
    */
    private void add(ConstraintDetails detail) {
        Stack<Set<ConstraintDetails>> toMerge = new Stack<>();
        for (Map.Entry<Set<ConstraintDetails>, Set<Location>> entry : sets.entrySet()) {
            for (Location variable : detail.getUnknownNeighbours()) {
                if (entry.getValue().contains(variable)) {
                    toMerge.push(entry.getKey());
                    break;
                }
            }
        }
        if (toMerge.isEmpty()) {
            Set<ConstraintDetails> newSet = new HashSet<>();
            newSet.add(detail);
            sets.put(newSet, detail.getUnknownNeighbours());
        } else {
            merge(toMerge, detail);
        }
    }

    /*
     * Removes all sets in the stack from the map, merges them, adds the
     * new constraint to them and adds the merged set into the map.
    */
    private void merge(Stack<Set<ConstraintDetails>> toMerge, ConstraintDetails detail) {
        Set<ConstraintDetails> keySet = new HashSet<>();
        Set<Location> valueSet = new HashSet<>();
        while (!toMerge.isEmpty()) {
            Set<ConstraintDetails> temp = toMerge.pop();
            valueSet.addAll(sets.get(temp));
            keySet.addAll(temp);
            sets.remove(temp);
        }
        keySet.add(detail);
        valueSet.addAll(detail.getUnknownNeighbours());
        sets.put(keySet, valueSet);
    }
}
