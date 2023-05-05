package it.polimi.ingsw.View.Cli;
import it.polimi.ingsw.Network.Client.ClientModel;
import it.polimi.ingsw.Network.Messages.*;
import it.polimi.ingsw.Network.ObserverImplementation.Observer;
import it.polimi.ingsw.View.OBSMessages.*;
import it.polimi.ingsw.View.OBSMessages.OBS_ChatMessage;
import it.polimi.ingsw.View.View;
import it.polimi.ingsw.model.Cards.ObjectCard;
import it.polimi.ingsw.model.Enums.ClientState;
import it.polimi.ingsw.model.Enums.Color;
import it.polimi.ingsw.model.Utility.Couple;
import it.polimi.ingsw.model.Utility.Position;

import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
/**This class handles input and output in cli version
 * @author: Riccardo Figini*/
public class Cli extends View implements Observer<ClientModel,Message>,Runnable {
    private final Scanner scanner;
    private boolean chatAvailable;
    private ClientState state;
    private String inputRequestBuffer;
    /**Constructor
     * @author: Riccardo Figini*/
    public Cli(ClientModel clientModel)
    {
        scanner = new Scanner(System.in);
        clientModel.addObserver(this);
        chatAvailable=false;
        state=ClientState.CHAT;
    }

    public void askInitialInfo()
    {
        String chosenUsername=askUsername();
        int chosenTechnology=askTechnology();
        String chosenAddress=askAddress();
        int chosenPort=0;
        if(chosenTechnology==0)
            chosenPort=askPort(defaultRMIPort);
        else
            chosenPort=askPort(defaultSocketPort);

        OBS_Message msg = new OBS_InitialInfoMessage(chosenUsername,chosenTechnology,chosenAddress,chosenPort);

        setChanged();
        notifyObservers(msg);
    }


    public String askUsername()
    {
        System.out.print("Type your username: ");
        return getInputRequest();
    }

    public int askTechnology()
    {
        String input;
        int parsedInput=0;
        boolean badInput;
        do
        {
            badInput=false;
            System.out.println("Select the communication technology to use,");
            System.out.print("RMI = '0'; Socket = '1': ");
            input = getInputRequest();
            try
            {
                parsedInput = Integer.parseInt(input);
                if (parsedInput > 1 || parsedInput < 0)
                    badInput = true;
            } catch (NumberFormatException e) {
                badInput = true;
            }
            finally
            {
                if(badInput)
                    System.out.println("ERROR: You typed an invalid input, please retry.\n");
            }
        }while (badInput);
        return parsedInput;
    }


    public String askAddress()
    {
        String input;
        System.out.print("Type a server address [Default: 'localhost']: ");
        input = getInputRequest();
        if(input.equals(""))
            return "localhost";
        else
            return input;
    }


    public int askPort(int defaultPort)
    {
        String input;
        int parsedInput;
        boolean badInput;
        do
        {
            badInput=false;
            System.out.print("Type a server port number [Default: ");
            parsedInput=defaultPort;
            System.out.print(parsedInput+"]: ");
            input = getInputRequest();
            if(!input.equals(""))
            {
                try
                {
                    parsedInput = Integer.parseInt(input);
                    if (parsedInput <= 0)
                        badInput = true;
                } catch (NumberFormatException e) {
                    badInput = true;
                }
                finally
                {
                    if(badInput)
                        System.out.println("ERROR: You typed an invalid input, please retry.\n");
                }
            }
        }while (badInput);
        return parsedInput;
    }



