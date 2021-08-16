package plc.project;

import java.io.PrintWriter;
import java.math.BigDecimal;
//WEDNESDAY SUBMISSION

public final class Generator implements Ast.Visitor<Void> {

    private final PrintWriter writer;
    private int indent = 0;

    public Generator(PrintWriter writer) {
        this.writer = writer;
    }

    private void print(Object... objects) {
        for (Object object : objects) {
            if (object instanceof Ast) {
                visit((Ast) object);
            } else {
                writer.write(object.toString());
            }
        }
    }

    private void newline(int indent) {
        writer.println();
        for (int i = 0; i < indent; i++) {
            writer.write("    ");
        }
    }

    @Override
    public Void visit(Ast.Source ast) {
        //throw new UnsupportedOperationException(); //TODO
        print("public class Main {");
        newline(0);

        indent++; //indent 1
        //newline(indent); //print fields at indent 1
        for(int i = 0; i < ast.getFields().size(); i++){
            newline(indent);
            print(ast.getFields().get(i));
        }

        newline(indent);
        print("public static void main(String[] args) {");
        indent++; //indent 2
        newline(indent);
        print("System.exit(new Main().main());"); //prints at indent 2
        indent--; //indent 1
        newline(indent);
        print("}"); //prints at indent 1
        newline(0); //adds space

        //newline(indent); //prints methods at indent 1
        for(int i = 0; i < ast.getMethods().size(); i++){
            newline(indent);
            print(ast.getMethods().get(i));
        }

        indent--; //indent 0
        newline(indent); //space
        newline(indent); //space
        print("}"); //prints at indent 0

        return null;
    }

    @Override
    public Void visit(Ast.Field ast) {
        //throw new UnsupportedOperationException(); //TODO
        //System.out.println(ast.getVariable().getType().getJvmName());
        //System.out.println(ast.getVariable().getJvmName());
        //System.out.println(ast.getValue().get());
        if(ast.getValue().isPresent()){
            print(ast.getVariable().getType().getJvmName(), " ", ast.getVariable().getJvmName(), " = ", ast.getValue().get(), ";");
        } else{
            print(ast.getVariable().getType().getJvmName(), " ", ast.getVariable().getJvmName(), ";");
        }

        return null;
    }

    @Override
    public Void visit(Ast.Method ast){
        //throw new UnsupportedOperationException(); //TODO
        //System.out.println(ast.getFunction().getJvmName());
        //System.out.println(ast.getFunction().getReturnType().getJvmName());
        print(ast.getFunction().getReturnType().getJvmName(), " ", ast.getFunction().getJvmName(), "(");
        for(int i = 0; i < ast.getParameters().size(); i++){
            print(ast.getFunction().getParameterTypes().get(i).getJvmName(), " ", ast.getParameters().get(i));
            if(i != ast.getParameters().size() - 1){
                print(", ");
            }
        }
        print(") {");
        if(ast.getStatements().isEmpty()){
            print("}");
        } else{
            indent++;
            for(int i = 0; i < ast.getStatements().size(); i++){
                newline(indent);
                print(ast.getStatements().get(i));
            }
            indent--;
            newline(indent);
            print("}");
        }

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Expression ast) {
        //throw new UnsupportedOperationException(); //TODO
        //System.out.println(ast.getExpression());
        //visit(ast.getExpression());
        print(ast.getExpression(), ";");

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Declaration ast) {
        //throw new UnsupportedOperationException(); //TODO
        //System.out.println(ast.getName());
        //System.out.println(ast.getVariable().getName());
        //System.out.println(ast.getValue().get());

        if(!ast.getValue().isPresent()){
            print(ast.getVariable().getType().getJvmName(), " ", ast.getVariable().getJvmName(), ";");
        } else{
            print(ast.getVariable().getType().getJvmName(), " ", ast.getVariable().getJvmName(), " = ", ast.getValue().get(), ";");
        }

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Assignment ast) {
        //throw new UnsupportedOperationException(); //TODO
        //System.out.println(ast.getReceiver());
        //System.out.println(ast.getValue());
        print(ast.getReceiver(), " = ", ast.getValue(), ";");

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.If ast) {
        //throw new UnsupportedOperationException(); //TODO
        if(ast.getElseStatements().isEmpty()){
            print("if ", "(", ast.getCondition(), ") ", "{");
            indent++;
            for(int i = 0; i < ast.getThenStatements().size(); i++){
                newline(indent);
                print(ast.getThenStatements().get(i));
            }
            indent--;
            newline(indent);
            print("}");
        } else{
            print("if ", "(", ast.getCondition(), ") ", "{");
            indent++;
            for(int i = 0; i < ast.getThenStatements().size(); i++){
                newline(indent);
                print(ast.getThenStatements().get(i));
            }
            indent--;
            newline(indent);
            print("} ", "else ", "{");
            indent++;
            for(int i = 0; i < ast.getElseStatements().size(); i++){
                newline(indent);
                print(ast.getElseStatements().get(i));
            }
            indent--;
            newline(indent);
            print("}");
        }

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.For ast) {
        //throw new UnsupportedOperationException(); //TODO
        print("for ", "(", "int ", ast.getName(), " : ", ast.getValue(), ") {");
        indent++;
        for(int i = 0; i < ast.getStatements().size(); i++){
            newline(indent);
            print(ast.getStatements().get(i));
        }
        indent--;
        newline(indent);
        print("}");

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.While ast) {
        //throw new UnsupportedOperationException(); //TODO
        if(ast.getStatements().isEmpty()){
            print("while ", "(", ast.getCondition(), ")", " {", "}");
        } else{
            print("while ", "(", ast.getCondition(), ")", " {");
            indent++;
            for(int i = 0; i < ast.getStatements().size(); i++){
                newline(indent);
                print(ast.getStatements().get(i));
            }
            indent--;
            newline(indent);
            print("}");
        }

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Return ast) {
        //throw new UnsupportedOperationException(); //TODO
        //System.out.println(ast.getValue());
        print("return ", ast.getValue(), ";");

        return null;
    }

