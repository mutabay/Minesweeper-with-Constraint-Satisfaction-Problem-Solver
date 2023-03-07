package ai_csp;


import org.chocosolver.solver.exception.ContradictionException;

import java.util.*;

// Minesweeper player agent
public class Agent {

    private static final int END_GAME_MARK = 15;

    // Temp Storage
    private Set<Location> markedBombs;
    private Set<Location> unmarkedBombs;
    private Set<Location> history;
    private Set<Location> pendingMoves;

    private Random generator;
    private LocationGrid grid;
    private ContextBoard board;

    private int width;
    private int height;
    private int bombs;
    private int initialBombCount;
    private int movesRemainingToWin;
    private boolean endgame;

    // Initialization with a random first move.
    public Agent(int width, int height, int bombs) {
        this.init(width, height, bombs);
        this.firstMove();
    }

    // Initialize properties
    private void init(int width, int height, int bombs) {
        this.markedBombs = new HashSet<>();
        this.unmarkedBombs = new HashSet<>();
        this.history = new HashSet<>();
        this.generator = new Random();
        this.pendingMoves = new HashSet<>();
        this.board = new ContextBoard(width, height);
        this.grid = new LocationGrid(width, height);
        this.width = width;
        this.height = height;
        this.bombs = bombs;
        this.initialBombCount = bombs;
        this.endgame = false;
        this.movesRemainingToWin = this.width * this.height - this.bombs;
    }

    // Adds the first move to the pending moves with avoiding corners
    private void firstMove() {
        this.pendingMoves.add(
                this.grid.getVariable(
                        1 + this.generator.nextInt(this.width - 2),
                        1 + this.generator.nextInt(this.height - 2)
                )
        );
    }

    public Location nextMove() {
        Location next = null;

        // Are there any moves bending?
        while (!this.pendingMoves.isEmpty()) {
            Location nextMove = nextPending();


            if (!this.history.contains(nextMove)) {
                next = nextMove;
                this.history.add(nextMove);
                break;
            }
        }
        // If not, search for one
        if (next == null) {
            findMove();
            next = nextPending();
            this.history.add(next);
        }

        this.movesRemainingToWin--;
        return next;
    }

    // Sending parameters back to the controller
    public void sendBackResult(Location position, int neighbour) {
        this.board.setNeighbour(
                position.getX(),
                position.getY(),
                neighbour,
                this.grid,
                this.pendingMoves,
                this.unmarkedBombs
        );
    }

    // Non-pending search moves
    private void findMove() {
        if (!search()) {
            if (this.movesRemainingToWin <= Agent.END_GAME_MARK) {
                this.endgame = true;
            }
            if (this.endgame) {
                if (endGameSearch()) {
                    return;
                }
            }
            guess();
        }
    }

    private boolean endGameSearch() {
        boolean found = false;
        Stack<Location> bombs = new Stack<>();

        FinalStageConstraint constraints = new FinalStageConstraint(this.board, this.initialBombCount, this.width, this.height, this.grid);
        try {
            CSPModel model = new CSPModel(constraints);
            for (Location pos : constraints.getVariables()) {
                if (model.hasBomb(pos)) bombs.add(pos);
                else if (model.hasNoBombs(pos)) {
                    found = true;
                    this.pendingMoves.add(pos);
                }
            }
        } catch (ContradictionException ex) {
            System.out.println("Model contradicted");
        }

        boolean searchAgain = !found && !bombs.isEmpty();
        while (!bombs.isEmpty()) {
            Location position = bombs.pop();
            this.board.manualSetBombAt(position.getX(), position.getY(), this.grid, this.pendingMoves, this.unmarkedBombs);
        }
        return searchAgain ? endGameSearch() : found;
    }

    // Returns true if a move was found
    private boolean search() {
        boolean found = false;
        // Any found bombs are set after the search
        Stack<Location> bombs = new Stack<>();
        ConstraintSets cSets = new ConstraintSets(this.board);
        for (Map.Entry<Set<ConstraintDetails>, Set<Location>> entry : cSets.getSets().entrySet()) {
            found = searchSet(entry, bombs);
        }
        boolean searchAgain = !found && !bombs.isEmpty();
        while (!bombs.isEmpty()) {
            Location position = bombs.pop();
            this.board.manualSetBombAt(position.getX(), position.getY(), this.grid, this.pendingMoves, this.unmarkedBombs);
        }
        // If only known bombs are found, we search again since some might result in a newly found 'known-no-bomb'
        return searchAgain ? search() : found;
    }

    // Searches for guarantee set
    private boolean searchSet(Map.Entry<Set<ConstraintDetails>, Set<Location>> entry, Stack<Location> bombs) {
        boolean found = false;
        try {
            CSPModel model = new CSPModel(entry.getKey(), entry.getValue());
            for (Location position : entry.getValue()) {
                if (model.hasNoBombs(position)) {
                    this.pendingMoves.add(position);
                    found = true;
                }
                else if (model.hasBomb(position)) bombs.add(position);
            }
        } catch (ContradictionException e) {
            System.out.println("Contradiction in model!");
        }
        return found;
    }

