package com.engine.chess;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Perft{
    private Position position;

    private static int positionCounter = 0;

    private static int captureCounter = 0;
    private static int enPassantCounter = 0;

    private static int castleCounter = 0;

    private static MoveGenerator engine;
    public Perft(){
        engine = new MoveGenerator();
    }

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
            positionCounter++;
            if(move.charAt(2) == 'x')captureCounter++;
            if(move.charAt(2) == 'e')enPassantCounter++;
            if(move.charAt(2) =='l' || move.charAt(2)=='c') castleCounter++;
            nextBitboards.add(holding);
        }
        for(BitBoardPosition nextBoard:nextBitboards){
            generateTree(depth -1, nextBoard);
        }
    }
    public static void main(String[] args){
        Perft tester = new Perft();
        Position initPosition = new Position("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1");
        initPosition.initBitboards();
        BitBoardPosition newBitboards = new BitBoardPosition(initPosition.getbR(), initPosition.getbN(), initPosition.getbB(), initPosition.getbQ(), initPosition.getbK(), initPosition.getbP(), initPosition.getwR(), initPosition.getwN(), initPosition.getwB(), initPosition.getwK(), initPosition.getwQ(), initPosition.getwP(), true);
        newBitboards.setCastling((byte)0b00000000);
        newBitboards.setEnPassant((byte) 0b00000000);
        long start = System.currentTimeMillis();
        tester.generateTree(2, newBitboards);
        System.out.println("took: " + (System.currentTimeMillis() - start) + "ms to generate:" );
        System.out.println("Total positions: " + positionCounter);
        System.out.println("total captures: " + captureCounter);
        System.out.println("en Pasasnt: " + enPassantCounter);
        System.out.println("castles " + castleCounter);
    }
}
