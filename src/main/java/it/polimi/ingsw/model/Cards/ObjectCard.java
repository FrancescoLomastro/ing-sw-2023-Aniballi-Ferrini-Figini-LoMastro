package it.polimi.ingsw.model.Cards;

import it.polimi.ingsw.enums.Color;
import it.polimi.ingsw.enums.Type;

import java.io.Serializable;

/**
 * This class is the concrete "Object card" class that represent cards which are placed in the board grid or in
 * player libraries.
 *
 * @author Riccardo Figini
 * */
public class ObjectCard extends Card implements Serializable {
    private final Color color;
    private final Type type;

    /**Constructor
     * @author Riccardo Figini
     * @param description objectcard's descrption
     * @param color objectcard's color
     * @param type objectcard's type
     * */
    public ObjectCard(String description, Color color, Type type) {
        super(description);
        this.color = color;
        this.type = type;
    }
    /**Get color
     * @author Riccardo Figini
     * @return color*/
    public Color getColor() {
        return color;
    }
    /**Get Type
     * @author Riccardo Figini
     * @return type*/
    public Type getType() {
        return type;
    }

    /**It verifies if this object card is equals to obj as parameter
     * @author Francesco Lo Mastro
     * @param obj Object card
     * @return True if cards are equal
     * */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ObjectCard)) {
            return false;
        }
        ObjectCard other = (ObjectCard) obj;
        return this.color == other.color && this.type == other.type;
    }

    /**It returns the card's color as string
     * @author Francesco Lo Mastro*/
    @Override
    public String toString() {
            return ""+color;
    }
}