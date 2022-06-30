package Test;

import com.engine.chess.BitBoardPosition;
import com.engine.chess.Position;
import com.engine.chess.StaticEvaluator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StaticEvaluatorTest {

    @Test
    void startPosMaterialBalance(){
        Position initPosition = new Position("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        BitBoardPosition newBitboards = new BitBoardPosition(initPosition.getbR(), initPosition.getbN(), initPosition.getbB(), initPosition.getbQ(), initPosition.getbK(), initPosition.getbP(), initPosition.getwR(), initPosition.getwN(), initPosition.getwB(), initPosition.getwK(), initPosition.getwQ(), initPosition.getwP(), initPosition.getWhiteToMove());
        StaticEvaluator evaluator = new StaticEvaluator();
        evaluator.initPieceArrays(newBitboards);

        assertEquals(0, evaluator.materialBalance(newBitboards));
    }
}