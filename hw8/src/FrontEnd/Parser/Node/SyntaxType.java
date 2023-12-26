package FrontEnd.Parser.Node;

public enum SyntaxType {
    //Compunit
    COMP_UNIT("CompUnit"),
    //Def & Decl
    CONST_DECL("ConstDecl"),
    VAR_DECL("VarDecl"),
    CONST_DEF("ConstDef"),
    VAR_DEF("VarDef"),
    FUNC_DEF("FuncDef"),
    MAIN_FUNC_DEF("MainFuncDef"),
    //Exp
    EXP("Exp"),
    ADD_EXP("AddExp"),
    LOR_EXP("LOrExp"),
    PRIMARY_EXP("PrimaryExp"),
    UNARY_EXP("UnaryExp"),
    MUL_EXP("MulExp"),
    REL_EXP("RelExp"),
    EQ_EXP("EqExp"),
    LAND_EXP("LAndExp"),
    CONST_EXP("ConstExp"),
    //Stmt
    STMT("Stmt"),
    FOR_STMT("ForStmt"),
    //Other
    COND("Cond"),
    LVAL("LVal"),
    NUMBER("Number"),
    UNARY_OP("UnaryOp"),
    FUNCR_PARAMS("FuncRParams"),
    FUNCF_PARAMS("FuncFParams"),//形参
    FUNCF_PARAM("FuncFParam"),
    CONST_INITVAL("ConstInitVal"),
    INITVAL("InitVal"),
    FUNC_TYPE("FuncType"),
    BLOCK("Block"),
    //do not print
    DECL("Decl"),
    BTYPE("BType"),
    BLOCK_ITEM("BlockItem"),
    TOKEN("Token");
    
    private String SyntaxTypeName;
    
    private SyntaxType(String SyntaxTypeName) {
        this.SyntaxTypeName = SyntaxTypeName;
    }
    
    public String getSyntaxTypeName() {
        return SyntaxTypeName;
    }
}

