package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard gameBoard;
    private TeamColor currentTurn = TeamColor.WHITE;
    public ChessGame() {
        this.gameBoard = new ChessBoard();
        this.gameBoard.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.currentTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    private Collection<ChessPosition> kingCheck(ChessBoard gameBoard, ChessPosition kingPosition, TeamColor color) {
        Collection<ChessPosition> enemyPos = new ArrayList<>();
        for (int i = 0; i < 64; i++) {
            int row = (i / 8) + 1;
            int col = (i % 8) + 1;
                ChessPiece currentPiece = gameBoard.getPiece(new ChessPosition(row, col));
                if (currentPiece != null && currentPiece.getTeamColor() != color) {
                    Collection<ChessMove> enemyMoves = currentPiece.pieceMoves(gameBoard, new ChessPosition(row, col));
                    for (ChessMove enemyMove: enemyMoves) {
                        if (enemyMove.getEndPosition().equals(kingPosition)) {
                            enemyPos.add(enemyMove.getStartPosition());
                        }
                    }
                }

        }
        return enemyPos;
    }


    private ChessPosition findKing(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPiece currentPiece = gameBoard.getPiece(new ChessPosition(row, col));
                if (currentPiece != null && currentPiece.getTeamColor() == teamColor && currentPiece.getPieceType() == ChessPiece.PieceType.KING) {
                    return new ChessPosition(row, col);
                }
            }
        }
        return new ChessPosition(-1,-1);
    }


    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece gamePiece = gameBoard.getPiece(startPosition);
        if (gamePiece == null) {
            return null;
        }

        Collection<ChessMove> legalMoves = new ArrayList<>();

        Collection<ChessMove> potentialMoves = gamePiece.pieceMoves(gameBoard, startPosition);
        TeamColor color = gamePiece.getTeamColor();
        ChessPiece.PieceType pType = gamePiece.getPieceType();

        for (ChessMove move : potentialMoves) {
            // simulate the move and make sure king doesn't end up in check
            ChessPiece taken = gameBoard.getPiece(move.getEndPosition());
            gameBoard.addPiece(startPosition, null);
            gameBoard.addPiece(move.getEndPosition(), new ChessPiece(color, pType));

            ChessPosition newKingPos = findKing(color);

            if (kingCheck(gameBoard, newKingPos, color).isEmpty()) {
                legalMoves.add(move);
            }

            gameBoard.addPiece(startPosition, new ChessPiece(color, pType));
            gameBoard.addPiece(move.getEndPosition(), taken);

        }
        return legalMoves;
    }


    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = gameBoard.getPiece(move.getStartPosition());

        // confirming there is a piece at the start position
        if (piece == null) {
            throw new InvalidMoveException("There is no piece at given start position");
        }

        // making sure it is this teams turn
        if (getTeamTurn() != piece.getTeamColor()) {
            throw new InvalidMoveException("Not this team's turn");
        }

        // making sure the move is valid
        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());
        if (!validMoves.contains(move)) {
            throw new InvalidMoveException("Invalid move");
        }

        // checking for pawn promotion
        if (move.getPromotionPiece() != null) {
            gameBoard.addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(), move.getPromotionPiece()));
            gameBoard.addPiece(move.getStartPosition(), null);
        } else {
            // putting the piece in the new spot and getting rid of it at old spot.
            gameBoard.addPiece(move.getEndPosition(), piece);
            gameBoard.addPiece(move.getStartPosition(), null);
        }

        // making it so it is the other team's turn now
        if (piece.getTeamColor() == TeamColor.WHITE) {
            setTeamTurn(TeamColor.BLACK);
        }
        else {
            setTeamTurn(TeamColor.WHITE);
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return !kingCheck(gameBoard, findKing(teamColor), teamColor).isEmpty();
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        // iterate through and if every piece of team color has no moves, checkmate is accomplished
        return iterate(teamColor);
    }

    private boolean iterate(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPiece currentPiece = gameBoard.getPiece(new ChessPosition(row, col));
                if (currentPiece != null && currentPiece.getTeamColor() == teamColor) {
                    if (!validMoves(new ChessPosition(row, col)).isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }

        // iterate through and if every piece of team color has no moves, checkmate is accomplished
        return iterate(teamColor);
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        gameBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return gameBoard;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChessGame chessGame)) {
            return false;
        }
        return Objects.equals(gameBoard, chessGame.gameBoard) && currentTurn == chessGame.currentTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameBoard, currentTurn);
    }
}
