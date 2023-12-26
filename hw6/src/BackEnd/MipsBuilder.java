package BackEnd;

import BackEnd.assembly.AluAsm;
import BackEnd.assembly.Assembly;
import LLVM.Function;
import LLVM.Param;
import LLVM.Value;
import tools.RegAllocator;

import java.util.HashMap;

public class MipsBuilder {
    private static MipsBuilder mipsBuilder = new MipsBuilder();
    private Function curFunc;
    private int curOffset;
    //private HashMap<Value,Integer> offsetMap;
    private HashMap<String,Integer> offsetMap;
    private HashMap<Value,Register> var2Reg;
    private AssemblyTable assemblyTable;
    
    public MipsBuilder() {
        this.assemblyTable = new AssemblyTable();
    }
    
    public static MipsBuilder getInstance() {
        return mipsBuilder;
    }
    
    public int getCurOffset() {
        return curOffset;
    }
    
    public void subCurOffset(int subOff) {
        curOffset -= subOff;
    }
    
    public void addDataAsm(Assembly assembly) {
        assemblyTable.addDataAsm(assembly);
    }
    
    public void addTextAsm(Assembly assembly) {
        assemblyTable.addTextAsm(assembly);
    }
    
    public void enterFunc(Function function) {
        this.curFunc = function;
        this.curOffset = 0;
        this.offsetMap = new HashMap<>();
        this.var2Reg = function.getVar2Reg();
    }
    
    public Register getRegByValue(Value value) {
        if (var2Reg == null) {
            return null;
        } else {
            return var2Reg.get(value);
        }
    }
    
    public void putVar2Reg(Value value,Register register) {
        var2Reg.put(value,register);
    }
    
    public void delVar2Reg(Value value) {
        var2Reg.remove(value);
    }
    
    public int getOffsetOfValue(String value) {
        if (offsetMap == null) {
            return Integer.MAX_VALUE;
        } else if (offsetMap.get(value) == null) {
            return Integer.MAX_VALUE;
        } else {
            return offsetMap.get(value);
        }
    }
    
    public void addOffsetOfValue(String value,int offset) {
        offsetMap.put(value,offset);
    }
    
    public void allocRegForParam(Value param, Register reg) {
        if (var2Reg == null) {
            return;
        } else {
            var2Reg.put(param,reg);
        }
    }
    
    public AssemblyTable getAssemblyTable() {
        return assemblyTable;
    }
    
}
