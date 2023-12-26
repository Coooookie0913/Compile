package LLVM;

public enum LLVMType {
    
    VOID("void"),
    INT32("i32"),
    INT32_POINTER("i32*"),
    INT1("i1"),
    FUNCTION("function"),
    BASIC_BLOCK("basicblock"),
    GLOBAL_VAL("globalVar"),
    LOCAL_VAL("localVar"),
    INSTR("Instr"),
    PARAM("param"),
    LABEL("label"),
    ARRAY("array"),
    INITVAL("initval"),
    MODULE("module");
    
    private String LLVMTypeName;
    
    private LLVMType(String LLVMTypeName) {
        this.LLVMTypeName = LLVMTypeName;
    }
    
    public String toString() {
        return LLVMTypeName;
    }
    
}
