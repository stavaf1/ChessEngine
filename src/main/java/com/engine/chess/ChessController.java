package com.engine.chess;

import javafx.application.Platform;
import javafx.event.ActionEvent;

import java.util.*;

public class ChessController {
    private boolean isPvP = false;

    private boolean playAsWhite = true;
    private static MoveGenerator moveGenerator;
    private static Position position;

    private static NegaMax engine;
    private String startLoc = null;

    private String previousMove = "";

    private BitBoardPosition bitPosition = null;

    private HashMap<String, String> moveArray;

    private int turn = 0;

    boolean humanTurnOver;
    private ChessView view;

    private static Thread engineThread;
    public ChessController(){
        position = new Position();
        moveGenerator = new MoveGenerator();
        engine = new NegaMax();
//        Perft perft = new Perft(position);
        position.initBitboards();
        position.bitBoardToPosition();
        bitPosition = position.getPositionToBitBoardWrapper();
    }

    /**
     *method fetches list of legal moves from the movegenerator
     * displays all moves from a given location when that location is clicked
     */
    public void showLegalMoves(String fromLoc)
    {
        position.initBitboards();
        String legalMoves = moveGenerator.getMoves(position.getCastlingRights(), position.getEnPassant(), position.getWhiteToMove(),position.getbR(), position.getbN(),position.getbB(),position.getbQ(),position.getbK(),position.getbP(),position.getwR(),position.getwN(),position.getwB(),position.getwK(),position.getwQ(),position.getwP());
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
        refresh();
        BitBoardPosition nextPosition = null;
        humanTurnOver = false;
        if((startLoc == null || startLoc.equals(""))) {
            //make sure the view agrees with the position class
            view.initialiseBoard(position);
            view.unShadePrevious();

            //remember where the user pressed, and show the legal moves from this location
            startLoc = wherepressed;
            showLegalMoves(wherepressed);
        }
        //if the first and second click from the user describe a from location and a
        //to location for a move, make that move
        if(moveArray.get(startLoc + wherepressed) != null){
            view.unShadePrevious();

            //check if more input is needed, ie what to promote to
            char promotionvalue = moveArray.get(startLoc + wherepressed).charAt(2);
            boolean isTakes = Character.isUpperCase(promotionvalue);

            if(promotionvalue == 'k' || promotionvalue == 'b' || promotionvalue == 'r' || promotionvalue == 'Q' || promotionvalue == 'K' || promotionvalue == 'B' || promotionvalue == 'R' || promotionvalue == 'q'){
                String moveType = isTakes ? view.getPromotionChar().toUpperCase() : view.getPromotionChar();
                nextPosition = moveGenerator.makeMove(position.getWhiteToMove(), (startLoc +moveType+wherepressed), position.getCastlingRights(), position.getbR(), position.getbN(),position.getbB(),position.getbQ(),position.getbK(),position.getbP(),position.getwR(),position.getwN(),position.getwB(),position.getwK(),position.getwQ(),position.getwP());

                System.out.println(moveType);
            }

            //generate bitboards and wrapper class describing position after the move has been made
            else {
                 nextPosition = moveGenerator.makeMove(position.getWhiteToMove(), moveArray.get(startLoc + wherepressed), position.getCastlingRights(), position.getbR(), position.getbN(), position.getbB(), position.getbQ(), position.getbK(), position.getbP(), position.getwR(), position.getwN(), position.getwB(), position.getwK(), position.getwQ(), position.getwP());
            }
            //updates the position class with these bitboards
            position.bitBoardToPosition(nextPosition);
            bitPosition = nextPosition;

            view.initialiseBoard(position);
            startLoc = null;
            nextTurn();


            /**
             * now that the player has made a move, so will the engine
             */
        }
        //if the square pressed does not represent a legal move
        if(moveArray.get(startLoc + wherepressed) == null){
            view.unShadePrevious();
            startLoc = wherepressed;
            showLegalMoves(wherepressed);
        }
    }



    public void computerMove(){
        while ((((turn % 2 == 0) == !playAsWhite) & !isPvP)) {
            turn++;
            position.bitBoardToPosition(bitPosition);

            String engineMove = engine.entryPoint(bitPosition);
            bitPosition = moveGenerator.makeMove(position.getWhiteToMove(), engineMove, position.getCastlingRights(), position.getbR(), position.getbN(), position.getbB(), position.getbQ(), position.getbK(), position.getbP(), position.getwR(), position.getwN(), position.getwB(), position.getwK(), position.getwQ(), position.getwP());

            position.bitBoardToPosition(bitPosition);
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
        bitPosition = position.getPositionToBitBoardWrapper();
    }



    public void setPlayAsWhite(ActionEvent e, boolean isWhite){playAsWhite = isWhite;}

    public void setPvP(ActionEvent e, boolean isPvP){this.isPvP = isPvP;}

    public void nextTurn(){turn++;}

    public void refresh(){view.initialiseBoard(position);}
}