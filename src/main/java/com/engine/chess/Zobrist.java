package com.engine.chess;

import java.security.SecureRandom;
import java.util.HashMap;

public class Zobrist {
    static final long[][] zArray = new long[12][64];
    static final long[] zEnPassant = new long[8];
    static final long[] zCastling = new long[8];
    static final HashMap<Long, TTentry> transpositionTable = new HashMap<>();
    static long blackHash = 0L;


    public Zobrist(){
        /**
         * we must generate hash with following fields:
         * each square and each piece, whiteorblack to move, en-passant, castling.
         */
        initZobristKeys();
    }

    /**
     * creates the programs unique random number initialisation
     */
    public void initZobristKeys(){
        SecureRandom rand = new SecureRandom();
        for(int i = 0; i < 12; i++){
            for(int j = 0; j < 64; j++){
                zArray[i][j] = rand.nextLong();
            }
        }
        for(int i = 0; i < 8; i++) {zEnPassant[i] = rand.nextLong(); zCastling[i] = rand.nextLong();}
        blackHash = rand.nextLong();
    }

    /**
     * convention 0:pawn, 1: bishop, 2: knight, 3: rook, 4: queen, 5: king ---- white:0, black:1
     * produces a key for a given position to be later stored in the hashmap.
     * @param position
     * @return
     */

    public long hashPosition(BitBoardPosition position){
        long zHash = 0L;
        for(int i = 0; i < 64; i++){
            if(((position.getwP() >> i) & 1L) == 1L)zHash ^= zArray[0][i];
            if(((position.getwB() >> i) & 1L) == 1L)zHash ^= zArray[1][i];
            if(((position.getwN() >> i) & 1L) == 1L)zHash ^= zArray[2][i];
            if(((position.getwR() >> i) & 1L) == 1L)zHash ^= zArray[3][i];
            if(((position.getwQ() >> i) & 1L) == 1L)zHash ^= zArray[4][i];
            if(((position.getwK() >> i) & 1L) == 1L)zHash ^= zArray[5][i];

            if(((position.getbP() >> i) & 1L) == 1L)zHash ^= zArray[6][i];
            if(((position.getbB() >> i) & 1L) == 1L)zHash ^= zArray[7][i];
            if(((position.getbN() >> i) & 1L) == 1L)zHash ^= zArray[8][i];
            if(((position.getbR() >> i) & 1L) == 1L)zHash ^= zArray[9][i];
            if(((position.getbQ() >> i) & 1L) == 1L)zHash ^= zArray[10][i];
            if(((position.getbK() >> i) & 1L) == 1L)zHash ^= zArray[11][i];

        }
        for(int i = 0; i < 8; i++) {
            if(((position.getEnPassant() >> i) & 1L) == 1L) zHash ^= zEnPassant[i];
            if(((position.getCastling() >> i) & 1L) == 1L) zHash ^= zCastling[i];
        }
        if(!position.getWhiteToMove()) zHash ^= blackHash;

        return zHash;
    }

    public void addEntry(BitBoardPosition position, TTentry entry)
    {
        long key = hashPosition(position);
        transpositionTable.put(key, entry);
    }

    /**
     * this value can be null
     * @param position being searched for in the algorithm
     * @return any known information about this entry
     */
    public TTentry getEntry(BitBoardPosition position)
    {
        long key = hashPosition(position);
        TTentry entry = transpositionTable.get(key);

        return entry;
    }


    public long[][] getZobr(){return zArray;}

}
