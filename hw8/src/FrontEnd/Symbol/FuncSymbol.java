package FrontEnd.Symbol;

import java.util.ArrayList;

public class FuncSymbol extends Symbol{
    private String returnType;//'void' or 'int'
    private int paramNum;
    private ArrayList<Integer> ParamDims;
    
    public FuncSymbol(String name,SymbolType type,String returnType,int paramNum,ArrayList<Integer> ParamDims) {
        this.name = name;
        this.type = type;
        this.returnType = returnType;
        this.paramNum = paramNum;
        this.ParamDims = ParamDims;
    }
    
    public int getParamNum() {
        return paramNum;
    }
    
    public ArrayList<Integer> getParamDims() {
        return ParamDims;
    }
    
    public String getReturnType() {
        return returnType;
    }
    
}