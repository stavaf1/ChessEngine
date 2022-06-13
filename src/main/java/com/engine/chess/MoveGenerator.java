package com.engine.chess;

import static java.lang.Long.*;

public class MoveGenerator {
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

    private static long CASTLE_TOPLEFT = 30L;

    private static long CASTLE_TOPRIGHT = 112;

    private static long CASTLE_BOTTOMLEFT = 2161727821137838080L;
    private static long CASTLE_BOTTOMRIGHT = 8070450532247928832L;

    private static long CASTLECLEAR_TOPLEFT = 14L;

    private static long CASTLECLEAR_TOPRIGHT = 96L;

    private static long CASTLECLEAR_BOTTOMLEFT = 1008806316530991104L;

    private static long CASTLECLEAR_BOTTOMRIGHT = 6917529027641081856L;

    private static long PIN_BOARD;
    protected static long RANK_4 = 1095216660480L;
    protected static long RANK_5 = 4278190080L;
    protected static long FILE_1 = -9151031864016699136L;
    protected static long FILE_8 = -9187201950435737472L;

    protected static long EDGES = -35604928818740737L;


    protected long BLACK_OCCUPANCY, WHITE_OCCUPANCY, OCCUPIED_TILES, WHITE_ATTACKS, BLACK_ATTACKS, BLOCK_CHECK;
    //BLACK/WHITE_ATTACKS denotes any square to which a black or white piece may move, especially for the purposes
    //of determining king moves

    public String getMoves(byte castling, byte enPassant ,boolean isWhite ,long bR, long bN, long bB, long bQ, long bK, long bP, long wR, long wN, long wB, long wK, long wQ, long wP){

        WHITE_ATTACKS = 0L;
        BLACK_ATTACKS = 0L;
        BLOCK_CHECK = 0b1111111111111111111111111111111111111111111111111111111111111111L;

        BLACK_OCCUPANCY = (bR|bN|bB|bQ|bK|bP);
        WHITE_OCCUPANCY = (wR|wN|wB|wP|wK|wQ);
        OCCUPIED_TILES = (WHITE_OCCUPANCY | BLACK_OCCUPANCY);


//        String leadingZeroes = "";
//        for(int i = 0; i<numberOfLeadingZeros(OCCUPIED_TILES);i++){leadingZeroes += "0";}
//        printBitBoard("" + leadingZeroes + toBinaryString(OCCUPIED_TILES));

        //GENERATE A PINBOARD TO PREVENT PINNED PIECES FROM GENERATING MOVES
        //it will be important to consider the fact that king move generation will come last.
        String whiteMoves = whitePawnMoves(wP, enPassant) + bishopMoves(wB, true) + rookMoves(wR, true) + knightMoves(wN, true) + rookMoves(wQ, true) + bishopMoves(wQ, true);
        String blackMoves = blackPawnMoves(bP, enPassant) + bishopMoves(bB, false) + rookMoves(bR, false) + knightMoves(bN, false) + rookMoves(bQ, false) + bishopMoves(bQ,false);

        if(isWhite){
            //to ensure white king cannot be adjacent to the black king, by
            //prioritising the black kings attack moves, and visa versa
            blackMoves += kingMoves(bK, castling, false);
            whiteMoves += kingMoves(wK, castling, true);
        } else {
            whiteMoves += kingMoves(wK, castling, true);
            blackMoves += kingMoves(bK, castling, false);
        }

        return isWhite ? whiteMoves: blackMoves;

    }

