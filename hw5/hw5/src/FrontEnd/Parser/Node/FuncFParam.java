package FrontEnd.Parser.Node;

import FrontEnd.Lexer.TokenType;
import FrontEnd.Symbol.SymbolType;
import FrontEnd.Symbol.VarSymbol;
import LLVM.IRBuilder;
import LLVM.LLVMType;
import LLVM.Param;
import LLVM.Value;

import java.util.ArrayList;

// FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
//只有第一维可以省略
public class FuncFParam extends Node{
    public FuncFParam(int startLine, int endLine, FrontEnd.Parser.Node.SyntaxType SyntaxType, ArrayList<Node> children) {
        super(startLine, endLine, SyntaxType, children);
    }
    
    //TODO:弄清含参函数的genIR
    @Override
    public Value genIR() {
        ArrayList<Integer> paramDims = new ArrayList<>();
        Param param;
        String varName = ((TokenNode)getChildren().get(1)).getToken().getContent();
        int dim = 0;
        //0维
        if (getChildren().size() == 2) {
            String targetType = "i32";
            String name = IRBuilder.getInstance().genParamName();
            param = new Param(LLVMType.PARAM,name,targetType,new ArrayList<>());
        }
        //数组
        //BType Ident ['[' ']' { '[' ConstExp ']' }]
        else {
            String targetType = "i32*";
            String name = IRBuilder.getInstance().genParamName();
            int i;
            for (i = 0; i < getChildren().size(); i++) {
                Node node = getChildren().get(i);
                if (node instanceof ConstExp) {
                    int subDim = ((ConstExp)node).calculate();
                    paramDims.add(subDim);
                }
                if (node instanceof TokenNode) {
                    if (((TokenNode) node).getToken().getType() == TokenType.LBRACK) {
                        if (getChildren().get(i+1) instanceof TokenNode && ((TokenNode)getChildren().get(i+1)).getToken().getType() == TokenType.RBRACK) {
                            paramDims.add(0);
                        }
                    }
                }
            }
            param = new Param(LLVMType.PARAM,name,targetType,paramDims);
            //被压成一维
            dim = 1;
            param.setIsPointer(true);
        }
        VarSymbol symbol = new VarSymbol(varName, SymbolType.var,dim);
        param.setSymbol(symbol);
        symbol.setIr(param);
        symbol.setDims(paramDims);
        return param;
    }
}
