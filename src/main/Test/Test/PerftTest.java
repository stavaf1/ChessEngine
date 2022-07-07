package Test;

import com.engine.chess.PseudoLegalPerft;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PerftTest {
    PseudoLegalPerft test = new PseudoLegalPerft();
    @Test
    void enPassantCorrectGeneration(){
        assertEquals(191, test.perftEntry("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", 2));
        assertEquals(2812, test.perftEntry("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", 3));
        assertEquals(43238, test.perftEntry("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", 4));
        assertEquals(674624, test.perftEntry("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", 5));
        assertEquals(11030083, test.perftEntry("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", 6));
        System.out.println("En Passant depth six pass");
    }

    @Test
    void kiwipeteDoubleCheckTest(){
        assertEquals(2039, test.perftEntry("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - ", 2));
        assertEquals(97862, test.perftEntry("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - ", 3));
        assertEquals(4085603, test.perftEntry("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - ", 4));
        assertEquals(193690690, test.perftEntry("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - ", 5));
        System.out.println("kiwipete depth 5 passed");
    }

    @Test
    void mirrorPerft(){
        assertEquals(264, test.perftEntry("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1", 2));
        assertEquals(9467, test.perftEntry("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1", 3));
        assertEquals(422333, test.perftEntry("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1", 4));
        assertEquals(15833292, test.perftEntry("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1", 5));
        System.out.println("pass mirrortest white");

        assertEquals(264, test.perftEntry("r2q1rk1/pP1p2pp/Q4n2/bbp1p3/Np6/1B3NBn/pPPP1PPP/R3K2R b KQ - 0 1 ", 2));
        assertEquals(9467, test.perftEntry("r2q1rk1/pP1p2pp/Q4n2/bbp1p3/Np6/1B3NBn/pPPP1PPP/R3K2R b KQ - 0 1 ", 3));
        assertEquals(422333, test.perftEntry("r2q1rk1/pP1p2pp/Q4n2/bbp1p3/Np6/1B3NBn/pPPP1PPP/R3K2R b KQ - 0 1 ", 4));
        assertEquals(15833292, test.perftEntry("r2q1rk1/pP1p2pp/Q4n2/bbp1p3/Np6/1B3NBn/pPPP1PPP/R3K2R b KQ - 0 1 ", 5));
        System.out.println("pass mirrortest black");
    }

    @Test
    void talkChessTrickyPerft(){
        assertEquals(1486, test.perftEntry("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8 ", 2));
        assertEquals(62379, test.perftEntry("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8 ", 3));
        assertEquals(2103487, test.perftEntry("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8 ", 4));
        assertEquals(89941194, test.perftEntry("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8 ", 5));
        System.out.println("pass tricky perft");
    }

    @Test
    void stevenPerft(){
        assertEquals(2079, test.perftEntry("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10", 2));
        assertEquals(89890, test.perftEntry("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10", 3));
        assertEquals(3894594, test.perftEntry("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10", 4));
//        assertEquals(164075551, test.perftEntry("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10", 5));
        System.out.println("pass stevenEdward perft");
    }
}