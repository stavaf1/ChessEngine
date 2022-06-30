package com.engine.chess;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

public class ChessView extends Application {

    private FlowPane gameTile;
    private ChessController controller;

    public ChessView(){
    }

    @Override
    public void start(Stage stage) throws IOException {
        controller = new ChessController();
        controller.addView(this);

        BorderPane layout = new BorderPane();
        //control buttons. remove later
        VBox userControls = new VBox();
        userControls.setSpacing(15.0);
        layout.setRight(userControls);

        //set starting position as described in the position class
        Button initButton = new Button("initialise");
        initButton.setOnAction(controller::showBoard);
        userControls.getChildren().add(initButton);

        //board clear button
        Button clearButton = new Button("clear");
        clearButton.setOnAction(controller::wipeScreen);
        userControls.getChildren().add(clearButton);

        //fen Input
        TextField fenInputField = new TextField();
        Button enterFenInput = new Button("enter");
        enterFenInput.setOnAction(e ->{
            String fen = fenInputField.getText();
            controller.initFen(fen);
        });
        userControls.getChildren().add(fenInputField);
        userControls.getChildren().add(enterFenInput);

        //default position
        Button startPos = new Button("Standard start");
        startPos.setOnAction(e -> controller.initFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"));
        userControls.getChildren().add(startPos);



        //generating the section with the board, game tiles, pieces ect
        gameTile = new FlowPane();
        gameTile.setMinSize(640.0, 640.0);
        gameTile.setMaxSize(640.0, 640.0);

        boolean isWhite = false;
        for(int i = 63; i >= 0; i--){
            isWhite = !isWhite;
            int j = 63 - i;
            GameTile thisTile = new GameTile(isWhite,"" + (j/8) + (j%8));
            thisTile.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                GameTile currentTile = (GameTile)event.getSource();
                controller.clickHandler(currentTile.getTileId());
                new Thread(controller::computerMove).start();
            });
            gameTile.getChildren().add(thisTile);

            if(i % 8 == 0) isWhite = !isWhite;
        }

        layout.setCenter(gameTile);
        Scene scene = new Scene(layout);

        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
        setGameMode();

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> controller.refresh());
            }
        }, 500, 200);
    }

    public void setGameMode(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("select a Gamemode");
        alert.setContentText("");
        alert.setHeaderText("");
        alert.setGraphic(null);
        DialogPane options = alert.getDialogPane();


        VBox optionCategories = new VBox();

        HBox pvpOptions = new HBox();
        HBox whiteOrBlack = new HBox();

        pvpOptions.setMinSize(500, 25);
        whiteOrBlack.setMinSize(500, 25);

        //the first hBox, giving pvp or pve options
        pvpOptions.getChildren().add(new Label("Play a human or computer?   "));

        Button human = new Button("Human");
        human.setOnAction(e -> {
            controller.setPvP(e, true);
            whiteOrBlack.setDisable(true);
            whiteOrBlack.setOpacity(50.0);
        });

        Button computer = new Button("Computer");
        computer.setOnAction(e -> {
            controller.setPvP(e, false);
            whiteOrBlack.setDisable(false);
            whiteOrBlack.setOpacity(100.0);
        });

        pvpOptions.getChildren().addAll(human, computer);

        //adding buttons for playing as black or white
        whiteOrBlack.getChildren().add(new Label("Play as black or white?   "));

        Button white = new Button("White");
        white.setOnAction(e -> controller.setPlayAsWhite(e, true));

        Button black = new Button("Black");
        black.setOnAction(e -> controller.setPlayAsWhite(e, false));

        whiteOrBlack.getChildren().addAll(white, black);

        //putting everything together
        optionCategories.getChildren().addAll(pvpOptions, whiteOrBlack);
        options.getChildren().add(optionCategories);

        //player can only chose white or black if he is playing against computer
        //otherwise the logic is the same
        whiteOrBlack.setDisable(true);
        whiteOrBlack.setOpacity(50.0);

        alert.setTitle("Settings");
        alert.showAndWait();


    }

    public String getPromotionChar(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("What to Promote to");
        DialogPane buttonPanel = alert.getDialogPane();

        HBox promotionOptions = new HBox();
        promotionOptions.setMinSize(200.0, 30.0);
        promotionOptions.setSpacing(5);
        AtomicReference<String> promotionString = new AtomicReference<>();
        promotionString.set("q");

        Button knightButton = new Button("knight");
        knightButton.setMinWidth(80);
        knightButton.setOnAction(e -> {
            promotionString.set("k");

        });

        Button bishopButton = new Button("bishop");
        bishopButton.setMinWidth(80);
        bishopButton.setOnAction(e -> {
            promotionString.set("b");
        });
        Button rookButton = new Button("rook");
        rookButton.setMinWidth(80);
        rookButton.setOnAction(e -> {
            promotionString.set("r");
        });
        Button queenButton = new Button("queen");
        queenButton.setMinWidth(80);
        queenButton.setOnAction(e -> {
            promotionString.set("q");
        });

        promotionOptions.getChildren().add(knightButton);
        promotionOptions.getChildren().add(bishopButton);
        promotionOptions.getChildren().add(rookButton);
        promotionOptions.getChildren().add(queenButton);

        buttonPanel.getChildren().add(promotionOptions);
        alert.showAndWait();


        return promotionString.toString();
    }

    /**
     * Reads the position object as it is passed in from the controller class
     * called after every turn
     * @param position to be displayed
     */
    public void initialiseBoard(Position position){
        clearBoard();
        HashMap<String, GameTile> indexTileMap = getIndexTileMap();
        for(int i = 0; i < 64; i++){
            char piece = position.getPieceAt(i);
            indexTileMap.get(tileNoToId(i)).addInhabitant(piece);
        }
    }

    /**
     * creates a HashMap between the gametiles' Id's and the tile objects
     * @return HashMap tileID -> tileObject
     */
    public HashMap<String, GameTile> getIndexTileMap(){
        HashMap<String, GameTile> indexTileMap = new HashMap<>();
        for(Node child: gameTile.getChildren()){
            if(child instanceof GameTile) indexTileMap.put(((GameTile) child).getTileId(), (GameTile) child);
        }
        return indexTileMap;
    }

    /**
     * removes shading from all tiles in the flowpane
     * will be used on clicks that produce moves
     */
    public void unShadePrevious(){
        HashMap<String, GameTile> indexTileMap = getIndexTileMap();
        for(GameTile thisTile : indexTileMap.values()){
            thisTile.unShade();
        }
    }
    /**
     * instructs a tile to shade itself
     */
    public void shadeTile(String tile){
        HashMap<String, GameTile> indexTileMap = getIndexTileMap();
        indexTileMap.get(tile).shade();
    }


    /**
     * resets the board
     */
    public void clearBoard(){
        HashMap<String, GameTile> indexTileMap = getIndexTileMap();
        for(GameTile tile: indexTileMap.values()){
            tile.removeInhabitant();
        }
    }
    public String tileNoToId(Integer previousValue){
        return("" + previousValue/8 + previousValue % 8);
    }

    public static void main(String[] args) {
        launch();
    }
}