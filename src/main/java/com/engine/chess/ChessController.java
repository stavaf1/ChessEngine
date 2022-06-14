package com.engine.chess;

import javafx.event.ActionEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class ChessController {

    private static MoveGenerator moveGenerator;
    private static Position position;
    private String startLoc = null;

    private String previousMove = "";

    private HashMap<String, String> moveArray;

    private int turn = 0;
    private ChessView view;
    public ChessController(){
        position = new Position();
        moveGenerator = new MoveGenerator();
        position.initBitboards();
        position.bitBoardToPosition();
    }

    /**
     *view related shenanigans, control ect.
     */
    public void showLegalMoves(String fromLoc)
    {
        position.initBitboards();
        String legalMoves = moveGenerator.getMoves(position.getCastlingRights(), position.getEnPassant(), (turn  % 2 == 0),position.getbR(), position.getbN(),position.getbB(),position.getbQ(),position.getbK(),position.getbP(),position.getwR(),position.getwN(),position.getwB(),position.getwK(),position.getwQ(),position.getwP());
        moveArray = new HashMap<>();
        for(int i = 0; i < legalMoves.length(); i += 5){
            moveArray.put((legalMoves.substring(i,i+2) + legalMoves.substring(i+3, i+5)),legalMoves.substring(i, i+5));
        }
        for(String move: moveArray.values()){
            if(startLoc.equals(move.substring(0, 2))){
                view.shadeTile((move.substring(3)));
            }
        }
    }

    public void addView(ChessView view){
        this.view = view;
    }

    public void showBoard(ActionEvent e){
        view.initialiseBoard(position);
    }
    public void clickHandler(String wherepressed){
        if(startLoc == null || startLoc.equals("")) {
            //make sure the view agrees with the position class
            view.initialiseBoard(position);
            view.unShadePrevious();

            //remember where the user pressed, and show the legal moves from this location
            startLoc = wherepressed;
            showLegalMoves(wherepressed);
        }
        if(moveArray.get(startLoc + wherepressed) != null){
            view.unShadePrevious();
            showLegalMoves(wherepressed);
            //check if more input is needed, ie what to promote to
            char promotionvalue = moveArray.get(startLoc + wherepressed).charAt(2);
            boolean isTakes = Character.isUpperCase(promotionvalue);
            BitBoardPosition nextPosition;
            if(promotionvalue == 'k' || promotionvalue == 'b' || promotionvalue == 'r' || promotionvalue == 'Q' || promotionvalue == 'K' || promotionvalue == 'B' || promotionvalue == 'R' || promotionvalue == 'q'){
                String moveType = isTakes ? view.getPromotionChar().toUpperCase() : view.getPromotionChar();
                nextPosition = moveGenerator.makeMove(turn % 2 == 0, (startLoc +moveType+wherepressed), position.getCastlingRights(), position.getbR(), position.getbN(),position.getbB(),position.getbQ(),position.getbK(),position.getbP(),position.getwR(),position.getwN(),position.getwB(),position.getwK(),position.getwQ(),position.getwP());

                System.out.println(moveType);
            }

            //generate bitboards and wrapper class describing position after the move has been made
            else {
                 nextPosition = moveGenerator.makeMove(turn % 2 == 0, moveArray.get(startLoc + wherepressed), position.getCastlingRights(), position.getbR(), position.getbN(), position.getbB(), position.getbQ(), position.getbK(), position.getbP(), position.getwR(), position.getwN(), position.getwB(), position.getwK(), position.getwQ(), position.getwP());
            }
                //updates the position class with these bitboards
            position.bitBoardToPosition(nextPosition);

            view.initialiseBoard(position);
            startLoc = null;
            turn++;
        }
        //if the square pressed does not represent a legal move
        if(moveArray.get(startLoc + wherepressed) == null){
            view.unShadePrevious();
            startLoc = wherepressed;
            showLegalMoves(wherepressed);
        }
    }


    public void wipeScreen(ActionEvent e){
        position.clear();
        view.clearBoard();
        turn = 0;
    }

    public void initFen(String fen){
        position.initialise(fen);
        view.initialiseBoard(position);
    }
}