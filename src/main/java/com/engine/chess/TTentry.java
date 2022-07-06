package com.engine.chess;

public class TTentry {
    enum Flag{
        EXACT, UPPERBOUND, LOWERBOUND
    }

    private Flag flag;
    private int depth;
    private int score;

    private int bestMove;


    public TTentry(Flag flag, int depth, int score){
        this.flag = flag;
        this.depth = depth;
        this.score = score;
    }
    public TTentry(Flag flag, int depth, int score, int bestMove){
        this.flag = flag;
        this.depth = depth;
        this.score = score;
        this.bestMove = bestMove;
    }

    public Flag getFlag() {
        return flag;
    }

    public int getDepth(){return depth;}

    public int getScore() {
        return score;
    }

    public int getBestMove(){return bestMove;}
}
