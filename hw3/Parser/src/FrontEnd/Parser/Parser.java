package FrontEnd.Parser;

import FrontEnd.Lexer.Token;
import FrontEnd.Lexer.TokenStream;
import FrontEnd.Lexer.TokenType;

import java.util.ArrayList;
import java.util.Objects;

public class Parser {
    private TokenStream tokenStream;
    private Token curToken;
    
    public Parser(TokenStream tokenStream) {
        this.tokenStream = tokenStream;
        peek();
    }
    
    //tool func
    //向后读一位，并返回当前token
    private void read() {
        tokenStream.read();
        curToken = tokenStream.peek();
    }
    //返回当前token
    private void peek() {
        curToken = tokenStream.peek();
    }
    
    //默认读取到当前node的第一个token
    //调用parse得到的node已经读完
    //token生成的node需要read 手动读完当前token
    //endLine 不能用 curToken 否则会出现 null pointer
    public Node parseCompUnit() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        while(curToken != null) {
            Node node = null;
            //保证顺序（此处适用于分析顺序正确的程序）
            //MainFuncDef
            if (tokenStream.watch(1).getType() == TokenType.MAINTK) {
                node = parseMainFuncDef();
            }
            //FuncDef
            //FuncType ident '('
            else if (tokenStream.watch(2).getType() == TokenType.LPARENT) {
                node = parseFuncDef();
            }
            //ConstDecl
            //const int ident
            else if (curToken.getType() == TokenType.CONSTTK) {
                node = parseConstDecl();
            }
            //varDecl
            //int ident
            else if (curToken.getType() == TokenType.INTTK) {
                node = parseVarDecl();
            }
            else break;
            children.add(node);
        }
        int endLine = tokenStream.watch(-1).getLine();
        return new Node(startLine,endLine,SyntaxType.COMP_UNIT,children);
    }
    
    //const int <ConstDef> {,<ConstDef>};
    //curToken = 'const'
    public Node parseConstDecl() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        //parse const
        TokenNode constNode = new TokenNode(startLine,startLine,SyntaxType.TOKEN,null,curToken);
        children.add(constNode);
        read();
        //curToken = 'int' parse int
        TokenNode intNode = new TokenNode(startLine,startLine,SyntaxType.TOKEN,null,curToken);
        children.add(intNode);
        read();
        //curToken -> ConstDecl
        Node node = parseConstDef();
        children.add(node);
        //curToken = ',' or ';'
        while (curToken.getType() != TokenType.SEMICN) {
            //parse comma
            TokenNode commaNode = new TokenNode(curToken.getLine(), curToken.getLine(), SyntaxType.TOKEN,null,curToken);
            children.add(commaNode);
            read();
            //parse constDef
            node = parseConstDef();
            children.add(node);
        }
        //parse semicn
        TokenNode semicnNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
        children.add(semicnNode);
        read();
        int endLine = tokenStream.watch(-1).getLine();
        return new Node(startLine,endLine,SyntaxType.CONST_DECL,children);
    }
    
    //int varDef {,varDef};
    public Node parseVarDecl() {
        int starLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        //parse int
        TokenNode intNode = new TokenNode(starLine,starLine,SyntaxType.TOKEN,null,curToken);
        children.add(intNode);
        read();
        //parse varDef
        Node node = parseVarDef();
        children.add(node);
        //curToken = ',' or ';'
        while(curToken.getType() != TokenType.SEMICN) {
            //parse comma
            TokenNode commaNode = new TokenNode(curToken.getLine(), curToken.getLine(), SyntaxType.TOKEN,null,curToken);
            children.add(commaNode);
            read();
            //parse varDef
            node = parseVarDef();
            children.add(node);
        }
        //parse semicn
        TokenNode semicnNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
        children.add(semicnNode);
        read();
        int endLine = tokenStream.watch(-1).getLine();
        return new Node(starLine,endLine,SyntaxType.VAR_DECL,children);
    }
    
    //ConstDef -> Ident {'[' ConstExp ']'} '=' ConstInitVal
    public Node parseConstDef() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        //parse ident
        TokenNode identNode = new TokenNode(startLine,startLine,SyntaxType.TOKEN,null,curToken);
        children.add(identNode);
        read();
        //curToken = '['
        //parse [ConstExp]
        while (curToken.getType() == TokenType.LBRACK) {
            //parse '['
            TokenNode lbrackNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(lbrackNode);
            read();
            //parse ConstExp
            Node node = parseConstExp();
            children.add(node);
            //parse ']'
            TokenNode rbrackNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(rbrackNode);
            read();
        }
        //parse '='
        TokenNode assignNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
        children.add(assignNode);
        read();
        //parse ConstInitVal
        Node node = parseConstInitVal();
        children.add(node);
        int endLine = tokenStream.watch(-1).getLine();
        return new Node(startLine,endLine,SyntaxType.CONST_DEF,children);
    }
    
    //VarDef -> Ident{'['ConstExp']'} | Ident{'['ConstExp']'} '=' InitVal
    public Node parseVarDef() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        //parse ident
        TokenNode identNode = new TokenNode(startLine,startLine,SyntaxType.TOKEN,null,curToken);
        children.add(identNode);
        read();
        //curToken = '['
        //parse '[' ConstExp ']'
        while (curToken.getType() == TokenType.LBRACK) {
            //parse '['
            TokenNode lbrackNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(lbrackNode);
            read();
            //parse ConstExp
            Node node = parseConstExp();
            children.add(node);
            //parse ']'
            TokenNode rbrackNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(rbrackNode);
            read();
        }
        if (curToken.getType() == TokenType.ASSIGN) {
            //parse '='
            TokenNode assignNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(assignNode);
            read();
            //parse initVal
            Node node = parseInitVal();
            children.add(node);
        }
        int endLine = tokenStream.watch(-1).getLine();
        return new Node(startLine,endLine,SyntaxType.VAR_DEF,children);
    }
    
    //FuncDef -> FuncType Ident '(' [FuncFParams] ')' Block
    //FuncType -> 'void'|'int'
    //有形参 or 无形参
    public Node parseFuncDef() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        //parse FuncType
        Node node = parseFuncType();
        children.add(node);
        //parse ident
        TokenNode identNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
        children.add(identNode);
        read();
        //parse '('
        TokenNode lparentNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
        children.add(lparentNode);
        read();
        if (curToken.getType() != TokenType.RPARENT) {
            node = parseFuncFParams();
            children.add(node);
        }
        //parse ')'
        TokenNode rparentNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
        children.add(rparentNode);
        read();
        //parse block
        node = parseBlock();
        children.add(node);
        int endLine = tokenStream.watch(-1).getLine();
        return new Node(startLine,endLine,SyntaxType.FUNC_DEF,children);
    }
    
    //'int' 'main' '(' ')' Block
    public Node parseMainFuncDef() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        //parse int
        TokenNode intNode = new TokenNode(startLine,startLine,SyntaxType.TOKEN,null,curToken);
        children.add(intNode);
        read();
        //parse main
        TokenNode mainNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
        children.add(mainNode);
        read();
        //parse '('
        TokenNode lparentNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
        children.add(lparentNode);
        read();
        //parse ')'
        TokenNode rparentNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
        children.add(rparentNode);
        read();
        //parse Block
        Node node = parseBlock();
        children.add(node);
        int endLine = tokenStream.watch(-1).getLine();
        return new Node(startLine,endLine,SyntaxType.MAIN_FUNC_DEF,children);
    }
    
    //Exp -> AddExp
    public Node parseExp() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        Node node = parseAddExp();
        children.add(node);
        int endLine = tokenStream.watch(-1).getLine();
        return new Node(startLine,endLine,SyntaxType.EXP,children);
    }
    
    //AddExp -> MulExp {'+' MulExp | '-' MulExp}
    public Node parseAddExp() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        //parse MulExp
        Node node = parseMulExp();
        children.add(node);
        //parse '+' or '-'
        while (curToken.getType() == TokenType.PLUS || curToken.getType() == TokenType.MINU) {
            //防止文法改变导致语法树改变
            node = new Node(startLine,tokenStream.watch(-1).getLine(),SyntaxType.ADD_EXP,null);
            children.add(node);
            //parse '+' or '-'
            TokenNode pmNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(pmNode);
            read();
            //parse MulExp
            node = parseMulExp();
            children.add(node);
        }
        int endLine = tokenStream.watch(-1).getLine();
        return new Node(startLine,endLine,SyntaxType.ADD_EXP,children);
    }
    
    //LOrExp -> LAndExp {'||' LAndExp}
    public Node parseLOrExp() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        //parse LAndExp
        Node node = parseLAndExp();
        children.add(node);
        while (curToken.getType() == TokenType.OR) {
            //防止文法改变导致语法树改变
            node = new Node(startLine,tokenStream.watch(-1).getLine(),SyntaxType.LOR_EXP,null);
            children.add(node);
            //parse '||'
            TokenNode orNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(orNode);
            read();
            //parse LAndExp
            node = parseLAndExp();
            children.add(node);
        }
        int endLine = tokenStream.watch(-1).getLine();
        return new Node(startLine,endLine,SyntaxType.LOR_EXP,children);
    }
    
    //PrimaryExp -> '('Exp')' | LVal | Number
    public Node parsePrimaryExp() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        //parse (Exp)
        if (curToken.getType() == TokenType.LPARENT) {
            //parse '('
            TokenNode lparentNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(lparentNode);
            read();
            //parse Exp
            Node node = parseExp();
            children.add(node);
            //parse ')'
            TokenNode rparentNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(rparentNode);
            read();
        }
        //parse Number
        else if (curToken.getType() == TokenType.INTCON) {
            Node node = parseNumber();
            children.add(node);
        }
        //parse LVal
        else {
            Node node = parseLVal();
            children.add(node);
        }
        int endLine = tokenStream.watch(-1).getLine();
        return new Node(startLine,endLine,SyntaxType.PRIMARY_EXP,children);
    }
    
    //UnaryExp -> PrimaryExp | Ident '('[FuncRParams]')' | UnaryOp UnaryExp
    public Node parseUnaryExp() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        //parse ident ([FuncRParams])
        if (curToken.getType() == TokenType.IDENFR && tokenStream.watch(1).getType() == TokenType.LPARENT) {
            //parse ident
            TokenNode identNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(identNode);
            read();
            //parse '('
            TokenNode lparentNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(lparentNode);
            read();
            //parse FuncRParams
            if (curToken.getType() != TokenType.RPARENT) {
                Node node = parseFuncRParams();
                children.add(node);
            }
            //parse ')'
            TokenNode rparentNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(rparentNode);
            read();
        }
        //parse UnaryOp UnaryExp
        else if (curToken.getType() == TokenType.PLUS || curToken.getType() == TokenType.MINU || curToken.getType() == TokenType.NOT) {
            Node node = parseUnaryOp();
            children.add(node);
            node = parseUnaryExp();
            children.add(node);
        }
        //parse PrimaryExp
        else {
            Node node = parsePrimaryExp();
            children.add(node);
        }
        int endLine = tokenStream.watch(-1).getLine();
        return new Node(startLine,endLine,SyntaxType.UNARY_EXP,children);
    }
    
    //MulExp -> UnaryExp {'*' UnaryExp | '/' UnaryExp | '%' UnaryExp}
    public Node parseMulExp() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        //parse UnaryExp
        Node node = parseUnaryExp();
        children.add(node);
        while (curToken.getType() == TokenType.MULT || curToken.getType() == TokenType.DIV || curToken.getType() == TokenType.MOD) {
            //防止文法改变导致语法树改变
            node = new Node(startLine,tokenStream.watch(-1).getLine(),SyntaxType.MUL_EXP,null);
            children.add(node);
            //parse '*' or '/' or '%'
            TokenNode mdmNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(mdmNode);
            read();
            //parse UnaryExp
            node = parseUnaryExp();
            children.add(node);
        }
        int endLine = tokenStream.watch(-1).getLine();
        return new Node(startLine,endLine,SyntaxType.MUL_EXP,children);
    }
    
    //RelExp -> AddExp {'<' AddExp | '>' AddExp | '<=' AddExp | '>=' AddExp}
    public Node parseRelExp() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        //parse AddExp
        Node node = parseAddExp();
        children.add(node);
        while (curToken.getType() == TokenType.LSS || curToken.getType() == TokenType.LEQ
        || curToken.getType() == TokenType.GRE || curToken.getType() == TokenType.GEQ) {
            //防止文法改变导致语法树改变
            node = new Node(startLine,tokenStream.watch(-1).getLine(),SyntaxType.REL_EXP,null);
            children.add(node);
            //parse > < <= >=
            TokenNode lgNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(lgNode);
            read();
            //parse AddExp
            node = parseAddExp();
            children.add(node);
        }
        int endLine = tokenStream.watch(-1).getLine();
        return new Node(startLine,endLine,SyntaxType.REL_EXP,children);
    }
    
    //EqExp -> RelExp {'==' RelExp | '!=' RelExp}
    public Node parseEqExp() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        Node node = parseRelExp();
        children.add(node);
        while (curToken.getType() == TokenType.EQL || curToken.getType() == TokenType.NEQ) {
            //防止文法改变导致语法树改变
            node = new Node(startLine,tokenStream.watch(-1).getLine(),SyntaxType.EQ_EXP,null);
            children.add(node);
            TokenNode enNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(enNode);
            read();
            node = parseRelExp();
            children.add(node);
        }
        int endLine = tokenStream.watch(-1).getLine();
        return new Node(startLine,endLine,SyntaxType.EQ_EXP,children);
    }
    
    //LAndExp -> EqExp {'&&' EqExp}
    public Node parseLAndExp() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        Node node = parseEqExp();
        children.add(node);
        while (curToken.getType() == TokenType.AND) {
            //防止文法改变导致语法树改变
            node = new Node(startLine,tokenStream.watch(-1).getLine(),SyntaxType.LAND_EXP,null);
            children.add(node);
            TokenNode andNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(andNode);
            read();
            node = parseEqExp();
            children.add(node);
        }
        int endLine = tokenStream.watch(-1).getLine();
        return new Node(startLine,endLine,SyntaxType.LAND_EXP,children);
    }
    
    //ConstExp -> AddExp
    public Node parseConstExp() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        Node node = parseAddExp();
        children.add(node);
        int endLine = tokenStream.watch(-1).getLine();
        return new Node(startLine,endLine,SyntaxType.CONST_EXP,children);
    }
    
    /// Stmt → LVal '=' Exp ';' // 每种类型的语句都要覆盖
    ///| [Exp] ';' //有无Exp两种情况
    ///| Block
    ///| 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
    ///| 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // 1. 无缺省 2. 缺省第一个ForStmt 3. 缺省Cond 4. 缺省第二个ForStmt
    ///| 'break' ';' | 'continue' ';'
    ///| 'return' [Exp] ';' // 1.有Exp 2.无Exp
    ///| LVal '=' 'getint''('')'';'
    ///| 'printf''('FormatString{','Exp}')'';' // 1.有Exp 2.无Exp
    public Node parseStmt() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        //block
        if (curToken.getType() == TokenType.LBRACE) {
            Node node = parseBlock();
            children.add(node);
        }
        //if
        //| 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
        else if (curToken.getType() == TokenType.IFTK) {
            //if
            TokenNode ifNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(ifNode);
            read();
            //'('
            TokenNode lparentNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(lparentNode);
            read();
            //Cond
            Node node = parseCond();
            children.add(node);
            //')'
            TokenNode rparentNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(rparentNode);
            read();
            //stmt
            node = parseStmt();
            children.add(node);
            //有无else
            if (curToken.getType() == TokenType.ELSETK) {
                TokenNode elseNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
                children.add(elseNode);
                read();
                //stmt
                node = parseStmt();
                children.add(node);
            }
        }
        //for
        //| 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // 1. 无缺省 2. 缺省第一个ForStmt 3. 缺省Cond 4. 缺省第二个ForStmt
        else if (curToken.getType() == TokenType.FORTK) {
            //for
            TokenNode forNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(forNode);
            read();
            //'('
            TokenNode lparentNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(lparentNode);
            read();
            if (curToken.getType() != TokenType.SEMICN) {
                Node node = parseForStmt();
                children.add(node);
            }
            //';'
            TokenNode semicnNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(semicnNode);
            read();
            if (curToken.getType() != TokenType.SEMICN) {
                Node node = parseCond();
                children.add(node);
            }
            //';'
            TokenNode semicnNode1 = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(semicnNode1);
            read();
            if (curToken.getType() != TokenType.RPARENT) {
                Node node = parseForStmt();
                children.add(node);
            }
            //')'
            TokenNode rparentNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(rparentNode);
            read();
            //stmt
            Node node = parseStmt();
            children.add(node);
        }
        //| 'break' ';' | 'continue' ';'
        else if (curToken.getType() == TokenType.BREAKTK) {
            TokenNode breakNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(breakNode);
            read();
            //parse ';'
            TokenNode semicnNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(semicnNode);
            read();
        } else if (curToken.getType() == TokenType.CONTINUETK) {
            TokenNode continueNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(continueNode);
            read();
            //parse ';'
            TokenNode semicnNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(semicnNode);
            read();
        }
        //| 'return' [Exp] ';' // 1.有Exp 2.无Exp
        else if (curToken.getType() == TokenType.RETURNTK) {
            //return
            TokenNode returnNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(returnNode);
            read();
            if (curToken.getType() != TokenType.SEMICN) {
                Node node = parseExp();
                children.add(node);
            }
            TokenNode semicnNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(semicnNode);
            read();
        }
        //| 'printf' '(' FormatString { ',' Exp} ')' ';' // 1.有Exp 2.无Exp
        else if (curToken.getType() == TokenType.PRINTFTK) {
            //'printf' '(' FormatString
            for (int i = 0; i < 3; i++) {
                TokenNode tokenNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
                children.add(tokenNode);
                read();
            }
            while (curToken.getType() == TokenType.COMMA) {
                //','
                TokenNode printfNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
                children.add(printfNode);
                read();
                //Exp
                Node node = parseExp();
                children.add(node);
            }
            //')' ';'
            for (int i = 0; i < 2; i++) {
                TokenNode tokenNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
                children.add(tokenNode);
                read();
            }
        }
        //LVal '=' Exp ';'
        //LVal '=' 'getint''('')'';'
        //| [Exp] ';' //有无Exp两种情况
        else {
            int assignFlag = 0;
            int i = 0;
            Token token = tokenStream.watch(i);
            while (token.getType() != TokenType.SEMICN) {
                if (token.getType() == TokenType.ASSIGN) {
                    assignFlag = 1;
                    break;
                }
                i++;
                token = tokenStream.watch(i);
            }
            if (assignFlag == 1) {
                //LVal
                Node node = parseLVal();
                children.add(node);
                //'='
                TokenNode tokenNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
                children.add(tokenNode);
                read();
                //parse 'getint' '(' ')'
                if (curToken.getType() == TokenType.GETINTTK) {
                    //'getint' '(' ')'
                    for (i = 0; i < 3; i++) {
                        tokenNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
                        children.add(tokenNode);
                        read();
                    }
                } else {
                    //parse Exp
                    node = parseExp();
                    children.add(node);
                }
                //parse ';'
                TokenNode semicnNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
                children.add(semicnNode);
                read();
            }
            //| [Exp] ';' //有无Exp两种情况
            else {
                if (curToken.getType() != TokenType.SEMICN) {
                    Node node = parseExp();
                    children.add(node);
                }
                //parse ';'
                TokenNode semicnNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
                children.add(semicnNode);
                read();
            }
        }
        int endLine = tokenStream.watch(-1).getLine();
        return new Node(startLine,endLine,SyntaxType.STMT,children);
    }
    
    //ForStmt -> LVal '=' Exp
    public Node parseForStmt() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        //LVal
        Node node = parseLVal();
        children.add(node);
        //'='
        TokenNode tokenNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
        children.add(tokenNode);
        read();
        //Exp
        node = parseExp();
        children.add(node);
        int endLine = tokenStream.watch(-1).getLine();
        return new Node(startLine,endLine,SyntaxType.FOR_STMT,children);
    }
    
    //Cond -> LOrExp
    public Node parseCond() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        Node node = parseLOrExp();
        children.add(node);
        int endLine = tokenStream.watch(-1).getLine();
        return new Node(startLine,endLine,SyntaxType.COND,children);
    }
    
    //LVal -> Ident{'[' Exp ']'} 1.普通变量 2.一维数组 3.二维数组
    public Node parseLVal() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        //ident
        TokenNode tokenNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,children,curToken);
        children.add(tokenNode);
        read();
        //'[' Exp ']'
        while (curToken.getType() == TokenType.LBRACK) {
            //'['
            tokenNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(tokenNode);
            read();
            //Exp
            Node node = parseExp();
            children.add(node);
            //']'
            tokenNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(tokenNode);
            read();
        }
        int endLine = tokenStream.watch(-1).getLine();
        return new Node(startLine,endLine,SyntaxType.LVAL,children);
    }
    
    //Number -> IntConst
    public Node parseNumber() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        TokenNode tokenNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
        children.add(tokenNode);
        read();
        int endLine = tokenStream.watch(-1).getLine();
        return new Node(startLine,endLine,SyntaxType.NUMBER,children);
    }
    
    //UnaryOp -> '+' | '-' | '!'
    public Node parseUnaryOp() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        TokenNode tokenNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
        children.add(tokenNode);
        read();
        int endLine = tokenStream.watch(-1).getLine();
        return new Node(startLine,endLine,SyntaxType.UNARY_OP,children);
    }
    
    //FuncRParams -> Exp {',' Exp}
    //实参变量
    public Node parseFuncRParams() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        Node node = parseExp();
        children.add(node);
        //',' Exp
        while (curToken.getType() == TokenType.COMMA) {
            TokenNode tokenNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(tokenNode);
            read();
            //Exp
            node = parseExp();
            children.add(node);
        }
        int endLine = tokenStream.watch(-1).getLine();
        return new Node(startLine,endLine,SyntaxType.FUNCR_PARAMS,children);
    }
    
    //FuncFParams -> FuncFParam {',' FuncFParam}
    public Node parseFuncFParams() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        Node node = parseFuncFParam();
        children.add(node);
        while (curToken.getType() == TokenType.COMMA) {
            //','
            TokenNode tokenNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(tokenNode);
            read();
            //FuncFParam
            node = parseFuncFParam();
            children.add(node);
        }
        int endLine = tokenStream.watch(-1).getLine();
        return new Node(startLine,endLine,SyntaxType.FUNCF_PARAMS,children);
    }
    
    //FuncParam -> BType Ident ['[' ']' {'[' ConstExp ']'}] 1.普通变量2.一维数组变量 3.二维数组变量
    public Node parseFuncFParam() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        //BTyte Ident
        for (int i = 0; i < 2; i++) {
            TokenNode tokenNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(tokenNode);
            read();
        }
        //'[' ']'
        if (curToken.getType() == TokenType.LBRACK) {
            for (int i = 0; i < 2; i++) {
                TokenNode tokenNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
                children.add(tokenNode);
                read();
            }
        }
        while (curToken.getType() == TokenType.LBRACK) {
            //'['
            TokenNode tokenNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(tokenNode);
            read();
            //ConstExp
            Node node = parseConstExp();
            children.add(node);
            //'['
            tokenNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(tokenNode);
            read();
        }
        int endLine = tokenStream.watch(-1).getLine();
        return new Node(startLine,endLine,SyntaxType.FUNCF_PARAM,children);
    }
    
    //ConstInitVal → ConstExp | '{'  [ ConstInitVal {  ','  ConstInitVal } ]  '}' // 1.常表达式初值 2.一维数组初值 3.二维数组初值
    public Node parseConstInitVal() {
        int starLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        //ConstExp
        if (curToken.getType() != TokenType.LBRACE) {
            Node node = parseConstExp();
            children.add(node);
        }
        //'{' [ ConstInitVal { ',' ConstInitVal } ] '}'
        else {
            //'{'
            TokenNode tokenNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(tokenNode);
            read();
            //ConstInitVal { ',' ConstInitVal}
            if (curToken.getType() != TokenType.RBRACE) {
                Node node = parseConstInitVal();
                children.add(node);
                while (curToken.getType() == TokenType.COMMA) {
                    //','
                    tokenNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
                    children.add(tokenNode);
                    read();
                    //ConstInitVal
                    node = parseConstInitVal();
                    children.add(node);
                }
            }
            //'}'
            tokenNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(tokenNode);
            read();
        }
        int endLine = tokenStream.watch(-1).getLine();
        return new Node(starLine,endLine,SyntaxType.CONST_INITVAL,children);
    }
    
    //InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
    public Node parseInitVal() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        //Exp
        if (curToken.getType() != TokenType.LBRACE) {
            Node node = parseExp();
            children.add(node);
        }
        //'{' [ InitVal { ',' InitVal } ] '}'
        else {
            //'{'
            TokenNode tokenNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(tokenNode);
            read();
            //InitVal { ',' InitVal}
            if (curToken.getType() != TokenType.RBRACE) {
                Node node = parseInitVal();
                children.add(node);
                while (curToken.getType() == TokenType.COMMA) {
                    //','
                    tokenNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
                    children.add(tokenNode);
                    read();
                    //InitVal
                    node = parseInitVal();
                    children.add(node);
                }
            }
            //'}'
            tokenNode = new TokenNode(curToken.getLine(), curToken.getLine(),SyntaxType.TOKEN,null,curToken);
            children.add(tokenNode);
            read();
        }
        int endLine = tokenStream.watch(-1).getLine();
        return new Node(startLine,endLine,SyntaxType.INITVAL,children);
    }
    
    // FuncType → 'void' | 'int'
    public Node parseFuncType() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        TokenNode tokenNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
        children.add(tokenNode);
        read();
        int endLine = tokenStream.watch(-1).getLine();
        return new Node(startLine,endLine,SyntaxType.FUNC_TYPE,children);
    }
    
    //Block → '{' { BlockItem } '}'
    //BlockItem → Decl | Stmt
    //Decl → ConstDecl | VarDecl
    public Node parseBlock() {
        int startLine = curToken.getLine();
        ArrayList<Node> children = new ArrayList<>();
        //'{'
        TokenNode tokenNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
        children.add(tokenNode);
        read();
        //{ BlockItem }
        while (curToken.getType() != TokenType.RBRACE) {
            //ConstDecl
            if (curToken.getType() == TokenType.CONSTTK) {
                Node node = parseConstDecl();
                children.add(node);
            }
            //VarDecl
            else if (curToken.getType() == TokenType.INTTK) {
                Node node = parseVarDecl();
                children.add(node);
            }
            //Stmt
            else {
                Node node = parseStmt();
                children.add(node);
            }
        }
        //'}'
        tokenNode = new TokenNode(curToken.getLine(),curToken.getLine(),SyntaxType.TOKEN,null,curToken);
        children.add(tokenNode);
        read();
        int endLine = tokenStream.watch(-1).getLine();
        return new Node(startLine,endLine,SyntaxType.BLOCK,children);
    }
}
