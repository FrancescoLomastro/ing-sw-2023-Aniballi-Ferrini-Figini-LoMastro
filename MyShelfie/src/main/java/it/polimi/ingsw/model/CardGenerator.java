package it.polimi.ingsw.model;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

//TODO devo ancora testarla per vedere se riesce a prendere tutti i dati e a scriverli

//TODO IMPORTANTE: NEGLI OGGETTI DI RITORNO FAI SEMPRE UNA COPIA, NON PASSARE RIFERIMETNO

/**IDEA DEI FILE: ho a disposizione 3 file di base in cui sono salvate tutte le carte e i vettori
 * con i valori che devono avere inizialemente. Quando faccio partire la partita inizializzo questa
 * classe passandogli un numero identificativo. Allora questa classe crea 3 file in cui sono
 * presenti solo i 3 vettori che gestiscono le carte prelevate e ancora da prelevare
 * Per semplicità i vettori all'interno dei 3 file di base e dei tre file riferiti alla singola
 * partita hanno lo stesso nome*/
public class CardGenerator {
    private final String pathCommonGoalFile;
    private final String pathPersonalGoalFile;
    private final String pathObjectCardFile;
    private final String generalArray;
    private final String controllerArray;
    private final int numberOfPlayer;
    private final String personalCardArray="personalCardArray";
    private final String commonCardArray="commonCardArray";
    private final String objectCardArray="objectCardArray";
    private final String gamePath;
    private final CustomizedFunction<CommonGoalCard>[] factoryMethodArray =
            new CustomizedFunction[]{CommonGoalCard0::new, CommonGoalCard1::new, CommonGoalCard2::new, CommonGoalCard3::new, CommonGoalCard4::new,
                    CommonGoalCard5::new, CommonGoalCard6::new, CommonGoalCard7::new, CommonGoalCard8::new, CommonGoalCard9::new,
                    CommonGoalCard10::new, CommonGoalCard11::new};

