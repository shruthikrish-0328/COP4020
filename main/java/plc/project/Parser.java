package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
//WEDNESDAY SUBMISSION PARSER
/**
 * The parser takes the sequence of tokens emitted by the lexer and turns that
 * into a structured representation of the program, called the Abstract Syntax
 * Tree (AST).
 *
 * The parser has a similar architecture to the lexer, just with {@link Token}s
 * instead of characters. As before, {@link #peek(Object...)} and {@link
 * #match(Object...)} are helpers to make the implementation easier.
 *
 * This type of parser is called <em>recursive descent</em>. Each rule in our
 * grammar will have it's own function, and reference to other rules correspond
 * to calling that functions.
 */
public final class Parser {

    private final TokenStream tokens;

    public Parser(List<Token> tokens) {
        this.tokens = new TokenStream(tokens);
    }

    /**
     * Parses the {@code source} rule.
     */
    public Ast.Source parseSource() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        List<Ast.Field> fields = new ArrayList<Ast.Field>();
        List<Ast.Method> methods = new ArrayList<Ast.Method>();

        //If code is empty
        if(tokens.tokens.size() == 0){
            return new Ast.Source(fields,methods);
        }

        while(tokens.index < tokens.tokens.size()){
            //Start looping
            if(peek("LET",Token.Type.IDENTIFIER)) {
                fields.add(parseField());
            } else if(peek("DEF",Token.Type.IDENTIFIER,"(")){
                methods.add(parseMethod());
            } else{
                throw new ParseException("Invalid Source",tokens.index);
            }
        }

