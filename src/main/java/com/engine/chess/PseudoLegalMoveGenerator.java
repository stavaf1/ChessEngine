package com.engine.chess;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Long.reverse;
import static java.lang.Long.toBinaryString;

public class PseudoLegalMoveGenerator {
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
    private static final long CASTLECLEAR_TOPLEFT = 14L;

    private static final long CASTLECLEAR_TOPRIGHT = 96L;

    private static final long CASTLECLEAR_BOTTOMLEFT = 1008806316530991104L;

    private static final long CASTLECLEAR_BOTTOMRIGHT = 6917529027641081856L;


    private static final long[] kingLookup = new long[64];

    private static final long[] knightLookup = new long[64];

    public static long timeTrial = 0;

    private long defPawn, defBish, defKnight, defQueen, defRook, defKing, attPieces, defPieces, allPieces, freeLocations;
    /**
     * constructor initialises knight and king lookup tables used throughout
     */
    public PseudoLegalMoveGenerator()
    {
        for(int i = 0; i < 64; i++){
            //=========================
            //array initialisation for knights
            long bitPieceLoc = 1L << i;

            long noNoWe = (bitPieceLoc >> 17) & ~fileMask[7] & ~rankMask[7] & ~rankMask[6];
            long noNoEa = (bitPieceLoc >> 15) & ~fileMask[0] & ~rankMask[7] & ~rankMask[6];

            long noWeWe = (bitPieceLoc >> 10) & ~fileMask[6] & ~fileMask[7] & ~rankMask[7];
            long noEaEa = (bitPieceLoc >> 6) & ~fileMask[0] & ~fileMask[1] & ~rankMask[7];

            long soSoWe = (bitPieceLoc << 17) & ~rankMask[0] & ~ rankMask[1] & ~fileMask[0];
            long soSoEa = (bitPieceLoc << 15) & ~rankMask[0] & ~ rankMask[1] & ~fileMask[7];

            long soWeWe = (bitPieceLoc << 6) & ~fileMask[6] & ~fileMask[7] & ~rankMask[0];
            long soEaEa = (bitPieceLoc << 10) & ~fileMask[0] & ~fileMask[1] & ~rankMask[0];

            knightLookup[i] = noNoWe | noNoEa | noWeWe | noEaEa | soSoWe | soSoEa | soWeWe | soEaEa;

            //=======================
            //array initialiser for kings
            long kingMoves = 0;

            //all natural king moves
            kingMoves |= (bitPieceLoc << 7) & ~fileMask[7] &~rankMask[0];
            kingMoves |= (bitPieceLoc << 8) & ~rankMask[0];
            kingMoves |= (bitPieceLoc << 9) & ~rankMask[0] & ~fileMask[0];

            kingMoves |= (bitPieceLoc<<1) & ~fileMask[0];
            kingMoves |= (bitPieceLoc >> 1) & ~fileMask[7];

            kingMoves |= (bitPieceLoc >> 7) & ~fileMask[0] &~rankMask[7];
            kingMoves |= (bitPieceLoc >> 8) & ~ rankMask[7];
            kingMoves |= (bitPieceLoc >> 9) & ~ fileMask[7] & ~rankMask[7];

            kingLookup[i] = kingMoves;
        }
    }

    /**
     * initialises all variables needed for movegeneration, calls moveGeneration functions and collects
     * a list of pseudo legal moves, legality can be checked later
     * gui will need a function for this which is grim but oh well.
     * ===================================================================
     * MOVE GENERATION FUNCTION
     * must be more lightweight than previous generator
     * store more information in move representation
     * pass perft testing
     *====================================================================
     * @param position position from which to move
     * @return list of positions possible to get to one move on.
     */

