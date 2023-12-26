package FrontEnd.Parser.Node;

import LLVM.LLVMType;
import LLVM.Value;

import java.util.ArrayList;

//InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
public class InitVal extends Node{
    public InitVal(int startLine, int endLine, FrontEnd.Parser.Node.SyntaxType SyntaxType, ArrayList<Node> children) {
        super(startLine, endLine, SyntaxType, children);
    }
    
    //注意 所有数组均转化成一维数组
    //InitVal → Exp(const)  | '{' [ InitVal(const) { ',' InitVal(const) } ] '}'
    public ArrayList<Value> getConstInitVal() {
        ArrayList<Value> constInitVal = new ArrayList<>();
        //InitVal → Exp
        if (getChildren().size() == 1) {
            int num = getChildren().get(0).calculate();
            Value value = new Value(LLVMType.INT32,String.valueOf(num));
            constInitVal.add(value);
            return constInitVal;
        } else {
            //InitVal → '{' [ InitVal { ',' InitVal } ] '}'
            for (Node node : getChildren()) {
                if (node instanceof InitVal) {
                    ArrayList<Value> subInitVal = new ArrayList<>();
                    subInitVal = ((InitVal) node).getConstInitVal();
                    for (Value value : subInitVal) {
                        constInitVal.add(value);
                    }
                }
            }
        }
        return constInitVal;
    }
    
    public ArrayList<Value> getValueOfVar() {
        ArrayList<Value> valueArrayList = new ArrayList<>();
        if (getChildren().size() == 1) {
            Value value = getChildren().get(0).genIR();
            valueArrayList.add(value);
        }
        else {
            int i;
            for (i = 0; i < getChildren().size(); i++) {
                if (getChildren().get(i) instanceof InitVal) {
                    ArrayList<Value> subArray = ((InitVal)getChildren().get(i)).getValueOfVar();
                    for (Value value : subArray) {
                        valueArrayList.add(value);
                    }
                }
            }
        }
        return valueArrayList;
    }
}
