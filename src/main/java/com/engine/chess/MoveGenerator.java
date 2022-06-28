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

    private static long CASTLE_TOPLEFT = 28L;

    private static long CASTLE_TOPRIGHT = 112;

    private static long CASTLE_BOTTOMLEFT = 2017612633061982208L;
    private static long CASTLE_BOTTOMRIGHT = 8070450532247928832L;

    private static long CASTLECLEAR_TOPLEFT = 14L;

    private static long CASTLECLEAR_TOPRIGHT = 96L;

    private static long CASTLECLEAR_BOTTOMLEFT = 1008806316530991104L;

    private static long CASTLECLEAR_BOTTOMRIGHT = 6917529027641081856L;

    private static long WHITEROOK_CASTLEKINGSIDE = 2305843009213693952L;

    private static long BLACKROOK_CASTLEKINGSIDE = 32L;

    private static long WHITEROOK_CASTLEQUEENSIDE = 576460752303423488L;

    private static long BLACKROOK_CASTLEQUEENSIDE = 8L;

    protected static long RANK_4 = 1095216660480L;
    protected static long RANK_5 = 4278190080L;
    protected static long FILE_1 = -9151031864016699136L;
    protected static long FILE_8 = -9187201950435737472L;

    protected static long EDGES = -35604928818740737L;


    protected long BLACK_OCCUPANCY, WHITE_OCCUPANCY, OCCUPIED_TILES, WHITE_ATTACKS, BLACK_ATTACKS, BLOCK_CHECK, PIN_BOARD, PIN_VERTICAL, PIN_HORIZONTAL, PIN_DIAGONAL, PIN_ANTIDIAGONAL, UNSAFE_FORKING;
    //BLACK/WHITE_ATTACKS denotes any square to which a black or white piece may move, especially for the purposes
    //of determining king moves

    public String getMoves(byte castling, byte enPassant ,boolean isWhite ,long bR, long bN, long bB, long bQ, long bK, long bP, long wR, long wN, long wB, long wK, long wQ, long wP){
        long startTime = System.currentTimeMillis();
        UNSAFE_FORKING = 0L;
        PIN_BOARD = 0L;
        PIN_ANTIDIAGONAL = 0L;
        PIN_DIAGONAL = 0L;
        PIN_HORIZONTAL = 0L;
        PIN_VERTICAL = 0L;
        WHITE_ATTACKS = 0L;
        BLACK_ATTACKS = 0L;
        BLOCK_CHECK = 0b1111111111111111111111111111111111111111111111111111111111111111L;

        BLACK_OCCUPANCY = (bR|bN|bB|bQ|bK|bP);
        WHITE_OCCUPANCY = (wR|wN|wB|wP|wK|wQ);
        OCCUPIED_TILES = (WHITE_OCCUPANCY | BLACK_OCCUPANCY);

        int numChecks = initBlockCheck(isWhite, bR, bN, bB, bQ, bK, bP, wR, wN, wB, wK, wQ, wP);

//        String leadingZeroes = "";
//        for(int i = 0; i<numberOfLeadingZeros(OCCUPIED_TILES);i++){leadingZeroes += "0";}
//        printBitBoard("" + leadingZeroes + toBinaryString(OCCUPIED_TILES));


        //it will be important to consider the fact that king move generation will come last.
        String whiteMoves = whitePawnMoves(wP, enPassant) + bishopMoves(wB, true) + rookMoves(wR, true) + knightMoves(wN, true) + rookMoves(wQ, true) + bishopMoves(wQ, true);
        String blackMoves = blackPawnMoves(bP, wP,enPassant) + bishopMoves(bB, false) + rookMoves(bR, false) + knightMoves(bN, false) + rookMoves(bQ, false) + bishopMoves(bQ,false);

        if(isWhite){
            //to ensure white king cannot be adjacent to the black king, by
            //prioritising the black kings attack moves, and visa versa
            blackMoves += kingMoves(bK, castling, false);
            whiteMoves += kingMoves(wK, castling, true);
        } else {
            whiteMoves += kingMoves(wK, castling, true);
            blackMoves += kingMoves(bK, castling, false);
        }

        if(numChecks == 2) return kingMoves(isWhite? wK : bK, castling, isWhite);
        return isWhite ? whiteMoves: blackMoves;
    }

    public int initBlockCheck(boolean isWhite,long bR, long bN, long bB, long bQ, long bK, long bP, long wR, long wN, long wB, long wK, long wQ, long wP)
    {
        //this method is
//        long defendingKing = isWhite ? wK : bK;
//        long offendingRooks = isWhite ? bR : wR;
//        long blockingPieces = isWhite? BLACK_OCCUPANCY : WHITE_OCCUPANCY;

        long defendingKing = isWhite ? wK : bK;
        long offendingRooks = isWhite ? (bR | bQ) : (wR | wQ);
        long offendingBishops = isWhite ?  (bB | bQ) : (wB | wQ);
        long offendingKnights = isWhite ? bN : wN;
        long blockers = OCCUPIED_TILES;

        int kingSerialized = 0;
        for(int i = 0; i < 64; i++) if(((defendingKing >> i) &1) == 1){kingSerialized = i;}

        int numChecks = 0;

        //where are the rooks?
        boolean rookIsLeft = ((defendingKing - 1) & (offendingRooks & rankMask[kingSerialized/8])) != 0;
        boolean rookIsAbove = ((defendingKing - 1) & (offendingRooks & fileMask[kingSerialized % 8])) != 0;

        boolean rookIsRight = ((reverse(reverse(defendingKing)-1) & ((offendingRooks & rankMask[kingSerialized/8]))) != 0);
        boolean rookIsBelow = ((reverse(reverse(defendingKing)-1) & (offendingRooks & fileMask[kingSerialized % 8])) != 0);

        //masking positions so we can separarte different checks
        long upMask = (defendingKing - 1);
        long downMask = (reverse(reverse(defendingKing)-1));


        //initialise bitboards
        long rookLeftRay = 0;
        long rookRightRay = 0;
        long rookAboveRay = 0;
        long rookBelowRay = 0;


        //generates all rays from rooks to kings
        if((rankMask[kingSerialized/8] & offendingRooks) != 0){
            //some rook may be on the same rank as the king
            //the attack line to and through the king with the king position omitted
            long kingToRookHorizontalXray = (((offendingRooks | fileMask[0] | fileMask[7]) - 2*defendingKing) ^ (reverse(reverse(offendingRooks | fileMask[0] | fileMask[7]) - 2*reverse(defendingKing)))) & rankMask[kingSerialized/8];
            if(rookIsRight){rookRightRay = kingToRookHorizontalXray & downMask;}
            if(rookIsLeft){rookLeftRay = kingToRookHorizontalXray & upMask;}
            if(((rookRightRay | rookLeftRay) & blockers & ~offendingRooks) != 0){
                long thisRook = offendingRooks & kingToRookHorizontalXray;
                long rookToBlocker = (((blockers | fileMask[0] | fileMask[7]) - 2*thisRook) ^ (reverse(reverse(blockers | fileMask[0] | fileMask[7]) - 2*reverse(thisRook)))) & rankMask[kingSerialized/8];
                long kingToBlocker = (((blockers | fileMask[0] | fileMask[7]) - 2*defendingKing) ^ (reverse(reverse(blockers | fileMask[0] | fileMask[7]) - 2*reverse(defendingKing)))) & rankMask[kingSerialized/8];
                PIN_BOARD |= rookToBlocker & kingToBlocker & blockers;
                PIN_HORIZONTAL = kingToRookHorizontalXray & (rookToBlocker | kingToBlocker) | thisRook; //where a horizontally pinned piece can move
                //preventing illegal en passant
                if((kingSerialized/8 ==3) || kingSerialized/8 == 4){
                    PIN_HORIZONTAL = kingToRookHorizontalXray & (rookToBlocker | kingToBlocker);
                    //^^ this is the line from a(INCLUSIVE) to respective offender/defender NOT INCLUSIVE
                }
                //if (pinhorizontal & pinBoard) != 0, the piece can move across the pinhorizontal
            } else if ((rookRightRay & blockers) == 0 | (rookLeftRay & blockers) == 0) {
                UNSAFE_FORKING |= (rankMask[kingSerialized/8] & ~offendingRooks);
                numChecks++;
                BLOCK_CHECK = (rookRightRay | rookLeftRay);
            }
        }
        if((fileMask[kingSerialized%8]&offendingRooks) != 0){
            long kingToRookVerticalXray = ((((offendingRooks & fileMask[kingSerialized % 8]) - 2*defendingKing) ^ reverse((reverse(offendingRooks & fileMask[kingSerialized % 8]) - 2*reverse(defendingKing)))) & fileMask[kingSerialized% 8]);
            if(rookIsBelow){rookBelowRay = kingToRookVerticalXray &downMask;}
            if(rookIsAbove){rookAboveRay = kingToRookVerticalXray & upMask;}
            if(((rookBelowRay | rookAboveRay) & blockers & ~offendingRooks) != 0){
                long thisRook = offendingRooks & kingToRookVerticalXray;
                long rookToBlocker = ((((blockers & fileMask[kingSerialized % 8]) - 2*thisRook ) ^ reverse((reverse(blockers & fileMask[kingSerialized % 8]) - 2*reverse(thisRook)))) & fileMask[kingSerialized% 8]);
                long kingToBlocker = ((((blockers & fileMask[kingSerialized % 8]) - 2*defendingKing ) ^ reverse((reverse(blockers & fileMask[kingSerialized % 8]) - 2*reverse(defendingKing)))) & fileMask[kingSerialized% 8]);
                PIN_BOARD |= rookToBlocker & kingToBlocker & blockers;
                PIN_VERTICAL = kingToRookVerticalXray & (rookToBlocker | kingToBlocker) | thisRook;
            } else {
                numChecks++;
                UNSAFE_FORKING |= (fileMask[kingSerialized%8] & ~offendingRooks);
                BLOCK_CHECK = (rookAboveRay | rookBelowRay);
            }
        }

        boolean bishopTopLeft = ((defendingKing-1) & (offendingBishops & antiDiagonalMask[(kingSerialized/8 + 7 - kingSerialized%8)])) != 0;
        boolean bishopTopRight = ((defendingKing - 1) & (offendingBishops & diagonalMask[kingSerialized/8 + kingSerialized%8])) != 0;

        boolean bishopBottomLeft = ((reverse(reverse(defendingKing) - 1) ) &(offendingBishops & diagonalMask[kingSerialized/8 + kingSerialized %8]))!=0;
        boolean bishopBottomRight = ((reverse(reverse(defendingKing) - 1) ) &(offendingBishops & antiDiagonalMask[kingSerialized/8 + 7 - kingSerialized %8]))!=0;

        long bishopBottomLeftRay = 0;
        long bishopBottomRightRay = 0;
        long bishopTopLeftRay = 0;
        long bishopTopRightRay = 0;

        //if there is a bishop on the same diagonal as the king, we calculate all pins for this direction
        //if there is no blocking piece then the king is in check
        if((diagonalMask[kingSerialized/8 + kingSerialized % 8] & offendingBishops) != 0) {
            //if there is a bishop on the same diagonal as the king,
            //draw the kings ray on this diagonal
            long kingToBishopDiagonalXRay = (((offendingBishops & diagonalMask[(kingSerialized / 8 + kingSerialized % 8)]) - 2 * defendingKing) ^ reverse(reverse(offendingBishops & diagonalMask[(kingSerialized / 8 + kingSerialized % 8)]) - 2 * reverse(defendingKing))) & diagonalMask[(kingSerialized / 8 + kingSerialized % 8)];
            if(bishopBottomLeft){bishopBottomLeftRay = kingToBishopDiagonalXRay & downMask;}
            if(bishopTopRight){bishopTopRightRay = kingToBishopDiagonalXRay & upMask;}
            if(((bishopBottomLeftRay | bishopTopRightRay) & blockers &~offendingBishops) != 0){
                long thisBishop = offendingBishops & kingToBishopDiagonalXRay;
                long bishopToBlocker = (((blockers & diagonalMask[(kingSerialized / 8 + kingSerialized % 8)])-2*thisBishop) ^ reverse(reverse(blockers & diagonalMask[(kingSerialized / 8 + kingSerialized % 8)]) - 2*reverse(thisBishop))) & diagonalMask[(kingSerialized / 8 + kingSerialized % 8)];
                long kingToBlocker = (((blockers & diagonalMask[(kingSerialized / 8 + kingSerialized % 8)])-2*defendingKing) ^ reverse(reverse(blockers & diagonalMask[(kingSerialized / 8 + kingSerialized % 8)]) - 2*reverse(defendingKing))) & diagonalMask[(kingSerialized / 8 + kingSerialized % 8)];
                PIN_BOARD |= bishopToBlocker & kingToBlocker & blockers;
                PIN_DIAGONAL = ((kingToBlocker | bishopToBlocker) & kingToBishopDiagonalXRay) | thisBishop;
            }else  {
                numChecks++;
                UNSAFE_FORKING |= (diagonalMask[kingSerialized/8 + kingSerialized % 8] & ~offendingBishops);
                BLOCK_CHECK = bishopBottomLeftRay | bishopTopRightRay;
            }
        }
        if((antiDiagonalMask[(kingSerialized/8 + 7 - kingSerialized%8)] & offendingBishops) != 0) {
            //if there is a bishop on the same diagonal as the king,
            //draw the kings ray on this diagonal
            long kingToBishopAntiDiagonalXRay = (((offendingBishops & antiDiagonalMask[(kingSerialized/8 + 7 - kingSerialized%8)]) - 2 * defendingKing) ^ reverse(reverse(offendingBishops & antiDiagonalMask[(kingSerialized/8 + 7 - kingSerialized%8)]) - 2 * reverse(defendingKing))) & antiDiagonalMask[(kingSerialized/8 + 7 - kingSerialized%8)];
            if(bishopBottomRight){bishopBottomRightRay = kingToBishopAntiDiagonalXRay & downMask;}
            if(bishopTopLeft){bishopTopLeftRay = kingToBishopAntiDiagonalXRay & upMask;}
            if(((bishopBottomRightRay | bishopTopLeftRay) & blockers & ~offendingBishops) != 0){
                long thisBishop = offendingBishops & kingToBishopAntiDiagonalXRay;
                long bishopToBlocker = (((blockers & antiDiagonalMask[(kingSerialized/8 + 7 - kingSerialized%8)])-2*thisBishop) ^ reverse(reverse(blockers & antiDiagonalMask[(kingSerialized/8 + 7 - kingSerialized%8)]) - 2*reverse(thisBishop))) & antiDiagonalMask[(kingSerialized/8 + 7 - kingSerialized%8)];
                long kingToBlocker = (((blockers & antiDiagonalMask[(kingSerialized/8 + 7 - kingSerialized%8)])-2*defendingKing) ^ reverse(reverse(blockers & antiDiagonalMask[(kingSerialized/8 + 7 - kingSerialized%8)]) - 2*reverse(defendingKing))) & antiDiagonalMask[(kingSerialized/8 + 7 - kingSerialized%8)];
                PIN_BOARD |= bishopToBlocker & kingToBlocker & blockers;
                PIN_ANTIDIAGONAL = ((bishopToBlocker | kingToBlocker) & kingToBishopAntiDiagonalXRay) | thisBishop;
            } else {
                numChecks++;
                UNSAFE_FORKING |= (antiDiagonalMask[(kingSerialized/8 + 7 - kingSerialized%8)] & ~offendingBishops);
                BLOCK_CHECK = bishopBottomRightRay | bishopTopLeftRay;
            }
        }

        long knightCheck = (offendingKnights & knightMoves(kingSerialized));
        if(knightCheck != 0){
            BLOCK_CHECK = knightCheck;
            numChecks++;
        }

        for(int i = 0; i < 64; i++){
            if(((offendingKnights>>i) & 1L) == 1L){
                UNSAFE_FORKING |= knightMoves(i);
            }
        }

        if(isWhite){
            long pawnAttackKingSquare = (((bP & ~FILE_1) << 7) & wK);
            if(pawnAttackKingSquare != 0){
                numChecks++;
                UNSAFE_FORKING = pawnAttackKingSquare;
                BLOCK_CHECK = pawnAttackKingSquare >>> 7;
            }
            pawnAttackKingSquare = (((bP & ~FILE_8) << 9) & wK);
            if(pawnAttackKingSquare != 0){
                numChecks++;
                UNSAFE_FORKING = pawnAttackKingSquare;
                BLOCK_CHECK = pawnAttackKingSquare >>> 9;
            }
        } else {
            long pawnAttackKingSquare = (((wP & ~FILE_8) >> 7) & bK);
            if(pawnAttackKingSquare != 0){
                numChecks++;
                UNSAFE_FORKING = pawnAttackKingSquare;
                BLOCK_CHECK = pawnAttackKingSquare << 7;
            }
            pawnAttackKingSquare = (((wP & ~FILE_1) >> 9) & bK);
            if(pawnAttackKingSquare != 0){
                numChecks++;
                UNSAFE_FORKING = pawnAttackKingSquare;
                BLOCK_CHECK = pawnAttackKingSquare << 9;
            }

        }

        return numChecks;

    }

    public BitBoardPosition makeMove(boolean isWhite, String move,byte previousCastling,long bR, long bN, long bB, long bQ, long bK, long bP, long wR, long wN, long wB, long wK, long wQ, long wP)
    {
        long from = 1L << (parseLong("" + move.charAt(0))*8 + parseLong("" + move.charAt(1)));
        long to = 1L << (parseLong("" + move.charAt(3))*8 + parseLong("" + move.charAt(4)));
        byte castlingRightsMask = 0;
        if((to & bR) != 0){if((to & bR & fileMask[0] & rankMask[0]) != 0)castlingRightsMask |= (byte) 0b00001000; if((to & bR & fileMask[7] & rankMask[0]) != 0) castlingRightsMask |= 0b00000100;}
        if((to & wR) != 0){if((to & wR & fileMask[0] & rankMask[7]) != 0) castlingRightsMask |= 0b00000010; if((to & wR & fileMask[7] & rankMask[7]) != 0) castlingRightsMask |= 0b00000001;}
        wN &= ~to;
        wB &= ~to;
        wR &= ~to;
        wP &= ~to;
        wQ &= ~to;
        bN &= ~to;
        bB &= ~to;
        bR &= ~to;
        bQ &= ~to;
        bP &= ~to;

        BitBoardPosition pseudoLegalPosition = null;
        //checking if a rook is moving
        if((wR & from & fileMask[0] & rankMask[7]) != 0)castlingRightsMask |= (byte) 0b00000010;
        if((wR & from & fileMask[7] & rankMask[7]) != 0)castlingRightsMask |= (byte) 0b00000001;

        if((bR & from & fileMask[0] & rankMask[0]) != 0)castlingRightsMask |= (byte) 0b00001000;
        if((bR & from & fileMask[7] & rankMask[0]) != 0)castlingRightsMask |= (byte) 0b00000100;

        switch (move.charAt(2)){
            case 'x', 'n':
                if((from & wR) != 0L){wR &= ~from; wR |= to;}
                if((from & wN) != 0L){wN &= ~from; wN |= to;}
                if((from & wB) !=  0L){wB &= ~from; wB |= to;}
                if((from & wQ) !=  0L){wQ &= ~from; wQ |= to;}
                if((from & wP) !=  0L){wP &= ~from; wP |= to;}
                if((from & bR) !=  0L){bR &= ~from; bR |= to;}
                if((from & bN) !=  0L){bN &= ~from; bN |= to;}
                if((from & bB) !=  0L){bB &= ~from; bB |= to;}
                if((from & bQ) !=  0L){bQ &= ~from; bQ |= to;}
                if((from & bP) !=  0L){bP &= ~from; bP |= to;}
                if((from & bK) !=  0L){bK &= ~from; bK |= to;castlingRightsMask |= 0b00001100;}
                if((from & wK) !=  0L){wK &= ~from; wK |= to;castlingRightsMask |= 0b00000011;}
                pseudoLegalPosition = new BitBoardPosition(bR, bN, bB, bQ, bK, bP, wR, wN, wB, wK, wQ, wP, !isWhite);
                pseudoLegalPosition.setCastling((byte)(previousCastling & ~castlingRightsMask));
                break;
            case 'p':
                if((from & wP) !=  0){wP &= ~from; wP |= to;}
                if((from & bP) !=  0){bP &= ~from; bP |= to;}
                pseudoLegalPosition= new BitBoardPosition(bR, bN, bB, bQ, bK, bP, wR, wN, wB, wK, wQ, wP, !isWhite);
                for(int i = 0; i < 64; i++)if(((to >> i) & 1) == 1) pseudoLegalPosition.setEnPassant((byte) (1 << (i%8)));
                pseudoLegalPosition.setCastling(previousCastling);
                break;
            case 'e':
                long removePawn = isWhite ? to << 8 : to >> 8;

                if((from & wP) !=  0){wP &= ~from; wP |= to; bP &= ~removePawn;}
                if((from & bP) !=  0){bP &= ~from; bP |= to; wP &= ~removePawn;}

                pseudoLegalPosition = new BitBoardPosition(bR, bN, bB, bQ, bK, bP, wR, wN, wB, wK, wQ, wP, !isWhite);
                pseudoLegalPosition.setCastling(previousCastling);
                break;
            case 'l':
                if(isWhite){
                    wK = wK << 2;
                    wR |= WHITEROOK_CASTLEKINGSIDE;
                    castlingRightsMask = 0b00000011;
                } else {
                    bK = bK << 2;
                    bR |= BLACKROOK_CASTLEKINGSIDE;
                    castlingRightsMask = 0b00001100;
                }
                pseudoLegalPosition = new BitBoardPosition(bR, bN, bB, bQ, bK, bP, wR, wN, wB, wK, wQ, wP, !isWhite);
                pseudoLegalPosition.setCastling((byte)(previousCastling & ~castlingRightsMask));
                break;
            case 'c':

                if(isWhite){
                    wK = wK >> 2;
                    wR |= WHITEROOK_CASTLEQUEENSIDE;
                    castlingRightsMask = 0b00000011;
                } else {
                    bK = bK >> 2;
                    bR |= BLACKROOK_CASTLEQUEENSIDE;
                    castlingRightsMask = 0b00001100;
                }

                pseudoLegalPosition= new BitBoardPosition(bR, bN, bB, bQ, bK, bP, wR, wN, wB, wK, wQ, wP, !isWhite);
                pseudoLegalPosition.setCastling((byte) (previousCastling & ~castlingRightsMask));
                break;
            case 'k':
                if(isWhite){
                    wP &= ~from;
                    wN |= to;
                } else {
                    bP &= ~from;
                    bN |= to;
                }
                pseudoLegalPosition = new BitBoardPosition(bR, bN, bB, bQ, bK, bP, wR, wN, wB, wK, wQ, wP, !isWhite);
                pseudoLegalPosition.setCastling((byte) (previousCastling & ~castlingRightsMask));
                break;
            case 'b':
                if(isWhite){
                    wP &= ~from;
                    wB |= to;
                } else {
                    bP &= ~from;
                    bB |= to;
                }
                pseudoLegalPosition = new BitBoardPosition(bR, bN, bB, bQ, bK, bP, wR, wN, wB, wK, wQ, wP, !isWhite);
                pseudoLegalPosition.setCastling((byte) (previousCastling & ~castlingRightsMask));
                break;
            case 'r':
                if(isWhite){
                    wP &= ~from;
                    wR |= to;
                } else {
                    bP &= ~from;
                    bR |= to;
                }
                pseudoLegalPosition = new BitBoardPosition(bR, bN, bB, bQ, bK, bP, wR, wN, wB, wK, wQ, wP, !isWhite);
                pseudoLegalPosition.setCastling((byte) (previousCastling & ~castlingRightsMask));
                break;
            case 'q':
                if(isWhite){
                    wP &= ~from;
                    wQ |= to;
                } else {
                    bP &= ~from;
                    bQ |= to;
                }
                pseudoLegalPosition = new BitBoardPosition(bR, bN, bB, bQ, bK, bP, wR, wN, wB, wK, wQ, wP, !isWhite);
                pseudoLegalPosition.setCastling((byte) (previousCastling & ~castlingRightsMask));
                break;

        }
        return pseudoLegalPosition;
    }
    
    public String blackPawnMoves(long pawnBoard, long opposingPawnBoard,byte enPassant)
    {
        String pawnMoves = "";
        long PAWN_MOVES = 0L;

//        moves forward by one
        long preForwardBoard = pawnBoard;
        long possiblePinnedPawn = PIN_BOARD & pawnBoard & (PIN_HORIZONTAL | PIN_DIAGONAL | PIN_ANTIDIAGONAL);
        if((possiblePinnedPawn) != 0){
            //if pinned diagonally or horizontally, cannot move forward
            preForwardBoard = pawnBoard &~possiblePinnedPawn;
        }
        long bitOneForward = (preForwardBoard << 8) & ~OCCUPIED_TILES & BLOCK_CHECK;
        long promotions = bitOneForward & rankMask[7];
        bitOneForward &= ~rankMask[7];
        //diagonally or horiontally pinned pawns cannot move forward
        for(int i = 0; i < 64; i++){
            if(((bitOneForward >> i) & 1) == 1){
                pawnMoves += "" + (i/8 - 1) + (i%8) + "n" + (i/8) + (i%8); //n denoting normal move
            }
            if(((promotions>>i) & 1) == 1){
                pawnMoves += "" + (i/8 - 1) + (i%8) + "k" + (i/8) + (i%8);
                pawnMoves += "" + (i/8 - 1) + (i%8) + "b" + (i/8) + (i%8);
                pawnMoves += "" + (i/8 - 1) + (i%8) + "r" + (i/8) + (i%8);
                pawnMoves += "" + (i/8 - 1) + (i%8) + "q" + (i/8) + (i%8);
            }
        }


        // moves forward by two
        long bitTwoForward = ((preForwardBoard << 16) & RANK_5 & (~OCCUPIED_TILES << 8) & ~OCCUPIED_TILES &BLOCK_CHECK);
        PAWN_MOVES = PAWN_MOVES | bitTwoForward;
        for(int i = 0; i < 64; i++){if(((PAWN_MOVES >> i) & 1) == 1){pawnMoves += "" + (i/8 - 2) + (i%8)+ "p" + (i/8) + (i%8);}}

        //removing any possibly pinned pawns pinned horizontally
        long tempTakesLeft = pawnBoard;
        long possibleAntidiagPinnedPawn = (tempTakesLeft & PIN_BOARD & (PIN_ANTIDIAGONAL | PIN_HORIZONTAL | PIN_VERTICAL));
        if((possibleAntidiagPinnedPawn)!= 0)tempTakesLeft &= ~possibleAntidiagPinnedPawn;

        //standard takes behaviour
        long takesLeft = ((tempTakesLeft & ~FILE_1) << 7) & WHITE_OCCUPANCY;
        BLACK_ATTACKS |= ((pawnBoard & ~FILE_1) << 7);
        long promotionTakesLeft = takesLeft & rankMask[7] & BLOCK_CHECK;
        takesLeft = takesLeft & ~rankMask[7] & BLOCK_CHECK;

//        PAWN_MOVES = PAWN_MOVES | takesLeft;
        for(int i = 0; i < 64; i++){
            if(((takesLeft >> i) & 1) == 1){
                pawnMoves += ("" + (i/8 - 1) + (i%8 + 1)+ "x" + (i/8) + (i%8));
            }
            if(((promotionTakesLeft >> i) & 1) == 1){
                pawnMoves += ("" + (i/8 - 1) + (i%8 + 1)+ "k" + (i/8) + (i%8));
                pawnMoves += ("" + (i/8 - 1) + (i%8 + 1)+ "b" + (i/8) + (i%8));
                pawnMoves += ("" + (i/8 - 1) + (i%8 + 1)+ "r" + (i/8) + (i%8));
                pawnMoves += ("" + (i/8 - 1) + (i%8 + 1)+ "q" + (i/8) + (i%8));
            }
        }
        //removing antidiagonally pinned piece
        long tempTakesRight = pawnBoard;
        long possibleDiagPinnedPawn = (tempTakesRight & PIN_BOARD & (PIN_DIAGONAL | PIN_HORIZONTAL | PIN_VERTICAL));
        if((possibleDiagPinnedPawn) != 0)tempTakesRight &= ~possibleDiagPinnedPawn;
        long takesRight = ((tempTakesRight & ~FILE_8) << 9) & WHITE_OCCUPANCY;
        BLACK_ATTACKS |= ((pawnBoard & ~FILE_8) << 9);
        long promotionTakesRight = takesRight & rankMask[7] & BLOCK_CHECK;
        takesRight = takesRight & ~rankMask[7] & BLOCK_CHECK;

//        PAWN_MOVES = PAWN_MOVES | takesRight;
        for(int i = 0; i < 64; i++){
            if(((takesRight >> i) & 1) == 1){
                pawnMoves += ("" + (i/8 - 1) + ((i-1)%8)+ "x" + (i/8) + (i%8));
            }
            if(((promotionTakesRight >> i) & 1) == 1){
                pawnMoves += ("" + (i/8 - 1) + ((i-1)%8)+ "k" + (i/8) + (i%8));
                pawnMoves += ("" + (i/8 - 1) + ((i-1)%8)+ "b" + (i/8) + (i%8));
                pawnMoves += ("" + (i/8 - 1) + ((i-1)%8)+ "r" + (i/8) + (i%8));
                pawnMoves += ("" + (i/8 - 1) + ((i-1)%8)+ "q" + (i/8) + (i%8));
            }
        }
        // en passant
        // i here should represent the file wherein the opposing pawn pushed two
        long enPassantLeft = 0L;
        long enPassantRight = 0L;
        for(int i = 0; i < 8; i++){
            if((((enPassant >> i) &1) == 1)){
                long tempEnPassant = pawnBoard;
                //to boards for the en passsanting pawns.
                //prevents horizontally pinned en passant
                if((PIN_HORIZONTAL != 0) && ((PIN_HORIZONTAL& OCCUPIED_TILES & ~((tempEnPassant|(rankMask[4] & fileMask[i]))&rankMask[4])) == 0)) break;
                //checks that the en passant victim is not pinned
                if((rankMask[4] & fileMask[i] & PIN_BOARD &(PIN_DIAGONAL | PIN_ANTIDIAGONAL)) !=0) break;
                //cant take left on file1, cant take right on file8, fileMask is en passant enabled file
                //piined pawn can take left if pinned antidiagonally, and right if pinned diagonally,no en passant if the black pawn is pinned(not vertically)
                enPassantLeft |= ((tempEnPassant & ~FILE_1 & rankMask[4]) << 7) & fileMask[i] & (BLOCK_CHECK << 8);
                enPassantRight |= ((tempEnPassant & ~FILE_8 & rankMask[4]) << 9) & fileMask[i] & (BLOCK_CHECK << 8);

                if((PIN_BOARD & PIN_ANTIDIAGONAL & (enPassantLeft>>7)) != 0) enPassantLeft &= PIN_ANTIDIAGONAL;
                if((PIN_BOARD & PIN_DIAGONAL & (enPassantRight >> 9)) != 0) enPassantRight &= PIN_DIAGONAL;
                //a vertically pinned pawn may not en passant
                if((PIN_BOARD & PIN_VERTICAL & ((enPassantRight >> 9) | (enPassantLeft>>7))) != 0){enPassantRight =0L; enPassantLeft = 0L;}
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
        long preForwardBoard = pawnBoard;
        long possiblePinnedPawn = PIN_BOARD & pawnBoard & (PIN_HORIZONTAL | PIN_DIAGONAL | PIN_ANTIDIAGONAL);
        if((possiblePinnedPawn) != 0){
            //if pinned diagonally or horizontally, cannot move forward
            preForwardBoard = pawnBoard &~possiblePinnedPawn;
        }

        //moves forward by one
        long bitOneForward = (preForwardBoard >>> 8) & ~OCCUPIED_TILES;
        long promotions = bitOneForward & rankMask[0] & BLOCK_CHECK;
        bitOneForward &= ~rankMask[0] &BLOCK_CHECK;
        for(int i = 0; i < 64; i++){
            if(((bitOneForward >> i) & 1L) == 1L){
                pawnMoves += "" + (i/8 + 1) + (i%8) + "n" +(i/8) + (i%8);
            }
            if(((promotions >> i) & 1) ==1){
                pawnMoves += "" + (i/8 + 1) + (i%8) + "b" +(i/8) + (i%8);
                pawnMoves += "" + (i/8 + 1) + (i%8) + "k" +(i/8) + (i%8);
                pawnMoves += "" + (i/8 + 1) + (i%8) + "r" +(i/8) + (i%8);
                pawnMoves += "" + (i/8 + 1) + (i%8) + "q" +(i/8) + (i%8);
            }
        }

        long bitTwoForward = ((preForwardBoard >>> 16) & RANK_4) & (~OCCUPIED_TILES >> 8) & ~OCCUPIED_TILES & BLOCK_CHECK;
        PAWN_MOVES = PAWN_MOVES | bitTwoForward;
        for(int i = 0; i < 64; i++){if(((PAWN_MOVES >> i) & 1) == 1){pawnMoves += "" + (i/8 + 2) + (i%8)+ "p" + (i/8) + (i%8);}}

        long tempTakesRight = pawnBoard;
//        long possibleAntidiagPinnedPawn = (tempTakesRight & PIN_BOARD & (PIN_ANTIDIAGONAL | PIN_HORIZONTAL | PIN_VERTICAL));
        long possibleDiagPinnedPawn = (tempTakesRight & PIN_BOARD & (PIN_DIAGONAL | PIN_HORIZONTAL | PIN_VERTICAL));
        if((possibleDiagPinnedPawn)!= 0)tempTakesRight &= ~possibleDiagPinnedPawn;
        long takesRight = ((tempTakesRight & ~FILE_1) >> 9) & BLACK_OCCUPANCY;
        WHITE_ATTACKS |= ((pawnBoard & ~FILE_1) >> 9);
        long takesRightPromotions = takesRight & rankMask[0] & BLOCK_CHECK;
        takesRight &= ~rankMask[0];
        takesRight &= BLOCK_CHECK;
//        PAWN_MOVES = PAWN_MOVES | takesRight;
        for(int i = 0; i < 64; i++){
            if(((takesRight >> i) & 1) == 1){
                pawnMoves += ("" + (i/8 + 1) + (i%8 + 1) +"x"+ (i/8) + (i%8));
            }
            if(((takesRightPromotions>>i)&1)==1){
                pawnMoves += ("" + (i/8 + 1) + (i%8 + 1) +"k"+ (i/8) + (i%8));
                pawnMoves += ("" + (i/8 + 1) + (i%8 + 1) +"b"+ (i/8) + (i%8));
                pawnMoves += ("" + (i/8 + 1) + (i%8 + 1) +"r"+ (i/8) + (i%8));
                pawnMoves += ("" + (i/8 + 1) + (i%8 + 1) +"q"+ (i/8) + (i%8));
            }
        }
        long tempTakesLeft = pawnBoard;
//        long possibleDiagPinnedPawn = (tempTakesLeft & PIN_BOARD & (PIN_DIAGONAL | PIN_HORIZONTAL | PIN_VERTICAL));
        long possibleAntidiagPinnedPawn = (tempTakesLeft & PIN_BOARD & (PIN_ANTIDIAGONAL | PIN_HORIZONTAL | PIN_VERTICAL));
        if((possibleAntidiagPinnedPawn) != 0)tempTakesLeft &= ~possibleAntidiagPinnedPawn;
        long takesLeft = ((tempTakesLeft & ~FILE_8) >> 7) & BLACK_OCCUPANCY;
        WHITE_ATTACKS |= ((pawnBoard & ~FILE_8) >> 7);
        long takesLeftPromotions = takesLeft & rankMask[0] & BLOCK_CHECK;
        takesLeft &= ~rankMask[0];
        takesLeft &= BLOCK_CHECK;
//        PAWN_MOVES = PAWN_MOVES | takesLeft;
        for(int i = 0; i < 64; i++){
            if(((takesLeft >> i) & 1) == 1){
                pawnMoves += ("" + (i/8 + 1) + ((i-1)%8)+ "x" + (i/8) + (i%8));
            }
            if(((takesLeftPromotions >> i)&1)==1){
                pawnMoves += ("" + (i/8 + 1) + ((i-1)%8)+ "k" + (i/8) + (i%8));
                pawnMoves += ("" + (i/8 + 1) + ((i-1)%8)+ "b" + (i/8) + (i%8));
                pawnMoves += ("" + (i/8 + 1) + ((i-1)%8)+ "r" + (i/8) + (i%8));
                pawnMoves += ("" + (i/8 + 1) + ((i-1)%8)+ "q" + (i/8) + (i%8));
            }
        }

        long enPassantLeft = 0L;
        long enPassantRight = 0L;
        for(int i = 0; i < 8; i++){
            if((((enPassant >> i) &1) == 1)){
                long tempEnPassant = pawnBoard;
                //to boards for the en passsanting pawns.
                //prevents horizontally pinned en passant
                //meant to check whether there is another piece on the line between the rook and king in an enpassant scenario
                if((PIN_HORIZONTAL != 0) && (((PIN_HORIZONTAL & OCCUPIED_TILES & ~((tempEnPassant | (rankMask[3] & fileMask[i])& rankMask[3]))) ) == 0)) break;

                //checks that the en passant victim is not pinned
                if((rankMask[3] & fileMask[i] & PIN_BOARD &(PIN_DIAGONAL | PIN_ANTIDIAGONAL)) !=0) break;
                //cant take left on file1, cant take right on file8, fileMask is en passant enabled file
                //piined pawn can take left if pinned antidiagonally, and right if pinned diagonally,no en passant if the black pawn is pinned(not vertically)

                enPassantLeft |= ((pawnBoard & ~FILE_1 & rankMask[3]) >> 9) & fileMask[i] & (BLOCK_CHECK >> 8);
                enPassantRight |= ((pawnBoard & ~FILE_8 & rankMask[3]) >> 7) & fileMask[i] & (BLOCK_CHECK >> 8);



                //if there exists a diagonally pinned pawn, an en passant move must intersect this pin to be valid
                if((PIN_BOARD & PIN_ANTIDIAGONAL & (enPassantRight << 7)) != 0) enPassantRight &= PIN_ANTIDIAGONAL;
                if((PIN_BOARD & PIN_DIAGONAL & (enPassantLeft << 9)) != 0) enPassantLeft &= PIN_DIAGONAL;
                //if the pawn in question is pinned vertically, an en passant is illegal
                if((PIN_BOARD & PIN_VERTICAL & ((enPassantRight << 7) | (enPassantLeft << 9))) != 0){enPassantRight =0L; enPassantLeft = 0L;}

            }
        }
        for (int i = 0; i < 64; i++){

            if(((enPassantLeft >>> i) & 1L) == 1L){pawnMoves += "" + "3" + (i%8 + 1) + "e" +(i/8) + (i%8);}
            if(((enPassantRight >>> i) & 1L) == 1L){pawnMoves += "" + "3" + ((i-1)%8) + "e" +(i/8) + (i%8);}
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
                    theseMoves &= BLOCK_CHECK;
                    if((1L<<i & PIN_BOARD) != 0){
                        if((rookBoard & PIN_BOARD & PIN_HORIZONTAL) != 0) theseMoves &= PIN_HORIZONTAL;
                        if((rookBoard & PIN_BOARD & PIN_VERTICAL) != 0) theseMoves &= PIN_VERTICAL;
                        if((rookBoard & PIN_BOARD & PIN_ANTIDIAGONAL) != 0) theseMoves &= PIN_ANTIDIAGONAL;
                        if((rookBoard & PIN_BOARD & PIN_DIAGONAL) != 0) theseMoves &= PIN_DIAGONAL;
                    }
                    theseMoves &= ~WHITE_OCCUPANCY;
                    takes = theseMoves & BLACK_OCCUPANCY;
                    quiets = theseMoves & ~BLACK_OCCUPANCY;
                }
                else {
                    BLACK_ATTACKS |= theseMoves;
                    theseMoves &= BLOCK_CHECK;
                    if((1L<<i  & PIN_BOARD) != 0){
                        if((rookBoard & PIN_BOARD & PIN_HORIZONTAL) != 0) theseMoves &= PIN_HORIZONTAL;
                        if((rookBoard & PIN_BOARD & PIN_VERTICAL) != 0) theseMoves &= PIN_VERTICAL;
                        if((rookBoard & PIN_BOARD & PIN_ANTIDIAGONAL) != 0) theseMoves &= PIN_ANTIDIAGONAL;
                        if((rookBoard & PIN_BOARD & PIN_DIAGONAL) != 0) theseMoves &= PIN_DIAGONAL;
                    }
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
                    theseMoves &= BLOCK_CHECK;
                    if((1L<<i & PIN_BOARD) != 0){
                        if((bishBoard & PIN_BOARD & PIN_HORIZONTAL) != 0) theseMoves &= PIN_HORIZONTAL;
                        if((bishBoard & PIN_BOARD & PIN_VERTICAL) != 0) theseMoves &= PIN_VERTICAL;
                        if((bishBoard & PIN_BOARD & PIN_ANTIDIAGONAL) != 0)theseMoves &= PIN_ANTIDIAGONAL;
                        if((bishBoard & PIN_BOARD & PIN_DIAGONAL) != 0) theseMoves &= PIN_DIAGONAL;
                    }
                    theseMoves &= ~WHITE_OCCUPANCY;
                    takes = theseMoves & BLACK_OCCUPANCY;
                    quiets = theseMoves & ~BLACK_OCCUPANCY;
                }
                else {
                    BLACK_ATTACKS |= theseMoves;
                    theseMoves &= BLOCK_CHECK;
                    if((1L<<i  & PIN_BOARD) != 0){
                        if((bishBoard & PIN_BOARD & PIN_HORIZONTAL) != 0) theseMoves &= PIN_HORIZONTAL;
                        if((bishBoard & PIN_BOARD & PIN_VERTICAL) != 0) theseMoves &= PIN_VERTICAL;
                        if((bishBoard & PIN_BOARD & PIN_ANTIDIAGONAL) != 0) theseMoves &= PIN_ANTIDIAGONAL;
                        if((bishBoard & PIN_BOARD & PIN_DIAGONAL) != 0) theseMoves &= PIN_DIAGONAL;
                    }
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
        if((PIN_BOARD & knightBoard) != 0) knightBoard &= ~PIN_BOARD;
        for(int i = 0; i < 64; i++){
            if(((knightBoard >> i) & 1) == 1){
                String startLoc = "" + i/8 + i%8;
                long theseMoves = knightMoves(i);
                long takes;
                long quiets;
                if(isWhite) {
                    WHITE_ATTACKS |= theseMoves;
                    theseMoves &= BLOCK_CHECK;
                    theseMoves &= ~WHITE_OCCUPANCY;
                    takes = theseMoves & BLACK_OCCUPANCY;
                    quiets = theseMoves & ~BLACK_OCCUPANCY;
                }
                else {
                    BLACK_ATTACKS |= theseMoves;
                    theseMoves &= BLOCK_CHECK;
                    theseMoves &= ~BLACK_OCCUPANCY; //remove any potential friendly fires
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
                    theseMoves &= ~UNSAFE_FORKING;// can not move to a position being x-rayed
                    takes = theseMoves & BLACK_OCCUPANCY;
                    quiets = theseMoves & ~BLACK_OCCUPANCY;
                }
                else {
                    BLACK_ATTACKS |= theseMoves; //any locations defended or attacked by a black piece
                    theseMoves &= ~BLACK_OCCUPANCY; //remove any potential friendly fires
                    theseMoves &= ~WHITE_ATTACKS;
                    theseMoves &= ~UNSAFE_FORKING;// can not move to a position being x-rayed
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

        long noWeWe = (bitPieceLoc >> 10) & ~fileMask[6] & ~fileMask[7] & ~rankMask[7];
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
                long blockers = (CASTLE_BOTTOMRIGHT & (UNSAFE_FORKING | BLACK_ATTACKS)) | (OCCUPIED_TILES & CASTLECLEAR_BOTTOMRIGHT);
                if(blockers == 0)
                    viableMoves += "74l77";
            }
            if(((castling >>> 1) & 1L) == 1){
                //white queenside castles
                long blockers = (CASTLE_BOTTOMLEFT & (UNSAFE_FORKING | BLACK_ATTACKS))|(OCCUPIED_TILES & CASTLECLEAR_BOTTOMLEFT);
                if(blockers == 0)
                    viableMoves += "74c70";
            }
        } else {
            if(((castling >>> 2) & 1L) == 1){
                //black castling kingside(topright)
                long blockers = (CASTLE_TOPRIGHT & (UNSAFE_FORKING | WHITE_ATTACKS))|(OCCUPIED_TILES & CASTLECLEAR_TOPRIGHT);
                if(blockers == 0)
                    viableMoves += "04l07";
            }
            if(((castling >>> 3) & 1 ) == 1){
                //black castles queenside
                long blockers = (CASTLE_TOPLEFT & (UNSAFE_FORKING | WHITE_ATTACKS))|(OCCUPIED_TILES & CASTLECLEAR_TOPLEFT);
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
        System.out.println("");
    }



}