    public ArrayList<Integer> getPseudoMoves(BitBoardPosition position)
    {
        long start = System.currentTimeMillis();

        ArrayList<Integer> moveBuffer = new ArrayList<>(64);
        boolean isWhite = position.getWhiteToMove();

        //will be used to determine victim in move serialisation function ?? or will it
        defPawn = isWhite ? position.getbP() : position.getwP();
        defBish = isWhite ? position.getbB() : position.getwB();
        defKnight = isWhite ? position.getbN() : position.getwN();
        defQueen = isWhite ? position.getbQ() : position.getwQ();
        defRook = isWhite ? position.getbR() : position.getwR();
        defKing = isWhite ? position.getbK() : position.getwK();

        //important to prevent friendly fire
        attPieces = isWhite ? (position.getwP() | position.getwN() | position.getwB() | position.getwQ() | position.getwK() | position.getwR()) :
                (position.getbB() | position.getbK() | position.getbR() | position.getbN() | position.getbP() | position.getbQ());

        defPieces = (defBish | defPawn | defKnight | defQueen | defRook | defKing);

        allPieces = attPieces | defPieces;

        freeLocations = ~allPieces;


        //calling and collecting all the move generation functions
        moveBuffer.addAll(isWhite ? whitePawnMoves(position.getwP(), position.getEnPassant()) : blackPawnMoves(position.getbP(), position.getEnPassant()));
        moveBuffer.addAll(isWhite ? moves(position.getwR(), position.getwB(), position.getwN(), position.getwK(), position.getwQ()) :
                moves(position.getbR(), position.getbB(), position.getbN(), position.getbK(), position.getbQ()));
        moveBuffer.addAll(castles(position.getCastling(), position.getWhiteToMove()));

        timeTrial += (System.currentTimeMillis() - start);

        return moveBuffer;
    }

    /**
     * i think and hope this will suffice for a pawn move generator
     * @param pawnBoard
     * @param enPassant
     * @return
     */
    public LinkedList<Integer> whitePawnMoves(long pawnBoard, byte enPassant)
    {
        LinkedList<Integer> pawnMovesList = new LinkedList<>();


        //step forward one
        long oneForward = (pawnBoard >> 8) & freeLocations;

        //step forward two
        long twoForward = (pawnBoard >> 16) & (freeLocations >> 8) & freeLocations & rankMask[4];

        //take right
        long takesRight = ((pawnBoard & ~fileMask[0]) >> 9) & defPieces;

        //take left
        long takesLeft =((pawnBoard & ~fileMask[7])>>7) & defPieces;

        //promotion
        long promotionLeft = takesLeft &  rankMask[0];
        takesLeft &= ~promotionLeft;

        long promotionRight = takesRight & rankMask[0];
        takesRight &= ~promotionRight;

        long pushPromotion = oneForward & rankMask[0];
        oneForward &= ~pushPromotion;

        for(int i = 0; i < 64; i++){
            //send relevant information to the interpreter
            if(((oneForward >>> i) & 1) == 1)pawnMovesList.add(moveInterpereter( (1L << (i + 8)), 1L << i, 0, 5));
            if(((twoForward >>> i) & 1) == 1)pawnMovesList.add(moveInterpereter((1L << (i + 16)), 1L << i, 0, 4));
            if(((takesRight >>> i) & 1) == 1)pawnMovesList.add(moveInterpereter((1L << (i + 9)), 1L << i, 0, 0));
            if(((takesLeft >>> i) & 1) == 1)pawnMovesList.add(moveInterpereter((1L << (i + 7)), 1L << i, 0, 0));

            if(((promotionLeft >>> i) & 1L) == 1L){
                pawnMovesList.add(moveInterpereter((1L << (i + 7)), 1L << i, 0, 2));
                pawnMovesList.add(moveInterpereter((1L << (i + 7)), 1L << i, 0, 8));
                pawnMovesList.add(moveInterpereter((1L << (i + 7)), 1L << i, 0, 9));
                pawnMovesList.add(moveInterpereter((1L << (i + 7)), 1L << i, 0, 10));
            }
            if(((promotionRight >>> i) & 1L) == 1L){
                pawnMovesList.add(moveInterpereter((1L << (i + 9)), 1L << i, 0, 2));
                pawnMovesList.add(moveInterpereter((1L << (i + 9)), 1L << i, 0, 8));
                pawnMovesList.add(moveInterpereter((1L << (i + 9)), 1L << i, 0, 9));
                pawnMovesList.add(moveInterpereter((1L << (i + 9)), 1L << i, 0, 10));
            }
            if(((pushPromotion >>> i) & 1L) == 1L){
                pawnMovesList.add(moveInterpereter((1L << (i + 8)), 1L << i, 0, 2));
                pawnMovesList.add(moveInterpereter((1L << (i + 8)), 1L << i, 0, 8));
                pawnMovesList.add(moveInterpereter((1L << (i + 8)), 1L << i, 0, 9));
                pawnMovesList.add(moveInterpereter((1L << (i + 8)), 1L << i, 0, 10));
            }
        }


        if(enPassant == 0) return pawnMovesList;

        //enPassants
        for(int i = 0; i < 8; i++){
            if(((enPassant >>>i) & 1L) == 1L){
                long enPassantRight = ((pawnBoard & ~fileMask[0] & rankMask[3]) >> 9) & fileMask[i];
                long enPassantLeft = ((pawnBoard & ~fileMask[7] & rankMask[3]) >> 7) & fileMask[i];
                if(enPassantRight != 0)pawnMovesList.add(moveInterpereter((enPassantRight << 9), enPassantRight,0, 3));
                if(enPassantLeft != 0)pawnMovesList.add(moveInterpereter((enPassantLeft << 7), enPassantLeft,0, 3));
            }
        }
        return pawnMovesList;
    }

