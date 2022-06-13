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
        rect = new Rectangle(100, 100);
        rect.setFill(color);


        getChildren().add(rect);
        getChildren().add(new Label(tileId));
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

        File file = new File("/home/stav/University/PPA/myWork/Chess/src/main/resources/com/engine/chess/ChessPieces/" + pieceName + ".png");
        Image image = new Image(file.toURI().toString());
        photo = new PieceView(image, tileId);

        addInhabitant(photo);
    }
}
