import java.io.*;
import java.util.*;

public class StringLengthComparator implements Comparator<File> {
    public int compare(File o1, File o2) {
        int a = getFileNumber(o1.getName());
        int b = getFileNumber(o2.getName());
        if (a < b) {
            return -1;
        } else if (a > b) {
            return 1;
        } else {
            return 0;
        }
    }
    public int getFileNumber(String fileName)
    {
       int x = fileName.lastIndexOf("_");
       int y = fileName.lastIndexOf(".");
       int a = Integer.parseInt(fileName.substring(x+1,y));
       return a;
    }
}