    public LinkedList<Integer> blackPawnMoves(long pawnBoard, byte enPassant)
    {
        LinkedList<Integer> pawnMoves = new LinkedList<>();

        //one forward
        long oneForward = (pawnBoard << 8) & freeLocations;

        //two forward
        long twoForward = (pawnBoard << 16) & (freeLocations << 8) & freeLocations & rankMask[3];

        //takes left
        long takesLeft = ((pawnBoard & ~ fileMask[0]) << 7) & defPieces;

        //takes right
        long takesRight = ((pawnBoard & ~fileMask[7]) << 9) & defPieces;

        //promotion
        long promotionLeft = takesLeft &  rankMask[7];
        takesLeft &= ~promotionLeft;

        long promotionRight = takesRight & rankMask[7];
        takesRight &= ~promotionRight;

        long pushPromotion = oneForward & rankMask[7];
        oneForward &= ~pushPromotion;

        for(int i = 0; i < 64; i++){
            if(((oneForward >>> i) & 1L) == 1L)pawnMoves.add(moveInterpereter((1L << (i - 8)), 1L << i, 0, 5));
            if(((twoForward >>> i) & 1L) == 1L)pawnMoves.add(moveInterpereter((1L <<(i - 16)), 1L << i,0,4));
            if(((takesLeft >>> i) & 1L) ==1L)pawnMoves.add(moveInterpereter((1L << (i - 7)), 1L << i, 0, 0));
            if(((takesRight >>> i) & 1L) == 1L)pawnMoves.add(moveInterpereter((1L << (i - 9)), 1L << i, 0, 0));

            if(((promotionLeft >> i) & 1L) == 1L){
                pawnMoves.add(moveInterpereter((1L << (i - 7)), 1L << i, 0, 2));
                pawnMoves.add(moveInterpereter((1L << (i - 7)), 1L << i, 0, 8));
                pawnMoves.add(moveInterpereter((1L << (i - 7)), 1L << i, 0, 9));
                pawnMoves.add(moveInterpereter((1L << (i - 7)), 1L << i, 0, 10));
            }
            if(((promotionRight >> i) & 1L) == 1L){
                pawnMoves.add(moveInterpereter((1L << (i - 9)), 1L << i, 0, 2));
                pawnMoves.add(moveInterpereter((1L << (i - 9)), 1L << i, 0, 8));
                pawnMoves.add(moveInterpereter((1L << (i - 9)), 1L << i, 0, 9));
                pawnMoves.add(moveInterpereter((1L << (i - 9)), 1L << i, 0, 10));
            }
            if(((pushPromotion >> i) & 1L) == 1L){
                pawnMoves.add(moveInterpereter((1L << (i - 8)), 1L << i, 0, 2));
                pawnMoves.add(moveInterpereter((1L << (i - 8)), 1L << i, 0, 8));
                pawnMoves.add(moveInterpereter((1L << (i - 8)), 1L << i, 0, 9));
                pawnMoves.add(moveInterpereter((1L << (i - 8)), 1L << i, 0, 10));
            }
        }



        if(enPassant == 0) return pawnMoves;
        //en passant
        for(int i = 0; i < 8; i++){
            if(((enPassant >>i) & 1L) == 1L){
                long enPassantRight = ((pawnBoard &~fileMask[7] & rankMask[4]) << 9) & fileMask[i];
                long enPassantLeft = ((pawnBoard & ~fileMask[0] & rankMask[4]) << 7) & fileMask[i];
                if(enPassantRight != 0)pawnMoves.add(moveInterpereter((enPassantRight >> 9), enPassantRight, 0, 3));
                if(enPassantLeft != 0)pawnMoves.add(moveInterpereter((enPassantLeft >> 7), enPassantLeft, 0, 3));
//                printBitBoard(enPassantLeft|enPassantRight | oneForward | twoForward | takesLeft | takesRight);
            }
        }
        return pawnMoves;
    }

