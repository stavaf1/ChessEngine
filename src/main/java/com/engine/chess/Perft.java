package com.engine.chess;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class Perft{
    private Position position;
    private static int totalPositions;
    private static int positionCounter = 0;

    private static int captureCounter = 0;
    private static int enPassantCounter = 0;

    private static int castleCounter = 0;

    private static int promotionCounter = 0;

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
    private static MoveGenerator engine;
    public Perft(){
        engine = new MoveGenerator();
    }


    /**
     * similar to perft split function, subdivides total positions into the number of positions reachable from each move in the
     * initial position
     */
    private void divide(int depth, BitBoardPosition position){
        totalPositions = 0;
        String moves = engine.getMoves(position.getCastling(), position.getEnPassant(), position.getWhiteToMove(),position.getbR(), position.getbN(),position.getbB(),position.getbQ(),position.getbK(),position.getbP(),position.getwR(),position.getwN(),position.getwB(),position.getwK(),position.getwQ(),position.getwP());
        ArrayList<String> movearray = new ArrayList<>();
        for(int i = 0; i < moves.length(); i += 5){
            movearray.add(moves.substring(i, i+5));
        }

        for(String move: movearray){
            //this doesnt look like it works but it should
            BitBoardPosition holding = engine.makeMove(position.getWhiteToMove(), move, position.getCastling(), position.getbR(), position.getbN(), position.getbB(), position.getbQ(), position.getbK(), position.getbP(), position.getwR(), position.getwN(), position.getwB(), position.getwK(), position.getwQ(), position.getwP());
            generateTree(depth - 1, holding);
            System.out.println(moveToAlgebra(move) + ": " + positionCounter);
            totalPositions += positionCounter;
            positionCounter = 0;
        }
    }

    /**
     * counts all possible resulatant positions from a starting position
     * @param depth how deep to look for moves
     * @param position the initial position from which to generate moves
     */
    private void generateTree(int depth, BitBoardPosition position){

        //find all moves in a given position
        if(depth == 0){
            return;
        }
        String moves = engine.getMoves(position.getCastling(), position.getEnPassant(), position.getWhiteToMove(),position.getbR(), position.getbN(),position.getbB(),position.getbQ(),position.getbK(),position.getbP(),position.getwR(),position.getwN(),position.getwB(),position.getwK(),position.getwQ(),position.getwP());

        ArrayList<String> movearray = new ArrayList<>();
        for(int i = 0; i < moves.length(); i += 5){
            movearray.add(moves.substring(i, i+5));
        }

        ArrayList<BitBoardPosition> nextBitboards = new ArrayList<>();
        for(String move:movearray){
            BitBoardPosition holding = engine.makeMove(position.getWhiteToMove(), move, position.getCastling(), position.getbR(), position.getbN(), position.getbB(), position.getbQ(), position.getbK(), position.getbP(), position.getwR(), position.getwN(), position.getwB(), position.getwK(), position.getwQ(), position.getwP());
            nextBitboards.add(holding);
            if(depth == 1){
                positionCounter++;
                char movetype = move.charAt(2);
                if(move.charAt(2) == 'e') {enPassantCounter++; captureCounter++;}
                if(movetype == 'k' | movetype == 'b' | movetype == 'r' | movetype == 'q') promotionCounter++;
                if(movetype == 'x')captureCounter++;
                if(movetype == 'c' || movetype == 'l') castleCounter++;
            }
        }
        for(BitBoardPosition nextBoard:nextBitboards){
            generateTree(depth -1, nextBoard);
        }
    }

    /**
     *
     *
     */
    private String moveToAlgebra(String move){
        String algebra = "";
        algebra += fileNumberToAlgebra.get("" + move.charAt(1));
        algebra += 8 - Integer.parseInt("" + move.charAt(0));

        algebra += fileNumberToAlgebra.get("" + move.charAt(4));
        algebra += 8 - Integer.parseInt("" + move.charAt(3));
        return algebra;
    }

    public static void main(String[] args){
        Perft tester = new Perft();
        Position initPosition = new Position("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - ");
        BitBoardPosition newBitboards = new BitBoardPosition(initPosition.getbR(), initPosition.getbN(), initPosition.getbB(), initPosition.getbQ(), initPosition.getbK(), initPosition.getbP(), initPosition.getwR(), initPosition.getwN(), initPosition.getwB(), initPosition.getwK(), initPosition.getwQ(), initPosition.getwP(), initPosition.getWhiteToMove());

        long start = System.currentTimeMillis();
        tester.divide(5, newBitboards);
        System.out.println("took: " + (System.currentTimeMillis() - start) + "ms to generate:" );
        System.out.println("Total positions: " + totalPositions);
        System.out.println("Total promotions: " + promotionCounter);
        System.out.println("total captures: " + captureCounter);
        System.out.println("en Pasasnt: " + enPassantCounter);
        System.out.println("castles " + castleCounter);
    }
}
