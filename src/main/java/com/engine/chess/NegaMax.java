package com.engine.chess;

import javafx.collections.transformation.SortedList;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.stream.Collectors;


public class NegaMax extends StaticEvaluator{
    private int positionSearched = 0;
    private final PseudoLegalMoveGenerator engine = new PseudoLegalMoveGenerator();

    private final Zobrist zorb = new Zobrist();

    private static final int MAX_DEPTH = 20;

    private static final long SEARCH_TIME = 500;
    private static int nodesSearched = 0;

    private static int nullMovespruned =0;

    private static int checkExtensions = 0;

    /**==========================================
     * CALLER FOR NEGAMAX ALGORITHM
     * ITERATIVE DEEPENING IMPLEMENTED HERE
     *===========================================
     * @param position from which to evaluate
     * @return integer the move to be played
     */
    public Integer entryPoint(BitBoardPosition position, int alpha, int beta, int depth){
        nodesSearched = 0;
        nullMovespruned = 0;
        int originalAlpha = alpha;
        long start = System.currentTimeMillis();
        Integer bestMove = null;


        int bestScore = -999999;
        int evalFist = 0;
        //for every child position
        for(int i = 1; i < MAX_DEPTH; i++){
            TTentry lastSearch = zorb.getEntry(position);
            if(lastSearch != null && lastSearch.getDepth() == i) {
                evalFist = lastSearch.getBestMove();
            }

            //get and sort all moves to be evaluated
            LinkedList<Integer> moves = negaMaxOrdering(engine.getPseudoMoves(position), evalFist);
            //time control
            if(System.currentTimeMillis() - start > (SEARCH_TIME)) break;
            int holdingScore;
            alpha = originalAlpha;
            for (Integer move : moves) {
                BitBoardPosition holding = engine.makeMove(position, move);
                if (engine.isLegal(holding)) {
                    holdingScore = -negaMax(holding, i, -beta, -alpha, 0);

                    if (holdingScore > alpha) {
                        bestMove = move;
                        alpha= holdingScore;
                        System.out.println(translator(move) + ": score" + holdingScore);
                    }
                    if (alpha >= beta) break;
                }
            }

            System.out.println("depth: " + i + " move: " + translator(bestMove) + " took: "+ (System.currentTimeMillis() - start) + "nodes Searched: " + nodesSearched + "score: " + alpha);
            evalFist = bestMove;
            if(System.currentTimeMillis() - start > (SEARCH_TIME)) break;

            //inaccurate entry, since it will be overwritten by the next search
            //use i+1 so that the algorithm uses the best move from the last iteration
            zorb.addEntry(position, new TTentry(TTentry.Flag.EXACT, i + 1, bestScore,bestMove));

        }

        System.out.println("searchTime " + (System.currentTimeMillis() - start));


        System.out.println("==============================================================");
        System.out.println("evaluation: " + evaluatorTimeTrial);
        System.out.println("generation: " + PseudoLegalMoveGenerator.timeTrial);
        System.out.println("nodes Searched: " + nodesSearched);
        System.out.println("nulls pruned: " + nullMovespruned);
        System.out.println("check extensions: " + checkExtensions);
        System.out.println("==============================================================");
        checkExtensions = 0;
        nodesSearched = 0;
        nullMovespruned = 0;
        evaluatorTimeTrial = 0;
        PseudoLegalMoveGenerator.timeTrial = 0;
        return bestMove;
    }


