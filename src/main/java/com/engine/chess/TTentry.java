package com.engine.chess;

public class TTentry {
    enum Flag{
        EXACT, UPPERBOUND, LOWERBOUND
    }

    private Flag flag;
    private int depth;
    private int score;


    public TTentry(Flag flag, int depth, int score){
        this.flag = flag;
        this.depth = depth;
        this.score = score;
    }

    public Flag getFlag() {
        return flag;
    }

    public int getDepth(){return depth;}

    public int getScore() {
        return score;
    }
}