    /**Print generic string
     * @author: Riccardo Figini
     * @param s String to print
     * */
    @Override
    public void printAString(String s)
    {
        System.out.println(s);
    }
    /**Ask number of player
     * @author: Riccardo Figini
     * @param min Minimum number of players
     * @param max Maximum number of player
     * @return {@code int} Number of player*/
    @Override
    public void askNumberOfPlayers(int min, int max)
    {
        String input;
        Boolean badInput;
        int value=0;
        do
        {
            System.out.println("You are the first player, please type the number of players,");
            System.out.print("[Number must be between " + min + " and " + max+"]: ");
            badInput=false;
            input = getInputRequest();
            try {
                value = Integer.parseInt(input);
                if(value<min || value> max)
                {
                    badInput=true;
                }
            }
            catch (NumberFormatException e)
            {
                badInput=true;
            }
            if(badInput)
            {
                System.out.println("ERROR: You typed an invalid number, please retry\n");
            }
        }while(badInput);

        OBS_Message msg = new OBS_NumberOfPlayerMessage(value);
        setChanged();
        notifyObservers(msg);
    }
    /**It handles invalid username chosen
     * @return {@code String} Username
     * */
    @Override
    public void onInvalidUsername()
    {
        System.out.println("The typed username was already used, please type another username or try later");
        OBS_Message msg = new OBS_ChangedUsernameMessage(askUsername());

        setChanged();
        notifyObservers(msg);
    }
    /**
     * Show grid
     * @author: Riccardo Figini
     * */
    @Override
    public void showGrid(ObjectCard[][] matrice)
    {
        String top_header = "|   |";
        String valoreStringa;
        System.out.println("Living room grid:");
        for(int colonna=1; colonna<=matrice.length;colonna++)
        {
            top_header+="| "+colonna+" |";
        }
        System.out.println(top_header);


        for (int riga = 0; riga < matrice.length; riga++) {
            System.out.print((riga+1) + " -> ");
            for (int colonna = 0; colonna < matrice[0].length; colonna++) {

                if(matrice[riga][colonna]==null)
                {
                    valoreStringa = " ";
                }
                else
                    valoreStringa = ""+matrice[riga][colonna];
                System.out.print("| "+valoreStringa+" |");
            }
            System.out.println();
        }

    }
    /**
     * Show Library of specific player
     * @author: Riccardo Figini
     * @param library library to print
     * */
    @Override
    public void showLibrary(ObjectCard[][] library, String username){
        if(library == null)
            System.err.println("Library is null, somethings goes wrong");
        System.out.println(username + "'s library");
        String bottom_header = "|   |";
        String bottom_row = "";
        for(int colonna=1; colonna<=library[0].length;colonna++)
        {
            bottom_header+="| "+colonna+" |";
            bottom_row+="-----";
        }

        for(int i=0; i<library.length; i++)
        {
            System.out.print((i+1)+" -> ");
            for(int j=0; j<library[0].length; j++)
            {
                System.out.print("| "+library[i][j]+" |");
            }
            System.out.println();
        }
        System.out.println(bottom_header);
    }
    /**Ask cli to do something
     * @param arg type of message to manage
     * @param o client model
     * */
    @Override
    public void update(ClientModel o, Message arg) {
        switch (arg.getType())
        {
            case UPDATE_GRID_MESSAGE -> {
                ObjectCard[][] obs = ((MessageGrid) arg).getGrid();
                showGrid(obs);
            }
            case UPDATE_LIBRARY_MESSAGE -> {
                ObjectCard[][] obs = ((MessageLibrary) arg).getLibrary();
                showLibrary(obs, ((MessageLibrary) arg).getOwnerOfLibrary());
            }
            case COMMON_GOAL -> showPoint( (MessageCommonGoal) arg);
        }
    }
    /**It prints points (from common goal card) achieved from specific player
     * @param arg Message with all information about points achieved from common goal card. It re-used an existing class to
     * pass paramter. Player contained in arg is who has achieved common goal. Points gained are conteined in "getGainedPointsFirstCard".
     * New points available are contained in PointAvailable1 and PointAvailable2*/
    private void showPoint(MessageCommonGoal arg) {
        System.out.println("In " + arg.getPlayer() + "'s move: ");
        if(arg.getGainedPointsSecondCard()==1)
            System.out.println("First common goal card has been reached");
        else if(arg.getGainedPointsSecondCard()==2)
            System.out.println("Second common goal card has been reached");
        else if(arg.getGainedPointsSecondCard() == 3)
            System.out.println("Both commons goal card has been reached");
        System.out.println("He has " + arg.getGainedPointsFirstCard() + " points now");
        System.out.println("Point for common goal card 1: " + arg.getPointAvailable1());
        System.out.println("Point for common goal card 2: " + arg.getPointAvailable2());
    }
    /**Print everything in client model
     * @author: Riccardo Figini
     * @param clientObject Copy of model kept by severer
     * */
    @Override
    public void printAll(ClientModel clientObject) {
        clearScreen();
        showGrid(clientObject.getGrid());
        System.out.println("First common goal: " + clientObject.getDescriptionFirstCommonGoal());
        System.out.println("Second common goal: " + clientObject.getDescriptionSecondCommonGoal());
        printPersonalGaol(clientObject.getGoalList());
        Map<String, ObjectCard[][]> map = clientObject.getAllLibrary();
        for(Map.Entry<String, ObjectCard[][]> entry : map.entrySet() ){
            showLibrary(clientObject.getLibrary(entry.getKey()), entry.getKey() );
        }
        System.out.println("Points:");
        printPoints(clientObject);
    }
    /**Prints points
     * @author: Riccardo Figini
     * @param clientObject model lato cliet
     * */
    @Override
    public void printPoints(ClientModel clientObject) {
        Map<String, Integer> map1 = clientObject.getPointsMap();
        for(Map.Entry<String, Integer> entry : map1.entrySet()){
            System.out.println("- "+entry.getKey()+": "+entry.getValue());
        }
    }

