package com.engine.chess;

import java.util.Collections;

public class StaticEvaluator {

    /**
     * split pieces this way as negamax requires evaluation from the perspective of the
     * moving player
     */
    private static long[] piecesToMoveList;
    private static long[] piecesNotMovingList;

    private static final int[] pieceWeights = new int[]{
            2538,
            1276,
            825,
            781,
            126
    };


    public int evaluate(BitBoardPosition position) {
        int material;
        int piecePlacement = 0;

        material = materialBalance(position);
        piecePlacement = piecePlacement(position.getWhiteToMove());

        return material + piecePlacement;
    }

    public int materialBalance(BitBoardPosition position) {
        int score = 0;
        initPieceArrays(position);
        for (int i = 0; i < 5; i++) {
            score += pieceWeights[i] * (bitBoardToPieceCount(piecesToMoveList[i]) - bitBoardToPieceCount(piecesNotMovingList[i]));
        }
        return score;
    }

    public int bitBoardToPieceCount(long bitboard) {
        int count = 0;
        for (int i = 0; i < 64; i++) {
            if (((bitboard >> i) & 1L) == 1L) count++;
        }
        return count;
    }

    public void initPieceArrays(BitBoardPosition position) {
        boolean isWhite = position.getWhiteToMove();
        piecesToMoveList = new long[]{
                isWhite ? position.getwQ() : position.getbQ(),
                isWhite ? position.getwR() : position.getbR(),
                isWhite ? position.getwB() : position.getbB(),
                isWhite ? position.getwN() : position.getbN(),
                isWhite ? position.getwP() : position.getbP(),
                isWhite ? position.getwK() : position.getbK()
        };
        piecesNotMovingList = new long[]{
                !isWhite ? position.getwQ() : position.getbQ(),
                !isWhite ? position.getwR() : position.getbR(),
                !isWhite ? position.getwB() : position.getbB(),
                !isWhite ? position.getwN() : position.getbN(),
                !isWhite ? position.getwP() : position.getbP(),
                !isWhite ? position.getwK() : position.getbK()
        };
    }


    public int piecePlacement(boolean isWhite){
        int pieceLocations = 0;
        for(int i = 1; i < 6; i++){
            pieceLocations += (bitBoardPositionValue(piecesToMoveList[i], i, isWhite) - bitBoardPositionValue(piecesNotMovingList[i], i, !isWhite));
        }
        return pieceLocations;
    }

    public int bitBoardPositionValue(long bitboard, int pieceId, boolean isWhite)
    {
        int positionalScore = 0;
        int[] pieceAnalysed = new int[0];
        if(isWhite)switch (pieceId) {
            case 1 -> pieceAnalysed = wrook_pieceSquare;
            case 2 -> pieceAnalysed = wbishop_pieceSquare;
            case 3 -> pieceAnalysed = wknight_pieceSquare;
            case 4 -> pieceAnalysed = wpawn_pieceSquare;
            case 5 -> pieceAnalysed = wking_pieceSquare;
        }
        if(!isWhite)switch (pieceId) {
            case 1 -> pieceAnalysed = brook_pieceSquare;
            case 2 -> pieceAnalysed = bbishop_pieceSquare;
            case 3 -> pieceAnalysed = bknight_pieceSquare;
            case 4 -> pieceAnalysed = bpawn_pieceSquare;
            case 5 -> pieceAnalysed = bking_pieceSquare;
        }

        for(int i = 0; i < 64; i++){
            if(((bitboard >> i) & 1L) == 1L){
                positionalScore += pieceAnalysed[i];
            }
        }
        return positionalScore;
    }

    static final int[] wknight_pieceSquare = new int[]
            {
                    -5, 0, 0, 0, 0, 0, 0, -5,
                    -5, 0, 0, 10, 10, 0, 0, -5,
                    -5, 5, 20, 20, 20, 20, 5, -5,
                    -5, 10, 20, 30, 30, 20, 10, -5,
                    -5, 10, 20, 30, 30, 20, 10, -5,
                    -5, 5, 20, 10, 10, 20, 5, -5,
                    -5, 0, 0, 0, 0, 0, 0, -5,
                    -5, -10, 0, 0, 0, 0, -10, -5,
            };

    static final int[] bknight_pieceSquare = new int[]
            {
                    -5, -10, 0, 0, 0, 0, -10, -5,
                    -5, 0, 0, 0, 0, 0, 0, -5,
                    -5, 5, 20, 10, 10, 20, 5, -5,
                    -5, 10, 20, 30, 30, 20, 10, -5,
                    -5, 10, 20, 30, 30, 20, 10, -5,
                    -5, 5, 20, 20, 20, 20, 5, -5,
                    -5, 0, 0, 10, 10, 0, 0, -5,
                    -5, 0, 0, 0, 0, 0, 0, -5,

            };


