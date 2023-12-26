package LLVM;

import BackEnd.assembly.LabelAsm;

import java.util.ArrayList;

//block 只会被别人用 本身只是 instr 集合
public class BasicBlock extends Value{
    private ArrayList<Instr> instrArrayList;
    private Function parentFunction;
    public BasicBlock(LLVMType llvmType, String name, Function parentFunction) {
        super(llvmType, name);
        this.instrArrayList = new ArrayList<>();
        this.parentFunction = parentFunction;
    }
    
    public void addInstr(Instr instr) {
        instrArrayList.add(instr);
    }
    
    public void setParentFunction(Function parentFunction) {
        this.parentFunction = parentFunction;
    }
    
    public ArrayList<Instr> getInstrArrayList() {
        return this.instrArrayList;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        sb.append(":\n");
        for (Instr instr : instrArrayList) {
            sb.append("    ");
            sb.append(instr.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
    
    public void toAssembly() {
        new LabelAsm(getName());
        for (Instr instr : instrArrayList) {
            instr.toAssembly();
        }
    }
}
