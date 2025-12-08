package client;

import chess.*;

import java.util.Collection;

import static ui.EscapeSequences.*;

public class BoardDrawer {


    public static void drawBoard(ChessBoard board, boolean white) {
        drawHeaders(white);

        int startRow = white ? 7 : 0;
        int endRow   = white ? -1 : 8;
        int rowStep  = white ? -1 : 1;

        int startCol = white ? 0 : 7;
        int endCol   = white ? 8 : -1;
        int colStep  = white ? 1 : -1;

        for (int row = startRow; row != endRow; row += rowStep) {
            printBorderNum(row + 1);
            for (int col = startCol; col != endCol; col +=colStep) {
                ChessPiece piece = board.getPiece(new ChessPosition(row + 1, col + 1));

                if ((row + col) % 2 == 0) {
                    setDark(false);
                } else {
                    setLight(false);
                }
                System.out.print(getPieceString(piece));
            }
            systemResetColor();
            printBorderNum(row + 1);
            System.out.println();
        }
        drawHeaders(white);
    }

    public static void drawValidMoves(ChessBoard board, boolean white, Collection<ChessMove> validMoves) {
        drawHeaders(white);

        int startRow;
        startRow = white ? 7 : 0;
        var endRow   = white ? -1 : 8;
        int rowStep  = white ? -1 : 1;

        int startCol = white ? 0 : 7;
        int endCol   = white ? 8 : -1;
        int colStep  = white ? 1 : -1;
        boolean highlight = false;

        for (int row = startRow; row != endRow; row += rowStep) {
            printBorderNum(row + 1);
            for (int col = startCol; col != endCol; col +=colStep) {
                ChessPosition position = new ChessPosition(row + 1, col + 1);

                for (ChessMove move : validMoves) {
                    if (move.getEndPosition().equals(position)) {
                        highlight = true;
                        break;
                    }
                }
                ChessPiece piece = board.getPiece(position);

                if ((row + col) % 2 == 0) {
                    setDark(highlight);
                } else {
                    setLight(highlight);
                }

                System.out.print(getPieceString(piece));
                highlight = false;
            }
            systemResetColor();
            printBorderNum(row + 1);
            System.out.println();
        }
        drawHeaders(white);
    }

    private static void drawHeaders(boolean white) {
        System.out.print(SET_BG_COLOR_DARK_GREY);
        System.out.print(SET_TEXT_COLOR_WHITE);
        System.out.print("   ");

        String[] headers = new String[]{" a ", " b ", " c ", " d ", " e ", " f ", " g ", " h "};
        if (!white) {
            headers = new String[]{" h ", " g ", " f ", " e ", " d ", " c ", " b ", " a "};
        }

        for (String letter : headers) {
            System.out.print(letter);
        }

        System.out.print("   ");
        System.out.println();
    }

    private static void printBorderNum(int row) {
        System.out.print(SET_BG_COLOR_DARK_GREY);
        System.out.print(SET_TEXT_COLOR_WHITE);

        System.out.print(" " + row + " ");
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

    private static void setDark(boolean highlight) {
        if (highlight) {
            System.out.print(SET_BG_COLOR_MAGENTA);
            System.out.print(SET_TEXT_COLOR_BLACK);
            return;
        }
        System.out.print(SET_BG_COLOR_BLUE);
        System.out.print(SET_TEXT_COLOR_BLACK);
    }

    private static void setLight(boolean highlight) {
        if (highlight) {
            System.out.print(SET_BG_COLOR_LIGHT_GREY);
            System.out.print(SET_TEXT_COLOR_BLACK);
            return;
        }
        System.out.print(SET_BG_COLOR_WHITE);
        System.out.print(SET_TEXT_COLOR_BLACK);
    }
}