    //Adds the most likely non-bomb to the pending moves.
    private void guess() {
        if (!this.pendingMoves.isEmpty()) return;
        Map<Location, Double> probabilities = new HashMap<>();                  // Probability map for variables
        Set<Location> variables = new HashSet<>();                              // Collects all variables in all sets
        ConstraintSets cSets = new ConstraintSets(this.board);            // Constraint sets
        int bombsOutsideVariables = this.bombs - this.unmarkedBombsCounter();   // Bomb counter for squares outside variables

        // Get probability for each set of variables
        for (Map.Entry<Set<ConstraintDetails>, Set<Location>> entry : cSets.getSets().entrySet()) {
            try {
                Probability pModel = new Probability(entry.getKey(), entry.getValue(), variables);
                // Subtract the minimum amount of bombs a solution can have from the bomb counter for non-variables
                bombsOutsideVariables -= pModel.getProbabilities(probabilities);
            } catch (ContradictionException e) {
                System.out.println("Contradiction in model!");
            }
        }
        // Sort
        PriorityQueue<Map.Entry<Location, Double>> entryPriorityQueue = new PriorityQueue<>(Comparator.comparingDouble(Map.Entry::getValue));
        entryPriorityQueue.addAll(probabilities.entrySet());

        // All unknown non variables
        ArrayList<Location> unknownNonVariables = getUnknownNonVariables(variables);

        // Cases:
        // 1: No variables, we add a random from unknown
        // 2: No unknown, we add the least likely bomb from the probability map
        // 3: Neither empty, we add the least likely out of [least likely variable, random unknown non-variable]

        if (entryPriorityQueue.isEmpty()) {
            this.pendingMoves.add(unknownNonVariables.get(this.generator.nextInt(unknownNonVariables.size())));
        } else if (unknownNonVariables.isEmpty()) {
            this.pendingMoves.add(randomLowestProbability(entryPriorityQueue));
        } else {
            double probabilityOfUnknowns = (100.0 * bombsOutsideVariables) / unknownNonVariables.size();
            if (probabilityOfUnknowns < entryPriorityQueue.peek().getValue()) {
                this.pendingMoves.add(unknownNonVariables.get(this.generator.nextInt(unknownNonVariables.size())));
            } else {
                this.pendingMoves.add(randomLowestProbability(entryPriorityQueue));
            }
        }
    }

    private Location randomLowestProbability(PriorityQueue<Map.Entry<Location, Double>> sortedProbabilities) {
        ArrayList<Map.Entry<Location, Double>> lowProb = new ArrayList<>();
        lowProb.add(sortedProbabilities.poll());
        double prob = lowProb.get(0).getValue();
        while (!sortedProbabilities.isEmpty() && sortedProbabilities.peek().getValue() == prob) {
            lowProb.add(sortedProbabilities.poll());
        }
        return lowProb.get(this.generator.nextInt(lowProb.size())).getKey();
    }

    // Intermediate method that's called by controller.
    public Location markBomb() {
        Location returnValue = null;
        while (!this.unmarkedBombs.isEmpty()) {
            Location bomb = nextBomb();
            if (!this.markedBombs.contains(bomb)) {
                this.markedBombs.add(bomb);
                returnValue = bomb;
                this.bombs--;
                break;
            }
        }
        return returnValue;
    }

    // Returns unmarket bomb
    private Location nextBomb() {
        Iterator<Location> it = this.unmarkedBombs.iterator();
        Location bomb = it.next();
        it.remove();
        return bomb;
    }

    // Gives number of unmarked bombs
    private int unmarkedBombsCounter() {
        int counter = 0;
        for (Location position : this.unmarkedBombs) {
            if (!this.markedBombs.contains(position)) counter++;
        }
        return counter;
    }

    // Returns pending move
    private Location nextPending() {
        Iterator<Location> it = pendingMoves.iterator();
        Location pos = it.next();
        it.remove();
        return pos;
    }

    // Returns the the unknown non variables as a list
    public ArrayList<Location> getUnknownNonVariables(Set<Location> variables) {
        ArrayList<Location> unknownNonVars = new ArrayList<>();
        for (int i = 0; i < this.width; i++) {
            for (int j = 0; j < this.height; j++) {
                if (
                        this.board.getBoard()[i][j] == ContextBoard.UNKNOWN &&
                                !variables.contains(this.grid.getVariable(i, j)) &&
                                !this.unmarkedBombs.contains(this.grid.getVariable(i, j))
                ) {
                    unknownNonVars.add(this.grid.getVariable(i, j));
                }
            }
        }
        return unknownNonVars;
    }


}
