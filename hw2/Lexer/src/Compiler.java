import FrontEnd.Input;
import FrontEnd.Lexer;
import FrontEnd.TokenStream;

import java.io.IOException;
import java.io.InputStream;

public class Compiler {
    public static void main(String[] args) throws IOException {
        Input input = new Input("testfile.txt");
        InputStream source = input.getInput();
        Lexer lexer = new Lexer(source);
        TokenStream tokenStream = new TokenStream(lexer.getTokenArrayList());
        tokenStream.output("output.txt");
    }
}
