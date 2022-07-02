package Test;

import com.engine.chess.BitBoardPosition;
import com.engine.chess.NegaMax;
import com.engine.chess.Position;
import com.engine.chess.StaticEvaluator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NegaMaxTest {

    @Test
    void initialPositionSimpleTest(){
        Position initPosition = new Position("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - ");
        BitBoardPosition newBitboards = new BitBoardPosition(initPosition.getbR(), initPosition.getbN(), initPosition.getbB(), initPosition.getbQ(), initPosition.getbK(), initPosition.getbP(), initPosition.getwR(), initPosition.getwN(), initPosition.getwB(), initPosition.getwK(), initPosition.getwQ(), initPosition.getwP(), initPosition.getWhiteToMove());
        NegaMax negaMax = new NegaMax();

//        System.out.println(negaMax.entryPoint(newBitboards));

    }
}