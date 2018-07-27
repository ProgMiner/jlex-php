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

        boolean verbose = false;

        int optCount = 0;
        for (String arg: args) {
            if ("-v".equals(arg)) {
                verbose = true;
            } else if ("--verbose".equals(arg)) {
                verbose = true;
            } else {
                break;
            }

            ++optCount;
        }

        if (args.length <= optCount) {
            System.out.println("Usage: " + APP_NAME + "-" + APP_VERSION + ".jar [-v|--verbose] <input file> [<output file>]");
            return;
        }

        String in = args[optCount];
        String out = in + ".php";

        if (args.length > optCount + 1) {
            out = args[optCount + 1];
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
            System.out.println();

            LexGen lg = new LexGen(in, out);
            lg.setVerbose(verbose);

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
