package com.engine.chess;

import java.util.Collections;

public class StaticEvaluator {

    /**
     * split pieces this way as negamax requires evaluation from the perspective of the
     * moving player
     */

    /**
     * define past pawns
     * king mobility
     * queening possibility
     */
    /**
     * endgame if 2 major pieces each side
     *              no queens
     */
    private static long[] piecesToMoveList;
    private static long[] piecesNotMovingList;

    private static long[] pastPawnMask;

    public static long evaluatorTimeTrial;

    public StaticEvaluator(){
        pastPawnMask = new long[8];
        pastPawnMask[0] = fileMask[0] | fileMask[1];
        for(int i = 1; i < 7; i++){
            pastPawnMask[i] = fileMask[i -1] | fileMask[i + 1] | fileMask[i];
        }
        pastPawnMask[7] = fileMask[7] | fileMask[6];
    }

    private static final int[] pieceWeights = new int[]{
            2538,
            1276,
            825,
            781,
            126
    };

    private static final int ENDGAME_MULTIPLIER = 100;


    public int evaluate(BitBoardPosition position) {
        //start by determining game phase
        //larger numbers imply closer to endgame
        int endGameValue =ENDGAME_MULTIPLIER - (ENDGAME_MULTIPLIER/(1 + ((bitBoardToPieceCount(position.getwR() | position.getwB() | position.getwN()
            |position.getbR() | position.getbB() | position.getbR()) + 2*bitBoardToPieceCount(position.getwQ() | position.getbQ())))));



        long startTime = System.currentTimeMillis();
        int material;
        int piecePlacement;
        int pawnStructure;

        pawnStructure = scorePawnStructure(position.getWhiteToMove(), position.getwP(), position.getbP(), endGameValue);
        material = materialBalance(position);
        piecePlacement = piecePlacement(position.getWhiteToMove());


        evaluatorTimeTrial += (System.currentTimeMillis()-startTime);
        return material + piecePlacement + pawnStructure;
    }

    /**
     * Calculates the how much material is left from the perspective of the moving player
     * @param position
     * @return moving players material - other players material
     */
    public int materialBalance(BitBoardPosition position) {
        int score = 0;
        initPieceArrays(position);
        for (int i = 0; i < 5; i++) {
            score += pieceWeights[i] * (bitBoardToPieceCount(piecesToMoveList[i]) - bitBoardToPieceCount(piecesNotMovingList[i]));
        }
        return score;
    }

    /**
     * counts how many ones there are in any given bitboard
     * @param bitboard
     * @return "cardinality" of the bitboard
     */

    public int bitBoardToPieceCount(long bitboard) {
        int count = 0;
        for (int i = 0; i < 64; i++) {
            if (((bitboard >> i) & 1L) == 1L) count++;
        }
        return count;
    }
    public int scorePawnStructure(boolean isWhite, long wP, long bP, int phase)
    {
        //count all pawns defending other pawns
        int whiteScore = phase * bitBoardToPieceCount((wP & ~fileMask[0] >> 9) & wP);
        whiteScore += phase * bitBoardToPieceCount((wP & ~fileMask[7] >> 7) & wP);

        int blackScore = phase*bitBoardToPieceCount((bP & ~fileMask[0] << 7) & bP);
        blackScore += phase*bitBoardToPieceCount((bP & ~fileMask[7] << 9) & bP);

        //check for past pawns
        for(int i = 0; i < 8; i++){
            //if pawn on file, and no opposing pawn on adjacent file, count number of passed pawns and multiply by game phase
            if((fileMask[i] & wP) != 0 && (pastPawnMask[i] & bP) == 0) whiteScore += phase * bitBoardToPieceCount((fileMask[i] & wP));
            if((fileMask[i] & bP) != 0 && (pastPawnMask[i] & wP) == 0) blackScore += phase * bitBoardToPieceCount((fileMask[i] & wP));
        }


        //depending on who is moving, return the evaluation from their perspective
        return isWhite ? whiteScore-blackScore : blackScore - whiteScore;
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

    public int piecePlacement(BitBoardPosition position)
    {
        initPieceArrays(position);
        int pieceLocations = 0;
        for(int i = 1; i < 6; i++){
            pieceLocations += (bitBoardPositionValue(piecesToMoveList[i], i, position.getWhiteToMove()) - bitBoardPositionValue(piecesNotMovingList[i], i, !position.getWhiteToMove()));
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
                    0, 0,  -10, 0, 0, -10, 0, 0,
                    0, 30, 0, 0, 0, 0, 30, 0,
                    0, 10, 0, 0, 0, 0, 10, 0,
                    0, 0,  10, 20, 20, 10, 0, 0,
                    0, 0,  10, 20, 20, 10, 0, 0,
                    0, 0,  0, 10, 10, 0, 0, 0,
                    0, 0,  0, 0, 0, 0, 0, 0,
                    0, 0,  0, 0, 0, 0, 0, 0,
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
    private static final long[] fileMask = {
            72340172838076673L, 144680345676153346L, 289360691352306692L, 578721382704613384L,
            1157442765409226768l, 2314885530818453536L, 4629771061636907072L, -9187201950435737472L
    };

    private static final long[] rankMask = {
            255L, 65280L, 16711680L, 4278190080L, 1095216660480L,
            280375465082880L, 71776119061217280L, -72057594037927936L
    };


    private static final long[] diagonalMask = {
            1L, 258L, 66052L, 16909320L, 4328785936L, 1108169199648L, 283691315109952L,
            72624976668147840L, 145249953336295424L, 290499906672525312L, 580999813328273408L,
            1161999622361579520L, 2323998145211531264L, 4647714815446351872L, -9223372036854775808L
    };

    private static final long[] antiDiagonalMask = {
            128L, 32832l, 8405024L, 2151686160L, 550831656968L, 141012904183812L,
            36099303471055874L, -9205322385119247871L, 4620710844295151872L, 2310355422147575808L,
            1155177711073755136L, 577588855528488960L ,288794425616760832L, 144396663052566528L, 72057594037927936L
    };

}