    /**
     * =======================================================
     *NEGAMAX SEARCH ALGORITHM
     * @param position the position to be evaluated
     * @param depth implicit
     * @param alpha
     * @param beta
     * @return
     */
    public int negaMax(BitBoardPosition position, int depth, int alpha, int beta, int ply){
        int originalAlpha = alpha;
        TTentry checkTables = zorb.getEntry(position);
        int evalFirst= 0;
        boolean checkForCheckmate = true;
        //============
        //SEARCH FOR NODE IN TRANSPOSITION TABLES
        if(checkTables != null && checkTables.getDepth() >= depth){
            checkForCheckmate = false;
            if(checkTables.getFlag() == TTentry.Flag.EXACT) return checkTables.getScore();
            else if (checkTables.getFlag() == TTentry.Flag.LOWERBOUND) {
                alpha = Math.max(alpha, checkTables.getScore());
                evalFirst = checkTables.getBestMove();
            }
            else if (checkTables.getFlag() == TTentry.Flag.UPPERBOUND) {
                beta = Math.min(beta, checkTables.getScore());
                evalFirst = checkTables.getBestMove();
            }
        }
        int value = -9999999;

        //null move pruning, skip my turn, and see if the opponent can improve on their previous eval
        //if the opponent cannot play two consecutive turns and improve on beta, probably bad node
        if(depth > 1 && ply > 1){
            BitBoardPosition newBitboards = new BitBoardPosition(position.getbR(), position.getbN(), position.getbB(), position.getbQ(), position.getbK(), position.getbP(), position.getwR(), position.getwN(), position.getwB(), position.getwK(), position.getwQ(), position.getwP(), !position.getWhiteToMove());
            newBitboards.setCastling(position.getCastling());
            newBitboards.setCastling(position.getEnPassant());

            value = - negaMax(newBitboards, depth - 2, -beta, -alpha, ply + 1);
            if (value >= beta){
                nullMovespruned++;
                return beta;
            }
        }
        //check extension, if a side is in check extend the search
        if(engine.isInCheck(position) && depth < 2) {
            depth++;
            checkExtensions++;
        }


        LinkedList<Integer> moves = negaMaxOrdering(engine.getPseudoMoves(position), evalFirst);

        if(depth <= 0){
            return quiesce(position, alpha, beta, 0);
//            return evaluate(position);
        }

        int bestMove = evalFirst;
        for(Integer move: moves){
            BitBoardPosition nextPosition = engine.makeMove(position, move);
            if(engine.isLegal(nextPosition)){
                checkForCheckmate = false;
                value = Math.max(value, -negaMax(nextPosition, depth -1, -beta, -alpha, ply + 1));
                if(value > alpha){
                    alpha = value;
                    bestMove = move;
                }
                if (alpha >= beta)break;
            }
        }

        if(checkForCheckmate){
            //if there are no legal moves, check whether there is a check, if so return a massive negative value.
            //ply added to imply being checkmated later is not as bad
            if(engine.locationAttacked(position.getWhiteToMove() ? position.getwK() : position.getbK(), position.getWhiteToMove())) return (-30000 + ply);
            //staleMate
            else return 0;
        }


        //transposition table entry code
        if(value <= originalAlpha){
            zorb.addEntry(position, new TTentry(TTentry.Flag.UPPERBOUND, depth, value));
        } else if (value >= beta) {
            zorb.addEntry(position, new TTentry(TTentry.Flag.LOWERBOUND, depth, value, bestMove));
        } else {
            zorb.addEntry(position, new TTentry(TTentry.Flag.EXACT, depth, value, bestMove));
        }

        return value;
    }

