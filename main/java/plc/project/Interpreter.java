package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
//WEDNESDAY SUBMISSION

public class Interpreter implements Ast.Visitor<Environment.PlcObject> {

    private Scope scope = new Scope(null);
    private Optional<Ast.Expr> receiver;
    //private Environment.PlcObject;

    public Interpreter(Scope parent) {
        scope = new Scope(parent);
        scope.defineFunction("print", 1, args -> {
            System.out.println(args.get(0).getValue());
            return Environment.NIL;
        });
    }

    public Scope getScope() {
        return scope;
    }

    @Override
    public Environment.PlcObject visit(Ast.Source ast) {
        //throw new UnsupportedOperationException(); //TODO
        //System.out.println(ast);
        for(Ast.Field flds : ast.getFields()){
            visit(flds);
        }
        for(Ast.Method mthds : ast.getMethods()){
            visit(mthds);
        }
        return scope.lookupFunction("main",0).invoke(Arrays.asList(Environment.NIL));
    }

    @Override
    public Environment.PlcObject visit(Ast.Field ast) { //same as declaration
        //throw new UnsupportedOperationException(); //TODO
        if (ast.getValue().isPresent()){
            scope.defineVariable(ast.getName(),visit(ast.getValue().get()));
        } else {
            scope.defineVariable(ast.getName(), Environment.NIL);
        }
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Method ast) {
        //throw new UnsupportedOperationException(); //TODO
        System.out.println(ast);
        System.out.println(ast.getName());
        System.out.println(ast.getStatements());
        System.out.println(ast.getParameters());

        scope.defineFunction(ast.getName(), ast.getParameters().size(), args -> {
            Scope oldScope = new Scope(scope);
            try {
                scope = new Scope(scope);

                for (int i = 0; i < ast.getParameters().size(); i++){
                    scope.defineVariable(ast.getParameters().get(i),Environment.create(args.get(i).getValue())); //variable definition for arguments
                }

                for(Ast.Stmt stmt : ast.getStatements()){ //evaluates method statements
                    System.out.println(stmt);
                    visit(stmt);
                }
            } catch (Return exception){
                System.out.println(exception);
                return exception.value;
            }
            finally {
                scope = oldScope;
            }
            return Environment.NIL;
        });

        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.Expression ast) {
        //throw new UnsupportedOperationException(); //TODO
        visit(ast.getExpression());
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.Declaration ast) {
        //throw new UnsupportedOperationException(); //TODO (in lecture)
        if (ast.getValue().isPresent()){
            scope.defineVariable(ast.getName(),visit(ast.getValue().get()));
        } else{
            scope.defineVariable(ast.getName(), Environment.NIL);
        }
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.Assignment ast) {
        //throw new UnsupportedOperationException(); //TODO
        /*
        First, ensure that the receiver is an Ast.Expr.Access (any other type is not assignable).
                If that access expression has a receiver, evaluate it and set a field,
                otherwise lookup and set a variable in the current scope.
                Returns NIL.
         */
        Ast.Expr.Access receiver;
        String receiverName;

        if(ast.getReceiver() instanceof Ast.Expr.Access){
            receiver = (Ast.Expr.Access)ast.getReceiver();
            receiverName = ((Ast.Expr.Access) ast.getReceiver()).getName();
        } else {
            throw new RuntimeException();
        }

        if(receiver.getReceiver().equals(Optional.empty())) { //no second receiver
            scope.lookupVariable(receiverName).setValue(visit(ast.getValue())); //sets ast value to variable name
        } else{
            Ast.Expr.Access secReceiver = (Ast.Expr.Access)receiver.getReceiver().get();
            String secRecName = secReceiver.getName();
            System.out.println(receiverName); //field
            System.out.println(secRecName); //object
            System.out.println(ast.getValue()); //prints Ast.Expr.Literal{literal = 1}
            System.out.println(scope.lookupVariable(secRecName).getValue().getField(receiverName).getValue().getValue()); //prints object.field
            System.out.println(scope.lookupVariable(secRecName).getValue());
            scope.lookupVariable(secRecName).getValue().setField(receiverName,visit(ast.getValue())); //sets field = 1
        }

        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.If ast) {
        //throw new UnsupportedOperationException(); //TODO
        if(requireType(Boolean.class,visit(ast.getCondition()))){
            try{
                scope = new Scope(scope);
                for(Ast.Stmt stmt : ast.getThenStatements()){
                    visit(stmt);
                }
            } finally{
                scope = scope.getParent();
            }
        } else{
            try{
                scope = new Scope(scope);
                for(Ast.Stmt stmt : ast.getElseStatements()){
                    visit(stmt);
                }
            } finally{
                scope = scope.getParent();
            }
        }

        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.For ast) {
        //throw new UnsupportedOperationException(); //TODO
        ArrayList<Object> list = new ArrayList<>();
        list = requireType(ArrayList.class, visit(ast.getValue()));
        for (Object value : list){
            try {
                scope = new Scope(scope);
                scope.defineVariable("num", (Environment.PlcObject) value);
                for (Ast.Expr.Stmt stmt : ast.getStatements()) {
                    visit(stmt);
                }
            }
            finally{
                scope = scope.getParent();
            }
        }
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.While ast) {
        //throw new UnsupportedOperationException(); //TODO (in lecture)
        while (requireType(Boolean.class,visit(ast.getCondition()))){
            try{
                scope = new Scope(scope);
                for(Ast.Stmt stmt : ast.getStatements()){
                    visit(stmt);
                }
            } finally{
                scope = scope.getParent();
            }
        }
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.Return ast) {
        //throw new UnsupportedOperationException(); //TODO
        Environment.PlcObject returnVar = visit(ast.getValue());

        throw new Return(returnVar);
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Literal ast) {
        //throw new UnsupportedOperationException(); //TODO
        if(ast.getLiteral() != null){
            return Environment.create(ast.getLiteral());
        } else{
            return Environment.NIL;
        }
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Group ast) {
        //throw new UnsupportedOperationException(); //TODO
        Environment.PlcObject returnVar = visit(ast.getExpression());

        return returnVar;
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Binary ast) {
        //throw new UnsupportedOperationException(); //TODO
        Environment.PlcObject left = visit(ast.getLeft());
        String operator = ast.getOperator();

        // AND / OR
        if(operator.equals("AND")){
            if(left.getValue().equals(false)){
                return Environment.create(false);
            } else{
                Environment.PlcObject right = visit(ast.getRight());
                if(right.getValue().equals(false)){
                    return Environment.create(false);
                } else{
                    return Environment.create(true);
                }
            }
        }
        if(operator.equals("OR")){
            if(left.getValue().equals(true)){
                return Environment.create(true);
            } else{
                Environment.PlcObject right = visit(ast.getRight());
                if(right.getValue().equals(true)){
                    return Environment.create(true);
                } else{
                    return Environment.create(false);
                }
            }
        }

        //REST OF CASES
        Environment.PlcObject right = visit(ast.getRight());
        if(left.getValue() instanceof BigInteger && !(right.getValue() instanceof BigInteger)){
            throw new RuntimeException();
        }
        if(left.getValue() instanceof BigDecimal && !(right.getValue() instanceof BigDecimal)){
            throw new RuntimeException();
        }
        if(left.getValue() instanceof String && !(right.getValue() instanceof String)){
            throw new RuntimeException();
        }

        if(operator.equals("<")){
            if(left.getValue() instanceof BigInteger && right.getValue() instanceof BigInteger){
                BigInteger leftVal = requireType(BigInteger.class, left);
                BigInteger rightVal = requireType(BigInteger.class, right);

                // < 0 means left is smaller
                // > 0 means left is bigger

                if(leftVal.compareTo(rightVal) < 0){
                    return Environment.create(true);
                } else{
                    return Environment.create(false);
                }

            } else if(left.getValue() instanceof BigDecimal && right.getValue() instanceof BigDecimal){
                BigDecimal leftVal = requireType(BigDecimal.class, left);
                BigDecimal rightVal = requireType(BigDecimal.class, right);

                // < 0 means left is smaller
                // > 0 means left is bigger

                if(leftVal.compareTo(rightVal) < 0){
                    return Environment.create(true);
                } else{
                    return Environment.create(false);
                }
            } else if(left.getValue() instanceof String && right.getValue() instanceof String){
                String leftVal = requireType(String.class, left);
                String rightVal = requireType(String.class, right);

                // < 0 means left is smaller
                // > 0 means left is bigger

                if(leftVal.compareTo(rightVal) < 0){
                    return Environment.create(true);
                } else{
                    return Environment.create(false);
                }
            } else{
                throw new RuntimeException();
            }
        }
        else if(operator.equals(">")){
            if(left.getValue() instanceof BigInteger && right.getValue() instanceof BigInteger) {
                BigInteger leftVal = requireType(BigInteger.class, left);
                BigInteger rightVal = requireType(BigInteger.class, right);

                // < 0 means left is smaller
                // > 0 means left is bigger

                if (leftVal.compareTo(rightVal) > 0) {
                    return Environment.create(true);
                } else {
                    return Environment.create(false);
                }
            } else if(left.getValue() instanceof BigDecimal && right.getValue() instanceof BigDecimal){
                BigDecimal leftVal = requireType(BigDecimal.class, left);
                BigDecimal rightVal = requireType(BigDecimal.class, right);

                // < 0 means left is smaller
                // > 0 means left is bigger

                if(leftVal.compareTo(rightVal) > 0){
                    return Environment.create(true);
                } else{
                    return Environment.create(false);
                }
            } else if(left.getValue() instanceof String && right.getValue() instanceof String){
                String leftVal = requireType(String.class, left);
                String rightVal = requireType(String.class, right);

                // < 0 means left is smaller
                // > 0 means left is bigger

                if(leftVal.compareTo(rightVal) > 0){
                    return Environment.create(true);
                } else{
                    return Environment.create(false);
                }
            } else{
                throw new RuntimeException();
            }
        }
        else if(operator.equals("<=")){
            if(left.getValue() instanceof BigInteger && right.getValue() instanceof BigInteger){
                BigInteger leftVal = requireType(BigInteger.class, left);
                BigInteger rightVal = requireType(BigInteger.class, right);
                // < 0 means left is smaller
                // > 0 means left is bigger

                if(leftVal.compareTo(rightVal) < 0 || leftVal.equals(rightVal)){
                    return Environment.create(true);
                } else{
                    return Environment.create(false);
                }
            } else if(left.getValue() instanceof BigDecimal && right.getValue() instanceof BigDecimal){
                BigDecimal leftVal = requireType(BigDecimal.class, left);
                BigDecimal rightVal = requireType(BigDecimal.class, right);
                // < 0 means left is smaller
                // > 0 means left is bigger

                if(leftVal.compareTo(rightVal) < 0 || leftVal.equals(rightVal)){
                    return Environment.create(true);
                } else{
                    return Environment.create(false);
                }
            } else if(left.getValue() instanceof String && right.getValue() instanceof String){
                String leftVal = requireType(String.class, left);
                String rightVal = requireType(String.class, right);
                // < 0 means left is smaller
                // > 0 means left is bigger

                if(leftVal.compareTo(rightVal) < 0 || leftVal.equals(rightVal)){
                    return Environment.create(true);
                } else{
                    return Environment.create(false);
                }
            } else{
                throw new RuntimeException();
            }
        }
        else if(operator.equals(">=")){
            if(left.getValue() instanceof BigInteger && right.getValue() instanceof BigInteger) {
                BigInteger leftVal = requireType(BigInteger.class, left);
                BigInteger rightVal = requireType(BigInteger.class, right);
                // < 0 means left is smaller
                // > 0 means left is bigger

                if (leftVal.compareTo(rightVal) > 0 || leftVal.equals(rightVal)) {
                    return Environment.create(true);
                } else {
                    return Environment.create(false);
                }
            } else if(left.getValue() instanceof BigDecimal && right.getValue() instanceof BigDecimal){
                BigDecimal leftVal = requireType(BigDecimal.class, left);
                BigDecimal rightVal = requireType(BigDecimal.class, right);
                // < 0 means left is smaller
                // > 0 means left is bigger

                if(leftVal.compareTo(rightVal) > 0 || leftVal.equals(rightVal)){
                    return Environment.create(true);
                } else{
                    return Environment.create(false);
                }
            } else if(left.getValue() instanceof String && right.getValue() instanceof String){
                String leftVal = requireType(String.class, left);
                String rightVal = requireType(String.class, right);
                // < 0 means left is smaller
                // > 0 means left is bigger

                if(leftVal.compareTo(rightVal) > 0 || leftVal.equals(rightVal)){
                    return Environment.create(true);
                } else{
                    return Environment.create(false);
                }
            } else{
                throw new RuntimeException();
            }
        }
        else if(operator.equals("==")){
            if(left.getValue().equals(right.getValue())){
                return Environment.create(true);
            } else{
                return Environment.create(false);
            }
        }
        else if(operator.equals("!=")){
            if(left.getValue().equals(right.getValue())){
                return Environment.create(false);
            } else{
                return Environment.create(true);
            }
        }
        else if(operator.equals("+")){
            if(left.getValue() instanceof BigInteger && right.getValue() instanceof BigInteger) {
                BigInteger leftVal = requireType(BigInteger.class, left);
                BigInteger rightVal = requireType(BigInteger.class, right);

                BigInteger returnVal = (leftVal.add(rightVal));

                return Environment.create(returnVal);
            } else if(left.getValue() instanceof BigDecimal && right.getValue() instanceof BigDecimal) {
                BigDecimal leftVal = requireType(BigDecimal.class, left);
                BigDecimal rightVal = requireType(BigDecimal.class, right);

                BigDecimal returnVal = (leftVal.add(rightVal));

                return Environment.create(returnVal);
            } else if(left.getValue() instanceof String && right.getValue() instanceof String) {
                String leftVal = requireType(String.class, left);
                String rightVal = requireType(String.class, right);

                String returnVal = (leftVal.concat(rightVal));

                return Environment.create(returnVal);
            } else{
                throw new RuntimeException();
            }
        }
        else if(operator.equals("-")){
            if(left.getValue() instanceof BigInteger && right.getValue() instanceof BigInteger) {
                BigInteger leftVal = requireType(BigInteger.class, left);
                BigInteger rightVal = requireType(BigInteger.class, right);

                BigInteger returnVal = leftVal.subtract(rightVal);

                return Environment.create(returnVal);
            } else if(left.getValue() instanceof BigDecimal && right.getValue() instanceof BigDecimal) {
                BigDecimal leftval = requireType(BigDecimal.class, left);
                BigDecimal rightVal = requireType(BigDecimal.class, right);

                BigDecimal returnVal = leftval.subtract(rightVal);

                return Environment.create(returnVal);
            } else{
                throw new RuntimeException();
            }
        }
        else if(operator.equals("*")){
            if(left.getValue() instanceof BigInteger && right.getValue() instanceof BigInteger) {
                BigInteger leftVal = requireType(BigInteger.class, left);
                BigInteger rightVal = requireType(BigInteger.class, right);

                BigInteger returnVal = leftVal.multiply(rightVal);

                return Environment.create(returnVal);
            } else if(left.getValue() instanceof BigDecimal && right.getValue() instanceof BigDecimal) {
                BigDecimal leftVal = requireType(BigDecimal.class, left);
                BigDecimal rightVal = requireType(BigDecimal.class, right);

                BigDecimal returnVal = leftVal.multiply(rightVal);

                return Environment.create(returnVal);
            } else{
                throw new RuntimeException();
            }
        }
        else if(operator.equals("/")){
            if(left.getValue() instanceof BigInteger && right.getValue() instanceof BigInteger) {
                BigInteger leftVal = requireType(BigInteger.class, left);
                BigInteger rightVal = requireType(BigInteger.class, right);

                BigInteger returnVal  = leftVal.divide(rightVal);

                return Environment.create(returnVal);
            } else if(left.getValue() instanceof BigDecimal && right.getValue() instanceof BigDecimal) {
                BigDecimal leftVal = requireType(BigDecimal.class, left);
                BigDecimal rightVal = requireType(BigDecimal.class, right);

                BigDecimal returnVal = leftVal.divide(rightVal,RoundingMode.HALF_EVEN);

                return Environment.create(returnVal);
            } else{
                throw new RuntimeException();
            }
        }
        else{
            throw new RuntimeException();
        }
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Access ast) {
        //throw new UnsupportedOperationException(); //TODO
        System.out.println(ast);
        if(ast.getReceiver().equals(Optional.empty())){
            String name = ast.getName();
            Environment.PlcObject value = scope.lookupVariable(name).getValue();
            return value;
        } else{
            Optional<Ast.Expr> reciever = ast.getReceiver();
            Ast.Expr.Access recieverObj = (Ast.Expr.Access) reciever.get();
            String reciverName = recieverObj.getName();
            String valueName = ast.getName();
            System.out.println(ast.getReceiver().get());

            Object returnVal = scope.lookupVariable(reciverName).getValue().getField(valueName).getValue().getValue();

            return Environment.create(returnVal);
        }
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Function ast) {
        //throw new UnsupportedOperationException(); //TODO
        if(ast.getReceiver().equals(Optional.empty())){
            String funcName = ast.getName();
            List<Environment.PlcObject> args = new ArrayList<>();

            for(int i = 0; i < ast.getArguments().size(); i++){
                //System.out.println(ast.getArguments().get(i));
                //args.add(visit(ast.getArguments().get(i)));
                args.add(0,visit(ast.getArguments().get(i)));
            }
            //System.out.println(scope.lookupFunction(funcName,args.size()).invoke(args));
            Object returnVar = scope.lookupFunction(funcName,args.size()).invoke(args).getValue();

            return Environment.create(returnVar);
        } else{
            String funcName = ast.getName();
            Optional<Ast.Expr> reciever = ast.getReceiver();
            Ast.Expr.Access recieverObj = (Ast.Expr.Access) reciever.get();
            String recName = recieverObj.getName();
            List<Environment.PlcObject> args = new ArrayList<>();

            for(int i = 0; i < ast.getArguments().size(); i++){
                //System.out.println(ast.getArguments().get(i));
                //args.add(visit(ast.getArguments().get(i)));
                args.add(0,visit(ast.getArguments().get(i)));
            }
            //System.out.println(funcName);
            //System.out.println(scope.lookupVariable(recName).getValue().callMethod(funcName,args).getValue());
            Object returnVar = scope.lookupVariable(recName).getValue().callMethod(funcName,args).getValue();

            return Environment.create(returnVar);
        }
    }

    /**
     * Helper function to ensure an object is of the appropriate type.
     */
    private static <T> T requireType(Class<T> type, Environment.PlcObject object) {
        if (type.isInstance(object.getValue())) {
            return type.cast(object.getValue());
        } else {
            throw new RuntimeException("Expected type " + type.getName() + ", received " + object.getValue().getClass().getName() + ".");
        }
    }

    /**
     * Exception class for returning values.
     */
    private static class Return extends RuntimeException {

        private final Environment.PlcObject value;

        private Return(Environment.PlcObject value) {
            this.value = value;
        }
    }
}