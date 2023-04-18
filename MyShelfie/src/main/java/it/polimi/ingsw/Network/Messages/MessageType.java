package it.polimi.ingsw.Network.Messages;


public enum MessageType
{
    PLAYERNUMBER_ANSWER,
    LOGIN_REQUEST,
    PLAYERNUMBER_REQUEST,
    ACCEPTEDLOGIN_MESSAGE,
    INVALIDUSERNAME_MESSAGE,
    LOBBYUPDATE_MESSAGE,
    UPDATE_GRID_MESSAGE,
    UPDATE_LIBRARY_MESSAGE,
    MY_MOVE_ANSWER,
    MY_MOVE_REQUEST,
    INIT_PLAYER_MESSAGE,
    NEW_GAME_SERVER_MESSAGE,
    START_GAME_MESSAGE,
    SOCKET_LOGIN_REQUEST,
    RMI_LOGIN_REQUEST, ERROR,
    AFTER_MOVE_POSITIVE,
    AFTER_MOVE_NEGATIVE,
    WINNER,
    COMMON_GOAL;
}