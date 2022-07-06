package Test;

import com.engine.chess.BitBoardPosition;
import com.engine.chess.Position;
import com.engine.chess.PseudoLegalMoveGenerator;
import com.engine.chess.PseudoLegalPerft;
import org.junit.jupiter.api.Test;

import java.sql.SQLOutput;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PseudoLegalMoveGeneratorTest {


    PseudoLegalMoveGenerator generator = new PseudoLegalMoveGenerator();

    @Test
    void moveInterpreterTest(){
        //checkign that reversing the integer actually works
        int move = generator.moveInterpereter(16, 1L << 63, 3, 0);

        assertEquals( 63, (move & 0b00000000000000000000000011111111)); // where the to is stored 8bit
        assertEquals( 4, Integer.reverse(move & 0b11111111000000000000000000000000)); // where from is stored 8bit
        assertEquals(3, ((move >>> 8) & 0b00000000000000000000000000001111)); //agressorId 4bit
        assertEquals(0, ((move >> 16) & 0b00000000000000000000000000001111)); //moveType 4bit
    }


    @Test
    void pawnMoveTest()
    {
        Position initPosition = new Position("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        BitBoardPosition newBitboards = new BitBoardPosition(initPosition.getbR(), initPosition.getbN(), initPosition.getbB(), initPosition.getbQ(), initPosition.getbK(), initPosition.getbP(), initPosition.getwR(), initPosition.getwN(), initPosition.getwB(), initPosition.getwK(), initPosition.getwQ(), initPosition.getwP(), initPosition.getWhiteToMove());
        generator.getPseudoMoves(newBitboards);

        LinkedList<Integer> pawnmoves = generator.whitePawnMoves(newBitboards.getwP(), (byte)0);
        for(int move: pawnmoves){
//            System.out.println((Integer.reverse(move & 0b11111111000000000000000000000000))+ "->" + (move & 0b00000000000000000000000011111111));
        }
        assertEquals(16, pawnmoves.size());

        initPosition.initialise("rnbqkbnr/pp1p2pp/5p2/2pPp3/5P2/8/PPP1P1PP/RNBQKBNR w KQkq c6 0 4");
        BitBoardPosition twoBitboard = new BitBoardPosition(initPosition.getbR(), initPosition.getbN(), initPosition.getbB(), initPosition.getbQ(), initPosition.getbK(), initPosition.getbP(), initPosition.getwR(), initPosition.getwN(), initPosition.getwB(), initPosition.getwK(), initPosition.getwQ(), initPosition.getwP(), initPosition.getWhiteToMove());
        twoBitboard.setEnPassant(initPosition.getEnPassant());
        twoBitboard.setCastling(initPosition.getCastlingRights());
        generator.getPseudoMoves(twoBitboard);
        LinkedList<Integer> pawntwomoves = generator.whitePawnMoves(twoBitboard.getwP(), twoBitboard.getEnPassant());
        for(int move: pawntwomoves){
//            System.out.println((Integer.reverse(move & 0b11111111000000000000000000000000))+ "->" + (move & 0b00000000000000000000000011111111));
        }

        initPosition.initialise("rnbqkbnr/p1p1ppp1/8/7p/1pPp4/6N1/PP1PPPPP/RNBQKB1R b KQkq c3 0 7");
        BitBoardPosition black = new BitBoardPosition(initPosition.getbR(), initPosition.getbN(), initPosition.getbB(), initPosition.getbQ(), initPosition.getbK(), initPosition.getbP(), initPosition.getwR(), initPosition.getwN(), initPosition.getwB(), initPosition.getwK(), initPosition.getwQ(), initPosition.getwP(), initPosition.getWhiteToMove());
        black.setEnPassant(initPosition.getEnPassant());
        black.setCastling(initPosition.getCastlingRights());
        generator.getPseudoMoves(black);
        LinkedList<Integer> bpMoves = generator.blackPawnMoves(black.getbP(), black.getEnPassant());
        for(int move: bpMoves){
            System.out.println((Integer.reverse(move & 0b11111111000000000000000000000000))+ "->" + (move & 0b00000000000000000000000011111111));
        }
    }

    @Test
    void slidingPieceTest(){
        Position initPosition = new Position();

        initPosition.initialise("r3k2r/pppqppbp/2npbnp1/8/3B4/2NP1NP1/PPPQPPBP/R3K2R b KQkq - 5 8");
        BitBoardPosition black = new BitBoardPosition(initPosition.getbR(), initPosition.getbN(), initPosition.getbB(), initPosition.getbQ(), initPosition.getbK(), initPosition.getbP(), initPosition.getwR(), initPosition.getwN(), initPosition.getwB(), initPosition.getwK(), initPosition.getwQ(), initPosition.getwP(), initPosition.getWhiteToMove());
        black.setEnPassant(initPosition.getEnPassant());
        black.setCastling(initPosition.getCastlingRights());
        List<Integer> allmoves = generator.getPseudoMoves(black);


        System.out.println(generator.castles(initPosition.getCastlingRights(), initPosition.getWhiteToMove()).size());

    }

    @Test
    void printBitboard(){
        generator.printBitBoard(1L | 1L << 3);
    }



}