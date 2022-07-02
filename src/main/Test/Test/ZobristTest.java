package Test;

import com.engine.chess.BitBoardPosition;
import com.engine.chess.Position;
import com.engine.chess.Zobrist;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.zone.ZoneRulesException;

import static org.junit.jupiter.api.Assertions.*;

class ZobristTest {

    /**
     * initialises a linear array of keys and copies all values for the zobrist hash into them
     * checks there is exactly one matching key in each keyArray
     */

    @Test
    void verifyUnique(){

        long[] checkfull = new long[2*6*64];
        Zobrist zorb = new Zobrist();
        long[][] test = zorb.getZobr();

        for(int i = 0; i < 12; i++){
            for(int j = 0; j < 64; j++){
                checkfull[i*64 + j] = test[i][j];
            }
        }
        for(int i = 0; i < 12*64; i++){
            int matches = 0;
            long checkKey = checkfull[i];
            for(int j = 0; j < 12; j++){
                for(int k =0; k < 64; k++){
                      if(checkKey == test[j][k]){
                          matches++;
                          assertTrue(matches <2);
                      }
                      assertNotEquals(0L, checkKey);
                }
            }
            assertEquals(1, matches);
        }
    }
}