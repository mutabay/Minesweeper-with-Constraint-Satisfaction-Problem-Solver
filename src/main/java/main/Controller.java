package main;

import ai_csp.Agent;
import ai_csp.Location;
import javafx.application.Platform;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Controller {

    public static Controller controller = new Controller();

    private Game.Player player = Game.Player.MANUAL;
    private Game.Size size = Game.Size.SMALL;
    private Board board;
    private Game.GameState state = Game.GameState.GAME_STATE;
    private final RandomGenerator boardGenerator;
    private Game.BoardCells boardButtons;
    private final BorderPane root;
    private final Game.TopMenu menu;
    private final Game.LabelFrame labelContent;
    private Stage stage;
    private int clicksToWin;
    private Agent agent;


    private Controller() {
        this.boardGenerator = new RandomGenerator();
        this.board = null;
        this.boardButtons = null;
        this.root = new BorderPane();
        this.menu = new Game.TopMenu();
        this.labelContent = new Game.LabelFrame();
        this.root.setTop(this.menu);
        this.board = this.boardGenerator.create(8, 8, 10, false);
        this.clicksToWin = 8 * 8 - 10;
        this.boardButtons = new Game.BoardCells(this.board);
        this.root.setCenter(this.boardButtons);
        this.root.setBottom(this.labelContent);
        this.labelContent.getPlay().setDisable(true);
        this.agent = null;
    }

    public void exit() {
        Platform.exit();
    }

    public void newGame() {
        this.state = Game.GameState.GAME_STATE;
        switch (size) {
            case SMALL -> {
                this.board = this.boardGenerator.create(8, 8, 10, false);
                this.labelContent.getBombsLeft().setAmountLeft(10);
                this.clicksToWin = 8 * 8 - 10;
                if (this.player == Game.Player.CSP_AGENT) this.agent = new Agent(8, 8, 10);
            }
            case MEDIUM -> {
                this.board = this.boardGenerator.create(12, 12, 30, false);
                this.labelContent.getBombsLeft().setAmountLeft(30);
                this.clicksToWin = 12 * 12 - 30;
                if (this.player == Game.Player.CSP_AGENT) this.agent = new Agent(12, 12, 30);
            }
            case LARGE -> {
                this.board = this.boardGenerator.create(16, 16, 60, false);
                this.labelContent.getBombsLeft().setAmountLeft(60);
                this.clicksToWin = 16 * 16 - 60;
                if (this.player == Game.Player.CSP_AGENT) this.agent = new Agent(16, 16, 60);
            }
        }
        this.labelContent.setStatus(this.state);
        labelContent.getPlay().setDisable(this.player == Game.Player.MANUAL);
        this.boardButtons = new Game.BoardCells(this.board);
        this.root.setCenter(this.boardButtons);
    }

    public void setPlayer(Game.Player player) {
        this.player = player;
        newGame();
    }

    public void setSize(Game.Size size) {
        this.size = size;
        newGame();
        this.resizeStage();
    }

    // UI root
    public BorderPane getRoot() {
        return this.root;
    }

    // Resize for levels
    public void resizeStage() {
        this.stage.sizeToScene();
        this.stage.centerOnScreen();
        this.stage.setWidth(this.stage.getWidth() + 20);
        this.stage.setHeight(this.stage.getHeight() + 20);
    }

    public static void designButton(Game.CellButton cellButton, String text, Color COLOR)
    {
        cellButton.setText(text);
        cellButton.setBackground(new Background(new BackgroundFill(COLOR, null, null)));
    }

    // region Manual Player Controls
    // cell buttons for manual player
    public void cellButtonActions(Game.CellButton button, int x, int y) {
        if (this.player == Game.Player.CSP_AGENT || this.state == Game.GameState.LOST || this.state == Game.GameState.WON || button.isClicked() || button.getText().equals("F")) return;
        if (this.state == Game.GameState.GAME_STATE) {
            this.state = Game.GameState.MOMENT;
            this.labelContent.setStatus(this.state);
        }
        button.click();

        if (this.board.containsBomb(x,y)) {
            designButton(button, "*", Color.ORANGERED);
            this.state = Game.GameState.LOST;
            this.labelContent.setStatus(this.state);
        } else {
            this.clicksToWin--;
            int adj = this.board.neighbourBombsCount(x, y);
            if (adj == 0) {
                // Recursive auto update for 0-squares
                if (board.outOfBoard(x - 1, y - 1)) this.boardButtons.get(x - 1, y - 1).fire();
                if (board.outOfBoard(x, y - 1)) this.boardButtons.get(x, y - 1).fire();
                if (board.outOfBoard(x + 1, y - 1)) this.boardButtons.get(x + 1, y - 1).fire();
                if (board.outOfBoard(x - 1, y)) this.boardButtons.get(x - 1, y).fire();
                if (board.outOfBoard(x + 1, y)) this.boardButtons.get(x + 1, y).fire();
                if (board.outOfBoard(x - 1, y + 1)) this.boardButtons.get(x - 1, y + 1).fire();
                if (board.outOfBoard(x, y + 1)) this.boardButtons.get(x, y + 1).fire();
                if (board.outOfBoard(x + 1, y + 1)) this.boardButtons.get(x + 1, y + 1).fire();
            } else {
                button.setText(Integer.toString(adj));
            }
            button.setBackground(new Background(new BackgroundFill(Color.SLATEGRAY, null, null)));
            if (clicksToWin == 0) {
                this.state = Game.GameState.WON;
                this.labelContent.setStatus(this.state);
            }
        }
    }

    // Put flag for manual player
    public void markBomb(Game.CellButton button) {
        if (this.player == Game.Player.CSP_AGENT || this.state == Game.GameState.LOST || this.state == Game.GameState.WON) return;
        if (button.getText().equals("F")) {
            button.setText("");
            this.labelContent.getBombsLeft().incrementBombsLeft();
        } else {
            button.setText("F");
            designButton(button, "F", Color.INDIANRED);
            this.labelContent.getBombsLeft().decrementBombsLeft();
        }
    }
    //endregion

    //region Agent Player Controls
    // Run Solver
    public void startPlay() {
        if (this.player == Game.Player.MANUAL) return;
        newGame();
        this.state = Game.GameState.MOMENT;
        this.labelContent.setStatus(this.state);
        while (this.state != Game.GameState.LOST && this.state != Game.GameState.WON) {
            // If a bomb hasn't been marked already, the agent should flag all of them.
            Location bomb;
            while ((bomb = this.agent.markBomb()) != null) {
                designButton(this.boardButtons.get(bomb.getX(), bomb.getY()), "F", Color.INDIANRED );
                this.labelContent.getBombsLeft().decrementBombsLeft();
            }
            // Get next move from agent.
            Location pos = this.agent.nextMove();

            this.agent.sendBackResult(pos, agentButtonClick(pos.getX(), pos.getY()));
        }
        if (this.state == Game.GameState.WON) {
            Location bomb;
            while ((bomb = this.agent.markBomb()) != null) {
                this.boardButtons.get(bomb.getX(), bomb.getY()).setText("F");
                this.boardButtons.get(bomb.getX(), bomb.getY()).setBackground(new Background(new BackgroundFill(Color.INDIANRED, null, null)));
                this.labelContent.getBombsLeft().decrementBombsLeft();
            }
        }
    }

    private int agentButtonClick(int x, int y) {
        Game.CellButton button = this.boardButtons.get(x, y);
        button.click();
        if (this.board.containsBomb(x,y)) {
            // Lost, just return sentinel val
            designButton(button, "*", Color.ORANGERED);
            this.state = Game.GameState.LOST;
            this.labelContent.setStatus(this.state);
            return 0;
        } else {
            // If the square does not contain a bomb, we return the number that it contains.
            // This is the only communication with the agent.
            this.clicksToWin--;
            int adj = this.board.neighbourBombsCount(x, y);
            if (adj != 0) button.setText(Integer.toString(adj));
            button.setBackground(new Background(new BackgroundFill(Color.SLATEGRAY, null, null)));


            if (clicksToWin == 0) {
                this.state = Game.GameState.WON;
                this.labelContent.setStatus(this.state);
            }
            return adj;
        }
    }
    //endregion

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
