package it.polimi.ingsw.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.polimi.ingsw.Network.Messages.*;
import it.polimi.ingsw.Network.Servers.Connection;
import it.polimi.ingsw.Network.Servers.PingTaskServer;
import it.polimi.ingsw.Network.Servers.PingTimer;
import it.polimi.ingsw.model.Utility.Request;

import java.io.*;
import java.util.*;

public class Controller implements ServerReceiver
{
    private Map<String, String> oldPlayer;
    private ArrayList<GameController> games;
    private GameController currentGame;
    private Request waitedRequest;
    private boolean isAsking;
    private final int minimumPlayers = 2;
    private final int maximumPlayers = 4;
    String pathFileWithNumberOfGame="src/main/resources/gameFile/gameNumbers.json";
    ArrayList<PingTimer> pingTimers;
    public Controller() {
        games = new ArrayList<>();
        currentGame= new GameController(choseNewNumberForGame(), this);
        waitedRequest=null;
        isAsking=false;
        manageOldPlayer();
        pingTimers = new ArrayList<>();
    }

    private void manageOldPlayer(){
        JsonArray arrayOfJsonCells = Objects.requireNonNull(getArrayJsonWithNumberGame()).getAsJsonArray("numOfGame");
        if (arrayOfJsonCells == null || arrayOfJsonCells.size() == 0)
            return;
        String number;
        for (JsonElement jsonCellElement : arrayOfJsonCells) {
            number = jsonCellElement.getAsString();
            String path = "src/main/resources/gameFile/ServerGame"+number+".bin";
            getPlayerFromFile(path, number);
        }
        System.out.println("File has been read, players: " + oldPlayer.size());
        for(Map.Entry<String, String> entry : oldPlayer.entrySet()){
            System.out.println("- " + entry.getKey() + ", " + entry.getValue());
        }
    }

    /**It returns jsonArray with number of ongoing game
     * @author: Riccardo Figini
     * @return {@code JsonArray} Array with number
     * */
    private JsonObject getArrayJsonWithNumberGame() {
        Gson gson = new Gson();
        oldPlayer = new LinkedHashMap<>();
        Reader reader;
        try {
            reader = new FileReader(pathFileWithNumberOfGame);
        } catch (FileNotFoundException e) {
            System.out.println("No file with old games, impossible to restore unfinished game");
            System.out.println("Server will continue without restoring games...");
            oldPlayer=null;
            return null;
        }
        return gson.fromJson(reader, JsonObject.class);
    }

    /**It adds name of player from ongoing game. It reads object "gameController" from a file indicated with path
     * @param path Game's path
     * */
    private void getPlayerFromFile(String path, String gameId) {
        GameController gameController ;
        try {
            FileInputStream finput = new FileInputStream(path);
            ObjectInputStream ois = new ObjectInputStream(finput);
            gameController= (GameController) ois.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("Error in opening file, path: "+path+", " + e);
            return;
        } catch (IOException e) {
            System.out.println("Error in opening stream to read object, " + e);
            return;
        } catch (ClassNotFoundException e) {
            System.out.println("Error in reading class from file, " + e);
            return;
        }
        ArrayList<String> list = gameController.getNameOfPlayer();
        for (String s : list) {
            oldPlayer.put(s, gameId);
        }
    }

    public void login(String username, Connection connection) {
        int id = isOldPlayer(username);
        if(id == -1) {
            if (availableUsername(username)) {
                if (currentGame.getSize() == 0) {
                    if (isAsking) {
                        try {
                            connection.sendMessage(new ErrorMessage("A lobby is being created from another user, please retry soon"));
                        } catch (IOException e) {
                            System.out.println("Couldn't ask number to the client " + username + ", dropping the request...");
                        }
                    } else {
                        waitedRequest = new Request(username, connection);
                        isAsking = true;
                        try {
                            connection.sendMessage(new PlayerNumberRequest(minimumPlayers, maximumPlayers));
                        } catch (IOException e) {
                            System.out.println("Couldn't ask number to the client " + username + ", dropping the request...");
                            waitedRequest = null;
                        }
                    }
                } else {
                    try {
                        connection.sendMessage(new AcceptedLoginMessage());
                        addPlayer(username, connection);
                    } catch (IOException e) {

                        System.out.println("Couldn't contanct client " + waitedRequest.getUsername());
                    }
                }
            } else {
                try {
                    connection.sendMessage(new InvalidUsernameMessage());
                } catch (IOException e) {
                    System.out.println("Problem contacting " + username + ", dropping the request...");
                    waitedRequest = null;
                }
            }
        }
        else
            manageOldGame(username, connection, id);
    }

    private int isOldPlayer(String username) {
        for(Map.Entry<String, String> entry : oldPlayer.entrySet()){
            if(entry.getKey().equals(username)){
                System.out.println("Old player has joined: " + username);
                return Integer.parseInt(entry.getValue());
            }
        }
        return -1;
    }

    private void addPlayer(String username, Connection connection) {
        currentGame.addPlayer(username,connection);

        PingTimer pt = new PingTimer();
        pt.setPlayerUsername(username);
        pingTimers.add(pt);
        pt.schedule(new PingTaskServer(username), 10000);

        games.add(currentGame);
        if(currentGame.getSize()==currentGame.getLimitOfPlayers())
        {

            for(PingTimer ptm: pingTimers){

                ptm.cancel();
            }
            writeNewGameInExecution(currentGame.getGameId());
            new Thread(currentGame).start();
            int num = choseNewNumberForGame();
            currentGame=new GameController(num, this);

        }
    }