    public void checkLegal(String move){

    }
    public BitBoardPosition makeMove(boolean isWhite, String move, long bR, long bN, long bB, long bQ, long bK, long bP, long wR, long wN, long wB, long wK, long wQ, long wP)
    {
        long fromBitBoard = 1L << (parseLong("" + move.charAt(0))*8 + parseLong("" + move.charAt(1)));
        long toBitBoard = 1L << (parseLong("" + move.charAt(3))*8 + parseLong("" + move.charAt(4)));


        BitBoardPosition pseudoLegalPosition = null;

        switch (move.charAt(2)){
            case 'x':
                pseudoLegalPosition = takes(isWhite, fromBitBoard, toBitBoard, bR, bN, bB, bQ, bK, bP, wR, wN, wB, wK, wQ, wP);
                pseudoLegalPosition.setEnPassant((byte) 0b00000000);
                break;
            case 'n':
                pseudoLegalPosition = quiet(isWhite, fromBitBoard, toBitBoard, bR, bN, bB, bQ, bK, bP, wR, wN, wB, wK, wQ, wP);
                pseudoLegalPosition.setEnPassant((byte) 0b00000000);
                break;
            case 'p':
                pseudoLegalPosition = doublePush(isWhite, fromBitBoard, toBitBoard, bR, bN, bB, bQ, bK, bP, wR, wN, wB, wK, wQ, wP);
                break;
            case 'e':
                pseudoLegalPosition = enPassant(isWhite, fromBitBoard, toBitBoard, bR, bN, bB, bQ, bK, bP, wR, wN, wB, wK, wQ, wP);
                pseudoLegalPosition.setEnPassant((byte) 0b00000000);
                break;
        }
        return pseudoLegalPosition;
    }
    public BitBoardPosition enPassant(boolean isWhite, long from, long to, long bR, long bN, long bB, long bQ, long bK, long bP, long wR, long wN, long wB, long wK, long wQ, long wP)
    {
        long removePawn = isWhite ? to << 8 : to >> 8;

        if((from & wP) > 1){wP &= ~from; wP |= to; bP &= ~removePawn;}
        if((from & bP) > 1){bP &= ~from; bP |= to; wP &= ~removePawn;}

        return new BitBoardPosition(bR, bN, bB, bQ, bK, bP, wR, wN, wB, wK, wQ, wP);
    }




    public BitBoardPosition doublePush(boolean isWhite, long from, long to, long bR, long bN, long bB, long bQ, long bK, long bP, long wR, long wN, long wB, long wK, long wQ, long wP)
    {
        if((from & wP) > 1){wP &= ~from; wP |= to;}
        if((from & bP) > 1){bP &= ~from; bP |= to;}

        BitBoardPosition next = new BitBoardPosition(bR, bN, bB, bQ, bK, bP, wR, wN, wB, wK, wQ, wP);
        for(int i = 0; i < 64; i++) if(((to >> i)&1)==1) next.setEnPassant((byte) (1 << (i%8)));
        System.out.println(next.getEnPassant());

        return next;
    }

    public BitBoardPosition quiet(boolean isWhite, long from, long to, long bR, long bN, long bB, long bQ, long bK, long bP, long wR, long wN, long wB, long wK, long wQ, long wP)
    {
        //if a piece is found in the from position, put it in the to position
        if((from & wR) > 1){wR &= ~from; wR |= to;}
        if((from & wN) > 1){wN &= ~from; wN |= to;}
        if((from & wB) > 1){wB &= ~from; wB |= to;}
        if((from & wQ) > 1){wQ &= ~from; wQ |= to;}
        if((from & wP) > 1){wP &= ~from; wP |= to;}
        if((from & bR) > 1){bR &= ~from; bR |= to;}
        if((from & bN) > 1){bN &= ~from; bN |= to;}
        if((from & bB) > 1){bB &= ~from; bB |= to;}
        if((from & bQ) > 1){bQ &= ~from; bQ |= to;}
        if((from & bP) > 1){bP &= ~from; bP |= to;}


        return new BitBoardPosition(bR, bN, bB, bQ, bK, bP, wR, wN, wB, wK, wQ, wP);
    }
    public BitBoardPosition takes(boolean isWhite, long from, long to, long bR, long bN, long bB, long bQ, long bK, long bP, long wR, long wN, long wB, long wK, long wQ, long wP)
    {
        wN &= ~to;
        wB &= ~to;
        wR &= ~to;
        wP &= ~to;
        wQ &= ~to;
        bN &= ~to;
        bB &= ~to;
        bR &= ~to;
        bQ &= ~to;

        //potential origins of the move
        if((from & wR) > 1){wR &= ~from; wR |= to;}
        if((from & wN) > 1){wN &= ~from; wN |= to;}
        if((from & wB) > 1){wB &= ~from; wB |= to;}
        if((from & wQ) > 1){wQ &= ~from; wQ |= to;}
        if((from & wP) > 1){wP &= ~from; wP |= to;}
        if((from & bR) > 1){bR &= ~from; bR |= to;}
        if((from & bN) > 1){bN &= ~from; bN |= to;}
        if((from & bB) > 1){bB &= ~from; bB |= to;}
        if((from & bQ) > 1){bQ &= ~from; bQ |= to;}
        if((from & bP) > 1){bP &= ~from; bP |= to;}

        return new BitBoardPosition(bR, bN, bB, bQ, bK, bP, wR, wN, wB, wK, wQ, wP);
    }



