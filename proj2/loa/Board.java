/* Skeleton Copyright (C) 2015, 2020 Paul N. Hilfinger and the Regents of the
 * University of California.  All rights reserved. */
package loa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;

import java.util.regex.Pattern;

import static loa.Piece.*;
import static loa.Square.*;

/** Represents the state of a game of Lines of Action.
 *  @author Colby Chang
 */
class Board {

    /** Default number of moves for each side that results in a draw. */
    static final int DEFAULT_MOVE_LIMIT = 60;

    /** Pattern describing a valid square designator (cr). */
    static final Pattern ROW_COL = Pattern.compile("^[a-h][1-8]$");

    /** A Board whose initial contents are taken from INITIALCONTENTS
     *  and in which the player playing TURN is to move. The resulting
     *  Board has
     *        get(col, row) == INITIALCONTENTS[row][col]
     *  Assumes that PLAYER is not null and INITIALCONTENTS is 8x8.
     *
     *  CAUTION: The natural written notation for arrays initializers puts
     *  the BOTTOM row of INITIALCONTENTS at the top.
     */
    Board(Piece[][] initialContents, Piece turn) {
        initialize(initialContents, turn);
    }

    /** A new board in the standard initial position. */
    Board() {
        this(INITIAL_PIECES, BP);
    }

    /** A Board whose initial contents and state are copied from
     *  BOARD. */
    Board(Board board) {
        this();
        copyFrom(board);
    }

