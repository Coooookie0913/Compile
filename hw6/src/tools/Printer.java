package tools;

import BackEnd.MipsBuilder;
import FrontEnd.Lexer.Token;
import FrontEnd.Parser.Node.Node;
import FrontEnd.Parser.Node.TokenNode;
import FrontEnd.Symbol.ErrorMessage;
import LLVM.Module;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;

public class Printer {
    private ArrayList<ErrorMessage> ErrorList;
    //private ArrayList<Value> LlvmList;
    private PrintStream outPs;
    private PrintStream errorPs;
    private PrintStream irPs;
    private PrintStream mipsPs;
    public static Printer printer;
    
    public static Printer getInstance() {
        return printer;
    }
    public Printer() throws FileNotFoundException {
        ErrorList = new ArrayList<>();
        //LlvmList = new ArrayList<>();
        outPs = new PrintStream("output.txt");
        errorPs = new PrintStream("error.txt");
        irPs = new PrintStream("llvm_ir.txt");
        mipsPs = new PrintStream("mips.txt");
    }
    
    public void printTokenList(ArrayList<Token> tokenList) throws FileNotFoundException {
        for (Token token : tokenList) {
            outPs.println(token.getType() + " " + token.getContent());
        }
    }
    public void printAllSyntax(Node node) {
        //终结符
        if (node instanceof TokenNode) {
            outPs.println(((TokenNode) node).getToken().getType() + " " + ((TokenNode) node).getToken().getContent());
        } else {
            //后序遍历
            for (int i = 0; i < node.getChildren().size(); i++) {
                printAllSyntax(node.getChildren().get(i));
            }
            outPs.println("<" + node.getSynName() + ">");
        }
    }
    
    public void addErrorMessage(ErrorMessage errorMessage) {
        ErrorList.add(errorMessage);
    }
    
    public void printError() throws FileNotFoundException {
        Collections.sort(ErrorList);
        for (ErrorMessage errorMessage:ErrorList) {
            errorPs.println(errorMessage.getLine() + " " + errorMessage.getType());
        }
    }
    
    public Boolean haveError() {
        return (ErrorList.size() != 0);
    }
    
    public void printIR(Module module) {
        irPs.println(module.toString());
    }
    
    public void printMips(Module module) {
        module.toAssembly();
        mipsPs.println(MipsBuilder.getInstance().getAssemblyTable().toString());
    }
}

