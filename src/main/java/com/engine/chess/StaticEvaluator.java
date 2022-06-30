package com.engine.chess;

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

    public int evaluate(BitBoardPosition position){
        int material;
        int piecePlacement = 0;

        material = materialBalance(position);
        initPieceArrays(position);

        return material + piecePlacement;
    }

    public int materialBalance(BitBoardPosition position){
        int score = 0;
        initPieceArrays(position);
        for(int i = 0; i < 5; i++){
                score += pieceWeights[i] * (bitBoardToPieceCount(piecesToMoveList[i]) - bitBoardToPieceCount(piecesNotMovingList[i]));
        }
        return score;
    }

    public int bitBoardToPieceCount(long bitboard){
        int count = 0;
        for(int i =0; i < 64; i++){
            if(((bitboard >> i) & 1L) == 1L) count++;
        }
        return count;
    }

    public void initPieceArrays(BitBoardPosition position){
        boolean isWhite = position.getWhiteToMove();
        piecesToMoveList = new long[]{
                isWhite ? position.getwQ() : position.getbQ(),
                isWhite ? position.getwR() : position.getbR(),
                isWhite ? position.getwB() : position.getbB(),
                isWhite ? position.getwN() : position.getbN(),
                isWhite ? position.getwP() : position.getwP()
        };
        piecesNotMovingList = new long[]{
                !isWhite ? position.getwQ() : position.getbQ(),
                !isWhite ? position.getwR() : position.getbR(),
                !isWhite ? position.getwB() : position.getbB(),
                !isWhite ? position.getwN() : position.getbN(),
                !isWhite ? position.getwP() : position.getwP()
        };
    }
}
