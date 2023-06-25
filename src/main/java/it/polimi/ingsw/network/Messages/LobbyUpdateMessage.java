package it.polimi.ingsw.network.Messages;

import java.util.List;


/**
 * This message returns an update reporting a lobby description and the size of the lobby
 */
public class LobbyUpdateMessage extends Message {
    private final List<String> lobbyUsernames;
    private final int lobbySize;

    /**
     * Constructor: Creates a lobby update massage with the list of players in the lobby
     * @param lobbyUsernames is the list of usernames in the lobby
     * @param lobbySize is the max size of the lobby
     */
    public LobbyUpdateMessage(List<String> lobbyUsernames, int lobbySize) {
        super("Server",MessageType.LOBBY_UPDATE_MESSAGE);
        this.lobbyUsernames = lobbyUsernames;
        this.lobbySize = lobbySize;
    }

    public List<String> getLobbyUsernames() {
        return lobbyUsernames;
    }

    public int getLobbySize() {
        return lobbySize;
    }
}
