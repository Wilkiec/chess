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
    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;
    private final int[][] allDirect = new int[][] {{1,0},{0,-1},{-1,0},{0,1},{1,1},{1,-1},{-1,1},{-1,-1}};

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
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

    private Collection<ChessMove> getMoves(ChessBoard board, ChessPosition myPosition, boolean diagonal, boolean vertical) {
        List<ChessMove> potentialMoves = new ArrayList<>();
        ChessPiece myPiece = board.getPiece(myPosition);

        int[][] directions;
        if (diagonal && !vertical) {
            directions = new int[][] {{1,1},{1,-1},{-1,1},{-1,-1}};
        } else if (!diagonal && vertical) {
            directions = new int[][] {{1,0},{0,-1},{-1,0},{0,1}};
        } else if (vertical) {
            directions = allDirect;
        } else {
            return potentialMoves;
        }

        ChessPosition currentPosition;
        ChessPiece currentPiece;
        int currentRow;
        int currentCol;

        for (int[] direction:directions) {
            currentRow = myPosition.getRow() + direction[0];
            currentCol = myPosition.getColumn() + direction[1];

            while (currentRow > 0 && currentRow < 9 && currentCol > 0 && currentCol < 9) {
                currentPosition = new ChessPosition(currentRow, currentCol);
                currentPiece = board.getPiece(currentPosition);

                if (currentPiece == null) {
                    potentialMoves.add(new ChessMove(myPosition, currentPosition, null));
                } else {
                    if (currentPiece.getTeamColor() != myPiece.getTeamColor()) {
                        potentialMoves.add(new ChessMove(myPosition, currentPosition, null));
                    }
                    break;
                }

                currentRow += direction[0];
                currentCol += direction[1];
            }
        }
        return potentialMoves;
    }

    private Collection<ChessMove> getMovesKingKnight(ChessBoard board, ChessPosition myPosition, boolean king) {
        List<ChessMove> potentialMoves = new ArrayList<>();
        ChessPiece myPiece = board.getPiece(myPosition);

        int[][] directions;
        if (king) {
            directions = allDirect;
        } else {
            directions = new int[][] {{1,2},{1,-2},{-1,2},{-1,-2},{2,1}, {2,-1},{-2,1},{-2,-1}};
        }

        ChessPosition currentPosition;
        ChessPiece currentPiece;
        int currentRow;
        int currentCol;

        for (int[] direction:directions) {
            currentRow = myPosition.getRow() + direction[0];
            currentCol = myPosition.getColumn() + direction[1];

            if (currentRow > 0 && currentRow < 9 && currentCol > 0 && currentCol < 9) {
                currentPosition = new ChessPosition(currentRow, currentCol);
                currentPiece = board.getPiece(currentPosition);

                if (currentPiece == null) {
                    potentialMoves.add(new ChessMove(myPosition, currentPosition, null));
                } else {
                    if (currentPiece.getTeamColor() != myPiece.getTeamColor()) {
                        potentialMoves.add(new ChessMove(myPosition, currentPosition, null));
                    }
                }
            }
        }
        return potentialMoves;
    }

    private Collection<ChessMove> getMovesPawn(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> potentialMoves = new ArrayList<>();
        ChessPiece myPiece = board.getPiece(myPosition);

        boolean white = myPiece.getTeamColor() == ChessGame.TeamColor.WHITE;

        int upDown;
        if (white) {
            upDown = 1;
        } else {
            upDown = -1;
        }

        ChessPosition currentPosition;
        ChessPiece currentPiece;
        int currentRow;
        int currentCol;

        for (int i = -1; i <2; i += 2) {
            currentRow = myPosition.getRow() + upDown;
            currentCol = myPosition.getColumn() + i;

            if (currentCol > 0 && currentCol < 9) {
                currentPosition = new ChessPosition(currentRow, currentCol);
                currentPiece = board.getPiece(currentPosition);

                if (currentPiece != null) {
                    if ((currentRow == 8 && white) || (currentRow == 1 && !white) && (currentPiece.getTeamColor() != myPiece.getTeamColor())) {
                        potentialMoves.add(new ChessMove(myPosition, currentPosition, PieceType.QUEEN));
                        potentialMoves.add(new ChessMove(myPosition, currentPosition, PieceType.ROOK));
                        potentialMoves.add(new ChessMove(myPosition, currentPosition, PieceType.BISHOP));
                        potentialMoves.add(new ChessMove(myPosition, currentPosition, PieceType.KNIGHT));

                    } else if ((currentPiece.getTeamColor() != myPiece.getTeamColor())) {
                        potentialMoves.add(new ChessMove(myPosition, currentPosition, null));
                    }
                }
            }
        }

        int cap = 1;
        if ((white && myPosition.getRow() == 2) || (!white && myPosition.getRow() == 7)) {
            cap = 2;
        }

        for (int i = 1; i <= cap; i++) {
            currentRow = myPosition.getRow() + upDown * i;
            currentCol = myPosition.getColumn();

            currentPosition = new ChessPosition(currentRow, currentCol);
            currentPiece = board.getPiece(currentPosition);

            if (currentPiece == null) {
                if ((currentRow == 8 && white) || (currentRow == 1 && !white)) {
                    potentialMoves.add(new ChessMove(myPosition, currentPosition, PieceType.QUEEN));
                    potentialMoves.add(new ChessMove(myPosition, currentPosition, PieceType.ROOK));
                    potentialMoves.add(new ChessMove(myPosition, currentPosition, PieceType.BISHOP));
                    potentialMoves.add(new ChessMove(myPosition, currentPosition, PieceType.KNIGHT));
                    return potentialMoves;
                } else {
                    potentialMoves.add(new ChessMove(myPosition, currentPosition, null));
                }
            } else {
                return potentialMoves;
            }
        }
        return potentialMoves;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece myPiece = board.getPiece(myPosition);
        if (myPiece.getPieceType() == PieceType.QUEEN) {
            return getMoves(board, myPosition, true, true);
        } else if (myPiece.getPieceType() == PieceType.BISHOP) {
            return getMoves(board, myPosition, true, false);
        } else if (myPiece.getPieceType() == PieceType.ROOK) {
            return getMoves(board, myPosition, false, true);
        } else if (myPiece.getPieceType() == PieceType.KING) {
            return getMovesKingKnight(board, myPosition, true);
        } else if (myPiece.getPieceType() == PieceType.KNIGHT) {
            return getMovesKingKnight(board, myPosition, false);
        } else if (myPiece.getPieceType() == PieceType.PAWN) {
            return getMovesPawn(board, myPosition);
        }
        return List.of();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChessPiece that)) {
            return false;
        }
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }
}