    @Override
    public void errorCreatingClient(String chosenAddress, int chosenPort) {
        System.out.println("Error >> It was impossible to create a client and contact the server at [" + chosenAddress + "," + chosenPort + "]");
    }

    /**Print personal goal card
     * @author: Riccardo Figini
     * @param goalVector Personal goal card vector
     * */
    private void printPersonalGaol(ArrayList<Couple> goalVector) {
        System.out.println("Here personal goal card: ");
        if(goalVector==null)
            System.err.println("Error, list of goal vector null");
        for(int i=0; i<goalVector.size(); i++){
            Position p = (Position) (goalVector.get(i).getFirst());
            Color c = (Color) (goalVector.get(i).getSecond());
            System.out.println("row: " + (p.getRow()+1) + ", column: " + (p.getColumn()+1)+", color: "+ c);
        }
    }
    /**Ask position in which extract cards
     * @author: Riccardo Figini
     * @param numberOfCards Number of card in input
     * @return {@code Position[]} Vector with position
     * */
    private Position[] askPositions(int numberOfCards)
    {
        int row, column;
        Position[] positions = new Position[numberOfCards];
        System.out.println("Now draw "+numberOfCards+" cards");
        for(int i=0; i<numberOfCards; i++){
            System.out.println("Card number " + i);
            System.out.println("Row: ");
            row= getNumberWithLimit(10)-1;
            System.out.println("Column: ");
            column= getNumberWithLimit(10)-1;
            positions[i] = new Position(row, column);
        }
        return positions;
    }
    /**Get a generic number with lower limit = 0 and upper limit as parameter
     * @author: Riccardo Figini
     * @param limit Upper limit
     * @return {@code int} inseted number
     * */
    private int getNumberWithLimit(int limit)
    {
        String input;
        int number;
        input = getInputRequest();
        while (true) {
            try {
                number = Integer.parseInt(input);
                if(number<=0 || number>limit)
                    throw new NumberFormatException();
                return number;
            } catch (NumberFormatException e) {
                System.out.println("That is not a good number! Try again...");
            }
            input = getInputRequest();
        }
    }
    /**Ask move
     * @author: Riccardo Figini*
     * @return {@code messahe} Message with move (Column and positions)*/
    @Override
    public void askMove()  {
        int column, numberOfCards;
        Position[] position;
        System.out.println("It's your turn, please make your move");
        System.out.println("How many card do you want? (minimum 1, max 3)");
        numberOfCards = getNumberWithLimit(3);

        position = askPositions(numberOfCards);

        System.out.println("In which column do you want insert this cards?");
        column= getNumberWithLimit(5)-1;

        OBS_Message msg = new OBS_MoveMessage(position, column);

        setChanged();
        notifyObservers(msg);

    }

    /**Clear the screen
     * @author: Riccardo Figini
     * */
    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }


    private String getInputRequest()
    {
        synchronized (this)
        {
            state=ClientState.REQUEST;
            try {
                this.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return inputRequestBuffer;
    }


    @Override
    public void run()
    {
        String input;
        OBS_Message msg;
        while (true)
        {
            input=scanner.nextLine();
            synchronized (this)
            {
                if (state == ClientState.CHAT)
                {
                    if(chatAvailable)
                    {
                        msg = new OBS_ChatMessage(input);
                        setChanged();
                        notifyObservers(msg);
                    }
                }
                else if (state == ClientState.REQUEST)
                {
                    inputRequestBuffer=input;
                    this.notifyAll();
                    state=ClientState.CHAT;
                }
            }
        }
    }

    @Override
    public void startChat() {
        chatAvailable=true;
    }


}