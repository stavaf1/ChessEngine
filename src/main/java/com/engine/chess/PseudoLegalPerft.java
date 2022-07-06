package com.engine.chess;

import java.util.ArrayList;
import java.util.HashMap;

public class PseudoLegalPerft {
    private static PseudoLegalMoveGenerator engine = new PseudoLegalMoveGenerator();
    private static int positionCounter = 0;
    private static int totalPositions = 0;
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
    public void divide(BitBoardPosition position, int depth){
        totalPositions = 0;
        ArrayList<Integer> moves = engine.getPseudoMoves(position);
        HashMap<Integer, BitBoardPosition> nextPositions = new HashMap<>();
        for(Integer move: moves){
            BitBoardPosition nextPosition = engine.makeMove(position, move);
            if(engine.isLegal(nextPosition))nextPositions.put(move, nextPosition);
        }

        for(Integer move : nextPositions.keySet()){
            generateTree(nextPositions.get(move), depth -1);
            System.out.println(translator(move) + "  nodes:" + positionCounter);
            totalPositions += positionCounter;
            positionCounter = 0;
        }
        System.out.println("total: " + totalPositions);
    }

    public void generateTree(BitBoardPosition position, int depth)
    {
        if(depth == 0){
            positionCounter++;
            return;
        }
        ArrayList<Integer> moves = engine.getPseudoMoves(position);
        ArrayList<BitBoardPosition> nextPositions = new ArrayList<>();
        for(Integer move: moves){
            BitBoardPosition nextPosition = engine.makeMove(position, move);
            if(engine.isLegal(nextPosition))nextPositions.add(nextPosition);
        }
        for(BitBoardPosition nextPosition: nextPositions){
            generateTree(nextPosition, depth-1);
        }
    }

    public String translator(int move){
        String returnString = "";


        int from = Integer.reverse(move & 0b11111111000000000000000000000000);
        int to = move & 0b00000000000000000000000011111111;

        returnString += fileNumberToAlgebra.get("" + from%8);
        returnString += 8 - ((int)from/8);

        returnString += fileNumberToAlgebra.get("" + to%8);
        returnString += 8 - ((int)to/8);


        return returnString;
    }

    public int perftEntry(String fen, int depth)
    {
        Position myPosition = new Position(fen);
        BitBoardPosition entryPoint = myPosition.getPositionToBitBoardWrapper();

        divide(entryPoint, depth);
        return totalPositions;
    }

    public static void main(String[] args){
        long start = System.currentTimeMillis();
        PseudoLegalPerft perft = new PseudoLegalPerft();
        Position myPosition = new Position("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/P1N2Q1p/1PPBBPPP/R3K2R b KQkq - 0 1");
        BitBoardPosition entryPoint = myPosition.getPositionToBitBoardWrapper();

        perft.divide(entryPoint, 2);
        System.out.println("took: " + (System.currentTimeMillis() - start) + "ms");
    }
}
