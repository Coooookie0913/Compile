package FrontEnd.Parser.Node;

import LLVM.Param;

import java.util.ArrayList;

//FuncFParams → FuncFParam { ',' FuncFParam }
public class FuncFParams extends Node{
    public FuncFParams(int startLine, int endLine, FrontEnd.Parser.Node.SyntaxType SyntaxType, ArrayList<Node> children) {
        super(startLine, endLine, SyntaxType, children);
    }
    
    public ArrayList<Param> getParamList() {
        ArrayList<Param> paramArrayList = new ArrayList<>();
        for (Node node : getChildren()) {
            if (node instanceof FuncFParam) {
                Param param = (Param) (node.genIR());
                paramArrayList.add(param);
            }
        }
        return paramArrayList;
    }
}
