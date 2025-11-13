package websocket;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import static ui.EscapeSequences.*;

public class BoardDrawer {
    public static void drawBoard(ChessBoard board, boolean white) {
        int startRow = white ? 7 : 0;
        int endRow   = white ? -1 : 8;
        int rowStep  = white ? -1 : 1;

        int startCol = white ? 0 : 7;
        int endCol   = white ? 8 : -1;
        int colStep  = white ? 1 : -1;

        for (int row = startRow; row != endRow; row += rowStep) {
            for (int col = startCol; col != endCol; col +=colStep) {

                ChessPiece piece = board.getPiece(new ChessPosition(row + 1, col + 1));

                if ((row + col) % 2 == 0) {
                    setDark();
                } else {
                    setLight();
                }
                System.out.print(getPieceString(piece));
            }
            systemResetColor();

            System.out.println();
        }
    }

    private static String getPieceString(ChessPiece piece) {
        if (piece == null) {
            return "   ";
        }

        boolean white = piece.getTeamColor() == ChessGame.TeamColor.WHITE;

        return switch (piece.getPieceType()) {
            case QUEEN -> white ? WHITE_QUEEN : BLACK_QUEEN;
            case KING -> white ? WHITE_KING: BLACK_KING;
            case BISHOP -> white ? WHITE_BISHOP : BLACK_BISHOP;
            case ROOK -> white ? WHITE_ROOK : BLACK_ROOK;
            case KNIGHT -> white ? WHITE_KNIGHT : BLACK_KNIGHT;
            case PAWN -> white? WHITE_PAWN : BLACK_PAWN;
        };
    }

    static void systemResetColor() {
        System.out.print(SET_BG_COLOR_DARK_GREY);
        System.out.print(SET_TEXT_COLOR_BLUE);
    }

    private static void setDark() {
        System.out.print(SET_BG_COLOR_BLUE);
        System.out.print(SET_TEXT_COLOR_BLACK);
    }

    private static void setLight() {
        System.out.print(SET_BG_COLOR_WHITE);
        System.out.print(SET_TEXT_COLOR_BLACK);
    }
}
