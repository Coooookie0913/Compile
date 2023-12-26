package FrontEnd.Parser.Node;

import LLVM.Constant;
import LLVM.LLVMType;
import LLVM.Value;

import java.util.ArrayList;

public class Number extends Node{
    public Number(int startLine, int endLine, FrontEnd.Parser.Node.SyntaxType SyntaxType, ArrayList<Node> children) {
        super(startLine, endLine, SyntaxType, children);
    }
    
    public int calculate() {
        return Integer.parseInt(((TokenNode)getChildren().get(0)).getToken().getContent());
    }
    
    public Value genIR() {
        String number = ((TokenNode)getChildren().get(0)).getToken().getContent();
        return new Constant(LLVMType.INT32,number);
    }
}
