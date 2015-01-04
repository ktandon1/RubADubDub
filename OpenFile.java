import java.io.*;
import java.util.*;

class OpenFile {
    public static Scanner openToRead(File fileName)
    {
        Scanner fromFile = null;
        try {
            fromFile = new Scanner(fileName);
        }
        catch(FileNotFoundException e)
        {
            System.out.println("\n Error: File could not be found");
            System.exit(1);
        }
        return fromFile;
    }
    public static PrintWriter openToWrite(String fileString)
    {
        PrintWriter outFile = null;
        try {
                outFile = new PrintWriter(fileString);
        }
        catch(Exception e)
        {
            System.out.println("\n Error: File could not be created " + fileString);
            System.exit(1);
        }
        return outFile;
    }   
}