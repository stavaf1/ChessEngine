package com.engine.chess;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;

import java.util.*;

public class ChessController {

    private static final int SEARCH_DEPTH = 3;
    private boolean isPvP = false;

    private boolean playAsWhite = true;
    private static PseudoLegalMoveGenerator moveGenerator;
    private static Position position;

    private static NegaMax engine;
    private String startLoc = null;

    private String previousMove = "";

    private BitBoardPosition bitPosition = null;

    private final HashMap<String, Integer> moveArray = new HashMap<>();

    private int turn = 0;

    boolean humanTurnOver;
    private ChessView view;

    private static Thread engineThread;
    public ChessController(){
        position = new Position();
        moveGenerator = new PseudoLegalMoveGenerator();
        engine = new NegaMax();
//        Perft perft = new Perft(position);
        position.initBitboards();
        position.bitBoardToPosition();
        bitPosition = position.getPositionToBitBoardWrapper();
        promoLookup.put("q", 2);
        promoLookup.put("r", 8);
        promoLookup.put("b", 9);
        promoLookup.put("k", 10);
    }

    /**
     *method fetches list of legal moves from the movegenerator
     * displays all moves from a given location when that location is clicked
     */
    public void showLegalMoves(String fromLoc)
    {
        position.initBitboards();
        ArrayList<Integer> moves = moveGenerator.getPseudoMoves(position.getPositionToBitBoardWrapper());
        for(int move:moves){
            if(moveGenerator.isLegal(moveGenerator.makeMove(position.getPositionToBitBoardWrapper(), move))) moveArray.put(translator(move), move);
        }

        for(String move: moveArray.keySet()){
            if(startLoc.equals(move.substring(0, 2))){
                view.shadeTile((move.substring(2)));
            }
        }
    }

    public void gameOver(String winner){
        String alertString = "";
        if(winner.equals("w")) {alertString = "White Wins";}
        if(winner.equals("b")) {alertString = "Black Wins";}
        else if(winner.equals("s")) {alertString = "StaleMate!";}

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Game Over!");
        alert.setHeaderText(alertString);

        alert.showAndWait();
        wipeScreen(new ActionEvent());
        initFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
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
        if((((turn % 2 == 0) == playAsWhite) && (startLoc == null || startLoc.equals(""))) | (isPvP && (startLoc == null || startLoc.equals("")))) {
            //make sure the view agrees with the position class
            view.initialiseBoard(position);
            view.unShadePrevious();

            //remember where the user pressed, and show the legal moves from this location
            startLoc = wherepressed;
            showLegalMoves(wherepressed);
        }
        //if the first and second click from the user describe a from location and a
        //to location for a move, make that move
        if((((turn % 2 == 0) == playAsWhite) && (moveArray.get(startLoc + wherepressed) != null && (turn % 2 == 0) == playAsWhite)) | (isPvP & moveArray.get(startLoc + wherepressed) != null)){
            view.unShadePrevious();
            int move = moveArray.get(startLoc + wherepressed);
            //check if more input is needed, ie what to promote to
            int promotionvalue = (move >> 16) & 0b00000000000000000000000000001111;

            if(promotionvalue == 2 | promotionvalue == 8 | promotionvalue == 9 | promotionvalue == 10){
                String promoType = view.getPromotionChar();
                int promoId  = promoLookup.get(promoType);
                move &= 0b11111111000000001111111111111111;
                move |= (promoId << 16);
                nextPosition = moveGenerator.makeMove(position.getPositionToBitBoardWrapper(), move);
            }

            //generate bitboards and wrapper class describing position after the move has been made
            else {
                nextPosition = moveGenerator.makeMove(position.getPositionToBitBoardWrapper(), move);
            }
            //updates the position class with these bitboards
            position.bitBoardToPosition(nextPosition);
            bitPosition = nextPosition;

            view.initialiseBoard(position);
            startLoc = null;
            nextTurn();
        }
        //if the square pressed does not represent a legal move
        if((((turn % 2 == 0) == playAsWhite) && moveArray.get(startLoc + wherepressed) == null) | (isPvP && moveArray.get(startLoc + wherepressed) == null)){
            view.unShadePrevious();
            startLoc = wherepressed;
            showLegalMoves(wherepressed);
        }
    }



    public void computerMove(){
        while ((((turn % 2 == 0) == !playAsWhite) & !isPvP)) {
            System.out.println("codexec");
            turn++;
            position.bitBoardToPosition(bitPosition);

            Integer engineMove = engine.entryPoint(bitPosition, -999999, 9999999, SEARCH_DEPTH);
            if (engineMove == null) {Platform.runLater(() -> gameOver(null)); return;}
            bitPosition = moveGenerator.makeMove(bitPosition, engineMove);
            position.bitBoardToPosition(bitPosition);
        }
    }


    public void wipeScreen(ActionEvent e){
        position.clear();
        view.clearBoard();
        view.unShadePrevious();
        startLoc = null;
        turn = 0;
    }

    public void initFen(String fen){
        wipeScreen(new ActionEvent());
        position.initialise(fen);
        view.initialiseBoard(position);
        bitPosition = position.getPositionToBitBoardWrapper();
        turn = position.getWhiteToMove() ? 0 : 1;
    }

    public String translator(int move){
        String returnString = "";


        int from = Integer.reverse(move & 0b11111111000000000000000000000000);
        int to = move & 0b00000000000000000000000011111111;

        returnString += "" + from/8 + from % 8 + to/8 + to%8;
        return returnString;
    }



    public void setPlayAsWhite(ActionEvent e, boolean isWhite){playAsWhite = isWhite;}

    public void setPvP(ActionEvent e, boolean isPvP){this.isPvP = isPvP;}

    public void nextTurn(){turn++;}

    public void refresh(){view.initialiseBoard(position);}

    private static final HashMap<String, Integer> promoLookup = new HashMap<>();
}