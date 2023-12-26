import FrontEnd.Lexer.Input;
import FrontEnd.Lexer.Lexer;
import FrontEnd.Lexer.TokenStream;
import FrontEnd.Parser.Node.Node;
import FrontEnd.Parser.Parser;
import FrontEnd.Symbol.TableStack;
import LLVM.IRBuilder;
import tools.Printer;
import LLVM.Module;

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
        //递归下降 root 输出 syntax
        Parser parser = new Parser(tokenStream,printer,tableStack);
        Node root = parser.parseCompUnit();
        printer.printAllSyntax(root);
        printer.printError();
        if (!printer.haveError()) {
            //输出llvm_ir
            root.genIR();
            Module module = IRBuilder.getInstance().getCurModule();
            printer.printIR(module);
            //输出mips
            printer.printMips(module);
        }
    }
}