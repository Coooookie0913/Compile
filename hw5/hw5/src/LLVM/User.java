package LLVM;

import java.util.ArrayList;

public class User extends Value{
    private ArrayList<Value> values;
    
    // 对于形如  %4 = load i32, i32* %2
    // %4 是 name
    // LLVMType 表明是 Instr basicBlock GlobalVar ...
    public User(LLVMType llvmType, String name) {
        super(llvmType, name);
        this.values = new ArrayList<>();
    }
    
    public void addValue(Value value) {
        values.add(value);
    }
}