    /**
     * quiescence search searches until a position with no takes then returns the static evaluation
     * with alpha beta pruning
     * @param position obtained through the negamax search
     * @param alpha
     * @param beta
     * @return
     */
    public int quiesce(BitBoardPosition position, int alpha, int beta, int depth)
    {
        nodesSearched++;
        int originalAlpha = alpha;
        TTentry checkTables = zorb.getEntry(position);
        int evalFirst = 0;
        //============
        //SEARCH FOR NODE IN TRANSPOSITION TABLES
        if(checkTables != null && checkTables.getDepth() >= depth){
            if(checkTables.getFlag() == TTentry.Flag.EXACT) return checkTables.getScore();
            else if (checkTables.getFlag() == TTentry.Flag.LOWERBOUND) {
                alpha = Math.max(alpha, checkTables.getScore());
                evalFirst = checkTables.getBestMove();
            }
            else if (checkTables.getFlag() == TTentry.Flag.UPPERBOUND) {
                beta = Math.min(beta, checkTables.getScore());
                evalFirst = checkTables.getBestMove();
            }
        }

        int staticEval = evaluate(position);
        if(staticEval >= beta)return beta;
        if(staticEval > alpha) alpha = staticEval;



        LinkedList<Integer> moves = takesOnly(engine.getPseudoMoves(position), evalFirst);

        int score = 0;
        int bestMove = 0;

        for(Integer move: moves) {
            BitBoardPosition holding = engine.makeMove(position, move);
            if (engine.isLegal(holding)) {
                score = -quiesce(holding, -beta, -alpha, depth - 1);

                if (score >= beta) return beta;
                if (score > alpha){
                    alpha = score;
                    bestMove = move;
                }
            }
        }
        if(score <= originalAlpha){
            zorb.addEntry(position, new TTentry(TTentry.Flag.UPPERBOUND, depth, score, bestMove));
        } else if (score >= beta) {
            zorb.addEntry(position, new TTentry(TTentry.Flag.LOWERBOUND, depth, score, bestMove));
        } else {
            zorb.addEntry(position, new TTentry(TTentry.Flag.EXACT, depth, score, bestMove));
        }
        return alpha;
    }

    /**
     * simple move ordering for th quiescence search
     * @param moves
     * @return
     */
    public LinkedList<Integer> takesOnly(ArrayList<Integer> moves, int evalFirst){
        LinkedList<Integer> takesOnly = new LinkedList<>();

        for(Integer move:moves){
            if(((move >> 16) & 0b00000000000000000000000011111111) ==0){
                int takeBalance = (move >> 12 & 0b00000000000000000000000000001111) - (move >> 8 & 0b00000000000000000000000000001111);
                if (takeBalance > 0) takesOnly.addFirst(move);
                else takesOnly.addLast(move);
            }
        }
        if(evalFirst != 0)takesOnly.addFirst(evalFirst);
        return takesOnly;
    }

    public LinkedList<Integer> negaMaxOrdering(ArrayList<Integer> moves, Integer evalFirst){
        LinkedList<Integer> best = new LinkedList<>();
        LinkedList<Integer> worst = new LinkedList<>();
        moves.remove(evalFirst);

        for(Integer move : moves){
            if(((move >> 16) & 0b00000000000000000000000000001111) ==0){
                int takeBalance = (move >> 12 & 0b00000000000000000000000000001111) - (move >> 8 & 0b00000000000000000000000000001111);
                if (takeBalance >= 0) {
                    best.addFirst(move);
                } else best.addLast(move);
            } else {
                int pieceId = ((move >> 8) & 0b00000000000000000000000000001111);
                if (pieceId == 1 | pieceId == 2 || pieceId == 4) worst.addFirst(move);
                else worst.addLast(move);
            }
        }
        best.addAll(worst);
        if(evalFirst != 0)best.addFirst(evalFirst);
        return best;
    }





    /**
     * helper method so its easier to see whats going on
     * @param move
     * @return
     */

    public String translator(int move){
        String returnString = "";


        int from = Integer.reverse(move & 0b11111111000000000000000000000000);
        int to = move & 0b00000000000000000000000011111111;

        returnString += fileNumberToAlgebra.get("" + from%8);
        returnString += 8 - ((int)from/8);

        if(((move >> 16) & 0b00000000000000000000000000001111) == 0) returnString += "x";

        returnString += fileNumberToAlgebra.get("" + to%8);
        returnString += 8 - ((int)to/8);


        return returnString;
    }

    private static HashMap<String, String> fileNumberToAlgebra = new HashMap<>(){{
        put("0", "a");
        put("1", "b");
        put("2", "c");
        put("3", "d");
        put("4", "e");
        put("5", "f");
        put("6", "g");
        put("7", "h");
    }};

}