    /** Set my state to CONTENTS with SIDE to move. */
    void initialize(Piece[][] contents, Piece side) {
        _winner = null;
        _winnerKnown = false;
        _subsetsInitialized = false;
        while (_moves.size() != 0) {
            _moves.remove(0);
        }
        Square sq;
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                sq = Square.sq(c, r);
                _board[sq.index()] = contents[sq.row()][sq.col()];
            }
        }
        _turn = side;
        _moveLimit = DEFAULT_MOVE_LIMIT;
    }

    /** Set me to the initial configuration. */
    void clear() {
        initialize(INITIAL_PIECES, BP);
    }

    /** Set my state to a copy of BOARD. */
    void copyFrom(Board board) {
        if (board == this) {
            return;
        }
        Square sq;
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                sq = Square.sq(c, r);
                set(sq, board.get(sq));
            }
        }
        _turn = board._turn;
        while (_moves.size() != 0) {
            _moves.remove(0);
        }
        for (Move move : board._moves) {
            _moves.add(move);
        }
        _moveLimit = board._moveLimit;
        _winnerKnown = board._winnerKnown;
        _winner = board._winner;
        _subsetsInitialized = board._subsetsInitialized;
        while (_whiteRegionSizes.size() != 0) {
            _whiteRegionSizes.remove(0);
        }
        for (int size : board._whiteRegionSizes) {
            _whiteRegionSizes.add(size);
        }
        while (_blackRegionSizes.size() != 0) {
            _blackRegionSizes.remove(0);
        }
        for (int size : board._blackRegionSizes) {
            _blackRegionSizes.add(size);
        }
    }

    /** Return the contents of the square at SQ. */
    Piece get(Square sq) {
        return _board[sq.index()];
    }

    /** Set the square at SQ to V and set the side that is to move next
     *  to NEXT, if NEXT is not null. */
    void set(Square sq, Piece v, Piece next) {
        _board[sq.index()] = v;
        if (next != null) {
            _turn = next;
        }
    }

    /** Set the square at SQ to V, without modifying the side that
     *  moves next. */
    void set(Square sq, Piece v) {
        set(sq, v, null);
    }

    /** Set limit on number of moves by each side that results in a tie to
     *  LIMIT, where 2 * LIMIT > movesMade(). */
    void setMoveLimit(int limit) {
        if (2 * limit <= movesMade()) {
            throw new IllegalArgumentException("move limit too small");
        }
        _moveLimit = 2 * limit;
    }

    /** Assuming isLegal(MOVE), make MOVE. This function assumes that
     *  MOVE.isCapture() will return false.  If it saves the move for
     *  later retraction, makeMove itself uses MOVE.captureMove() to produce
     *  the capturing move. */
    void makeMove(Move move) {
        assert isLegal(move);
        Square from = move.getFrom();
        Square to = move.getTo();
        boolean capture = get(to) != EMP;
        set(to, get(from));
        set(from, EMP, _turn.opposite());
        _winner = winner();
        if (capture) {
            _moves.add(move.captureMove());
        } else {
            _moves.add(move);
        }
        _subsetsInitialized = false;
    }

    /** Retract (unmake) one move, returning to the state immediately before
     *  that move.  Requires that movesMade () > 0. */
    void retract() {
        assert movesMade() > 0;
        Move move = _moves.remove(_moves.size() - 1);
        Square rFrom = move.getTo();
        Square rTo = move.getFrom();
        set(rTo, get(rFrom));
        if (move.isCapture()) {
            set(rFrom, get(rTo).opposite(), _turn.opposite());
        } else {
            set(rFrom, EMP, _turn.opposite());
        }
    }

    /** Return the Piece representing who is next to move. */
    Piece turn() {
        return _turn;
    }

    /** Return true iff FROM - TO is a legal move for the player currently on
     *  move. */
    boolean isLegal(Square from, Square to) {
        if (from == null || to == null || get(from) != _turn
                || get(to) == _turn || !from.isValidMove(to)
                || blocked(from, to)) {
            return false;
        }
        int posDir = from.direction(to);
        int negDir = to.direction(from);
        int count = 1;
        for (int i = 1; i < from.distance(to); i++) {
            if (get(from.moveDest(posDir, i)) != EMP) {
                count++;
            }
        }
        Square sq;
        for (int i = from.distance(to); i < BOARD_SIZE; i++) {
            sq = from.moveDest(posDir, i);
            if (sq == null) {
                break;
            } else if (get(sq) != EMP) {
                count++;
            }
        }
        for (int i = 1; i < BOARD_SIZE; i++) {
            sq = from.moveDest(negDir, i);
            if (sq == null) {
                break;
            } else if (get(sq) != EMP) {
                count++;
            }
        }
        return count == from.distance(to);
    }

    /** Return true iff MOVE is legal for the player currently on move.
     *  The isCapture() property is ignored. */
    boolean isLegal(Move move) {
        return isLegal(move.getFrom(), move.getTo());
    }

    /** Return a sequence of all legal moves from this position. */
    List<Move> legalMoves() {
        ArrayList<Move> lst = new ArrayList<Move>();
        Square to;
        for (Square from : ALL_SQUARES) {
            for (int dir = 0; dir < 8; dir++) {
                for (int steps = 1; steps < BOARD_SIZE; steps++) {
                    to = from.moveDest(dir, steps);
                    if (to == null) {
                        break;
                    } else if (isLegal(from, to)) {
                        lst.add(Move.mv(from, to));
                    }
                }
            }
        }
        return lst;
    }

    /** Return true iff the game is over (either player has all his
     *  pieces continguous or there is a tie). */
    boolean gameOver() {
        return winner() != null;
    }

    /** Return true iff SIDE's pieces are continguous. */
    boolean piecesContiguous(Piece side) {
        return getRegionSizes(side).size() == 1;
    }

    /** Return the winning side, if any.  If the game is not over, result is
     *  null.  If the game has ended in a tie, returns EMP. */
    Piece winner() {
        if (!_winnerKnown) {
            if (piecesContiguous(WP) && piecesContiguous(BP)) {
                _winner = _turn.opposite();
            } else if (piecesContiguous(WP)) {
                _winner = WP;
            } else if (piecesContiguous(BP)) {
                _winner = BP;
            } else if (movesMade() >= _moveLimit) {
                _winner = EMP;
            } else {
                return null;
            }
            _winnerKnown = true;
        }
        return _winner;
    }

    /** Return the total number of moves that have been made (and not
     *  retracted).  Each valid call to makeMove with a normal move increases
     *  this number by 1. */
    int movesMade() {
        return _moves.size();
    }

    @Override
    public boolean equals(Object obj) {
        Board b = (Board) obj;
        return Arrays.deepEquals(_board, b._board) && _turn == b._turn;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(_board) * 2 + _turn.hashCode();
    }

    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("===%n");
        for (int r = BOARD_SIZE - 1; r >= 0; r -= 1) {
            out.format("    ");
            for (int c = 0; c < BOARD_SIZE; c += 1) {
                out.format("%s ", get(sq(c, r)).abbrev());
            }
            out.format("%n");
        }
        out.format("Next move: %s%n===", turn().fullName());
        return out.toString();
    }

    /** Return true if a move from FROM to TO is blocked by an opposing
     *  piece or by a friendly piece on the target square. */
    private boolean blocked(Square from, Square to) {
        int dir = from.direction(to);
        for (int i = 1; i < from.distance(to); i++) {
            Piece p = get(from.moveDest(dir, i));
            if (p == _turn.opposite()) {
                return true;
            }
        }
        return false;
    }

    /** Return the size of the as-yet unvisited cluster of squares
     *  containing P at and adjacent to SQ.  VISITED indicates squares that
     *  have already been processed or are in different clusters.  Update
     *  VISITED to reflect squares counted. */
    private int numContig(Square sq, boolean[][] visited, Piece p) {
        if (get(sq) != p || visited[sq.col()][sq.row()]) {
            return 0;
        } else {
            visited[sq.col()][sq.row()] = true;
            int count = 1;
            for (int r = -1; r <= 1; r++) {
                for (int c = -1; c <= 1; c++) {
                    if (Square.exists(c + sq.col(), r + sq.row())) {
                        count += numContig(sq(c + sq.col(), r + sq.row()),
                                visited, p);
                    }
                }
            }
            return count;
        }
    }

    /** Set the values of _whiteRegionSizes and _blackRegionSizes. */
    private void computeRegions() {
        if (_subsetsInitialized) {
            return;
        }
        _whiteRegionSizes.clear();
        _blackRegionSizes.clear();
        boolean[][] visited = new boolean[BOARD_SIZE][BOARD_SIZE];
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                int bSize = numContig(sq(c, r), visited, BP);
                int wSize = numContig(sq(c, r), visited, WP);
                if (bSize > 0) {
                    _blackRegionSizes.add(bSize);
                } else if (wSize > 0) {
                    _whiteRegionSizes.add(wSize);
                }
            }
        }
        Collections.sort(_whiteRegionSizes, Collections.reverseOrder());
        Collections.sort(_blackRegionSizes, Collections.reverseOrder());
        _subsetsInitialized = true;
    }

    /** Return the sizes of all the regions in the current union-find
     *  structure for side S. */
    List<Integer> getRegionSizes(Piece s) {
        computeRegions();
        if (s == WP) {
            return _whiteRegionSizes;
        } else {
            return _blackRegionSizes;
        }
    }

    /** The standard initial configuration for Lines of Action (bottom row
     *  first). */
    static final Piece[][] INITIAL_PIECES = {
        { EMP, BP,  BP,  BP,  BP,  BP,  BP,  EMP },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { EMP, BP,  BP,  BP,  BP,  BP,  BP,  EMP }
    };

    /** Current contents of the board.  Square S is at _board[S.index()]. */
    private final Piece[] _board = new Piece[BOARD_SIZE  * BOARD_SIZE];

    /** List of all unretracted moves on this board, in order. */
    private final ArrayList<Move> _moves = new ArrayList<>();
    /** Current side on move. */
    private Piece _turn;
    /** Limit on number of moves before tie is declared.  */
    private int _moveLimit;
    /** True iff the value of _winner is known to be valid. */
    private boolean _winnerKnown;
    /** Cached value of the winner (BP, WP, EMP (for tie), or null (game still
     *  in progress).  Use only if _winnerKnown. */
    private Piece _winner;

    /** True iff subsets computation is up-to-date. */
    private boolean _subsetsInitialized;

    /** List of the sizes of continguous clusters of pieces, by color. */
    private final ArrayList<Integer>
        _whiteRegionSizes = new ArrayList<>(),
        _blackRegionSizes = new ArrayList<>();
}
