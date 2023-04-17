package it.polimi.ingsw.Network.Messages;

/**
 * class for messages about the availability of a move
 * @author Andrea Ferrini
 * */
public class MessageAfterMovePositive extends Message{

    int gainedPointsFirstCard = 0;
    int gainedPointsSecondCard = 0;

    /**
     * constructor
     * */
    public MessageAfterMovePositive(){
        super(MessageType.AFTER_MOVE_POSITIVE);
    }

    public int getGainedPointsFirstCard() {
        return gainedPointsFirstCard;
    }

    public void setGainedPointsFirstCard(int gainedPointsFirstCard) {
        this.gainedPointsFirstCard = gainedPointsFirstCard;
    }

    public int getGainedPointsSecondCard() {
        return gainedPointsSecondCard;
    }

    public void setGainedPointsSecondCard(int gainedPointsSecondCard) {
        this.gainedPointsSecondCard = gainedPointsSecondCard;
    }
}
