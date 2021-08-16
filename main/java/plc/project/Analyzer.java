package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
//WEDNESDAY SUBMISSION
/**
 * See the specification for information about what the different visit
 * methods should do.
 */
public final class Analyzer implements Ast.Visitor<Void> {

    public Scope scope;
    private Ast.Method method;

    public Analyzer(Scope parent) {
        scope = new Scope(parent);
        scope.defineFunction("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL);
    }

    public Scope getScope() {
        return scope;
    }

    @Override
    public Void visit(Ast.Source ast) {
        //throw new UnsupportedOperationException();  // TODO
        //System.out.println(ast.getFields());
        //System.out.println(ast.getMethods());

        for(int i = 0; i < ast.getFields().size(); i++){
            System.out.println("1");
            visit(ast.getFields().get(i));
        }

        for(int i = 0; i < ast.getMethods().size(); i++){
            visit(ast.getMethods().get(i));
        }
        System.out.println("Hi");
        System.out.println(scope.lookupFunction("main",0).getReturnType());
        //How do I check if the function exists?
        scope.lookupFunction("main",0);

        if(!(scope.lookupFunction("main",0).getReturnType().equals(Environment.Type.INTEGER))){
            throw new RuntimeException("Return Type Is Not Of Type Integer");
        }

        return null;
    }

    @Override
    public Void visit(Ast.Field ast) {
        //throw new UnsupportedOperationException();  // TODO

        if(ast.getValue().isPresent()){
            visit(ast.getValue().get());
            Environment.Type type = Environment.getType(ast.getTypeName());
            Environment.Type valType = ast.getValue().get().getType();
            requireAssignable(type,valType);
        }

        Environment.Variable variable = scope.defineVariable(ast.getName(), ast.getName(), Environment.getType(ast.getTypeName()), Environment.NIL);
        ast.setVariable(variable);

        return null;
    }

    @Override
    public Void visit(Ast.Method ast) {
        //throw new UnsupportedOperationException();  // TODO
        //System.out.println(ast.getName());
        //System.out.println(ast.getReturnTypeName());
        List<Environment.Type> paramTypes = new ArrayList<>();

        for(int i = 0; i < ast.getParameters().size(); i++){
            //System.out.println("Parameters: " + ast.getParameters().get(i) + " Type: " + ast.getParameterTypeNames().get(i));
            paramTypes.add(Environment.getType(ast.getParameterTypeNames().get(i)));
        }

        //how do i assign nil in function definition when return type is missing
        if(!ast.getReturnTypeName().isPresent()){
            scope.defineFunction(ast.getName(), ast.getName(), paramTypes, Environment.Type.NIL, args -> Environment.NIL);
        } else{
            scope.defineFunction(ast.getName(), ast.getName(), paramTypes, Environment.getType(ast.getReturnTypeName().get()), args -> Environment.NIL);
        }

        ast.setFunction(scope.lookupFunction(ast.getName(),ast.getParameters().size()));

        try {
            scope = new Scope(scope);

            if(!ast.getReturnTypeName().isPresent()){
                scope.defineVariable("typeOfReturn","typeOfReturn", Environment.Type.NIL, Environment.NIL);
            } else{
                scope.defineVariable("typeOfReturn","typeOfReturn", Environment.getType(ast.getReturnTypeName().get()), Environment.NIL);
            }

            for(int i = 0; i < ast.getParameters().size(); i++){
                scope.defineVariable(ast.getParameters().get(i), ast.getParameters().get(i), Environment.getType(ast.getParameterTypeNames().get(i)), Environment.NIL);
            }

            for (Ast.Stmt stmt : ast.getStatements()){
                visit(stmt);
            }
        } finally {
            scope = scope.getParent();
        }

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Expression ast) {
        //throw new UnsupportedOperationException();  // TODO
        //System.out.println("Expression: " + ast.getExpression());
        visit(ast.getExpression());
        if(!(ast.getExpression() instanceof Ast.Expr.Function)){
            throw new RuntimeException("Expression Not Ast.Expr.Fucntion");
        }

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Declaration ast) {
        //throw new UnsupportedOperationException();  // TODO

        //System.out.println(ast);
        //System.out.println(ast.getName());
        //System.out.println(ast.getValue().get());
        //System.out.println(ast.getTypeName());

        if(ast.getValue().equals(Optional.empty()) && ast.getTypeName().equals(Optional.empty())){
            throw new RuntimeException("Empty Value And Typename");
        } else if(ast.getTypeName().isPresent()){
            if(ast.getValue().isPresent()){
                visit(ast.getValue().get());
                Environment.Type valType = ast.getValue().get().getType();
                Environment.Type type = Environment.getType(ast.getTypeName().get());
                requireAssignable(type,valType);
                //ast.setVariable(scope.defineVariable(ast.getName(),ast.getName(),ast.getValue().get().getType(),Environment.NIL));
            }
            ast.setVariable(scope.defineVariable(ast.getName(),ast.getName(),Environment.getType(ast.getTypeName().get()),Environment.NIL));
        } else{
            visit(ast.getValue().get());
            ast.setVariable(scope.defineVariable(ast.getName(),ast.getName(),ast.getValue().get().getType(),Environment.NIL));
        }

        /*
        //value empty type empty : 1
        //value empty type unknown : 2
        //value empty type full : 3
        //value full type empty : 4
        //value full type full : 5

        else if(ast.getValue().equals(Optional.empty()) && ast.getTypeName().get().equals("Unknown")){
            //System.out.println("2");
            //System.out.println(Environment.getType(ast.getTypeName().get()).getName());
            throw new RuntimeException("Unknown Type");
        } else if(!(ast.getTypeName().equals(Optional.empty())) && ast.getValue().equals(Optional.empty())){
            //System.out.println("3");
            //scope.defineVariable(ast.getName(), ast.getName(), Environment.getType(ast.getTypeName().get()), Environment.NIL);
            ast.setVariable(scope.defineVariable(ast.getName(),ast.getName(),Environment.getType(ast.getTypeName().get()),Environment.NIL));
        } else if(ast.getTypeName().equals(Optional.empty()) && !(ast.getValue().equals(Optional.empty()))){
            //System.out.println("4");
            visit(ast.getValue().get());
            ast.setVariable(scope.defineVariable(ast.getName(),ast.getName(),ast.getValue().get().getType(),Environment.NIL));
        } else if(!(ast.getValue().equals(Optional.empty())) && ast.getTypeName().equals(Optional.empty())){
            //System.out.println("5");
            Environment.Type valType = ast.getValue().get().getType();
            Environment.Type type = Environment.getType(ast.getTypeName().get());
            requireAssignable(type,valType);
        }
        */

        //scope.defineVariable(ast.getName(), ast.getName(), ast.getVariable().getType(), Environment.NIL);
        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Assignment ast) {
        //throw new UnsupportedOperationException();  // TODO
        //reciever is not an access expression
        //value is not assignable to reciever

        //PRINTS
        //System.out.println(ast);
        //System.out.println(ast.getReceiver());
        //System.out.println(scope.lookupVariable(((Ast.Expr.Access) ast.getReceiver()).getName()).getType());
        //System.out.println(ast.getValue());
        //System.out.println(ast.getValue());
        //System.out.println(ast.getValue().getType());
        //Environment.Type receiverType = scope.lookupVariable(((Ast.Expr.Access) ast.getReceiver()).getName()).getType();
        //Environment.Type valueType = ast.getValue().getType();

        if(!(ast.getReceiver() instanceof Ast.Expr.Access)){
            throw new RuntimeException("Receiver is not Ast.Expr.Access");
        }

        visit(ast.getValue());
        visit(ast.getReceiver());
        requireAssignable(ast.getReceiver().getType(),ast.getValue().getType());

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.If ast) {
        //throw new UnsupportedOperationException();  // TODO
        //CONDITION IS NOT BOOLEAN
        //THEN STATEMENTS LIST IS EMPTY

        //System.out.println(ast.getCondition());
        //System.out.println(ast.getThenStatements().size());
        //System.out.println(ast);

        if(ast.getThenStatements().size() == 0){
            throw new RuntimeException("Empty Then Statements");
        } else{
            visit(ast.getCondition());
            //System.out.println(ast.getCondition().getType().equals(Environment.Type.BOOLEAN));
            if(!(ast.getCondition().getType().equals(Environment.Type.BOOLEAN))){
                throw new RuntimeException("Condtion Is Not Of Type Boolean");
            }

            try{
                scope = new Scope(scope);
                for(Ast.Stmt stmt: ast.getThenStatements()){
                    visit(stmt);
                }
            } finally{
                scope = scope.getParent();
            }

            if(!(ast.getElseStatements().size() == 0)){
                try{
                    scope = new Scope(scope);
                    for (Ast.Stmt stmt : ast.getElseStatements()) {
                        visit(stmt);
                    }
                } finally{
                    scope = scope.getParent();
                }
            }

            /*
            for(int i = 0; i < ast.getThenStatements().size(); i++){
                scope = new Scope(scope);
                visit(ast.getThenStatements().get(i));
            }
            for(int i = 0; i < ast.getElseStatements().size(); i++){
                visit(ast.getElseStatements().get(i));
            }
            */
        }

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.For ast) {
        //throw new UnsupportedOperationException();  // TODO
        //value is not integer iterable
        //statements list is empty
        //System.out.println(ast.getValue());
        //System.out.println(ast.getName());
        //System.out.println(scope.lookupVariable(ast.getName()));
        visit(ast.getValue());

        if(!(ast.getValue().getType().equals(Environment.Type.INTEGER_ITERABLE))){
            throw new RuntimeException("Value Is Not Of Type Integer Iterable");
        } else if(ast.getStatements().isEmpty()){
            throw new RuntimeException("Statements List Is Empty");
        } else{
            try {
                scope = new Scope(scope);
                scope.defineVariable(ast.getName(), ast.getName(), Environment.Type.INTEGER, Environment.NIL);
                for (Ast.Stmt stmt : ast.getStatements()){
                    visit(stmt);
                }
            } finally {
                scope = scope.getParent();
            }
        }

        return null;
    }

    //UNSURE
    @Override
    public Void visit(Ast.Stmt.While ast) {
        //throw new UnsupportedOperationException();  // TODO
        visit(ast.getCondition());
        requireAssignable(Environment.Type.BOOLEAN,ast.getCondition().getType());

        try{
            scope = new Scope(scope);
            for (Ast.Stmt stmt : ast.getStatements()){
                visit(stmt);
            }
        } finally{
            scope = scope.getParent();
        }

        return null;
    }

    //CHECK
    @Override
    public Void visit(Ast.Stmt.Return ast) {
        //throw new UnsupportedOperationException();  // TODO
        visit(ast.getValue());
        Environment.Type retType = scope.lookupVariable("typeOfReturn").getType();
        requireAssignable(retType,ast.getValue().getType());

        return null;
    }

    @Override
    public Void visit(Ast.Expr.Literal ast) {
        //throw new UnsupportedOperationException();  // TODO
        //System.out.println(ast.getLiteral());
        //System.out.println(ast.getType());

        if(ast.getLiteral() instanceof Boolean){
            ast.setType(Environment.Type.BOOLEAN);
        } else if(ast.getLiteral() instanceof Character){
            ast.setType(Environment.Type.CHARACTER);
        } else if(ast.getLiteral() instanceof String){
            ast.setType(Environment.Type.STRING);
        } else if(ast.getLiteral() instanceof BigInteger){
            //System.out.println(ast.getLiteral());
            //System.out.println(BigInteger.valueOf(Integer.MIN_VALUE));
            //System.out.println(((BigInteger)ast.getLiteral()).compareTo(BigInteger.valueOf(Integer.MIN_VALUE)));
            //System.out.println(((BigInteger)ast.getLiteral()).compareTo(BigInteger.valueOf(Integer.MAX_VALUE)));
            if(((BigInteger)ast.getLiteral()).compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0){
                throw new RuntimeException("Integer Value Too Large");
            } else if(((BigInteger)ast.getLiteral()).compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) < 0){
                throw new RuntimeException("Integer Value Too Small");
            } else{
                ast.setType(Environment.Type.INTEGER);
            }
        } else if(ast.getLiteral() instanceof BigDecimal){
            //System.out.println(((BigDecimal)ast.getLiteral()).doubleValue());
            /*
            if(((BigDecimal) ast.getLiteral()).compareTo(BigDecimal.valueOf(Double.MAX_VALUE)) > 0){
                System.out.println("INVALID");
                throw new RuntimeException("Invalid Decimal Number");
            } else{
                ast.setType(Environment.Type.DECIMAL);
            }
            */

            if(((BigDecimal)ast.getLiteral()).doubleValue() == Double.POSITIVE_INFINITY || ((BigDecimal) ast.getLiteral()).doubleValue() == Double.NEGATIVE_INFINITY){
                throw new RuntimeException("Decimal Value Too Large");
            } else{
                ast.setType(Environment.Type.DECIMAL);
            }

        } else if(ast.getLiteral() instanceof Iterable){
            ast.setType(Environment.Type.INTEGER_ITERABLE);
        } else{
            if(ast.getLiteral() == null){
                ast.setType(Environment.Type.NIL);
            }
        }
        return null;
    }

