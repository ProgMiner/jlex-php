package by.progminer.JLexPHP;

import by.progminer.JLexPHP.Utility.Utility;

/**
 * Top-level lexical analyzer generator function.
 */
public class Main {

    public final static String APP_NAME    = "JLexPHP";
    public final static String APP_VERSION = "1.0-SNAPSHOT";

    public static void main(String args[]) {
        System.out.println(APP_NAME + " version " + APP_VERSION);

        if (args.length < 1) {
            System.out.println("Usage: " + APP_NAME + "-" + APP_VERSION + ".jar <input file> [<output file>]");
            return;
        }

        String in = args[0];
        String out = in + ".php";

        if (args.length > 1) {
            out = args[1];
        }

        System.out.println();
        System.out.println("Input file: " + in);
        System.out.println("Output file: " + out);

        // Note: For debuging, it may be helpful to remove the try/catch
        //       block and permit the Exception to propagate to the top level.
        //       This gives more information.

        try {
            System.out.println();
            System.out.println("Start generating");

            LexGen lg = new LexGen(in, out);
            lg.generate();

            System.out.println();
            System.out.println("Complete!");
        } catch (Throwable e) {
            if (Utility.DEBUG) {
                e.printStackTrace();
            } else {
                System.err.println(">>> " + e.getLocalizedMessage());
            }
        }
    }
}