    /**Write a new number of game into file
     * @author: Riccardo Figini
     * @param gameId Number to be written
     * */
    private void writeNewGameInExecution(int gameId) {
        Gson gson = new Gson();
        JsonObject jsonObject = getArrayJsonWithNumberGame();

        if(jsonObject==null) {
            System.out.println("Impossible to open jsonObject");
            System.err.println("Error");
            return;
        }

        JsonArray jsonArray = jsonObject.getAsJsonArray("numOfGame");

        if(jsonArray==null) {
            System.out.println("Impossible to open jsonArray");
            System.err.println("Error");
            return;
        }

        Writer writer;
        try {
            writer = new FileWriter(pathFileWithNumberOfGame);
            jsonArray.add(gameId);
            String jsonWrite = gson.toJson(jsonObject);
            writer.write(jsonWrite);
            writer.close();
        }catch (IOException e) {
            System.out.println("Impossible to open file to write game, " + e);
            System.err.println("Error");
            throw new RuntimeException(e);
            //TODO fai qualcosa in caso di errore
        }
    }

    /**It chose available number for game. It controls a file with every game
     * @author: Riccardo Figini
     * @return {@code int} Available number
     * */
    private int choseNewNumberForGame() {
        JsonArray arrayOfJsonCells = getArrayJsonWithNumberGame().getAsJsonArray("numOfGame");
        int max=-1, number;
        if (arrayOfJsonCells == null)
            return 1;
        for (JsonElement jsonCellElement : arrayOfJsonCells) {
            number = jsonCellElement.getAsInt();
            if(number>max)
                max=number;
        }
        max++;
        return max;
    }

    private boolean availableUsername(String username) {
        for(GameController gc : games)
        {
            if(gc.isRegistered(username))
            {
                return false;
            }
        }
        return true;
    }

    /**It adds player if his game has been re-loaded from file, otherwise load game from and add player
     * @author: Riccardo Figini
     * @param username Player's username
     * @param connection Player's connection
     * @param idGame ID of game
     * */
    private void manageOldGame(String username, Connection connection, int idGame) {
        GameController gameController = null;

        for (GameController game : games) {
            if (game.getGameId() == idGame)
                gameController = game;
        }

        if(gameController == null)
            gameController = reloadGame(idGame);

        try {
            gameController.addPlayer(username, connection);
            connection.sendMessage(new MessageReturnToGame(MessageType.RETURN_TO_OLD_GAME_MESSAGE));
            connection.sendMessage(new AcceptedLoginMessage());
            if(gameController.getSize() == gameController.getLimitOfPlayers()) {
                gameController.restartGameAfterReload();
                removePlayerFromOldList(gameController);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void removePlayerFromOldList(GameController gameController) {
        for(int i=0; i< gameController.getNameOfPlayer().size(); i++){
            oldPlayer.remove(gameController.getNameOfPlayer().get(i));
        }
    }

    private GameController reloadGame(int gameId) {
        try {
            FileInputStream file = new FileInputStream("src/main/resources/gameFile/ServerGame"+gameId+".bin");
            ObjectInputStream inputStream = new ObjectInputStream(file);
            GameController gameController = (GameController) inputStream.readObject();
            games.add(gameController);
            gameController.reloadPlayer();
            return gameController;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("File game has problems");
            throw new RuntimeException(e);
        }
    }


    @Override
    synchronized public void onMessage(Message message) {

            MessageType type = message.getType();
            switch (type)
            {
                case LOGIN_REQUEST ->
                {
                    LoginMessage msg = (LoginMessage) message;
                    login(msg.getUsername(),msg.getClientConnection());
                }
                case PLAYER_NUMBER_ANSWER ->
                {
                    if (waitedRequest.getUsername().equals(message.getUsername())) {

                        try {

                            waitedRequest.getConnection().sendMessage(new AcceptedLoginMessage());
                            PlayerNumberAnswer msg = (PlayerNumberAnswer) message;
                            currentGame.setLimitOfPlayers(msg.getPlayerNumber());
                            isAsking=false;
                            addPlayer(waitedRequest.getUsername(), waitedRequest.getConnection());
                        } catch (IOException e) {

                            System.out.println("Couldn't contact client " + waitedRequest.getUsername());
                        } finally {

                            waitedRequest = null;
                        }
                    }
                }
                case PING_MESSAGE ->
                {
                    for(PingTimer pt: pingTimers){

                        if(pt.getPlayerUsername().equals(((ServerPingMessage) message).getPlayerUsername())){

                            pt.cancel();

                            ServerPingMessage serverPingMessage = new ServerPingMessage("SERVER");
                            try {
                                currentGame.getClients().get(pt.getPlayerUsername()).sendMessage(serverPingMessage);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            pt = new PingTimer();
                            pt.setPlayerUsername(((ServerPingMessage) message).getPlayerUsername());
                            pt.schedule(new PingTaskServer(((ServerPingMessage) message).getPlayerUsername()), 10000);
                        }
                    }
                }
            }
    }
    /**Delete number game from file
     * @author: Riccardo Figini
     * @param gameId Game's id
     * */
    public void deleteGame(int gameId) {
        JsonObject j =  getArrayJsonWithNumberGame();
        JsonArray jsonArray = j.getAsJsonArray("numOfGame");

        if(jsonArray == null)
            throw new RuntimeException("Error, array with game number is null");

        for(int i=0; i< jsonArray.size(); i++){
            if(jsonArray.get(i).getAsInt() == gameId){
                jsonArray.remove(i);
                break;
            }
        }

        Gson gson = new Gson();
        Writer writer;
        try {
            writer = new FileWriter(pathFileWithNumberOfGame);
            String jsonWrite = gson.toJson(j);
            writer.write(jsonWrite);
            writer.close();
        }catch (IOException e) {
            System.out.println("Impossible to open file to write game, " + e);
            System.err.println("Error");
            throw new RuntimeException(e);
            //TODO fai qualcosa in caso di errore
        }

    }
}