    @Override
    public Void visit(Ast.Expr.Group ast) {
        //throw new UnsupportedOperationException();  // TODO
        visit(ast.getExpression());
        if(!(ast.getExpression() instanceof Ast.Expr.Binary)){
            throw new RuntimeException("Not a Binary Group");
        } else{
            visit(ast.getExpression()); //When I call this function doesn't it set the type in Binary
            Environment.Type exprType = ast.getExpression().getType();
            ast.setType(exprType);
        }

        return null;
    }

    @Override
    public Void visit(Ast.Expr.Binary ast) {
        //throw new UnsupportedOperationException();  // TODO
        if(ast.getOperator().equals("AND") || ast.getOperator().equals("OR")){
            visit(ast.getLeft());
            if(ast.getLeft().getType().equals(Environment.Type.BOOLEAN)){
                visit(ast.getRight());
                if(ast.getRight().getType().equals(Environment.Type.BOOLEAN)){
                    ast.setType(Environment.Type.BOOLEAN);
                } else{
                    throw new RuntimeException("Right Value Not Boolean Type");
                }
            } else{
                throw new RuntimeException("Left Value Not Boolean Type");
            }
        } else if(ast.getOperator().equals("<") || ast.getOperator().equals(">") || ast.getOperator().equals("==") || ast.getOperator().equals("<=") || ast.getOperator().equals(">=") || ast.getOperator().equals("!=")){
            visit(ast.getLeft());
            visit(ast.getRight());
            if(ast.getLeft().getType().equals(Environment.Type.BOOLEAN) || ast.getLeft().getType().equals(Environment.Type.NIL) || ast.getRight().getType().equals(Environment.Type.BOOLEAN) || ast.getRight().getType().equals(Environment.Type.NIL)){
                throw new RuntimeException("Not Comparable");
            } else{
                if(ast.getLeft().getType().equals(ast.getRight().getType())){
                    ast.setType(Environment.Type.BOOLEAN);
                } else{
                    throw new RuntimeException("Left and Right Not of Same Type");
                }
            }
        } else if(ast.getOperator().equals("+")){
            visit(ast.getLeft());
            visit(ast.getRight());
            Environment.Type leftType = ast.getLeft().getType();
            Environment.Type rightType = ast.getRight().getType();

            if(leftType.equals(Environment.Type.STRING) || rightType.equals(Environment.Type.STRING)){
                ast.setType(Environment.Type.STRING);
            } else{
                if(leftType.equals(Environment.Type.INTEGER) || leftType.equals(Environment.Type.DECIMAL)){
                    if(leftType.equals(rightType)){
                        if(leftType.equals(Environment.Type.INTEGER)){
                            ast.setType(Environment.Type.INTEGER);
                        }
                        if(leftType.equals(Environment.Type.DECIMAL)){
                            ast.setType(Environment.Type.DECIMAL);
                        }
                    } else{
                        throw new RuntimeException("Not Same Types");
                    }
                } else{
                    throw new RuntimeException("Not Integer or Decimal Type");
                }
            }
        } else if(ast.getOperator().equals("-") || ast.getOperator().equals("*") || ast.getOperator().equals("/")){
            visit(ast.getLeft());
            visit(ast.getRight());
            Environment.Type leftType = ast.getLeft().getType();
            Environment.Type rightType = ast.getRight().getType();

            if(leftType.equals(Environment.Type.INTEGER) || leftType.equals(Environment.Type.DECIMAL)){
                if(leftType.equals(rightType)){
                    if(leftType.equals(Environment.Type.INTEGER)){
                        ast.setType(Environment.Type.INTEGER);
                    }
                    if(leftType.equals(Environment.Type.DECIMAL)){
                        ast.setType(Environment.Type.DECIMAL);
                    }
                } else{
                    throw new RuntimeException("Not Same Types");
                }
            } else{
                throw new RuntimeException("Not Integer or Decimal Type");
            }
        } else{
            throw new RuntimeException("Invalid Operator");
        }

        return null;
    }

