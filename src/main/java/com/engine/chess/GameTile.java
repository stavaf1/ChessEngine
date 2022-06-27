package com.engine.chess;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

public class GameTile extends StackPane {
    private boolean hasInhabitant = false;
    private String tileId;
    private FlowPane parent;
    private PieceView photo;
    private Color color;
    private Rectangle rect;

    private char moveType = ' ';
    public GameTile(boolean isWhite, String id){
        this.tileId = id;
        this.parent = parent;
        //initialises the square behind the pieces
        color = isWhite ? Color.WHITE : Color.DARKGRAY;
        rect = new Rectangle(80, 80);
        rect.setFill(color);


        getChildren().add(rect);
        getChildren().add(new Label(idToAlgebra(tileId)));
    }

    private static HashMap<String, String> fileNumberToAlgebra = new HashMap<>(){{
        put("0", "a");
        put("1", "b");
        put("2", "c");
        put("3", "d");
        put("4", "e");
        put("5", "f");
        put("6", "g");
        put("7", "h");
    }};

    private String idToAlgebra(String tileId){
        String algebra = "";
        algebra += fileNumberToAlgebra.get("" + tileId.charAt(1));
        algebra += 8 - Integer.parseInt("" + tileId.charAt(0));

        return algebra;
    }

    public void setMoveType(char movetype){this.moveType = movetype;}

    public char getMoveType(){return moveType;}
    public void shade(){
        rect.setFill(Color.RED);
    }

    public void unShade(){
        rect.setFill(color);
    }
    public String getTileId() {return tileId;}

    public boolean hasInhabitant(){return hasInhabitant;}

    public PieceView getInhabitant(){return photo;}

    public void removeInhabitant(){hasInhabitant = false; getChildren().remove(photo);photo = null;}
    //old inhabitants get deleted, if previously at this location
    //not a problem takes will be handled better than this

    public void addInhabitant(PieceView photo){this.photo = photo;getChildren().add(photo); hasInhabitant = true;}

    public void addInhabitant(char pieceName)
    {
        if(pieceName == ' ') return;
        String imageName;
        if(Character.isUpperCase(pieceName)){imageName = "w" + pieceName;} else {imageName = "" + pieceName;}

        URL imageUrl = getClass().getResource("ChessPieces/" + imageName + ".png");
        Image image = new Image(imageUrl.toString());
        photo = new PieceView(image, tileId);

        addInhabitant(photo);
    }
}
