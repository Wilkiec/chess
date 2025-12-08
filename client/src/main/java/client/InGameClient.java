package client;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import jakarta.websocket.DeploymentException;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Scanner;

import static chess.ChessPiece.PieceType.*;

public class InGameClient implements ReplClient, ServerMessageObserver {
    private final Repl repl;
    private final String serverUrl;
    private WebSocketFacade ws;
    private final boolean isPlayer;
    private ChessGame currentGame;
    private boolean whiteTurn = true;

    public InGameClient(Repl repl) {
        this.repl = repl;
        this.serverUrl = repl.serverUrl;
        isPlayer = repl.player;
    }

    public String eval(String line) {
        try {
            var tokens = line.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "leave" -> leave(params);
                case "redraw" -> redraw(params);
                case "highlight" -> highlightLegalMoves(params);
                case "resign" -> resign(params);
                case "move" -> makeMove(params);
                case "quit" -> "quit";
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    private String makeMove(String[] params) {
        if (!isPlayer) {
            return "observer cannot move piece";
        } if (params.length != 2) {
            return "please include start and end position";
        } if (repl.white ^ whiteTurn) {
            return "Not your turn, wait for opponent to move";
        }

        ChessPiece.PieceType promotionPiece = null;

        try {
            ChessPosition startPos = moveParse(params[0]);
            ChessPosition endPos = moveParse(params[1]);

            // check if move is valid
            boolean moveValid = false;
            var validMoves = currentGame.validMoves(startPos);
            for (ChessMove move : validMoves) {
                if (move.getEndPosition().equals(endPos)) {
                    moveValid = true;
                    break;
                }
            }

            if (!moveValid) {
                return "Invalid move, please try again";
            }

            // check if piece is promoting.
            if ((currentGame.getBoard().getPiece(startPos).getPieceType() == PAWN) && (repl.white && endPos.getRow() == 8)
                    || (!repl.white && endPos.getRow() == 1)) {
                Scanner scanner = new Scanner(System.in);
                do {
                    System.out.println("""
                        What would you like to promote your pawn to?
                        - Queen
                        - Rook
                        - Bishop
                        - Knight
                        """);

                    String piece = scanner.nextLine();
                    piece = piece.toLowerCase();

                    switch (piece) {
                        case "queen" -> promotionPiece = QUEEN;
                        case "rook" -> promotionPiece = ROOK;
                        case "bishop" -> promotionPiece = BISHOP;
                        case "knight" -> promotionPiece = KNIGHT;
                        default -> System.out.println("Invalid piece type, please try again.");
                    }
                } while (promotionPiece == null);
            }



            ChessMove move = new ChessMove(startPos, endPos, promotionPiece);

            ws.makeMove(repl.authToken, repl.gameId, move);
            whiteTurn = !whiteTurn;
            return "";
        } catch (Exception | ResponseException e) {
            return "please use a valid start and end position. Ex: move a2 a4";
        }
    }

    private String resign(String[] params) {
        if (!isPlayer) {
            return "observer cannot resign";
        }

        Scanner scanner = new Scanner(System.in);
        if (params.length != 0) {
            return "Resign Command Takes No Inputs";
        }

        System.out.println("Are you sure you would like to resign? Type 'yes' to confirm");

        String confirmation = scanner.nextLine();
        confirmation = confirmation.toLowerCase();

        if (!confirmation.equals("yes")) {
            return "Resignation canceled";
        }

        ws.resignGame(repl.gameId, repl.authToken);

        return "";
    }

    private String highlightLegalMoves(String[] params) {
        if (currentGame == null) {
            return "no game to draw";
        } if (params.length != 1) {
            return "highlight command takes one input";
        }

        try {
            ChessPosition piece = moveParse(params[0]);
            var validMoves = currentGame.validMoves(piece);

            if (validMoves == null || validMoves.isEmpty()) {
                BoardDrawer.drawBoard(currentGame.getBoard(), repl.white);
                return "No moves possible";
            }

            BoardDrawer.drawValidMoves(currentGame.getBoard(), repl.white, validMoves);
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }

        return "";
    }

    private String redraw(String[] params) {
        if (params.length != 0) {
            return "redraw command takes no input";
        }
        if (currentGame == null) {
            return "no game to draw";
        }

        BoardDrawer.drawBoard(currentGame.getBoard(), repl.white);

        return "";
    }

    public String leave(String[] params) throws ResponseException {
        if (params.length != 0) {
            return "leave Command Takes No Inputs";
        }

        ws.leaveSession(repl.gameId, repl.authToken);

        repl.setLoggedIn(repl.authToken);
        return "";
    }

    public String help() throws ResponseException {
        return """
                highlight <Piece Position> - highlight all legal moves for a given piece
                move <Start Position> <End Position> - make a move!
                resign - forfeit the chess match
                leave - go back to menu (doesn't forfeit the game)
                quit - Exit application
                help - Learn the commands
                """;
    }

    ChessPosition moveParse(String position) throws ResponseException {
        if (position.length() != 2) {
            throw new ResponseException("invalid move");
        }

        char colChar = Character.toLowerCase(position.charAt(0));
        int col = colChar - 'a' + 1;

        int row = Character.getNumericValue(position.charAt(1));

        if (row < 0 || row > 8 || col < 0 || col > 8) {
            throw new ResponseException("invalid move");
        }

        return new ChessPosition(row, col);
    }

    public void enter() {
        try {
            this.ws = new WebSocketFacade(serverUrl, this);

            ws.joinPlayer(repl.authToken, repl.gameId);

        } catch (DeploymentException | URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void notify(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case LOAD_GAME -> {
                LoadGameMessage loadGame = (LoadGameMessage) message;
                this.currentGame = loadGame.getGame();

                System.out.println();

                BoardDrawer.drawBoard(loadGame.getGame().getBoard(), repl.white);
            }
            case ERROR -> {
                ErrorMessage error = (ErrorMessage) message;

                System.out.println();
                System.out.println(error.getErrorMessage());
            }
            case NOTIFICATION -> {
                NotificationMessage noti = (NotificationMessage) message;

                System.out.println();
                System.out.println(noti.getMessage());
            }
        }
    }
}
