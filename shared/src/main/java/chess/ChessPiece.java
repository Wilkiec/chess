package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private boolean moveIterate(ChessBoard board, ChessPosition myPosition, List<ChessMove> possibleMoves, int currentRow, int currentCol) {
        ChessPosition currentPosition = new ChessPosition(currentRow, currentCol);

        ChessPiece currentPiece = board.getPiece(currentPosition);
        if (currentPiece == null) {
            possibleMoves.add(new ChessMove(myPosition, currentPosition, null));
        }
        else {
            if (currentPiece.getTeamColor() != board.getPiece(myPosition).getTeamColor()) {
                possibleMoves.add(new ChessMove(myPosition, currentPosition, null));
            }
            return true;
        }
        return false;
    }

    public List<ChessMove> getMoves(ChessBoard board, ChessPosition myPosition, boolean diagonal, boolean vertical) {
        List<ChessMove> possibleMoves = new ArrayList<>();

        // figuring out which directions the piece can move
        int[][] directions;
        if (diagonal && !vertical) {
            directions = new int[][] {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        } else if (vertical && !diagonal) {
            directions = new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        } else if (vertical) {
            directions = new int[][] {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        } else {
            directions = new int[][] {{}};
        }

        for (int[] direction : directions) {
            // takes the piece's actual position and moves it in whichever direction the loop is at
            int currentRow = myPosition.getRow() + direction[0];
            int currentCol = myPosition.getColumn() + direction[1];

            // will continue to go in the same direction until it is out of bounds or hits another piece
            while (currentRow >= 1 && currentRow <=8 && currentCol >= 1 && currentCol <=8 ) {
                if (moveIterate(board, myPosition, possibleMoves, currentRow, currentCol)) {
                    break;
                }
                currentRow += direction[0];
                currentCol += direction[1];
            }
        }
        return possibleMoves;
    }

    public List<ChessMove> getMovesKing(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> possibleMoves = new ArrayList<>();

        // checking if it is a pawn or a king
        int[][] directions = new int[][] {{-1,1}, {0,1}, {1,1}, {-1,0}, {1,0}, {-1,-1}, {0,-1}, {1,-1}};

        // adding potential moves
        for (int[] direction : directions) {
            // takes the piece's actual position and moves it in whichever direction the loop is at
            int currentRow = myPosition.getRow() + direction[0];
            int currentCol = myPosition.getColumn() + direction[1];

            // will continue to go in the same direction until it is out of bounds or hits another piece
            if (moveIterate(board, myPosition, possibleMoves, currentRow, currentCol)) {
                break;
            }
        }
        return possibleMoves;
    }

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        if (piece.getPieceType() == PieceType.KING) {
            return getMovesKing(board, myPosition);
        }else if (piece.getPieceType() == PieceType.QUEEN) {
            return getMoves(board, myPosition, true, true);
        }else if (piece.getPieceType() == PieceType.BISHOP) {
            return getMoves(board, myPosition, true, false);
        }else if (piece.getPieceType() == PieceType.KNIGHT) {
            return List.of();
        }else if (piece.getPieceType() == PieceType.ROOK) {
            return getMoves(board, myPosition, false, true);
        }else if (piece.getPieceType() == PieceType.PAWN) {
            return List.of();
        }
        else {
            return List.of();
        }
    }
}