    @Override
    public Void visit(Ast.Expr.Access ast) {
        //throw new UnsupportedOperationException();  // TODO

        if(ast.getReceiver().equals(Optional.empty())){
            Environment.Variable variable = scope.lookupVariable(ast.getName());
            ast.setVariable(variable);
        } else{
            Ast.Expr.Access variable = (Ast.Expr.Access) ast.getReceiver().get();
            String astName = ast.getName();
            String recieverName = variable.getName();
            visit(variable);

            Environment.Variable finalVar = scope.lookupVariable(recieverName).getType().getField(astName);
            ast.setVariable(finalVar);
        }

        return null;
    }

    @Override
    public Void visit(Ast.Expr.Function ast) {
        //throw new UnsupportedOperationException();  // TODO

        if(ast.getReceiver().equals(Optional.empty())){
            //SET FUNCTION
            Environment.Function function = scope.lookupFunction(ast.getName(),ast.getArguments().size());
            ast.setFunction(function);

            for(int i = 0; i < ast.getArguments().size(); i++){
                visit(ast.getArguments().get(i));
                Environment.Type parType = Environment.getType(ast.getFunction().getParameterTypes().get(i).getName());
                Environment.Type argType = ast.getArguments().get(i).getType();
                requireAssignable(parType,argType);
            }
        } else{
            Ast.Expr.Access function = (Ast.Expr.Access) ast.getReceiver().get();
            String astName = ast.getName();
            String recieverName = function.getName();
            visit(function);

            //SET FUNCTION
            Environment.Function finalFunc = scope.lookupVariable(recieverName).getType().getMethod(astName,ast.getArguments().size());
            ast.setFunction(finalFunc);

            for(int i = 1; i < ast.getArguments().size(); i++){
                visit(ast.getArguments().get(i));
                Environment.Type parType = Environment.getType(ast.getFunction().getParameterTypes().get(i).getName());
                Environment.Type argType = ast.getArguments().get(i).getType();
                requireAssignable(parType,argType);
            }
        }

        return null;
    }