    public LinkedList<Integer> moves(long rookBoard, long bishopBoard, long knightBoard, long kingBoard, long queenBoard)
    {
        LinkedList<Integer> moves = new LinkedList<>();
        for(int i = 0; i < 64; i++){
            if(((rookBoard >>> i) & 1L) == 1L){
                long toBoard = straightSlide(1L << i, i)&~attPieces;
                long takes = toBoard & defPieces;
                long quiets = toBoard & ~defPieces;
                for(int j = 0; j < 64; j++) {
                    if (((takes >> j) & 1L) == 1L) moves.add(moveInterpereter((1L << i), (1L << j), 3, 0));
                    if (((quiets >> j) & 1L) == 1L) moves.add(moveInterpereter((1L << i), (1L << j), 3, 5));
                }
            }
            if(((bishopBoard >>> i) & 1L) == 1L){
                long toBoard = diagSlide(1L << i, i) &~attPieces;
                long takes = toBoard & defPieces;
                long quiets = toBoard & ~defPieces;
                for(int j = 0; j < 64; j++){
                    if (((takes >> j) & 1L) == 1L) moves.add(moveInterpereter((1L << i), (1L << j), 2, 0));
                    if (((quiets >> j) & 1L) == 1L) moves.add(moveInterpereter((1L << i), (1L << j), 2, 5));
                }
            }
            if(((knightBoard >>> i) & 1L) == 1L){
                long toBoard = knightLookup[i]&~attPieces;
                long takes = toBoard & defPieces;
                long quiets = toBoard & ~defPieces;
                for(int j = 0; j < 64; j++){
                    if (((takes >> j) & 1L) == 1L) moves.add(moveInterpereter((1L << i), (1L << j), 1, 0));
                    if (((quiets >> j) & 1L) == 1L) moves.add(moveInterpereter((1L << i), (1L << j), 1, 5));
                }
            }
            if(((kingBoard >>> i) & 1L) == 1L){
                long toBoard = kingLookup[i]&~attPieces;
                long takes = toBoard & defPieces;
                long quiets = toBoard & ~defPieces;
                for(int j = 0; j < 64; j++){
                    if (((takes >> j) & 1L) == 1L) moves.add(moveInterpereter((1L << i), (1L << j), 5, 0));
                    if (((quiets >> j) & 1L) == 1L) moves.add(moveInterpereter((1L << i), (1L << j), 5, 5));
                }
            }
            if(((queenBoard >>> i) & 1L) == 1L){
                long toBoard = (diagSlide(1L << i, i) | straightSlide(1L << i, i))&~attPieces;
                long takes = toBoard & defPieces;
                long quiets = toBoard & ~defPieces;
                for(int j = 0; j < 64; j++){
                    if (((takes >> j) & 1L) == 1L) moves.add(moveInterpereter((1L << i), (1L << j), 4, 0));
                    if (((quiets >> j) & 1L) == 1L) moves.add(moveInterpereter((1L << i), (1L << j), 4, 5));
                }
            }
        }
        return moves;
    }
    public long straightSlide(long pieceBoard, int pieceLoc)
    {
        long horizontalMoves = (((allPieces | fileMask[0] | fileMask[7]) - 2*pieceBoard) ^ reverse(reverse(allPieces | fileMask[0] | fileMask[7]) - 2*reverse(pieceBoard))) & rankMask[pieceLoc/8] &~attPieces;
        long verticalMoves = (((allPieces & fileMask[pieceLoc%8]) - 2*pieceBoard) ^ reverse(reverse(allPieces & fileMask[pieceLoc%8]) - 2*reverse(pieceBoard))) & fileMask[pieceLoc%8] & ~attPieces;

        return (horizontalMoves | verticalMoves);
    }

