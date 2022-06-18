package com.engine.chess;

/**
 * simple immutable wrapper classs for a bitBoard position
 */


/**---------------------------------------\
 * FLAGS FOR SPECIAL MOVE TYPES
 * n = normal move,
 * p = double push,
 * l = king castle,
 * c = queen castle,
 * x = captures,
 * e = en passant
 * k = knight promotion //any of these with capture simply capitalised
 * b = bishop promotion
 * r = rook promotion
 * q = queen promotion
 */




public class BitBoardPosition {
    private long bR = 0L;
    private long bN = 0L;
    private long bB = 0L;
    private long bK = 0L;
    private long bQ = 0L;
    private long bP;
    private long wR = 0L;
    private long wN = 0L;
    private long wB = 0L;
    private long wK = 0L;
    private long wQ = 0L;
    private long wP = 0L;

    private byte castling;

    private byte enPassant = 0b00000000;

    private boolean whiteToMove;

    /**
     * Simple wrapper class for a position object
     *
     */

    public BitBoardPosition(long bR, long bN, long bB, long bQ, long bK, long bP, long wR, long wN, long wB, long wK, long wQ, long wP, boolean whiteToMove)
    {
        this.bB = bB;
        this.bN = bN;
        this.bR = bR;
        this.bQ = bQ;
        this.bK = bK;
        this.bP = bP;
        this.wR = wR;
        this.wN = wN;
        this.wB = wB;
        this.wK = wK;
        this.wQ = wQ;
        this.wP = wP;
        this.whiteToMove = whiteToMove;
    }

    public void setCastling(byte castles){castling = castles;}
    public void setEnPassant(byte enPassant){this.enPassant = enPassant;}


    public byte getCastling(){
        return castling;
    }

    public byte getEnPassant() {
        return enPassant;
    }

    public boolean getWhiteToMove(){return whiteToMove;}

    public long getbR() {
        return bR;
    }

    public long getbN() {
        return bN;
    }

    public long getbB() {
        return bB;
    }

    public long getbK() {
        return bK;
    }

    public long getbQ() {
        return bQ;
    }

    public long getbP() {
        return bP;
    }

    public long getwR() {
        return wR;
    }

    public long getwN() {
        return wN;
    }

    public long getwB() {
        return wB;
    }

    public long getwK() {
        return wK;
    }

    public long getwQ() {
        return wQ;
    }

    public long getwP() {
        return wP;
    }
}
