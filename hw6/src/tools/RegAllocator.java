package tools;

import BackEnd.MipsBuilder;
import BackEnd.Register;
import LLVM.Module;
import LLVM.Value;

import java.util.ArrayList;
import java.util.HashMap;

public class RegAllocator {
    //private Module module;
    private static RegAllocator regAllocator;
    private HashMap<Register,Value> reg2Var;
    private HashMap<Value,Register> var2Reg;
    private ArrayList<Register> freeReg;
    
    public RegAllocator() {
        //this.module = module;
        this.freeReg = new ArrayList<>();
        this.reg2Var = new HashMap<>();
        this.var2Reg = new HashMap<>();
        for (Register register : Register.values()) {
            if (register.ordinal() >= Register.T0.ordinal() && register.ordinal() <= Register.T9.ordinal()) {
                freeReg.add(register);
            }
        }
    }
    
    public static RegAllocator getInstance() {
        return regAllocator;
    }
    
    public Register getFreeReg(Value value) {
        if (freeReg.size() > 0) {
            Register register = freeReg.get(0);
            freeReg.remove(register);
            reg2Var.put(register,value);
            var2Reg.put(value,register);
            MipsBuilder.getInstance().putVar2Reg(value,register);
            return register;
        } else {
            return null;
        }
    }
    
    public void freeReg(Register register) {
        if (register.ordinal() >= Register.T0.ordinal() && register.ordinal() <= Register.T9.ordinal()) {
            freeReg.add(register);
            Value value = reg2Var.get(register);
            reg2Var.remove(register);
            var2Reg.remove(value);
            MipsBuilder.getInstance().delVar2Reg(value);
        }
    }
}