    public long diagSlide(long pieceBoard, int pieceLoc)
    {
        long diagonalMoves = (((allPieces & diagonalMask[pieceLoc/8 + pieceLoc % 8]) - 2*pieceBoard)^reverse(reverse(allPieces & diagonalMask[pieceLoc/8 + pieceLoc % 8]) - 2*reverse(pieceBoard))) & diagonalMask[pieceLoc/8 + pieceLoc % 8];
        long antiDiagonalMoves = (((allPieces & antiDiagonalMask[(pieceLoc/8 + 7 - pieceLoc%8)]) - 2*pieceBoard) ^ reverse(reverse(allPieces & antiDiagonalMask[(pieceLoc/8 + 7 - pieceLoc%8)]) -2*reverse(pieceBoard))) & antiDiagonalMask[(pieceLoc/8 + 7 - pieceLoc%8)];

        return diagonalMoves | antiDiagonalMoves;
    }

    public LinkedList<Integer> castles(byte castling, boolean isWhite)
    {
        LinkedList<Integer> castleList = new LinkedList<>();
        if(isWhite){
            //if no castling locations are under attack,
            if((castling & 1) == 1 && !locationAttacked(1L << 62, true) && !locationAttacked(1L << 61, true)
                    && !locationAttacked(1L << 60, true) &&((CASTLECLEAR_BOTTOMRIGHT & allPieces)==0)){
                castleList.add(moveInterpereter((1L << 60),(1L << 62),5, 6));
            }
            if(((castling >>1) & 1) == 1 && !locationAttacked(1L << 60, true) && !locationAttacked(1L << 59, true)
                    && !locationAttacked(1L << 58, true) &&((CASTLECLEAR_BOTTOMLEFT & allPieces)==0))
                castleList.add(moveInterpereter((1L << 60),(1L << 58),5, 7));

        } else {
            if(((castling >>2) & 1) == 1 && !locationAttacked(1L << 4, false) && !locationAttacked(1L << 5, false)
                    && !locationAttacked(1L << 6, false) &&((CASTLECLEAR_TOPRIGHT & allPieces)==0))
                castleList.add(moveInterpereter(1L << 4, 1L << 6, 5, 6));
            if(((castling >>3) & 1) == 1 && !locationAttacked(1L << 2, false) && !locationAttacked(1L << 3, false)
                    && !locationAttacked(1L << 4, false) &&((CASTLECLEAR_TOPLEFT & allPieces)==0))
                castleList.add(moveInterpereter(1L << 4, 1L << 2, 5, 7));
        }
        return castleList;
    }


    public boolean isInCheck(BitBoardPosition position){
        boolean isWhite = position.getWhiteToMove();
        //all the pieces that could be giving a check
        defPawn = isWhite ? position.getbP() : position.getwP();
        defBish = isWhite ? position.getbB() : position.getwB();
        defKnight = isWhite ? position.getbN() : position.getwN();
        defQueen = isWhite ? position.getbQ() : position.getwQ();
        defRook = isWhite ? position.getbR() : position.getwR();
        defKing = isWhite ? position.getbK() : position.getwK();

        return locationAttacked(isWhite? position.getwK() : position.getbK(), isWhite);
    }

    /**
     * more lightweight check for whether a location is attacked
     * used by the castling function to determine wether the king would pass
     * through any attacked squares
     * @param kingBoard location of king trying to castle
     * @param isWhite which side to check for
     * @return
     */

