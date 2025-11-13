package model;

import chess.ChessGame;

public record GameJoinRequired(ChessGame.TeamColor playerColor, int gameID) {
}
