package main;



import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

import javafx.scene.Scene;
import javafx.util.Duration;


public class Game {

    // Main Function for starting
    public static class Main extends Application {
        @Override
        public void start(Stage primaryStage) {
            Scene scene = new Scene(Controller.controller.getRoot());

            Controller.controller.setStage(primaryStage);
            primaryStage.setTitle("Minesweeper Game");
            scene.setCursor(Cursor.HAND);
            primaryStage.setScene(scene);
            primaryStage.setOpacity(0.97);
            primaryStage.show();
        }
        public static void main(String[] args) {
            launch(args);
        }
    }

    //region Enums
    // TODO: Add other player agents
    public enum Player {
        MANUAL, CSP_AGENT
    }

    // Size of the board
    public enum Size {
        SMALL, MEDIUM, LARGE
    }
    
    public enum GameState {
        GAME_STATE,  WON, LOST, MOMENT;

        @Override
        public String toString() {
            return switch (this) {
                case GAME_STATE -> "Status | ...";
                case WON -> "Status | Won!";
                case LOST -> "Status | Lost!";
                case MOMENT -> "Status | Playing...";
            };
        }
    }
    //endregion

    public static class TopMenu extends MenuBar {

        private final Menu size;
        private final Menu player;

        public TopMenu() {
            Menu game, settings;
            MenuItem newGame, exit;
            RadioMenuItem player, agent, small, medium , large;

            game = new Menu("Game");
            settings = new Menu("Settings");
            this.size = new Menu("Board Size");
            this.player = new Menu("Player");

            newGame = new MenuItem("New Game");
            newGame.setOnAction(event -> Controller.controller.newGame());

            exit = new MenuItem("Exit");
            exit.setOnAction(event -> Controller.controller.exit());

            agent = new RadioMenuItem("CSP Agent");
            agent.setOnAction(event -> Controller.controller.setPlayer(Player.CSP_AGENT));
            player = new RadioMenuItem("Manual Player");
            player.setOnAction(event -> Controller.controller.setPlayer(Player.MANUAL));


            small = new RadioMenuItem("Small");
            small.setOnAction(event -> Controller.controller.setSize(Size.SMALL));
            medium = new RadioMenuItem("Medium");
            medium.setOnAction(event -> Controller.controller.setSize(Size.MEDIUM));
            large = new RadioMenuItem("Large");
            large.setOnAction(event -> Controller.controller.setSize(Size.LARGE));

            ToggleGroup play = new ToggleGroup();
            agent.setToggleGroup(play);
            player.setToggleGroup(play);
            player.setSelected(true);

            ToggleGroup difficulty = new ToggleGroup();
            small.setToggleGroup(difficulty);
            small.setSelected(true);
            medium.setToggleGroup(difficulty);
            large.setToggleGroup(difficulty);

            this.size.getItems().addAll(small, medium, large);
            this.player.getItems().addAll(player, agent);

            settings.getItems().addAll(this.size, this.player);
            game.getItems().addAll(newGame, exit);
            this.getMenus().addAll(game, settings);

            exit.setOnAction((event) -> Controller.controller.exit());
        }

        public void setSettings(boolean enable) {
            this.player.setDisable(enable);
            this.size.setDisable(enable);
        }
    }

    public static class LabelFrame extends VBox {

        private final BombCounter bombsLeft;
        private final Button play;
        private final Label status;
        private final Label info;


        public LabelFrame() {
            this.bombsLeft = new BombCounter(10);
            this.play = new Button("Start");
            this.play.setOnAction(event -> Controller.controller.startPlay());
            this.status = new Label("Status");
            this.info = new Label(" `*` Mine | `F` Flag ");

            HBox upper = new HBox();
            upper.setSpacing(35);
            upper.setAlignment(Pos.BASELINE_CENTER);
            upper.getChildren().addAll(this.play, this.status);

            HBox middle = new HBox();
            upper.setSpacing(35);
            upper.setAlignment(Pos.BASELINE_CENTER);
            upper.getChildren().addAll(this.bombsLeft);

            HBox lower = new HBox();
            lower.setSpacing(35);
            lower.setAlignment(Pos.BASELINE_CENTER);
            lower.getChildren().addAll(this.info);

            this.setSpacing(10);
            this.getChildren().addAll(upper, lower);
        }


        // Getter and Setters
        public BombCounter getBombsLeft() {
            return this.bombsLeft;
        }

        public Button getPlay() {
            return this.play;
        }

        public void setStatus(GameState state) {
            this.status.setText(state.toString());
        }
    }

    // Single Cell Button
    public static class CellButton extends Button {

        private final int x;
        private final int y;
        private boolean clicked;

        // x,y: coordinates
        public CellButton(int x, int y) {
            this.x = x;
            this.y = y;
            this.clicked = false;

            // main button
            this.setOnAction(event -> Controller.controller.cellButtonActions(this, this.x, this.y));

            // secondary button
            this.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                if (event.getButton() == MouseButton.SECONDARY && !clicked) Controller.controller.markBomb(this);
            });
            this.setMaxWidth(35);
            this.setMinWidth(35);
            this.setMaxHeight(35);
            this.setMinHeight(35);
        }

        // true iff the button has already been clicked.
        public boolean isClicked() {
            return this.clicked;
        }

        // Sets the button as 'already clicked'.
        public void click() {
            this.clicked = true;
        }
    }

    public static class BoardCells extends GridPane {

        private final CellButton[][] cells;

        public BoardCells(Board board) {
            this.setAlignment(Pos.CENTER);
            this.cells = new CellButton[board.getWidth()][board.getHeight()];
            setPadding(new Insets(4));
            setStyle("-fx-background-color: black;");
            for (int i = 0; i < board.getWidth(); i++) {
                for (int j = 0; j < board.getHeight(); j++) {
                    CellButton button = new CellButton(i,j);
                    this.add(button, i, j);
                    this.cells[i][j] = button;
                }
            }
        }

        public CellButton get(int x, int y) {
            return this.cells[x][y];
        }
    }

    // Remained Bomb Label
    public static class BombCounter extends Label {
        private int amountLeft;

        public BombCounter(int amountLeft) {
            this.setAmountLeft(amountLeft);
        }

        public void setAmountLeft(int amountLeft) {
            this.amountLeft = amountLeft;
            this.changeLabels();
        }

        public void incrementBombsLeft() {
            this.amountLeft++;
            this.changeLabels();
        }

        public void decrementBombsLeft() {
            this.amountLeft--;
            this.changeLabels();
        }

        private void changeLabels() {
            this.setText(String.format("Bomb Count | %d", this.amountLeft));
        }
    }
    //endregion
}
