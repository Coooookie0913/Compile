package FrontEnd.Symbol;

import LLVM.Value;

import java.util.ArrayList;

public class Symbol {
    String name;
    SymbolType type;
    
    Value ir;
    ArrayList<Integer> dims;
    
    public Symbol() {
    
    }
    
    public String getName() {
        return name;
    }
    
    public SymbolType getType() {
        return type;
    }
    
    public void setIr(Value ir) {
        this.ir = ir;
    }
    
    public Value getIr() {
        return ir;
    }
    
    public String getIrName() {
        return ir.getName();
    }
    
    public ArrayList<Integer> getDims() {
        return dims;
    }
}
