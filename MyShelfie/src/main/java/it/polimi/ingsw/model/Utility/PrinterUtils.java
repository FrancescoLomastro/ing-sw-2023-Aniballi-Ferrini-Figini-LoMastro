package it.polimi.ingsw.model.Utility;

public class PrinterUtils {
    public static String printEquivalentSpace(String string)
    {
        String s="";
        for(int i=0; i<string.length();i++)
            s+=" ";
        return s;
    }
}
