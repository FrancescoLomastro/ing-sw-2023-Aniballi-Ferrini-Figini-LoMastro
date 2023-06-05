package it.polimi.ingsw.Network.Servers;

import it.polimi.ingsw.Network.Messages.ServerPingMessage;
import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.controller.ServerReceiver;

import java.io.IOException;
import java.util.TimerTask;

public class PingerTask extends TimerTask {

    private final ServerReceiver server;
    private final Connection connection;

    public PingerTask(ServerReceiver server, Connection connection){
        this.server = server;
        this.connection = connection;
    }
    @Override
    public void run()
    {
            try {
                connection.sendMessage(new ServerPingMessage(connection.getPlayerName()));
            } catch (IOException e) {
                server.tryToDisconnect(connection, connection.getPlayerName());
            }
    }
}