    public String blackPawnMoves(long pawnBoard, byte enPassant)
    {
        String pawnMoves = "";
        long PAWN_MOVES = 0L;

//        moves forward by one
        long bitOneForward = (pawnBoard << 8) & ~OCCUPIED_TILES;

        for(int i = 0; i < 64; i++){
            if(((bitOneForward >> i) & 1) == 1){
                pawnMoves += "" + (i/8 - 1) + (i%8) + "n" + (i/8) + (i%8); //n denoting normal move
            }
        }
        // moves forward by two
        long bitTwoForward = ((pawnBoard << 16) & RANK_5 & (~OCCUPIED_TILES << 8) & ~OCCUPIED_TILES);
        PAWN_MOVES = PAWN_MOVES | bitTwoForward;
        for(int i = 0; i < 64; i++){if(((PAWN_MOVES >> i) & 1) == 1){pawnMoves += "" + (i/8 - 2) + (i%8)+ "p" + (i/8) + (i%8);}}

        long takesLeft = ((pawnBoard & ~FILE_1) << 7) & WHITE_OCCUPANCY;
        BLACK_ATTACKS |= ((pawnBoard & ~FILE_1) << 7);
//        PAWN_MOVES = PAWN_MOVES | takesLeft;
        for(int i = 0; i < 64; i++){
            if(((takesLeft >> i) & 1) == 1){
                pawnMoves += ("" + (i/8 - 1) + (i%8 + 1)+ "x" + (i/8) + (i%8));
            }
        }

        long takesRight = ((pawnBoard & ~FILE_8) << 9) & WHITE_OCCUPANCY;
        BLACK_ATTACKS |= ((pawnBoard & ~FILE_8) << 9);
//        PAWN_MOVES = PAWN_MOVES | takesRight;
        for(int i = 0; i < 64; i++){
            if(((takesRight >> i) & 1) == 1){
                pawnMoves += ("" + (i/8 - 1) + ((i-1)%8)+ "x" + (i/8) + (i%8));
            }
        }

        // en passant
        // i here should represent the file wherein the opposing pawn pushed two
        long enPassantLeft = 0L;
        long enPassantRight = 0L;
        for(int i = 0; i < 8; i++){
            if(((enPassant >> i) &1) == 1){
                enPassantLeft |= ((pawnBoard & ~FILE_1 & rankMask[4]) << 7) & fileMask[i];
                enPassantRight |= ((pawnBoard & ~FILE_8 & rankMask[4]) << 9) & fileMask[i];

            }
        }
        for (int i = 0; i < 64; i++){
            if(((enPassantLeft >>> i) & 1L) == 1L){pawnMoves += "" + "4" + (i%8 + 1) + "e" +(i/8) + (i%8);}
            if(((enPassantRight >>> i) & 1L) == 1L){pawnMoves += "" + "4" + ((i-1)%8) + "e" +(i/8) + (i%8);}
        }


        return pawnMoves;
    }


