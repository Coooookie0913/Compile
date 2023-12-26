package FrontEnd.Parser.Node;

import FrontEnd.Lexer.TokenType;
import LLVM.*;
import LLVM.Instruction.AddInstruction;
import LLVM.Instruction.SubInstruction;

import java.util.ArrayList;

//AddExp -> MulExp {'+' MulExp | '-' MulExp}
public class AddExp extends Node{
    public AddExp(int startLine, int endLine, SyntaxType SyntaxType, ArrayList<Node> children) {
        super(startLine, endLine, SyntaxType, children);
    }
    
    //只有 constExp 中有效
    public int calculate() {
        int ans = getChildren().get(0).calculate();
        int i = 0;
        int op = 0;
        for (i = 1; i < getChildren().size(); i++) {
            if (getChildren().get(i) instanceof TokenNode) {
                if (((TokenNode)getChildren().get(i)).getToken().getType() == TokenType.PLUS) {
                    op = 1;
                } else {
                    op = -1;
                }
            }
            if (getChildren().get(i) instanceof MulExp) {
                if (op == 1) {
                    ans += ((MulExp)getChildren().get(i)).calculate();
                } else {
                    ans -= ((MulExp)getChildren().get(i)).calculate();
                }
            }
        }
        return ans;
    }
    
    public Value genIR() {
        Value operand1 = getChildren().get(0).genIR();
        Value operand2 = null;
        int op = 0;// 1:+  -1:-
        int i;
        LLVMType llvmType = LLVMType.INSTR;
        for (i = 1; i < getChildren().size(); i++) {
            if (getChildren().get(i) instanceof TokenNode) {
                if (((TokenNode)getChildren().get(i)).getToken().getType() == TokenType.PLUS) {
                    op = 1;
                } else {
                    op = -1;
                }
            }
            if (getChildren().get(i) instanceof MulExp) {
                operand2 = ((MulExp)getChildren().get(i)).genIR();
                String name = IRBuilder.getInstance().genLocalVarName();
                BasicBlock basicBlock = IRBuilder.getInstance().getCurBasicBlock();
                Instr instr;
                if (op == 1) {
                    instr = new AddInstruction(llvmType,name,basicBlock,operand1,operand2);
                    IRBuilder.getInstance().addInstr(instr);
                } else {
                    instr = new SubInstruction(llvmType,name,basicBlock,operand1,operand2);
                    IRBuilder.getInstance().addInstr(instr);
                }
                //更新 operand1 为之前相加之和
                operand1 = instr;
            }
        }
        return operand1;
    }
}
