package FrontEnd.Symbol;

import java.util.HashMap;

public class SymbolTable {
    private HashMap<String,Symbol> symbolTable;
    
    public SymbolTable() {
        symbolTable = new HashMap<>();
    }
    
    public void addSymbol(Symbol symbol) {
        symbolTable.put(symbol.name,symbol);
    }
    
    public boolean check(String name) {
        if (symbolTable.get(name) != null) {
            return true;
        } else {
            return false;
        }
    }
    
    public Symbol getSymbol(String name) {
        return symbolTable.get(name);
    }
    
    public boolean checkVarSymbol(String name) {
        if (symbolTable.get(name) != null && symbolTable.get(name).getType() == SymbolType.var) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean checkFuncSymbol(String name) {
        if (symbolTable.get(name) != null && symbolTable.get(name).getType() == SymbolType.func) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean checkConstSymbol(String name) {
        if (symbolTable.get(name) != null && symbolTable.get(name).getType() == SymbolType.con) {
            return true;
        } else {
            return false;
        }
    }
}

