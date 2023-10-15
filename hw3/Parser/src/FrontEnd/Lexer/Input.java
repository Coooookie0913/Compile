package FrontEnd.Lexer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;


public class Input {
    private String filename;
    private InputStream input;
    
    public Input(String filename) throws FileNotFoundException {
        this.filename = filename;
        this.input = new FileInputStream(filename);
    }
    
    public InputStream getInput() {
        return input;
    }
}
