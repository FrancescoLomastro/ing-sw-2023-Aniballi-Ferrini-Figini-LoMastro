package it.polimi.ingsw.model.Cards.ConcreteCommonCards;

import it.polimi.ingsw.model.Cards.*;
import it.polimi.ingsw.model.Enums.*;
import it.polimi.ingsw.model.Player.*;

/**
 * This class contains the algorithm to verify if the following common goal is satisfied.
 * The goal is: Five tiles of the same type forming an X.
 *
 * @author: Alberto Aniballi
 * */
public class CommonGoalCard9 extends CommonGoalCard {
    /**
     * It verifies if the library satisfies the goal of this specific common goal card
     *
     * @param library   the library on which we will verify if the goal is satisfied or not
     * @return: boolean that is true if the goal is satisfied, false otherwise
     * */
    @Override
    public boolean isSatisfied(Library library) {
        /*
        Cinque tessere delle stesso tipo che formano una x.
        Idea:
        - Verifico usando come punto di partenza la cella centrale della stella.
        - sicuramente la ricerca è ristretta al riquadro interno (5x4) per le celle centrali. Poiche quelle nei lati esterni non possono avere la stella completa.
        - Itero su tutte le celle del riquadro interno cercando la formazione a stella, mi fermo una volta trovata oppure quando sono finite le celle.
        Sarà poi da aggiungere la verifica che la carta sia effettivamente presente (nel senso library[row][col] != null
         */
        for(int row=1;row<=4;row++) {
            for (int col=1;col<=3;col++) {
                if (library.getLibrary()[row][col] != null) {
                    Color centralCellColor = library.getLibrary()[row][col].getColor();
                    if ((library.getLibrary()[row + 1][col - 1]!=null) && library.getLibrary()[row + 1][col - 1].getColor().equals(centralCellColor)) {
                        if ((library.getLibrary()[row - 1][col - 1]!=null) && library.getLibrary()[row - 1][col - 1].getColor().equals(centralCellColor)) {
                            if ((library.getLibrary()[row + 1][col + 1]!=null) && library.getLibrary()[row + 1][col + 1].getColor().equals(centralCellColor)) {
                                if ((library.getLibrary()[row - 1][col + 1]!=null) && library.getLibrary()[row - 1][col + 1].getColor().equals(centralCellColor)) {

                                    // Checking that the other cells inside the square but not in X have different color
                                    if (!(library.getLibrary()[row - 1][col].getColor().equals(centralCellColor)) &&
                                            !(library.getLibrary()[row + 1][col].getColor().equals(centralCellColor)) &&
                                            !(library.getLibrary()[row][col - 1].getColor().equals(centralCellColor)) &&
                                            !(library.getLibrary()[row][col + 1].getColor().equals(centralCellColor))) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
