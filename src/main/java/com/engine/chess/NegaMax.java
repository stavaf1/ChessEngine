package com.engine.chess;

import java.lang.reflect.Array;
import java.util.ArrayList;

import static com.engine.chess.Perft.moveToAlgebra;

public class NegaMax extends StaticEvaluator{
    private int positionSearched = 0;
    private final MoveGenerator engine = new MoveGenerator();


    public String entryPoint(BitBoardPosition position){
        String bestMove = "";
        int bestScore = -999999;
        ArrayList<String> moveArray = new ArrayList<>();
        String moveString = engine.getMoves(position.getCastling(), position.getEnPassant(), position.getWhiteToMove(),position.getbR(), position.getbN(),position.getbB(),position.getbQ(),position.getbK(),position.getbP(),position.getwR(),position.getwN(),position.getwB(),position.getwK(),position.getwQ(),position.getwP());

        for(int i = 0; i < moveString.length(); i += 5){
            moveArray.add(moveString.substring(i, i+5));
        }

        for(String move: moveArray){
            BitBoardPosition holding = (engine.makeMove(position.getWhiteToMove(), move, position.getCastling(), position.getbR(), position.getbN(), position.getbB(), position.getbQ(), position.getbK(), position.getbP(), position.getwR(), position.getwN(), position.getwB(), position.getwK(), position.getwQ(), position.getwP()));
            positionSearched = 0;
            int holdingScore = -negaMax(holding, 4);
            System.out.println(moveToAlgebra(move) + ": given score " + holdingScore);
            if(holdingScore > bestScore){
                bestScore = holdingScore;
                bestMove = move;
                System.out.println("new best move: " + move + "with score: " + bestScore);
            }
        }
        return bestMove;
    }

    public int negaMax(BitBoardPosition position, int depth){
        if(depth == 0){
            positionSearched++;
            return evaluate(position);
        }

        String moveString = engine.getMoves(position.getCastling(), position.getEnPassant(), position.getWhiteToMove(),position.getbR(), position.getbN(),position.getbB(),position.getbQ(),position.getbK(),position.getbP(),position.getwR(),position.getwN(),position.getwB(),position.getwK(),position.getwQ(),position.getwP());
        ArrayList<String> moveArray = new ArrayList<>();
        for(int i = 0; i < moveString.length(); i += 5){
            moveArray.add(moveString.substring(i, i+5));
        }
        int value = -9999999;
        for(String move: moveArray){
            BitBoardPosition holding = (engine.makeMove(position.getWhiteToMove(), move, position.getCastling(), position.getbR(), position.getbN(), position.getbB(), position.getbQ(), position.getbK(), position.getbP(), position.getwR(), position.getwN(), position.getwB(), position.getwK(), position.getwQ(), position.getwP()));
            value = Math.max(value, -negaMax(holding, depth -1));
        }

        return value;
    }


}