    public static void requireAssignable(Environment.Type target, Environment.Type type) {
        //throw new UnsupportedOperationException();  // TODO
        if(target.equals(Environment.Type.COMPARABLE)) {
            if(!(type.equals(Environment.Type.INTEGER) || type.equals(Environment.Type.DECIMAL) || type.equals(Environment.Type.STRING) || type.equals(Environment.Type.CHARACTER))) {
                throw new RuntimeException("Type Cannot Be Assigned To Target");
            }
        } else if(!target.equals(type)) {
            if(!target.equals(Environment.Type.ANY)) {
                throw new RuntimeException("Type Cannot Be Assigned To Target");
            }
        }

        /*
        if(target.equals(Environment.Type.ANY)){

        } else if(target.equals(Environment.Type.COMPARABLE)){
            //Integer, Decimal, Character, String
            if(!(type.equals(Environment.Type.INTEGER) || type.equals(Environment.Type.DECIMAL) || type.equals(Environment.Type.STRING) || type.equals(Environment.Type.CHARACTER))){
                throw new RuntimeException("Type Cannot Be Assigned To Target");
            } //Do I make sure the Target can be Int, Dec, Str, Char not sure hwo this works
        } else if(target.equals(type)){
            System.out.println("Assignment Can Be Performed");
        } else{
            throw new RuntimeException("Type Cannot Be Assigned To Target");
        }
        */
    }
}