    public String whitePawnMoves(long pawnBoard, byte enPassant)
    {

        String pawnMoves = "";
        long PAWN_MOVES = 0L;

        //moves forward by one
        long bitOneForward = (pawnBoard >>> 8) & ~OCCUPIED_TILES;
        for(int i = 0; i < 64; i++){
            if(((bitOneForward >> i) & 1L) == 1L){
                pawnMoves += "" + (i/8 + 1) + (i%8) + "n" +(i/8) + (i%8);
            }
        }

        long bitTwoForward = ((pawnBoard >>> 16) & RANK_4) & (~OCCUPIED_TILES >> 8) & ~OCCUPIED_TILES;
        PAWN_MOVES = PAWN_MOVES | bitTwoForward;
        for(int i = 0; i < 64; i++){if(((PAWN_MOVES >> i) & 1) == 1){pawnMoves += "" + (i/8 + 2) + (i%8)+ "p" + (i/8) + (i%8);}}


        long takesRight = ((pawnBoard & ~FILE_1) >> 9) & BLACK_OCCUPANCY;
        WHITE_ATTACKS |= ((pawnBoard & ~FILE_1) >> 9);
//        PAWN_MOVES = PAWN_MOVES | takesRight;
        for(int i = 0; i < 64; i++){
            if(((takesRight >> i) & 1) == 1){
                pawnMoves += ("" + (i/8 + 1) + (i%8 + 1) +"x"+ (i/8) + (i%8));
            }
        }

        long takesLeft = ((pawnBoard & ~FILE_8) >> 7) & BLACK_OCCUPANCY;
        WHITE_ATTACKS |= ((pawnBoard & ~FILE_8) >> 7);
//        PAWN_MOVES = PAWN_MOVES | takesLeft;
        for(int i = 0; i < 64; i++){
            if(((takesLeft >> i) & 1) == 1){
                pawnMoves += ("" + (i/8 + 1) + ((i-1)%8)+ "x" + (i/8) + (i%8));
            }
        }

        long enPassantLeft = 0L;
        long enPassantRight = 0L;
        for(int i = 0; i < 8; i++){
            if(((enPassant >> i) &1) == 1){
                enPassantLeft |= ((pawnBoard & ~FILE_8 & rankMask[3]) >> 7) & fileMask[i];
                enPassantRight |= ((pawnBoard & ~FILE_1 & rankMask[3]) >> 9) & fileMask[i];

            }
        }
        for (int i = 0; i < 64; i++){
            if(((enPassantLeft >>> i) & 1L) == 1L){pawnMoves += "" + "3" + ((i-1)%8) + "e" +(i/8) + (i%8);}
            if(((enPassantRight >>> i) & 1L) == 1L){pawnMoves += "" + "3" + (i%8 + 1) + "e" +(i/8) + (i%8);}
        }
        return pawnMoves;
    }

    /**
     * these ugly methods will need agressive reformatting
     * possibly refactor into different classes
     *
     * these methods generate the move information
     * @param rookBoard
     * @return
     */

    public String rookMoves(long rookBoard, boolean isWhite){
        String viableMoves = "";
        for(int i = 0; i < 64; i++){
            if(((rookBoard >> i) & 1) == 1){
                String startLoc = "" + i/8 + i%8;
                long theseMoves = horizontalVerticalMoves(i);
                long takes;
                long quiets;
                if(isWhite) {
                    WHITE_ATTACKS |= theseMoves;
                    theseMoves &= ~WHITE_OCCUPANCY;
                    takes = theseMoves & BLACK_OCCUPANCY;
                    quiets = theseMoves & ~BLACK_OCCUPANCY;
                }
                else {
                    BLACK_ATTACKS |= theseMoves;
                    theseMoves &= ~BLACK_OCCUPANCY; //remove any potential friendly fires
                    takes = theseMoves & WHITE_OCCUPANCY;
                    quiets = theseMoves & ~WHITE_OCCUPANCY; //add moves to attackmap
                }
                for(int j =0; j < 64; j++){
                    if(((takes >> j) & 1) == 1){
                        viableMoves += startLoc + "x" + j/8 + j%8;
                    }
                    if(((quiets >> j) & 1) == 1){
                        viableMoves += startLoc + "n" + j/8 + j%8;
                    }
                }
            }
        }

        return viableMoves;
    }

