import FrontEnd.Lexer.Input;
import FrontEnd.Lexer.Lexer;
import FrontEnd.Lexer.TokenStream;
import FrontEnd.Parser.Node;
import FrontEnd.Parser.Parser;
import FrontEnd.Parser.TokenNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

public class Compiler {
    public static void main(String[] args) throws IOException {
        Input input = new Input("testfile.txt");
        //输入的字节流
        InputStream source = input.getInput();
        //词法分析
        Lexer lexer = new Lexer(source);
        TokenStream tokenStream = new TokenStream(lexer.getTokenArrayList());
        //语法分析
        Parser parser = new Parser(tokenStream);
        Node root = parser.parseCompUnit();
        //打印流
        PrintStream ps = new PrintStream("output.txt");
        System.setOut(ps);
        //后序遍历输出语法树
        visitNode(root);
    }
    
    private static void visitNode(Node node) {
        //终结符
        if (node instanceof TokenNode) {
            System.out.println(((TokenNode) node).getToken().getType() + " " + ((TokenNode) node).getToken().getContent());
        } else {
            //后序遍历
            for (int i = 0; i < node.getChildren().size(); i++) {
                visitNode(node.getChildren().get(i));
            }
            System.out.println("<" + node.getSynName() + ">");
        }
    }
}
