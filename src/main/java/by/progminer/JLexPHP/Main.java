package by.progminer.JLexPHP;

/**
 * Top-level lexical analyzer generator function.
 */
public class Main {
    
    public static void main(String args[]) throws java.io.IOException {
        LexGen lg;

        if (args.length < 1) {
            System.out.println("Usage: JLexPHP.Main <filename>");
            return;
        }
    
        String in = args[0];
        String out = in + ".php";
        
        if (args.length > 1) {
            out = args[1];
        }

        // Note: For debuging, it may be helpful to remove the try/catch
        //       block and permit the Exception to propagate to the top level.
        //       This gives more information.

        // try {
            lg = new LexGen(in, out);
            lg.generate();
        // } catch (Error e) {
        //     System.out.println(e.getMessage());
        // }
    }
}
