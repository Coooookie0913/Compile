import FrontEnd.Lexer.Input;
import FrontEnd.Lexer.Lexer;
import FrontEnd.Lexer.TokenStream;
import FrontEnd.Parser.Node;
import FrontEnd.Parser.Parser;
import FrontEnd.Visitor.TableStack;
import tools.Printer;

import java.io.IOException;
import java.io.InputStream;

public class Compiler {
    public static void main(String[] args) throws IOException {
        Printer printer = new Printer();
        TableStack tableStack = new TableStack();
        Input input = new Input("testfile.txt");
        //输入的字节流
        InputStream source = input.getInput();
        //词法分析
        Lexer lexer = new Lexer(source,printer);
        TokenStream tokenStream = new TokenStream(lexer.getTokenArrayList());
        //语法分析（同时进行了错误处理）
        Parser parser = new Parser(tokenStream,printer,tableStack);
        Node root = parser.parseCompUnit();
        printer.printError();
    }
}