        return new Ast.Source(fields,methods);
    }

    /**
     * Parses the {@code field} rule. This method should only be called if the
     * next tokens start a field, aka {@code LET}.
     */
    public Ast.Field parseField() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO

        match("LET");
        String identifier = tokens.get(0).getLiteral();
        String typeName = "";
        match(tokens.get(0).getLiteral());
        if(peek(":",Token.Type.IDENTIFIER)){
            match(":");
            typeName = tokens.get(0).getLiteral();
            match(tokens.get(0).getLiteral());
        } else{
            throw new ParseException("Missing Type Name", tokens.index);
        }


        if(peek("=")){
            match("=");
            Ast.Expr expression = parseExpression();
            if(peek(";")){
                match(";");
                return new Ast.Field(identifier,typeName,Optional.of(expression));
            } else{
                throw new ParseException("Missing Semicolon", tokens.index);
            }
        } else{
            if(peek(";")){
                match(";");
                return new Ast.Field(identifier,typeName, Optional.empty());
            } else{
                throw new ParseException("Missing Semicolon", tokens.index);
            }
        }
    }

    /**
     * Parses the {@code method} rule. This method should only be called if the
     * next tokens start a method, aka {@code DEF}.
     */
    public Ast.Method parseMethod() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        String identifier;
        List<String> parameters = new ArrayList<String>();
        List<String> parTypeNames = new ArrayList<String>();
        List<Ast.Stmt> statements = new ArrayList<Ast.Stmt>();
        String returnType = "";

        match("DEF");
        identifier = tokens.get(0).getLiteral();
        match(tokens.get(0).getLiteral());
        match("(");

        if(peek(")")){
            match(")");
        } else{
            while(!peek(")")){
                if(peek(",",")")){
                    throw new ParseException("Trailing Comma", tokens.index);
                }
                else if(peek(",")){
                    match(",");
                } else{
                    if(peek(Token.Type.IDENTIFIER,":",Token.Type.IDENTIFIER)){
                        parameters.add(tokens.get(0).getLiteral());
                        match(tokens.get(0).getLiteral());
                        match(":");
                        parTypeNames.add(tokens.get(0).getLiteral());
                        match(tokens.get(0).getLiteral());
                    } else{
                        throw new ParseException("Missing Type Name", tokens.index);
                    }
                }
            }

            match(")");
        }
        if(peek(":",Token.Type.IDENTIFIER)){
            match(":");
            returnType = tokens.get(0).getLiteral();
            match(tokens.get(0).getLiteral());
        }

        if(peek("DO")){
            match("DO");
        } else{
            throw new ParseException("Missing DO Keyword", tokens.index);
        }

        while(!peek("END") && tokens.index < tokens.tokens.size()){
            statements.add(parseStatement());
        }

        if(peek("END")){
            match("END");
            return new Ast.Method(identifier,parameters,parTypeNames,Optional.of(returnType),statements);
        } else{
            throw new ParseException("Missing END Keyword", tokens.index);
        }
    }

    /**
     * Parses the {@code statement} rule and delegates to the necessary method.
     * If the next tokens do not start a declaration, if, while, or return
     * statement, then it is an expression/assignment statement.
     */
    public Ast.Stmt parseStatement() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO

        if(peek("LET",Token.Type.IDENTIFIER)){
            return parseDeclarationStatement();
        } else if(peek("IF")){
            return parseIfStatement();
        } else if(peek("FOR",Token.Type.IDENTIFIER)){
            return parseForStatement();
        } else if(peek("WHILE")){
            return parseWhileStatement();
        } else if(peek("RETURN")){
            return parseReturnStatement();
        } else{
            Ast.Expr expr1 = parseExpression();
            if(peek("=")){
                match("=");
                Ast.Expr expr2 = parseExpression();
                if(peek(";")){
                    match(";");
                    return new Ast.Stmt.Assignment(expr1,expr2);
                } else{
                    throw new ParseException("Missing Semicolon", tokens.index);
                }
            } else{
                if(peek(";")){
                    match(";");
                    return new Ast.Stmt.Expression(expr1);
                } else{
                    throw new ParseException("Missing Semicolon", tokens.index);
                }
            }
        }
    }

    /**
     * Parses a declaration statement from the {@code statement} rule. This
     * method should only be called if the next tokens start a declaration
     * statement, aka {@code LET}.
     */
    public Ast.Stmt.Declaration parseDeclarationStatement() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        match("LET");
        if(peek(Token.Type.IDENTIFIER,";")){
            String identifier = tokens.get(0).getLiteral();
            match(tokens.get(0).getLiteral());
            match(";");
            return new Ast.Stmt.Declaration(identifier,Optional.empty());
        } else if(peek(Token.Type.IDENTIFIER,"=")) {
            //its declaration with assignment
            String identifier = tokens.get(0).getLiteral();
            match(Token.Type.IDENTIFIER,"=");
            Ast.Expr expression = parseExpression();
            if(peek(";")){
                match(";");
                return new Ast.Stmt.Declaration(identifier,Optional.of(expression));
            }
            else{
                throw new ParseException("Missing Semicolon", tokens.index);
            }
        } else if(peek(Token.Type.IDENTIFIER,":")){
            //LET identifier : identifier = expression;
            String identifier = tokens.get(0).getLiteral();
            match(Token.Type.IDENTIFIER,":");
            String typeName = tokens.get(0).getLiteral();
            match(tokens.get(0).getLiteral());
            if(peek("=")){
                match("=");
                Ast.Expr expression = parseExpression();
                if(peek(";")){
                    match(";");
                    return new Ast.Stmt.Declaration(identifier,Optional.of(typeName),Optional.of(expression));
                } else{
                    throw new ParseException("Missing Semicolon", tokens.index);
                }
            } else{
                if(peek(";")){
                    match(";");
                    return new Ast.Stmt.Declaration(identifier,Optional.of(typeName),Optional.empty());
                } else{
                    throw new ParseException("Missing Semicolon", tokens.index);
                }
            }
        }
        else{
            throw new ParseException("Invalid Statement", tokens.index);
        }
    }

    /**
     * Parses an if statement from the {@code statement} rule. This method
     * should only be called if the next tokens start an if statement, aka
     * {@code IF}.
     */
    public Ast.Stmt.If parseIfStatement() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        List<Ast.Stmt> doStatement = new ArrayList<Ast.Stmt>();
        List<Ast.Stmt> elseStatement = new ArrayList<Ast.Stmt>();

        if(peek("IF","DO")){
            throw new ParseException("Missing Expression", tokens.index);
        }

        match("IF");
        Ast.Expr expression = parseExpression();
        if(peek("DO")){
            match("DO");
        } else{
            throw new ParseException("Missing DO Keyword", tokens.index);
        }
        while(!peek("END") && tokens.index < tokens.tokens.size()){
            doStatement.add(parseStatement());
            if(peek("ELSE")){
                match("ELSE");
                while(!peek("END") && tokens.index < tokens.tokens.size()){
                    elseStatement.add(parseStatement());
                }
            }
        }
        if(peek("END")){
            match("END");
            return new Ast.Stmt.If(expression,doStatement,elseStatement);
        } else {
            throw new ParseException("Missing END keyword ",tokens.index);
        }
    }

    /**
     * Parses a for statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a for statement, aka
     * {@code FOR}.
     */
    public Ast.Stmt.For parseForStatement() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        List<Ast.Stmt> forStatement = new ArrayList<Ast.Stmt>();

        if(peek("FOR",Token.Type.IDENTIFIER,"IN")){
            match("FOR");
            String identifier = tokens.get(0).getLiteral();
            match(tokens.get(0).getLiteral());
            if(peek("IN","DO")){
                throw new ParseException("Missing Expression", tokens.index);
            }
            match("IN");
            Ast.Expr expression = parseExpression();
            if(peek("DO")){
                match("DO");
            } else{
                throw new ParseException("Missing DO keyword", tokens.index);
            }
            while(!peek("END") && tokens.index < tokens.tokens.size()) {
                forStatement.add(parseStatement());
            }
            if(peek("END")){
                match("END");
                return new Ast.Stmt.For(identifier,expression,forStatement);
            } else {
                throw new ParseException("Missing END keyword ",tokens.index);
            }
        } else{
            throw new ParseException("Invalid FOR Statement", tokens.index);
        }
        /*
        match("FOR");
        if (peek(Token.Type.IDENTIFIER)) {
            elem = tokens.get(0).getLiteral();
            match(Token.Type.IDENTIFIER);
        } else {
            throw new ParseException("FOR not followed by IDENTIFIER", tokens.index);
        }

        if (peek("IN")) {
            match("IN");
        } else {
            throw new ParseException("FOR need IN", tokens.index);
        }

        list = parseExpression();

        if (peek("DO")) {
            match("DO");
        } else {
            throw new ParseException("FOR need DO", tokens.index);
        }

        while(!peek("END") && tokens.index < tokens.tokens.size()) {
            forStatement.add(parseStatement());
            if(peek(";")){
                match(";");
            }

        }
        if(peek("END")){
            System.out.println("Reached END and found END");
            match("END");
            return new Ast.Stmt.For(elem,list,forStatement);
        } else {
            System.out.println("NO END found");
            throw new ParseException("No END keyword ",tokens.index);
        }
        */
    }

    /**
     * Parses a while statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a while statement, aka
     * {@code WHILE}.
     */
    public Ast.Stmt.While parseWhileStatement() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        List<Ast.Stmt> whileStatement = new ArrayList<Ast.Stmt>();

        if(peek("WHILE","DO")){
            throw new ParseException("Missing Expression", tokens.index);
        }

        match("WHILE");
        Ast.Expr condition = parseExpression();
        if(peek("DO")){
            match("DO");
        } else{
            throw new ParseException("Missing DO keyword", tokens.index);
        }
        while(!peek("END") && tokens.index < tokens.tokens.size()){
            whileStatement.add(parseStatement());
        }
        if(peek("END")){
            match("END");
            return new Ast.Stmt.While(condition,whileStatement);
        } else {
            throw new ParseException("Missing END keyword",tokens.index);
        }
    }

    /**
     * Parses a return statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a return statement, aka
     * {@code RETURN}.
     */
    public Ast.Stmt.Return parseReturnStatement() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO

        if(peek("RETURN",";")){
            throw new ParseException("Invalid RETURN Statement", tokens.index);
        }
        match("RETURN");
        Ast.Expr expression = parseExpression();
        if(peek(";")) {
            match(";");
            return new Ast.Stmt.Return(expression);
        } else {
            throw new ParseException("Missing Semicolon", tokens.index);
        }
    }

    /**
     * Parses the {@code expression} rule.
     */
    public Ast.Expr parseExpression() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        // x + y OR x - y
        return parseLogicalExpression();
    }

    /**
     * Parses the {@code logical-expression} rule.
     */
    public Ast.Expr parseLogicalExpression() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        Ast.Expr left = parseEqualityExpression();
        //System.out.println("Logical left :" + left);
        while(match("AND") || match("OR")){
            if(!(peek(Token.Type.IDENTIFIER) || peek(Token.Type.INTEGER) || peek(Token.Type.DECIMAL) || peek(Token.Type.CHARACTER) || peek(Token.Type.STRING))){
                throw new ParseException("Missing Operand", tokens.index);
            }
            //System.out.println("In logical AND");

            String name = tokens.get(-1).getLiteral();
            //System.out.println(name);
            //System.out.println(tokens.get(0).getLiteral());

            Ast.Expr right = parseEqualityExpression();
            //System.out.println("Logical right: " + right);
            left = new Ast.Expr.Binary(name, left, right);
        }

        return left;
    }

    /**
     * Parses the {@code equality-expression} rule.
     */
    public Ast.Expr parseEqualityExpression() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        Ast.Expr left = parseAdditiveExpression();
        //System.out.println("Equality left " + left);
        while(match("<") || match(">") || match("<=") || match(">=") || match("==") || match("!=")){
            if(!(peek(Token.Type.IDENTIFIER) || peek(Token.Type.INTEGER) || peek(Token.Type.DECIMAL) || peek(Token.Type.CHARACTER) || peek(Token.Type.STRING))){
                //System.out.println("Found");
                throw new ParseException("Missing Operand", tokens.index);
            }
            String name = tokens.get(-1).getLiteral();
            //System.out.println(name);
            //System.out.println(tokens.get(0).getLiteral());

            Ast.Expr right = parseAdditiveExpression();
            left = new Ast.Expr.Binary(name, left, right);
        }
        return left;
    }

    /**
     * Parses the {@code additive-expression} rule.
     */
    public Ast.Expr parseAdditiveExpression() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        Ast.Expr left = parseMultiplicativeExpression();
        //System.out.println("Add left " + left);
        while(match("+") || match("-")){
            if(!(peek(Token.Type.IDENTIFIER) || peek(Token.Type.INTEGER) || peek(Token.Type.DECIMAL) || peek(Token.Type.CHARACTER) || peek(Token.Type.STRING))){
                throw new ParseException("Missing Operand", tokens.index);
            }
            String name = tokens.get(-1).getLiteral();
            //System.out.println(tokens.get(0).getLiteral());

            Ast.Expr right = parseMultiplicativeExpression();
            left = new Ast.Expr.Binary(name, left, right);
        }
        return left;
    }

    /**
     * Parses the {@code multiplicative-expression} rule.
     */
    public Ast.Expr parseMultiplicativeExpression() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        Ast.Expr left = parseSecondaryExpression();
        //System.out.println("Mul left " + left);
        while(match("*") || match("/")){
            if(!(peek(Token.Type.IDENTIFIER) || peek(Token.Type.INTEGER) || peek(Token.Type.DECIMAL) || peek(Token.Type.CHARACTER) || peek(Token.Type.STRING))){
                throw new ParseException("Missing Operand", tokens.index);
            }
            String name = tokens.get(-1).getLiteral();
            //System.out.println(tokens.get(0).getLiteral());

            Ast.Expr right = parseSecondaryExpression();
            left = new Ast.Expr.Binary(name, left, right);
        }
        return left;
    }

    /**
     * Parses the {@code secondary-expression} rule.
     */
    public Ast.Expr parseSecondaryExpression() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        List<Ast.Expr> arguments = new ArrayList<Ast.Expr>();
        //System.out.println("Primary primExpr " + tokens.get(0).getLiteral());

        if(peek(Token.Type.IDENTIFIER,"(")){
            return parsePrimaryFunction();
        } else if(peek(Token.Type.IDENTIFIER,".")) {
            //if no dot after parenthesis or identifier, set bool to false

            if(!peek(Token.Type.IDENTIFIER, ".", Token.Type.IDENTIFIER)) {
                throw new ParseException("Invalid Expression", tokens.index);
            }
            Ast.Expr primExpr = parsePrimaryExpression();
            match(".");
            //System.out.println("Secondary primExpr " + tokens.get(0).getLiteral());

            String identifier = tokens.get(0).getLiteral();
            match(tokens.get(0).getLiteral());

            if(peek("(")){
                match("(");
                while(!peek(")")) {
                    if (peek(",")) {
                        match(",");
                    } else{
                        arguments.add(new Ast.Expr.Access(Optional.empty(), tokens.get(0).getLiteral()));
                        match(tokens.get(0).getLiteral());
                    }
                }
                if(peek(")")) {
                    match(")");
                } else{
                    throw new ParseException("Missing Parenthesis", tokens.index);
                }
            } else{
                return new Ast.Expr.Access(Optional.of(primExpr), identifier);
            }

            return new Ast.Expr.Function(Optional.of(primExpr), identifier, arguments);

        } else{
            return parsePrimaryExpression();
        }
    }

    public Ast.Expr parsePrimaryFunction() throws ParseException{
        List<Ast.Expr> arguments = new ArrayList<Ast.Expr>();
        //System.out.println("Primary Fun ");
        String identifier = tokens.get(0).getLiteral();
        match(tokens.get(0).getLiteral());

        if(peek("(")){
            match("(");
            while(!peek(")")){
                if(peek(",",")")){
                    throw new ParseException("Trailing Comma", tokens.index);
                }
                else if(peek(",")){
                    match(",");
                } else {
                    //arguments.add(new Ast.Expr.Access(Optional.empty(), tokens.get(0).getLiteral()));
                    //match(tokens.get(0).getLiteral());
                    arguments.add(parseExpression());
                }
            }
            match(")");
        }
        return new Ast.Expr.Function(Optional.empty(), identifier, arguments);
    }

    /**
     * Parses the {@code primary-expression} rule. This is the top-level rule
     * for expressions and includes literal values, grouping, variables, and
     * functions. It may be helpful to break these up into other methods but is
     * not strictly necessary.
     */
    public Ast.Expr parsePrimaryExpression() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        if(peek(Token.Type.IDENTIFIER)){
            if(peek("TRUE")){
                match(tokens.get(0).getLiteral());
                return new Ast.Expr.Literal(Boolean.TRUE);
            }
            else if(peek("FALSE")){
                match(tokens.get(0).getLiteral());
                return new Ast.Expr.Literal(Boolean.FALSE);
            }
            else if(peek("NIL")){
                match(tokens.get(0).getLiteral());
                return new Ast.Expr.Literal(null);
            } else{
                String returnVar = tokens.get(0).getLiteral();
                match(tokens.get(0).getLiteral());
                //System.out.println("Primary returnVar " + returnVar);
                return new Ast.Expr.Access(Optional.empty(),returnVar);
            }
        }
        else if (peek(Token.Type.INTEGER)){
            String returnVar = tokens.get(0).getLiteral();
            match(tokens.get(0).getLiteral());
            return new Ast.Expr.Literal(new BigInteger(returnVar));
        }
        else if (peek(Token.Type.DECIMAL)){
            String returnVar = tokens.get(0).getLiteral();
            match(tokens.get(0).getLiteral());
            return new Ast.Expr.Literal(new BigDecimal(returnVar));
        }
        else if (peek(Token.Type.CHARACTER)){
            String str = tokens.get(0).getLiteral();
            if(str == "'\\n'"){
                match(tokens.get(0).getLiteral());
                return new Ast.Expr.Literal('\n');
            } else if(str == "'\\b'"){
                match(tokens.get(0).getLiteral());
                return new Ast.Expr.Literal('\b');
            } else if(str == "'\\r'"){
                match(tokens.get(0).getLiteral());
                return new Ast.Expr.Literal('\r');
            } else if(str == "'\\t'"){
                match(tokens.get(0).getLiteral());
                return new Ast.Expr.Literal('\t');
            } else if(str == "'\"'"){
                match(tokens.get(0).getLiteral());
                return new Ast.Expr.Literal('\"');
            } else if(str == "'\''"){
                match(tokens.get(0).getLiteral());
                return new Ast.Expr.Literal('\'');
            }
            else if(str == "'\\\\'"){
                match(tokens.get(0).getLiteral());
                return new Ast.Expr.Literal('\\');
            } else{
                match(tokens.get(0).getLiteral());
                return new Ast.Expr.Literal(str.charAt(1));
            }
        }
        else if (peek(Token.Type.STRING)){
            String str = tokens.get(0).getLiteral();
            String returnStr = "";
            String strReplace = "";

            boolean escape = false;
            int index = 0;

            for(int i = 0; i < str.length(); i++){
                if((str.charAt(i) == '\\')){
                    index = i;
                    escape = true;
                    break;
                }
            }
            strReplace = str.substring(index,index+2);
            char temp = str.charAt(index+1);

            if(temp == 'n'){
                returnStr = str.replace(strReplace,"\n");
            }
            if(temp == 'r'){
                returnStr = str.replace(strReplace,"\r");
            }
            if(temp == 'b'){
                returnStr = str.replace(strReplace,"\b");
            }
            if(temp == 't'){
                returnStr = str.replace(strReplace,"\t");
            }

            match(tokens.get(0).getLiteral());

            if(escape){
                return new Ast.Expr.Literal(returnStr.substring(1,returnStr.length()-1));
            }
            return new Ast.Expr.Literal(str.substring(1,str.length()-1));
        }
        else if(peek("(",")")){
            throw new ParseException("Missing Expression", tokens.index);
        }
        else if(peek("(",Token.Type.IDENTIFIER)){
            if(peek("(",Token.Type.IDENTIFIER,")")){
                //System.out.println("In ( and IDEN");
                match("(");
                String returnExpr = tokens.get(0).getLiteral();
                match(tokens.get(0).getLiteral());
                match(")");
                //return new Ast.Expr.Group(parseExpression());
                return new Ast.Expr.Group(new Ast.Expr.Access(Optional.empty(), returnExpr));
            } else if(peek("(",Token.Type.IDENTIFIER,Token.Type.OPERATOR) || peek("(",Token.Type.IDENTIFIER,"AND") || peek("(",Token.Type.IDENTIFIER,"OR")){
                match("(");
                Ast.Expr expr = parseExpression();
                //System.out.println("expr :" + expr);
                //System.out.println("After expr : " + tokens.get(0).getLiteral());
                match(")");
                return new Ast.Expr.Group(expr);
            } else{
                throw new ParseException("Missing Closing Parenthesis", tokens.index);
            }
        }
        else{
            //System.out.println("Passed Token is " + tokens.get(-1).getLiteral());
            throw new ParseException("Invalid Primary Expression", tokens.index);
        }
    }

    /**
     * As in the lexer, returns {@code true} if the current sequence of tokens
     * matches the given patterns. Unlike the lexer, the pattern is not a regex;
     * instead it is either a {@link Token.Type}, which matches if the token's
     * type is the same, or a {@link String}, which matches if the token's
     * literal is the same.
     *
     * In other words, {@code Token(IDENTIFIER, "literal")} is matched by both
     * {@code peek(Token.Type.IDENTIFIER)} and {@code peek("literal")}.
     */
    private boolean peek(Object... patterns) {
        //throw new UnsupportedOperationException(); //TODO (in lecture)
        for ( int i = 0; i < patterns.length; i++){
            //System.out.println("Pattern passed to peek is : " + patterns[i]);
            //System.out.println("I is : " + i);
            if(!tokens.has(i)){
                //System.out.println("Peek false @ has(i)");
                return false;
            } else if(patterns[i] instanceof Token.Type){
                if(patterns[i] != tokens.get(i).getType()){
                    //System.out.println("Peek false @ instance of token type");
                    return false;
                }
            } else if(patterns[i] instanceof String){
                if(!patterns[i].equals(tokens.get(i).getLiteral())){
                    //System.out.println("Peek false @ instance of string");
                    return false;
                }
            } else {
                throw new AssertionError("Invalid pattern object: " + patterns[i].getClass());
            }
        }
        return true;
    }

    /**
     * As in the lexer, returns {@code true} if {@link #peek(Object...)} is true
     * and advances the token stream.
     */
    private boolean match(Object... patterns) {
        //throw new UnsupportedOperationException(); //TODO (in lecture)
        boolean peek = peek(patterns);
        if (peek){
            for(int i = 0; i < patterns.length;i++){
                //System.out.println("Advancing the index");
                tokens.advance();
                //System.out.println("Advancing the index " + tokens.index);
            }
        }
        return peek;
    }

    private static final class TokenStream {

        private final List<Token> tokens;
        private int index = 0;

        private TokenStream(List<Token> tokens) {
            this.tokens = tokens;
        }

        /**
         * Returns true if there is a token at index + offset.
         */
        public boolean has(int offset) {
            return index + offset < tokens.size();
        }

        /**
         * Gets the token at index + offset.
         */
        public Token get(int offset) {
            return tokens.get(index + offset);
        }

        /**
         * Advances to the next token, incrementing the index.
         */
        public void advance() {
            index++;
        }

    }
}