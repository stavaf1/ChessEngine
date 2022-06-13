package com.engine.chess;

import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.binding.ListBinding;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Array;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReferenceArray;

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

        //generating the section with the board, game tiles, pieces ect
        gameTile = new FlowPane();
        gameTile.setMinSize(800.0, 800.0);
        gameTile.setMaxSize(800.0, 800.0);

        boolean isWhite = false;
        for(int i = 63; i >= 0; i--){
            isWhite = !isWhite;
            int j = 63 - i;
            GameTile thisTile = new GameTile(isWhite,"" + (j/8) + (j%8));
            thisTile.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                GameTile currentTile = (GameTile)event.getSource();
                controller.clickHandler(currentTile.getTileId());

            });

            gameTile.getChildren().add(thisTile);

            if(i % 8 == 0) isWhite = !isWhite;

        }


        layout.setCenter(gameTile);
        Scene scene = new Scene(layout);

        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();

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
            tile.unShade();
        }
    }
    public String tileNoToId(Integer previousValue){
        return("" + previousValue/8 + previousValue % 8);
    }

    public static void main(String[] args) {
        launch();
    }
}