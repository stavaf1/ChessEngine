package com.engine.chess;

import java.util.Comparator;

public class BitBoardComparator implements Comparator {
    private static NegaMax negaMax;
    public BitBoardComparator(NegaMax negaMax){
        this.negaMax = negaMax;
    }

    @Override
    public int compare(Object o1, Object o2) {
        //first argument smaller return negative
        if(!(o1 instanceof BitBoardPosition) || !(o2 instanceof BitBoardPosition))return 0;

        int first = negaMax.piecePlacement((BitBoardPosition) o1);
        int second = negaMax.piecePlacement((BitBoardPosition) o2);

        if(first < second) return -1;
        if(second < first) return 1;
        return 0;
    }
}
