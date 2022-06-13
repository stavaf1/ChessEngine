package com.engine.chess;

import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class PieceView extends ImageView {
    private String startLoc;

    public PieceView(Image image, String id){
        super(image);
        startLoc = id;
    }
}
