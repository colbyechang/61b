/* Skeleton Copyright (C) 2015, 2020 Paul N. Hilfinger and the Regents of the
 * University of California.  All rights reserved. */
package loa;

import static loa.Piece.*;

/** An automated Player.
 *  @author Colby Chang
 */
class MachinePlayer extends Player {

    /** A position-score magnitude indicating a win (for white if positive,
     *  black if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new MachinePlayer with no piece or controller (intended to produce
     *  a template). */
    MachinePlayer() {
        this(null, null);
    }

    /** A MachinePlayer that plays the SIDE pieces in GAME. */
    MachinePlayer(Piece side, Game game) {
        super(side, game);
    }

    @Override
    String getMove() {
        Move choice;

        assert side() == getGame().getBoard().turn();
        int depth;
        choice = searchForMove();
        getGame().reportMove(choice);
        return choice.toString();
    }

    @Override
    Player create(Piece piece, Game game) {
        return new MachinePlayer(piece, game);
    }

    @Override
    boolean isManual() {
        return false;
    }

    /** Return a move after searching the game tree to DEPTH>0 moves
     *  from the current position. Assumes the game is not over. */
    private Move searchForMove() {
        Board work = new Board(getBoard());
        int value;
        assert side() == work.turn();
        _foundMove = null;
        if (side() == WP) {
            value = findMove(work, chooseDepth(), true, 1, -INFTY, INFTY);
        } else {
            value = findMove(work, chooseDepth(), true, -1, -INFTY, INFTY);
        }
        return _foundMove;
    }

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _foundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _foundMove. If the game is over
     *  on BOARD, does not set _foundMove. */
    private int findMove(Board board, int depth, boolean saveMove,
                         int sense, int alpha, int beta) {
        if (board.gameOver() || depth == 0) {
            if (board.winner() == side()) {
                return WINNING_VALUE;
            } else if (board.winner() == side().opposite()) {
                return -WINNING_VALUE;
            } else if (board.winner() == EMP) {
                return _tieval;
            } else {
                return board.getRegionSizes(side().opposite()).size()
                        - board.getRegionSizes(side()).size();
            }
        }
        int bestscore = -INFTY;
        Move found = null;
        for (Move move : board.legalMoves()) {
            Board b = new Board(board);
            b.makeMove(move);
            int score = findMove(b, depth - 1, false, -sense, alpha, beta);
            if (score > bestscore) {
                bestscore = score;
                found = move;
            } else if (score == bestscore) {
                if (Math.random() > 0.5) {
                    found = move;
                }
            }
            if (sense == 1) {
                alpha = Math.max(score, alpha);
            } else {
                beta = Math.min(score, beta);
            }
            if (alpha >= beta) {
                break;
            }
        }
        if (saveMove) {
            _foundMove = found;
        }
        return bestscore;
    }

    /** Return a search depth for the current position. */
    private int chooseDepth() {
        return 3;
    }

    /** Used to convey moves discovered by findMove. */
    private Move _foundMove;

    /** A position-score magnitude indicating a tie. */
    private final int _tieval = -WINNING_VALUE + 20;
}