    static final int[] wbishop_pieceSquare = new int[]
            {
                    0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 10, 10, 0, 0, 0,
                    0, 0, 10, 20, 20, 10, 0, 0,
                    0, 0, 10, 20, 20, 10, 0, 0,
                    0, 10, 0, 0, 0, 0, 10, 0,
                    0, 30, 0, 0, 0, 0, 30, 0,
                    0, 0, -10, 0, 0, -10, 0, 0,
            };
    static final int[] bbishop_pieceSquare = new int[]
            {
                    0, 0, -10, 0, 0, -10, 0, 0,
                    0, 30, 0, 0, 0, 0, 30, 0,
                    0, 10, 0, 0, 0, 0, 10, 0,
                    0, 0, 10, 20, 20, 10, 0, 0,
                    0, 0, 10, 20, 20, 10, 0, 0,
                    0, 0, 0, 10, 10, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0,
            };

    static final int[] wrook_pieceSquare = new int[]
            {
                    50,  50,  50,  50,  50,  50,  50,  50,
                    50,  50,  50,  50,  50,  50,  50,  50,
                    0,   0,  10,  20,  20,  10,   0,   0,
                    0,   0,  10,  20,  20,  10,   0,   0,
                    0,   0,  10,  20,  20,  10,   0,   0,
                    0,   0,  10,  20,  20,  10,   0,   0,
                    0,   0,  10,  20,  20,  10,   0,   0,
                    0,   0,   0,  20,  20,   0,   0,   0,
            };
    static final int[] brook_pieceSquare = new int[]
            {
                    0,   0,   0,  20,  20,   0,   0,   0,
                    0,   0,  10,  20,  20,  10,   0,   0,
                    0,   0,  10,  20,  20,  10,   0,   0,
                    0,   0,  10,  20,  20,  10,   0,   0,
                    0,   0,  10,  20,  20,  10,   0,   0,
                    0,   0,  10,  20,  20,  10,   0,   0,
                    50,  50,  50,  50,  50,  50,  50,  50,
                    50,  50,  50,  50,  50,  50,  50,  50,
            };


    static final int[] wpawn_pieceSquare = new int[]
            {
                    90,  90,  90,  90,  90,  90,  90,  90,
                    30,  30,  30,  40,  40,  30,  30,  30,
                    20,  20,  20,  30,  30,  30,  20,  20,
                    10,  10,  10,  20,  20,  10,  10,  10,
                    5,   5,  10,  20,  20,   5,   5,   5,
                    0,   0,   0,   5,   5,   0,   0,   0,
                    0,   0,   0, -10, -10,   0,   0,   0,
                    0,   0,   0,   0,   0,   0,   0,   0,
            };
    static final int[] bpawn_pieceSquare = new int[]
            {
                    0,   0,   0,   0,   0,   0,   0,   0,
                    0,   0,   0, -10, -10,   0,   0,   0,
                    0,   0,   0,   5,   5,   0,   0,   0,
                    5,   5,  10,  20,  20,   5,   5,   5,
                    10,  10,  10,  20,  20,  10,  10,  10,
                    20,  20,  20,  30,  30,  30,  20,  20,
                    30,  30,  30,  40,  40,  30,  30,  30,
                    90,  90,  90,  90,  90,  90,  90,  90,
            };

    static final int[] wking_pieceSquare = new int[]
            {
                    0,   0,   0,   0,   0,   0,   0,   0,
                    0,   0,   5,   5,   5,   5,   0,   0,
                    0,   5,   5,  10,  10,   5,   5,   0,
                    0,   5,  10,  20,  20,  10,   5,   0,
                    0,   5,  10,  20,  20,  10,   5,   0,
                    0,   0,   5,  10,  10,   5,   0,   0,
                    0,   5,   5,  -5,  -5,   0,   5,   0,
                    0,   0,   5,   0, -15,   0,  10,   0,

            };
    static final int[] bking_pieceSquare = new int[]
            {
                    0,   0,   5,   0, -15,   0,  10,   0,
                    0,   5,   5,  -5,  -5,   0,   5,   0,
                    0,   0,   5,  10,  10,   5,   0,   0,
                    0,   5,  10,  20,  20,  10,   5,   0,
                    0,   5,  10,  20,  20,  10,   5,   0,
                    0,   5,   5,  10,  10,   5,   5,   0,
                    0,   0,   5,   5,   5,   5,   0,   0,
                    0,   0,   0,   0,   0,   0,   0,   0,
            };

}
