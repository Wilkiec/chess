package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

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
                // function returns true if piece runs into another piece (forcing it to stop)
                if (moveIterate(board, myPosition, possibleMoves, currentRow, currentCol)) {
                    break;
                }
                currentRow += direction[0];
                currentCol += direction[1];
            }
        }
        return possibleMoves;
    }

    public List<ChessMove> getMovesKingHorsey(ChessBoard board, ChessPosition myPosition, boolean horsey) {
        List<ChessMove> possibleMoves = new ArrayList<>();

        // checking if it is a pawn or a king
        int[][] directions;
        if (!horsey) {
            directions = new int[][] {{-1,1}, {0,1}, {1,1}, {-1,0}, {1,0}, {-1,-1}, {0,-1}, {1,-1}};
        } else {
            directions = new int[][] {{-1,2}, {1,2}, {2,1}, {2,-1}, {-2,1}, {-2,-1}, {-1,-2}, {1,-2}};
        }


        // adding potential moves
        for (int[] direction : directions) {
            // takes the piece's actual position and moves it in whichever direction the loop is at
            int currentRow = myPosition.getRow() + direction[0];
            int currentCol = myPosition.getColumn() + direction[1];

            // make sure move is in bounds
            if ((currentRow > 8 || currentRow < 1) || (currentCol > 8 || currentCol < 1)) {
                continue;
            }

            // will continue to go in the same direction until it is out of bounds or hits another piece
            moveIterate(board, myPosition, possibleMoves, currentRow, currentCol);
        }
        return possibleMoves;
    }

    public List<ChessMove> getMovesPawn(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> possibleMoves = new ArrayList<>();
        ChessPiece myPiece = board.getPiece(myPosition);

        // finding team color to see if pawn moves up or down
        boolean black = myPiece.pieceColor == ChessGame.TeamColor.BLACK;
        int upDown;
        if (black) {
            upDown = -1;
        } else {
            upDown = 1;
        }

        // check if pawn is at starting point
        boolean atStart = (black && myPosition.getRow() == 7) || (!black && myPosition.getRow() == 2);
        boolean promote = (!black && myPosition.getRow() == 7) || (black && myPosition.getRow() == 2);

        // checking for valid moves in diagonal direction
        for (int i = -1; i <= 1; i = i + 2) {
            // checking for illegal horizontal moves
            if (myPosition.getColumn() + i < 1 || myPosition.getColumn() + i > 8) {
                continue; // Skip invalid diagonal moves
            }

            // finding the current position of potential move and if any pieces are on it
            ChessPosition currentPosition = new ChessPosition(myPosition.getRow() + upDown, myPosition.getColumn() + i);
            ChessPiece currentPiece = board.getPiece(currentPosition);

            // if pawn is moving diagonal it must be taking enemy piece
            if (currentPiece != null && currentPiece.pieceColor != myPiece.pieceColor) {
                if (promote) {
                    possibleMoves.add(new ChessMove(myPosition, currentPosition, PieceType.QUEEN));
                    possibleMoves.add(new ChessMove(myPosition, currentPosition, PieceType.ROOK));
                    possibleMoves.add(new ChessMove(myPosition, currentPosition, PieceType.BISHOP));
                    possibleMoves.add(new ChessMove(myPosition, currentPosition, PieceType.KNIGHT));
                } else {
                    possibleMoves.add(new ChessMove(myPosition, currentPosition, null));
                }
            }
        }

        int cap;
        if (atStart) { cap = 2; } else { cap = 1; }

        // checking for valid moves going forward
        for (int i = 1; i <= cap; i++) {
            ChessPosition currentPosition = new ChessPosition(myPosition.getRow() + upDown*i, myPosition.getColumn());
            ChessPiece currentPiece = board.getPiece(currentPosition);

            if (currentPiece != null) {
                break;
            } else {
                if (promote) {
                    possibleMoves.add(new ChessMove(myPosition, currentPosition, PieceType.QUEEN));
                    possibleMoves.add(new ChessMove(myPosition, currentPosition, PieceType.ROOK));
                    possibleMoves.add(new ChessMove(myPosition, currentPosition, PieceType.BISHOP));
                    possibleMoves.add(new ChessMove(myPosition, currentPosition, PieceType.KNIGHT));
                    return possibleMoves;
                } else {
                    possibleMoves.add(new ChessMove(myPosition, currentPosition, null));
                }
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
            return getMovesKingHorsey(board, myPosition, false);
        }else if (piece.getPieceType() == PieceType.QUEEN) {
            return getMoves(board, myPosition, true, true);
        }else if (piece.getPieceType() == PieceType.BISHOP) {
            return getMoves(board, myPosition, true, false);
        }else if (piece.getPieceType() == PieceType.KNIGHT) {
            return getMovesKingHorsey(board, myPosition, true);
        }else if (piece.getPieceType() == PieceType.ROOK) {
            return getMoves(board, myPosition, false, true);
        }else if (piece.getPieceType() == PieceType.PAWN) {
            return getMovesPawn(board, myPosition);
        }
        else {
            return List.of();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChessPiece that = (ChessPiece) o;
        return getPieceType() == that.getPieceType() &&
                getTeamColor() == that.getTeamColor();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPieceType(), getTeamColor());
    }
}