    public String bishopMoves(long bishBoard, boolean isWhite){
        String viableMoves = "";
        for(int i = 0; i < 64; i++){
            if(((bishBoard >> i) & 1) == 1){
                String startLoc = "" + i/8 + i%8;
                long theseMoves = diagonalMoves(i);
                long takes;
                long quiets;
                if(isWhite) {
                    WHITE_ATTACKS |= theseMoves;
                    theseMoves &= ~WHITE_OCCUPANCY;
                    takes = theseMoves & BLACK_OCCUPANCY;
                    quiets = theseMoves & ~BLACK_OCCUPANCY;
                }
                else {
                    BLACK_ATTACKS |= theseMoves;
                    theseMoves &= ~BLACK_OCCUPANCY; //remove any potential friendly fires
                    takes = theseMoves & WHITE_OCCUPANCY;
                    quiets = theseMoves & ~WHITE_OCCUPANCY; //split moves into takes and captures for the interpreter
                }
                for(int j =0; j < 64; j++){
                    if(((takes >> j) & 1) == 1){
                        viableMoves += startLoc + "x" + j/8 + j%8;
                    }
                    if(((quiets >> j) & 1) == 1){
                        viableMoves += startLoc + "n" + j/8 + j%8;
                    }
                }

            }
        }
        return viableMoves;
    }

    public String knightMoves(long knightBoard, boolean isWhite){
        String viableMoves = "";
        for(int i = 0; i < 64; i++){
            if(((knightBoard >> i) & 1) == 1){
                String startLoc = "" + i/8 + i%8;
                long theseMoves = knightMoves(i);
                long takes;
                long quiets;
                if(isWhite) {
                    WHITE_ATTACKS |= theseMoves;
                    theseMoves &= ~WHITE_OCCUPANCY;
                    takes = theseMoves & WHITE_OCCUPANCY;
                    quiets = theseMoves & ~WHITE_OCCUPANCY;
                }
                else {
                    BLACK_ATTACKS |= theseMoves;
                    theseMoves &= ~BLACK_OCCUPANCY; //remove any potential friendly fires
                    takes = theseMoves & BLACK_OCCUPANCY;
                    quiets = theseMoves & ~BLACK_OCCUPANCY;
                }
                for(int j =0; j < 64; j++){
                    if(((takes >> j) & 1) == 1){
                        viableMoves += startLoc + "x" + j/8 + j%8;
                    }
                    if(((quiets >> j) & 1) == 1){
                        viableMoves += startLoc + "n" + j/8 + j%8;
                    }
                }
            }
        }

        return viableMoves;
    }
    public String kingMoves(long kingBoard, byte castling, boolean isWhite){
        String viableMoves = "";
        for(int i = 0; i < 64; i++){
            if(((kingBoard >> i) & 1) == 1){
                String startLoc = "" + i/8 + i%8;
                long theseMoves = kingMoves(i, castling, isWhite);
                long takes;
                long quiets;
                if(isWhite) {
                    WHITE_ATTACKS |= theseMoves;
                    theseMoves &= ~WHITE_OCCUPANCY; // cannot move anywhere a friendly piece is
                    theseMoves &= ~BLACK_ATTACKS; // cannot move anywhere that a black piece is attacking
                    takes = theseMoves & BLACK_OCCUPANCY;
                    quiets = theseMoves & ~BLACK_OCCUPANCY;
                }
                else {
                    BLACK_ATTACKS |= theseMoves; //any locations defended or attacked by a black piece
                    theseMoves &= ~BLACK_OCCUPANCY; //remove any potential friendly fires
                    theseMoves &= ~WHITE_ATTACKS;
                    takes = theseMoves & WHITE_OCCUPANCY;
                    quiets = theseMoves & ~WHITE_OCCUPANCY;
                }
                for(int j =0; j < 64; j++){
                    if(((takes >> j) & 1) == 1){
                        viableMoves += startLoc + "x" + j/8 + j%8;
                    }
                    if(((quiets >> j) & 1) == 1){
                        viableMoves += startLoc + "n" + j/8 + j%8;
                    }
                }
                viableMoves += getCastles(castling, isWhite);
            }
        }

        return viableMoves;
    }


