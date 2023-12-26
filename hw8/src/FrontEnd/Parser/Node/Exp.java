package FrontEnd.Parser.Node;

import LLVM.Value;

import java.util.ArrayList;

//Exp -> AddExp
public class Exp extends Node{
    public Exp(int startLine, int endLine, FrontEnd.Parser.Node.SyntaxType SyntaxType, ArrayList<Node> children) {
        super(startLine, endLine, SyntaxType, children);
    }
    
    public int calculate() {
        return getChildren().get(0).calculate();
    }
    
    @Override
    public Value genIR() {
        return getChildren().get(0).genIR();
    }
}
