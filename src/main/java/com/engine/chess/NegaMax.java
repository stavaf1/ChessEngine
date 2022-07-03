package com.engine.chess;

import javafx.collections.transformation.SortedList;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.stream.Collectors;

import static com.engine.chess.Perft.moveToAlgebra;

public class NegaMax extends StaticEvaluator{
    private int positionSearched = 0;
    private final MoveGenerator engine = new MoveGenerator();

    private final Zobrist zorb = new Zobrist();

    private final BitBoardComparator comparator = new BitBoardComparator(this);

    /**
     * caller for the negamax algorithm
     * @param position from which to evaluate
     * @return String for the move chosen
     */
    public String entryPoint(BitBoardPosition position, int alpha, int beta, int depth){
        long start = System.currentTimeMillis();
        String bestMove = "";
        int bestScore = -999999;


        String moveString = engine.getMoves(position.getCastling(), position.getEnPassant(), position.getWhiteToMove(),position.getbR(), position.getbN(),position.getbB(),position.getbQ(),position.getbK(),position.getbP(),position.getwR(),position.getwN(),position.getwB(),position.getwK(),position.getwQ(),position.getwP());

        if(moveString.length() == 1) return moveString;


        LinkedList<String> moveArray = new LinkedList<>();

        for(int i = 0; i < moveString.length(); i += 5){
            moveArray.add(moveString.substring(i, i+5));
        }


        LinkedList<String> takesFirst = takesFirst(moveArray);

        //for every child position
        for(String move: takesFirst){
            BitBoardPosition holding = (engine.makeMove(position.getWhiteToMove(), move, position.getCastling(), position.getbR(), position.getbN(), position.getbB(), position.getbQ(), position.getbK(), position.getbP(), position.getwR(), position.getwN(), position.getwB(), position.getwK(), position.getwQ(), position.getwP()));
            positionSearched = 0;
            int holdingScore = -negaMax(holding, 4, -beta, -alpha);
//            System.out.println(move + ": given score " + holdingScore);
            zorb.addEntry(position, new TTentry(TTentry.Flag.EXACT, depth, holdingScore));

            if(holdingScore > bestScore){
                bestScore = holdingScore;
                bestMove = move;
                System.out.println("new best move: " + move + "with score: " + bestScore);
            }
            alpha = Math.max(bestScore, alpha);
            if(alpha >= beta) break;
        }
        System.out.println("took :" + (System.currentTimeMillis() - start));
        System.out.println("evaluation: " + evaluatorTimeTrial);
        System.out.println("generation: " + MoveGenerator.generatorTimeTrial);
        evaluatorTimeTrial = 0;
        MoveGenerator.generatorTimeTrial = 0;
        return bestMove;
    }


