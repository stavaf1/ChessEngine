package com.engine.chess;

import java.util.HashMap;

public class Position {
    private char[][] position;

    private byte enPassant = 0b00000000;

    private byte castlingRights = 0b00001111;

    private boolean whiteToMove = true;
    private long bR = 0L;
    private long bN = 0L;
    private long bB = 0L;
    private long bK = 0L;
    private long bQ = 0L;
    private long bP = 0L;
    private long wR = 0L;
    private long wN = 0L;
    private long wB = 0L;
    private long wK = 0L;
    private long wQ = 0L;
    private long wP = 0L;
    public Position(){
        initialise();
    }

    public Position(String fen){
        initialise(fen);
    }
    public void initialise(){
        position =
                new char[][]{
                        {'r', ' ', ' ', ' ', 'k', ' ', ' ', 'r'},
                        {' ', 'p', 'p', 'p', ' ', 'p', 'p', ' '},
                        {' ', ' ', ' ', ' ', ' ', ' ', ' ', 'q'},
                        {' ', ' ', ' ', ' ', ' ', ' ', ' ', 'Q'},
                        {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
                        {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
                        {' ', 'P', 'P', 'P', ' ', 'P', 'P', ' '},
                        {'R', ' ', ' ', ' ', 'K', ' ', ' ', 'R'},
                };
    }

    /**
     * fen interpreter, only accepts valid fen strings at the moment, otherwise wierd things happen
     * @param fenString
     */
    public void initialise(String fenString){
        int i = 0;
        int j = 0;
        String validPieceCharacters = "rnbqkpRNBQKP";

        position = new char[][]{
                {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '}, {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '}, {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '}, {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '}, {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '}, {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '}, {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '}, {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
        };

        //parse the first segement of the fen string, initialising all pieces
        String[] fenSegments = fenString.split(" ");
        for(char piece: fenSegments[0].toCharArray()){
            if(validPieceCharacters.contains("" + piece)){
                position[i][j] = piece;
                j++;
            } else if (piece == '/'){
                i++;
                j=0;
            } else {
                try {
                    int whiteSpaceCount = Integer.parseInt("" + piece);
                    for (int k = 0; k < whiteSpaceCount; k++) {
                        position[i][j] = ' ';
                        j++;
                    }
                } catch (Exception e) {
                    System.out.println("unrecognised character in fen string");
                }
            }
        }
        //next fen segment is white or blacks turn
        whiteToMove = fenSegments[1].equals("w") ? true : false;

        //now castling rights
        if(fenSegments[2].length() == 4) castlingRights = 0b00001111;
        else if (fenSegments[2].equals("-")) castlingRights = 0b00000000;
        else{
            byte castleBuffer = (byte) 0b00000000;
            for(char fenCastleKey: fenSegments[2].toCharArray()){
                castleBuffer |= fenCastlingLookup.get(fenCastleKey);
            }
            castlingRights = castleBuffer;
        }

        //finally any potential en passants
        if(!fenSegments[3].equals("-")){
            enPassant = fenEnPassantLookup.get(fenSegments[3].charAt(0));
            System.out.println(Long.toBinaryString(enPassant));
        }
        initBitboards();

    }

//    {'r', 'n', 'b', 'q', 'k', 'b', 'n', 'r'},
//    {'p', 'p', 'p', 'p', 'p', 'p', 'p', 'p'},
//    {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
//    {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
//    {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
//    {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
//    {'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P'},
//    {'R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R'},

//    {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
//    {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
//    {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
//    {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
//    {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
//    {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
//    {' ', ' ', ' ', ' ', 'P', ' ', ' ', ' '},
//    {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},


    public void initBitboards()
    {
        clearBitboards();
        for(int i = 0; i < 64; i++)
        {
            String bitString = "0000000000000000000000000000000000000000000000000000000000000000";
//            MoveGenerator.printBitBoard(bitString);
            bitString = bitString.substring(i+1) + "1" + bitString.substring(0, i);

            switch (position[i / 8][i % 8]) {
                case 'r' -> bR |= stringToBinary(bitString);
                case 'n' -> bN |= stringToBinary(bitString);
                case 'b' -> bB |= stringToBinary(bitString);
                case 'k' -> bK |= stringToBinary(bitString);
                case 'q' -> bQ |= stringToBinary(bitString);
                case 'p' -> bP |= stringToBinary(bitString);
                case 'R' -> wR |= stringToBinary(bitString);
                case 'N' -> wN |= stringToBinary(bitString);
                case 'B' -> wB |= stringToBinary(bitString);
                case 'Q' -> wQ |= stringToBinary(bitString);
                case 'K' -> wK |= stringToBinary(bitString);
                case 'P' -> wP |= stringToBinary(bitString);
            }
        }
//        System.out.println(wP);
    }

    public long stringToBinary(String bitboard)
    {
        if(bitboard.charAt(0) == '0'){
            return Long.parseLong(bitboard, 2); //converts to base 2
        } else {
            return Long.parseLong("1" + bitboard.substring(2),2) * 2;
        }
    }


    /**
     * the controller class reads the current board position from the charArray position in this class
     * this method converts the current bitboard representations back into position
     */
    public void bitBoardToPosition(){
        for(int i = 0; i < 64; i++){
            if(((getbR() >> i) & 1) == 1) {position[i/8][i%8] = 'r';}
            if(((getbN() >> i) & 1) == 1) {position[i/8][i%8] = 'n';}
            if(((getbB() >> i) & 1) == 1) {position[i/8][i%8] = 'b';}
            if(((getbK() >> i) & 1) == 1) {position[i/8][i%8] = 'k';}
            if(((getbQ() >> i) & 1) == 1) {position[i/8][i%8] = 'q';}
            if(((getbP() >> i) & 1) == 1) {position[i/8][i%8] = 'p';}
            if(((getwR() >> i) & 1) == 1) {position[i/8][i%8] = 'R';}
            if(((getwN() >> i) & 1) == 1) {position[i/8][i%8] = 'N';}
            if(((getwB() >> i) & 1) == 1) {position[i/8][i%8] = 'B';}
            if(((getwK() >> i) & 1) == 1) {position[i/8][i%8] = 'K';}
            if(((getwQ() >> i) & 1) == 1) {position[i/8][i%8] = 'Q';}
            if(((getwP() >> i) & 1) == 1) {position[i/8][i%8] = 'P';}
        }
    }
    public void bitBoardToPosition(BitBoardPosition newPosition){
        clearBitboards();
        clearPosition();
        for(int i = 0; i < 64; i++){
            if(((newPosition.getbR() >> i) & 1) == 1) {position[i/8][i%8] = 'r';}
            if(((newPosition.getbN() >> i) & 1) == 1) {position[i/8][i%8] = 'n';}
            if(((newPosition.getbB() >> i) & 1) == 1) {position[i/8][i%8] = 'b';}
            if(((newPosition.getbK() >> i) & 1) == 1) {position[i/8][i%8] = 'k';}
            if(((newPosition.getbQ() >> i) & 1) == 1) {position[i/8][i%8] = 'q';}
            if(((newPosition.getbP() >> i) & 1) == 1) {position[i/8][i%8] = 'p';}
            if(((newPosition.getwR() >> i) & 1) == 1) {position[i/8][i%8] = 'R';}
            if(((newPosition.getwN() >> i) & 1) == 1) {position[i/8][i%8] = 'N';}
            if(((newPosition.getwB() >> i) & 1) == 1) {position[i/8][i%8] = 'B';}
            if(((newPosition.getwK() >> i) & 1) == 1) {position[i/8][i%8] = 'K';}
            if(((newPosition.getwQ() >> i) & 1) == 1) {position[i/8][i%8] = 'Q';}
            if(((newPosition.getwP() >> i) & 1) == 1) {position[i/8][i%8] = 'P';}
            enPassant = newPosition.getEnPassant();
            castlingRights = newPosition.getCastling();
            whiteToMove = newPosition.getWhiteToMove();
        }
        initBitboards();
    }





    public void clearPosition(){
        for(int i =0; i<64; i++){
            position[i/8][i%8] = ' ';
        }
    }



    public char getPieceAt(Integer tileId)
    {
        return position[tileId / 8][tileId%8];
    }

    public void printBoard(){
        for(int i= 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
                System.out.print(position[i][j] + ", ");
            }
            System.out.println();
        }
    }
    public void clearBitboards(){
        bR = 0L;
        bN = 0L;
        bB = 0L;
        bK = 0L;
        bQ = 0L;
        bP = 0L;
        wR = 0L;
        wN = 0L;
        wB = 0L;
        wK = 0L;
        wQ = 0L;
        wP = 0L;
    }
    public void clear(){
        for(int i= 0; i < 8; i++)
            for(int j = 0; j < 8; j++)
                position[i][j] = ' ';

        castlingRights = 0b00001111;
    }




    /**
     * getter methods for all bitboards, so they can be extracted and used in later calculations
     *
     * @return
     */
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

    public byte getEnPassant(){return enPassant;}

    public byte getCastlingRights(){return castlingRights;}

    public boolean getWhiteToMove(){return whiteToMove;}

    public static HashMap<Character, Byte> fenCastlingLookup = new HashMap<>(){{
        put('K', (byte) 0b00000001);
        put('Q', (byte) 0b00000010);
        put('k', (byte) 0b00000100);
        put('K', (byte) 0b00001000);
    }};


    /**
     * en passant convention in this program is reversed because im an idiot
     */
    public static HashMap<Character, Byte> fenEnPassantLookup = new HashMap<>(){{
        put('a', (byte) 0b00000001);
        put('b', (byte) 0b00000010);
        put('c', (byte) 0b00000100);
        put('d', (byte) 0b00001000);
        put('e', (byte) 0b00010000);
        put('f', (byte) 0b00100000);
        put('g', (byte) 0b01000000);
        put('h', (byte) 0b10000000);
    }};

}