    /**
     * OCCUPIED TILES & FILEMASK gives a bitboard with the occupancy
     * substracting the bit location fills ones from the bit location to the next bit
     * later, applying the filemask again will yield all the possbible moves on that file
     *
     *
     * @param pieceLoc
     * @return
     */

    public long horizontalVerticalMoves(int pieceLoc)
    {
        long bitPieceLoc = 1L << pieceLoc;

        long horizontalMoves = (((OCCUPIED_TILES | fileMask[0] | fileMask[7]) - 2*bitPieceLoc) ^ (reverse(reverse(OCCUPIED_TILES | fileMask[0] | fileMask[7]) - 2*reverse(bitPieceLoc)))) & rankMask[pieceLoc/8];
        long verticalMoves = ((((OCCUPIED_TILES & fileMask[pieceLoc % 8]) - 2*bitPieceLoc) ^ reverse((reverse(OCCUPIED_TILES & fileMask[pieceLoc % 8]) - 2*reverse(bitPieceLoc)))) & fileMask[pieceLoc% 8]);

        return (verticalMoves | horizontalMoves);
    }


    public long diagonalMoves(int pieceLoc)
    {
        long bitPieceLoc = 1L << pieceLoc;

        long diagonalMoves = (((OCCUPIED_TILES & diagonalMask[(pieceLoc/8 + pieceLoc%8)])-2*bitPieceLoc) ^ reverse(reverse(OCCUPIED_TILES & diagonalMask[(pieceLoc/8 + pieceLoc%8)]) - 2*reverse(bitPieceLoc))) & diagonalMask[(pieceLoc/8 + pieceLoc%8)];
        long antiDiagonalMoves = (((OCCUPIED_TILES & antiDiagonalMask[(pieceLoc/8 + 7 - pieceLoc%8)])-2*bitPieceLoc) ^ reverse(reverse(OCCUPIED_TILES & antiDiagonalMask[(pieceLoc/8 + 7 - pieceLoc%8)]) - 2*reverse(bitPieceLoc))) & antiDiagonalMask[(pieceLoc/8 + 7 - pieceLoc%8)];

        return (antiDiagonalMoves | diagonalMoves);
    }

    public long knightMoves(int pieceLoc){
        long bitPieceLoc = 1L << pieceLoc;

        long noNoWe = (bitPieceLoc >> 17) & ~fileMask[7] & ~rankMask[7] & ~rankMask[6];
        long noNoEa = (bitPieceLoc >> 15) & ~fileMask[0] & ~rankMask[7] & ~rankMask[6];

        long noWeWe = (bitPieceLoc >> 10) & ~fileMask[6] & ~ fileMask[7] & ~rankMask[7];
        long noEaEa = (bitPieceLoc >> 6) & ~fileMask[0] & ~fileMask[1] & ~rankMask[7];

        long soSoWe = (bitPieceLoc << 17) & ~rankMask[0] & ~ rankMask[1] & ~fileMask[0];
        long soSoEa = (bitPieceLoc << 15) & ~rankMask[0] & ~ rankMask[1] & ~fileMask[7];

        long soWeWe = (bitPieceLoc << 6) & ~fileMask[6] & ~fileMask[7] & ~rankMask[0];
        long soEaEa = (bitPieceLoc << 10) & ~fileMask[0] & ~fileMask[1] & ~rankMask[0];

        return (noNoWe | noNoEa | noWeWe | noEaEa | soSoWe | soSoEa | soWeWe | soEaEa);
    }