    /**Constructor: create path, adding gameNumber in those associated with specific game.
     * Simultaneously can exist different game, so exist different file which can provide
     * information about card
     * @author Riccardo Figini
     * @param gameNumber ID number of game
     * @param numberOfPleyer number of player in the game*/
    public CardGenerator(int gameNumber, int numberOfPleyer){
        this.pathCommonGoalFile =System.getProperty("user.dir")+"/risorse/CommonCards.json";
        this.pathPersonalGoalFile =System.getProperty("user.dir")+"/risorse/PersonalCards.json";
        this.pathObjectCardFile =System.getProperty("user.dir")+"/risorse/ObjectCards.json";
        this.generalArray = "generalArray";
        this.controllerArray = "controllerArray";
        this.gamePath=System.getProperty("user.dir")+"/risorse/Game"+gameNumber+".json";
        this.numberOfPlayer=numberOfPleyer;
        initFileWithArray();
    }
    /**Casual generation of Common goal card. Attribute number is associated with free
     * commond goal card, that it's not already in the game. Allocate with factoryMethod
     * and return the CommondGoalCard chosen.
     * @author Riccardo Figini
     * @return commonGoalCard*/
    public CommonGoalCard generateCommonGoalCard() {
        int number;
        number=casualGenerationOfNumber(commonCardArray);
        CommonGoalCard commonGoalCard = factoryMethodArray[number].apply();
        commonGoalCard.setDescription(getDescriptionFromFile(pathCommonGoalFile, number));
        commonGoalCard.setScorePointCard(new ScorePointCard(numberOfPlayer));
        return commonGoalCard;
    }
    /**Casual generation of personal goal card. Attribute number is associated with
     * free personal goal card (like generateCommonGoalCard). Get description
     * and couple from PersonalGoalCards.json. Return personalGoalCard
     * @author Riccardo Figini
     * @return PersonalGoalCard*/
    public PersonalGoalCard generatePersonalGoalCard() {
        int number;
        String descrption;
        number = casualGenerationOfNumber(personalCardArray);
        descrption = getDescriptionFromFile(pathPersonalGoalFile, number);
        ArrayList<Couple> couples = getCouplesFromFile(number);
        return new PersonalGoalCard(descrption, couples);
    }
    /**Casual generation of object card, in "actual game exsist 132 card, 22 for color.
     * Every color have 3 different type (8 cards type 1, 7 cards type 2, 7 cards type 3).
     * return object card
     * @author Riccardo Figini
     * @return ObjectCard
     * @exception RuntimeException*/
    public ObjectCard generateObjectCard() {
        int number;
        String description;
        number=casualGenerationOfNumberCounted(objectCardArray);
        JsonObject jsonObject = getJsonObjectInArray(pathObjectCardFile, number);
        description = getDescriptionFromFile(pathObjectCardFile, number);
        return new ObjectCard(description,
                Color.valueOf(jsonObject.get("Color").getAsString()) ,
                Type.valueOf(jsonObject.get("Type").getAsString()));
    }
    /**Casual generation of number with limit. This method read a jsonArray from json game file
     *  and generate available number (number isn't available if in the position "number" of jsonarray
     *  is 0)
     *  @author Riccardo Figini
     *  @param path string contained path of game's file
     *  @return number integer
     *  @exception RuntimeException*/
    private int casualGenerationOfNumberCounted(String path)  {
        int number;
        Random random = new Random();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Reader reader = getReader(gamePath);

        JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
        JsonArray jsonArray = jsonObject.getAsJsonArray(path);
        checkIsEmpty(jsonArray);

        do{
            number = random.nextInt(jsonArray.size());
        }while( jsonArray.get(number).getAsInt() <= 0);

        JsonObject input = new JsonObject();
        input.addProperty("numero", jsonArray.get(number).getAsInt() -1 );
        jsonArray.set(number, input.get("numero"));

        FileWriter file = getWriter(gamePath);
        try {
            file.write(gson.toJson(jsonObject));
            file.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        closeFunctionWriter(file);
        closeFunctionReader(reader);
        return number;
    }

    /**Return true if jsonArray contains only 0
    @author Riccardo Figini
    @param jsonArray this jsonarray contains the number of card available for each object card
    * */
    private void checkIsEmpty(JsonArray jsonArray){
        for(int i=0; i<jsonArray.size();i++)
            if(jsonArray.get(i).getAsInt() > 0)
                return;
        refill();
    }
    /**This method refill Object card's controllerArray
     * @author: Ricccardo Figini
     * */
    private void refill(){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Reader reader0 = getReader(pathObjectCardFile);
        Reader reader1 = getReader(gamePath);
        JsonObject jsonObject0 = gson.fromJson(reader0, JsonObject.class);
        JsonObject jsonObject1 = gson.fromJson(reader1, JsonObject.class);
        JsonArray jsonArray = jsonObject0.getAsJsonArray(controllerArray);
        jsonObject1.remove(objectCardArray);
        jsonObject1.add(objectCardArray, jsonArray);
        closeFunctionReader(reader0);
        closeFunctionReader(reader1);
        Writer writer = getWriter(gamePath);
        try {
            writer.write(gson.toJson(jsonObject1));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    /**Casual generation number. This number is associated with a card, so this method
     * provides to a casual generation of card usable and available
     * @author Riccardo Figini
     * @param path string contained path of game's file
     * @return number integer
     * @exception RuntimeException*/
    private int casualGenerationOfNumber(String path) {
        int number;
        Random random = new Random();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Reader reader = getReader(gamePath);

        JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
        JsonArray jsonArray= jsonObject.getAsJsonArray(path);

        do{
            number = random.nextInt(jsonArray.size());
        }while( jsonArray.get(number).getAsBoolean());

        JsonObject input = new JsonObject();
        input.addProperty("this", true);
        jsonArray.set(number, input.get("this"));

        FileWriter file = getWriter(gamePath);
        try {
            file.write(gson.toJson(jsonObject));
            file.flush();
            file.close();
        } catch (IOException e) {
            throw new RuntimeException("Error in effectively write in file json, " +
                    "info path: " +gamePath,e);
        }
        closeFunctionReader(reader);
        closeFunctionWriter(file);
        return number;
    }
    /**Read Personal goal card from json file and write value in couple
     * @author Riccardo Figini
     * @param number integer associated with card
     * @return couples
     * */
    private ArrayList<Couple> getCouplesFromFile(int number){
        JsonObject obj4 = getJsonObjectInArray(pathPersonalGoalFile, number);
        JsonArray colorArray = obj4.getAsJsonArray("color");
        JsonArray xCoordinate =  obj4.getAsJsonArray("x");
        JsonArray yCoordinate =  obj4.getAsJsonArray("y");
        ArrayList<Couple> couples = new ArrayList<>();
        for(int i=0; i<colorArray.size(); i++){
            Couple couple = new Couple(
                    new Position(xCoordinate.get(i).getAsInt(), yCoordinate.get(i).getAsInt()),
                    Color.valueOf(colorArray.get(i).getAsString())
            );
            couples.add(couple);
        }
        return couples;
    }

    /**This method return jsonObject conteined in jsonarray.
     * jsonarray is a container of cards (Can be any card type)
     * @author Riccardo Figini
     * @param path path of json file with information
     * @param number integer used to get card's object from file
     * */
    private JsonObject getJsonObjectInArray(String path, int number){
        Gson gson = new Gson();
        FileReader fileReader = getReader(path);

        JsonObject obj2 = gson.fromJson(fileReader, JsonObject.class);
        JsonArray obj3 = obj2.getAsJsonArray(generalArray);
        closeFunctionReader(fileReader);

        return obj3.get(number).getAsJsonObject();
    }

    /**return description of specific card
     * @author Riccardo figini
     * @param path path of json file with information
     * @param number integer used to get card's description from file
     * */
    private String getDescriptionFromFile(String path, int number) {
        Gson gson = new Gson();
        JsonObject jsonObject;
        Reader reader = getReader(path);

        jsonObject = gson.fromJson(reader, JsonObject.class);
        JsonArray jsonArray = jsonObject.getAsJsonArray(generalArray);
        jsonObject = jsonArray.get(number).getAsJsonObject();

        closeFunctionReader(reader);

        return jsonObject.get("description").getAsString();
    }
    /**Create game file with initial array.
     * This method reads controllerArrays from a file and writes them into another.
     * @author Riccardo Figini
     * @exception RuntimeException*/
    private void initFileWithArray() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Reader reader0 = getReader(pathCommonGoalFile);
        Reader reader1 = getReader(pathObjectCardFile);
        Reader reader2 = getReader(pathPersonalGoalFile);
        JsonObject jsonObject0 = gson.fromJson(reader0, JsonObject.class);
        JsonObject jsonObject1 = gson.fromJson(reader1, JsonObject.class);
        JsonObject jsonObject2 = gson.fromJson(reader2, JsonObject.class);
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(commonCardArray, jsonObject0.getAsJsonArray(controllerArray));
        jsonObject.add(objectCardArray, jsonObject1.getAsJsonArray(controllerArray));
        jsonObject.add(personalCardArray, jsonObject2.getAsJsonArray(controllerArray));
        closeFunctionReader(reader0);
        closeFunctionReader(reader1);
        closeFunctionReader(reader2);
        Writer writer = getWriter(gamePath);
        try {
            writer.write(gson.toJson(jsonObject));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        closeFunctionWriter(writer);
    }
    /**Delete game file
     * @author Riccardo Figini
     * */
    public void deleteFileGame(){
        if(!(deleteFile(gamePath)))
            System.err.println("Delete doesn't work");
    }
    /**Delete specific file and return if it works
     * @param path
     * @return boolean */
    private boolean deleteFile(String path){
        File file = new File(path);
        return file.delete();
    }

    /**Create filewriter and handle exception
     * @author: Riccardo Figini
     * @param path file's writer path
     * @return FileWriter*/
    private FileWriter getWriter(String path){
        try{
            return new FileWriter(path);
        }
        catch(IOException e){
            throw new RuntimeException(e);
        }
    }
    /**Create fileReader and handle exception
     * @author: Riccardo Figini
     * @param path file's reader path
     * @return fileReader*/
    private FileReader getReader(String path){
        try{
            return new FileReader(path);
        }
        catch(IOException e){
            throw new RuntimeException(e);
        }
    }
    /**Close Reader and handle exception
     * @author: Riccarod Figini
     * @param file reader*/
    private void closeFunctionReader(Reader file){
        try {
            file.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    /**Close Wrtier and handle exception
     * @author Riccardo Figini
     * @param file writer*/
    private void closeFunctionWriter(Writer file){
        try {
            file.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