    public boolean locationAttacked(long kingBoard, boolean isWhite)
    {
        int kingLoc = 0;
        for(int i = 0; i < 64; i++) if(((kingBoard >>i)&1)==1){ kingLoc = i; break;}
        boolean isInCheck = false;

        if(isWhite){
            if((((kingBoard & ~fileMask[0]) >> 7) & defPawn) != 0) isInCheck = true;
            if((((kingBoard & ~fileMask[7]) >> 9) & defPawn)!= 0) isInCheck = true;
        } else {
            if((((kingBoard & ~fileMask[0]) << 7) & defPawn) != 0) isInCheck = true;
            if((((kingBoard & ~fileMask[7]) << 9)& defPawn)!= 0)isInCheck = true;
        }
        if(((straightSlide(kingBoard, kingLoc)) & (defRook | defQueen)) !=0)isInCheck = true;
        if(((diagSlide(kingBoard, kingLoc)) & (defBish | defQueen)) !=0)isInCheck = true;
        if((knightLookup[kingLoc] & defKnight) != 0) isInCheck = true;

        return isInCheck;
    }

    public boolean isLegal(BitBoardPosition position)
    {
        allPieces = position.getwP() | position.getwN() | position.getwB() | position.getwR() | position.getwQ() | position.getwK() | position.getbP() | position.getbN() | position.getbB() | position.getbR() | position.getbQ() | position.getbK();

        boolean isLegal = true;
        boolean isWhite = position.getWhiteToMove();
        //if white is about to move, black cannot be in check
        //if white is about to move, blacks king cannot be adjacent to whites king
        long killableKing = isWhite ? position.getbK() : position.getwK();
        int kingLoc = 0;
        for(int i = 0; i < 64; i++)if(((killableKing >> i)&1)==1) kingLoc = i;

        //if these pieces can reach killableking, ie, if any of these pieces and killableking towards them its illegal
        long attackingPawn = isWhite ? position.getwP():position.getbP();
        long attackingKnight = isWhite ? position.getwN(): position.getbN();
        long attackingBishop = isWhite ? position.getwB(): position.getbB();
        long attackingRook = isWhite ? position.getwR(): position.getbR();
        long attackingQueen = isWhite ? position.getwQ(): position.getbQ();
        long attackingKing = isWhite ? position.getwK(): position.getbK();


        if(isWhite){
            if((((killableKing & ~fileMask[0]) << 7) & attackingPawn) != 0) isLegal = false;
            if((((killableKing & ~fileMask[7]) << 9) & attackingPawn)!= 0) isLegal = false;

        } else {
            if((((killableKing & ~fileMask[7]) >> 7) & attackingPawn) != 0) isLegal = false;
            if((((killableKing & ~fileMask[0]) >> 9)& attackingPawn)!= 0)isLegal = false;
        }

        if(((straightSlide(killableKing, kingLoc)) & (attackingRook | attackingQueen)) !=0)isLegal = false;
        if(((diagSlide(killableKing, kingLoc)) & (attackingBishop | attackingQueen)) !=0)isLegal = false;
        if((knightLookup[kingLoc] & attackingKnight) != 0) isLegal = false;
        if((kingLookup[kingLoc] &attackingKing) != 0)isLegal = false;

        return isLegal;
    }

    /**
     * =========================
     * IMPORTANT
     * consistent and cannon use of this function is critical
     * @param from bitboard startinglocation
     * @param to bitboard targetlocation
     * @param aggressorID the Id of the moving piece. useful for move ordering and evaluation
     * @return an integer move package.
     */
    public int moveInterpereter(long from, long to, int aggressorID, int moveType)
    {
        int movePackage = 0;
        int serialisedFrom = 0;
        int serialisedTo = 0;
        int victimId = 0;
        //serialise the locations
        for(int i = 0; i < 64; i++){
            if(((from >> i) & 1L) == 1L) serialisedFrom = i;
            if(((to >> i) & 1L) == 1L) serialisedTo = i;
        }
        //find any piece being taken
        if(moveType == 0 | moveType == 2 | moveType == 8 | moveType == 9 | moveType == 10){
            if((to & defPawn) != 0)victimId = 0;
            if((to & defKnight) != 0) victimId = 1;
            if((to & defBish) != 0) victimId = 2;
            if((to & defRook) != 0) victimId = 3;
            if((to & defQueen) != 0) victimId = 4;
        } else {
            victimId = 5;
        }


        //first 8 bits: to Location
        //last 8 bits reversed: from location
        movePackage |= Integer.reverse(serialisedFrom);
        movePackage |= serialisedTo;

        //bits 8-12 agressorNumber (moving piece)
        //bits 12-16 defenderNumber
        //bits 16-24 movetype
        movePackage |= (aggressorID << 8);
        movePackage |= (victimId << 12);
        movePackage |= (moveType << 16);

        return movePackage;
    }

