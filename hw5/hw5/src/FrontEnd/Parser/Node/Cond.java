package FrontEnd.Parser.Node;

import LLVM.BasicBlock;

import java.util.ArrayList;

//Cond → LOrExp
public class Cond extends Node{
    public Cond(int startLine, int endLine, FrontEnd.Parser.Node.SyntaxType SyntaxType, ArrayList<Node> children) {
        super(startLine, endLine, SyntaxType, children);
    }
    
    //for LOrExp
    public void genIRforCond(BasicBlock thenBlock, BasicBlock elseBlock) {
        ((LOrExp)getChildren().get(0)).genIRforLOrExp(thenBlock,elseBlock);
    }
}
