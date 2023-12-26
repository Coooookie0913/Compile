package FrontEnd.Parser.Node;

import LLVM.Value;

import java.util.ArrayList;

//PrimaryExp -> '('Exp')' | LVal | Number
public class PrimaryExp extends Node{
    public PrimaryExp(int startLine, int endLine, FrontEnd.Parser.Node.SyntaxType SyntaxType, ArrayList<Node> children) {
        super(startLine, endLine, SyntaxType, children);
    }
    
    // constExp 中只会出现 (Exp) 或者 Number 也可能出现LVal
    public int calculate() {
        int ans = 0;
        if (getChildren().size() == 1) {
            //Number
            if (getChildren().get(0) instanceof Number) {
                ans = getChildren().get(0).calculate();
            }
            //LVal
            else {
                /*String varName = ((TokenNode)getChildren().get(0).getChildren().get(0)).getToken().getContent();
                Symbol symbol = IRBuilder.getInstance().getTableStack().getSymbol(varName);
                Value symbolIr = symbol.getIr();
                ArrayList<Value> init = null;
                if (symbolIr instanceof GlobalVar) {
                    init = ((GlobalVar) symbolIr).getValueArrayList();
                }
                if (symbolIr instanceof LocalVar) {
                    init = ((LocalVar) symbolIr).getValueArrayList();
                }
                ans = Integer.parseInt(init.get(0).getName());*/
                ans = getChildren().get(0).calculate();
            }
        }
        //(Exp)
        else {
            ans = getChildren().get(1).calculate();
        }
        return ans;
    }
    
    @Override
    public Value genIR() {
        //Number
        if (getChildren().get(0) instanceof Number) {
            return getChildren().get(0).genIR();
        }
        //'('Exp')'
        else if (getChildren().get(0) instanceof TokenNode) {
            return getChildren().get(1).genIR();
        }
        //LVal
        else {
            //如果得到的是指针的话，主要用于实参？
            return ((LVal)getChildren().get(0)).genIRforExp();
        }
    }
}