    public BitBoardPosition makeMove(BitBoardPosition position, int move)
    {

        boolean isWhite = position.getWhiteToMove();
        //easier access (i think faster im not sure)
        long[] black = new long[]{
                position.getbP(),
                position.getbN(),
                position.getbB(),
                position.getbR(),
                position.getbQ(),
                position.getbK()
        };

        long[] white = new long[]{
                position.getwP(),
                position.getwN(),
                position.getwB(),
                position.getwR(),
                position.getwQ(),
                position.getwK()
        };

        int fromSer = move & 0b00000000000000000000000011111111;

        //clear to location
        long to = 1L << (fromSer);
        long from = 1L << Integer.reverse(move & 0b11111111000000000000000000000000);
        //piece ids for the moving and the moved
        int agressor = ((move >> 8) & 0b00000000000000000000000000001111);
        int victim = ((move >>12) & 0b00000000000000000000000000001111);
        byte enPassant = 0;
        byte castling = position.getCastling();
        //initialise references to all bitboards
        long[] agressors = isWhite ? white : black;
        long[] victims = isWhite ? black: white;




        //if a black rook in the top left is a from or to location mask castling accordingly
        if ((black[3] & 1L & (from | to)) != 0) castling &= 0b00000111;
        if ((black[3] & (1L << 7) & (from | to)) != 0) castling &= 0b00001011;
        if ((black[5] & from) != 0) castling &= 0b00000011;
        if((white[3] & 1L << 56 & (from | to)) != 0) castling &= 0b00001101;
        if((white[3] & 1L << 63 & (from | to)) != 0) castling &= 0b00001110;
        if((white[5] & from) != 0) castling &= 0b00001100;

        agressors[agressor] ^= (from | to);
        victims[victim] &= ~to;

        switch (((move >> 16) & 0b00000000000000000000000000001111)){
            case 2:
                agressors[0] &= ~to;
                agressors[4] |= to;
                break;
            case 3:
                //cool trick, the to which an en passant moves cannot have a pawn above or below at this time. classic
                victims[0] &= ~(to << 8 | to >> 8);
                break;
            case 4:
                enPassant = (byte) (1 << (fromSer%8));
                break;
            case 6:
                if(isWhite){
                    agressors[3] ^= (1L << 63 | 1L << 61);
                    castling &= 0b00001100;
                } else {
                    agressors[3] ^= (1L << 7 | 1L << 5);
                    castling &= 0b00000011;
                }
                break;
            case 7:
                if(isWhite){
                    agressors[3] ^= (1L << 59 | 1L << 56);
                    castling &= 0b00001100;
                } else {
                    agressors[3] ^= (1L | 1L << 3);
                    castling &= 0b00000011;
                }
                break;
            case 8:
                agressors[0] &= ~to;
                agressors[3] |= to;
                break;
            case 9:
                agressors[0] &= ~to;
                agressors[2] |= to;
                break;
            case 10:
                agressors[0] &= ~to;
                agressors[1] |= to;
                break;
        }

        BitBoardPosition nextPosition = new BitBoardPosition(black[3], black[1],black[2], black[4],black[5], black[0], white[3], white[1], white[2], white[5], white[4], white[0],!isWhite);
        nextPosition.setCastling(castling);
        nextPosition.setEnPassant(enPassant);
        return nextPosition;
    }




    public static void printBitBoard(long bitboard){
        StringBuilder leadingZeroes = new StringBuilder();
        for(int i = 0; i < Long.numberOfLeadingZeros(bitboard); i++) leadingZeroes.append("0");
        String printout = new StringBuilder(leadingZeroes + toBinaryString(bitboard)).reverse().toString();
        for(int i = 0; i < printout.length()/8; i++){
            System.out.println("");
            for(int j = 0; j < printout.length()/8; j++){
                System.out.printf("" + printout.charAt(i*8 + j));
            }
        }
        System.out.println("");
    }

}
