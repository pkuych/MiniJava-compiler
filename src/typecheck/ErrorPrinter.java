package typecheck;

import java.util.ArrayList;

/**
 * @author yuchanghong
 * Created on 2020/3/8
 */
public class ErrorPrinter {
    private static int errorSize = 0;
    private static ArrayList<String> errorMessage = new ArrayList<>();

    public static int getErrorSize() {
        return errorSize;
    }

    public static void printAll() {
        for (String msg: errorMessage) {
            System.out.println(msg);
        }
    }

    public static void print(String msg, int row, int col) {
        System.out.printf("Error:(%d, %d), %s\n", row, col, msg);
    }

    public static void printResult() {
        if (errorSize == 0) {
            System.out.println("Program type checked successfully");
        } else {
            System.out.println("Type check error");
            printAll();
        }
    }
}