    /**
     * recursive alpha-beta negamax algorithm
     * @param position the position to be evaluated
     * @param depth implicit
     * @param alpha
     * @param beta
     * @return
     */
    public int negaMax(BitBoardPosition position, int depth, int alpha, int beta){
        int originalAlpha = alpha;
        TTentry checkTables = zorb.getEntry(position);

        //============
        //SEARCH FOR NODE IN TRANSPOSITION TABLES
        if(checkTables != null && checkTables.getDepth() >= depth){
            if(checkTables.getFlag() == TTentry.Flag.EXACT) return checkTables.getScore();
            else if (checkTables.getFlag() == TTentry.Flag.LOWERBOUND) alpha = Math.max(alpha, checkTables.getScore());
            else if (checkTables.getFlag() == TTentry.Flag.UPPERBOUND) beta = Math.min(beta, checkTables.getScore());
        }

        String moveString = engine.getMoves(position.getCastling(), position.getEnPassant(), position.getWhiteToMove(),position.getbR(), position.getbN(),position.getbB(),position.getbQ(),position.getbK(),position.getbP(),position.getwR(),position.getwN(),position.getwB(),position.getwK(),position.getwQ(),position.getwP());
        if(moveString.length() == 1){
            if(moveString.equals("s")) return 0;
            else return -300000;
        }

        if(depth == 0){
            return quiesce(position, alpha, beta);
        }

        ArrayList<String> legalMoves = new ArrayList<>();
        for(int i = 0; i < moveString.length(); i += 5){
            legalMoves.add(moveString.substring(i, i+5));
        }

        List<BitBoardPosition> moveArray = orderMoves(legalMoves, position);

        int value = -9999999;
        for(BitBoardPosition holding: moveArray){
            value = Math.max(value, -negaMax(holding, depth -1, -beta, -alpha));
            alpha = Math.max(alpha, value);
            if (alpha >= beta)break;
        }


        //transposition table entry code
        if(value <= originalAlpha){
            zorb.addEntry(position, new TTentry(TTentry.Flag.UPPERBOUND, depth, value));
        } else if (value >= beta) {
            zorb.addEntry(position, new TTentry(TTentry.Flag.LOWERBOUND, depth, value));
        } else {
            zorb.addEntry(position, new TTentry(TTentry.Flag.EXACT, depth, value));
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
    public int quiesce(BitBoardPosition position, int alpha, int beta)
    {
        int originalAlpha = alpha;
        TTentry checkTables = zorb.getEntry(position);

        //============
        //SEARCH FOR NODE IN TRANSPOSITION TABLES
        if(checkTables != null && checkTables.getDepth() >= -1){
            if(checkTables.getFlag() == TTentry.Flag.EXACT) return checkTables.getScore();
            else if (checkTables.getFlag() == TTentry.Flag.LOWERBOUND) alpha = Math.max(alpha, checkTables.getScore());
            else if (checkTables.getFlag() == TTentry.Flag.UPPERBOUND) beta = Math.min(beta, checkTables.getScore());
        }


        String moveString = engine.getMoves(position.getCastling(), position.getEnPassant(), position.getWhiteToMove(),position.getbR(), position.getbN(),position.getbB(),position.getbQ(),position.getbK(),position.getbP(),position.getwR(),position.getwN(),position.getwB(),position.getwK(),position.getwQ(),position.getwP());
        if(moveString.length() == 1){
            if(moveString.equals("s")) return 0;
            else return -300000;
        }

        int staticEval = evaluate(position);
        if(staticEval >= beta) return beta;
        if(staticEval > alpha) alpha = staticEval;

        LinkedList<String> moveArray = new LinkedList<>();
        for(int i = 0; i < moveString.length(); i += 5){
            if(moveString.charAt(i + 2) == 'x')moveArray.add(moveString.substring(i, i+5));
        }

        int score = 0;
        for(String move: moveArray){
            BitBoardPosition holding = (engine.makeMove(position.getWhiteToMove(), move, position.getCastling(), position.getbR(), position.getbN(), position.getbB(), position.getbQ(), position.getbK(), position.getbP(), position.getwR(), position.getwN(), position.getwB(), position.getwK(), position.getwQ(), position.getwP()));
            score = -quiesce(holding, -beta, -alpha);

            if(score >= beta) return beta;
            if(score > alpha) alpha = score;
        }
        if(score <= originalAlpha){
            zorb.addEntry(position, new TTentry(TTentry.Flag.UPPERBOUND, -1, score));
        } else if (score >= beta) {
            zorb.addEntry(position, new TTentry(TTentry.Flag.LOWERBOUND, -1, score));
        } else {
            zorb.addEntry(position, new TTentry(TTentry.Flag.EXACT, -1, score));
        }




        return alpha;
    }

    /**
     * currently simple implementation of chunk ordering
     * @param unordered list of moves
     * @return list of moves with takes at the start
     */
    public LinkedList<String> takesFirst(LinkedList<String> unordered)
    {
        LinkedList<String> ordered = new LinkedList<>();
        for(String move: unordered){
            char moveType = move.charAt(2);
            if(moveType == 'x'){
                ordered.add(move);
            } else {
                ordered.addLast(move);
            }
        }
        return ordered;
    }




    public ArrayList<BitBoardPosition> orderMoves(ArrayList<String> unordered, BitBoardPosition position){
        ArrayList<BitBoardPosition> orderedPositions = new ArrayList<>();
        ArrayList<BitBoardPosition> quietUnordered = new ArrayList<>();

        for(String move: unordered){
            char moveType = move.charAt(2);
            if(moveType == 'x' |moveType == 'r' |moveType == 'b' |moveType == 'q' |moveType == 'e'){
                orderedPositions.add((engine.makeMove(position.getWhiteToMove(), move, position.getCastling(), position.getbR(), position.getbN(), position.getbB(), position.getbQ(), position.getbK(), position.getbP(), position.getwR(), position.getwN(), position.getwB(), position.getwK(), position.getwQ(), position.getwP())));
            } else {
                quietUnordered.add(engine.makeMove(position.getWhiteToMove(), move, position.getCastling(), position.getbR(), position.getbN(), position.getbB(), position.getbQ(), position.getbK(), position.getbP(), position.getwR(), position.getwN(), position.getwB(), position.getwK(), position.getwQ(), position.getwP()));
            }
        }
        LinkedList<BitBoardPosition> quietOrdered = sortQuiets(quietUnordered);
        orderedPositions.addAll(quietOrdered);
        return orderedPositions;
    }

    public LinkedList<BitBoardPosition> sortQuiets(ArrayList<BitBoardPosition> positions)
    {
        PriorityQueue sorter = new PriorityQueue<>(50, comparator);
        sorter.addAll(positions);
        LinkedList<BitBoardPosition> ordered = new LinkedList<>(sorter);

        return ordered;
    }
}