    @Override
    public Void visit(Ast.Expr.Literal ast) {
        //throw new UnsupportedOperationException(); //TODO
        //System.out.println(ast.getLiteral());
        //System.out.println(ast.getLiteral());
        //System.out.println(ast.getType().getName());
        //print(ast.getLiteral());
        if(ast.getType().getName().equals("String") || ast.getType().getName().equals("Character")){
            print("\"", ast.getLiteral(), "\"");
        } else if(ast.getType().getName().equals("Decimal")){
            print(new BigDecimal(ast.getLiteral().toString()));
        } else {
            print(ast.getLiteral());
        }

        return null;
    }

    @Override
    public Void visit(Ast.Expr.Group ast) {
        //throw new UnsupportedOperationException(); //TODO
        //System.out.println(ast.getExpression());
        print("(", ast.getExpression(), ")");

        return null;
    }

    @Override
    public Void visit(Ast.Expr.Binary ast) {
        //throw new UnsupportedOperationException(); //TODO
        //System.out.println(ast.getOperator());

        if(ast.getOperator().equals("AND")){
            print(ast.getLeft(), " && ", ast.getRight());
        } else if(ast.getOperator().equals("OR")){
            print(ast.getLeft(), " || ", ast.getRight());
        } else{
            //visit(ast.getLeft());
            print(ast.getLeft(), " ", ast.getOperator(), " ", ast.getRight());
            //visit(ast.getRight());
        }

        return null;
    }

    @Override
    public Void visit(Ast.Expr.Access ast) {
        //throw new UnsupportedOperationException(); //TODO
        if(ast.getReceiver().isPresent()){
            print(ast.getReceiver().get(), ".", ast.getVariable().getJvmName());
        } else{
            print(ast.getVariable().getJvmName());
        }

        return null;
    }

    @Override
    public Void visit(Ast.Expr.Function ast) {
        //throw new UnsupportedOperationException(); //TODO
        /*
        System.out.println(ast.getFunction().getName());
        System.out.println(ast.getFunction().getJvmName());
        for(int i = 0; i < ast.getArguments().size(); i++){
            System.out.println(ast.getArguments().get(i));
        }
        */
        if(ast.getReceiver().isPresent()){
            print(ast.getReceiver().get(),".");
        }
        print(ast.getFunction().getJvmName(), "(");
        for(int i = 0; i < ast.getArguments().size(); i++){
            //System.out.println(ast.getArguments().get(i));
            print(ast.getArguments().get(i));
            if(i != ast.getArguments().size() - 1){
                print(", ");
            }
        }
        print(")");

        return null;
    }

}