    /**
     * castling byte convention
     * 00001111 = both sides can castle
     *     ^^^^ ones denote castling rights, beginning in top-left, top-right, bottom-left, bottom-right;
     * @param pieceLoc
     * @param castling
     * @param isWhite
     * @return
     */
    public long kingMoves(int pieceLoc, byte castling, boolean isWhite){
        long bitPieceLoc = 1L << pieceLoc;
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


        return kingMoves;
    }
    /**
     * castling byte convention
     * 00001111 = both sides can castle
     *     ^^^^ ones denote castling rights, beginning in top-left, top-right, bottom-left, bottom-right;
     * @param castling
     * @param isWhite
     * @return
     */
    public String getCastles(byte castling, boolean isWhite)
    {
        String viableMoves = "";

        if(isWhite){
            if((castling & 1) == 1){
                //white kingside castles
                long blockers = (CASTLE_BOTTOMRIGHT & BLACK_ATTACKS) | (OCCUPIED_TILES & CASTLECLEAR_BOTTOMRIGHT);
                if(blockers == 0)
                    viableMoves += "74l77";
            }
            if(((castling >>> 1) & 1L) == 1){
                //white queenside castles
                long blockers = (CASTLE_BOTTOMLEFT & BLACK_ATTACKS)|(OCCUPIED_TILES & CASTLECLEAR_BOTTOMLEFT);
                if(blockers == 0)
                    viableMoves += "74c70";
            }
        } else {
            if(((castling >>> 2) & 1L) == 1){
                //black castling kingside(topright)
                long blockers = (CASTLE_TOPRIGHT & WHITE_ATTACKS)|(OCCUPIED_TILES & CASTLECLEAR_TOPRIGHT);
                if(blockers == 0)
                    viableMoves += "04l07";
            }
            if(((castling >>> 3) & 1 ) == 1){
                //black castles queenside
                long blockers = (CASTLE_TOPLEFT & WHITE_ATTACKS)|(OCCUPIED_TILES & CASTLECLEAR_TOPLEFT);
                if(blockers == 0){
                    viableMoves += "04c00";
                }
            }


        }


        return viableMoves;
    }

    private static long[] fileMask = {
            72340172838076673L, 144680345676153346L, 289360691352306692L, 578721382704613384L,
            1157442765409226768l, 2314885530818453536L, 4629771061636907072L, -9187201950435737472L
    };

    /**
     * rankmask[0] is the topmost rank
     */
    private static long[] rankMask = {
            255L, 65280L, 16711680L, 4278190080L, 1095216660480L,
            280375465082880L, 71776119061217280L, -72057594037927936L
    };


    /**
     * bottom left to top right
     * first index is square in top left, index cascades after this point
//     */
    private static long[] diagonalMask = {
            1L, 258L, 66052L, 16909320L, 4328785936L, 1108169199648L, 283691315109952L,
            72624976668147840L, 145249953336295424L, 290499906672525312L, 580999813328273408L,
            1161999622361579520L, 2323998145211531264L, 4647714815446351872L, -9223372036854775808L
    };

    /**
     * top right to bottom left
     * first index is square in top right
     */

    private static long[] antiDiagonalMask = {
            128L, 32832l, 8405024L, 2151686160L, 550831656968L, 141012904183812L,
            36099303471055874L, -9205322385119247871L, 4620710844295151872L, 2310355422147575808L,
            1155177711073755136L, 577588855528488960L ,288794425616760832L, 144396663052566528L, 72057594037927936L
    };

    public static void printBitBoard(long bitboard){
        String leadingZeroes = "";
        for(int i = 0; i < Long.numberOfLeadingZeros(bitboard); i++) leadingZeroes += "0";
        String printout = new StringBuilder(leadingZeroes + toBinaryString(bitboard)).reverse().toString();
        for(int i = 0; i < printout.length()/8; i++){
            System.out.println("");
            for(int j = 0; j < printout.length()/8; j++){
                System.out.printf("" + printout.charAt(i*8 + j));
            }
        }
    }



}
