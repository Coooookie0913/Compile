package FrontEnd.Parser.Node;

import LLVM.Constant;
import LLVM.LLVMType;
import LLVM.Value;

import java.util.ArrayList;

public class ConstInitVal extends Node{
    public ConstInitVal(int startLine, int endLine, FrontEnd.Parser.Node.SyntaxType SyntaxType, ArrayList<Node> children) {
        super(startLine, endLine, SyntaxType, children);
    }
    
    //注意 所有数组均转化成一维数组
    //ConstInitVal → ConstExp  | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
    public ArrayList<Value> getInitVal() {
        ArrayList<Value> initVal = new ArrayList<>();
        //ConstInitVal → ConstExp
        if (getChildren().size() == 1) {
            int num = getChildren().get(0).calculate();
            Constant constant = new Constant(LLVMType.INT32,String.valueOf(num));
            initVal.add(constant);
            return initVal;
        } else {
            //ConstInitVal → '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
            for (Node node : getChildren()) {
                if (node instanceof ConstInitVal) {
                    ArrayList<Value> subInitVal = new ArrayList<>();
                    subInitVal = ((ConstInitVal) node).getInitVal();
                    for (Value value : subInitVal) {
                        initVal.add(value);
                    }
                }
            }
        }
        return initVal;
    }
}
