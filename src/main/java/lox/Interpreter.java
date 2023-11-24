package lox;

import java.util.List;
import java.util.Objects;

import lox.Expr.Binary;
import lox.Expr.Grouping;
import lox.Expr.Literal;
import lox.Expr.Ternary;
import lox.Expr.Unary;
import lox.Stmt.Expression;
import lox.Stmt.Print;

class Interpreter implements Expr.Visitor<Object>,
        Stmt.Visitor<Void> {

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    @Override
    public Object visitBinaryExpr(Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch(expr.operator.type) {
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                if ((double)right == 0) throw new RuntimeError(expr.operator, "Division by 0.");
                return (double)left / (double)right;
            case PLUS:
                if (left instanceof Double) {
                    if (right instanceof Double)
                        return (double)left + (double)right;
                    if (right instanceof String)
                        return stringify(left) + (String)right;
                }
                if (left instanceof String) {
                    if (right instanceof Double)
                        return (String)left + stringify(right);
                    if (right instanceof String)
                        return (String)left + (String)right;
                }

                throw new RuntimeError(expr.operator,
                        "Operands must be two numbers or two strings.");
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case EQUAL_EQUAL: return isEqual(left, right);
            case BANG_EQUAL: return !isEqual(left, right);
            default: break;
        }

        return null;
    }

    @Override
    public Object visitGroupingExpr(Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Unary expr) {
        Object right = evaluate(expr.right);

        switch(expr.operator.type) {
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double)right;
            case BANG:
                return !isTruthy(right);

            default: break;
        }

        // Unreachable
        return null;
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;

        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private void checkNumberOperand(Token operator, Object object) {
        if (object instanceof Double) return;

        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private boolean isEqual(Object a, Object b) {
        return Objects.equals(a, b);
    }

    private String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0"))
                text = text.substring(0, text.length() - 2);

            return text;
        }

        return object.toString();
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }

    private Object evaluate(Expr expression) {
        return expression.accept(this);
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    @Override
    public Void visitExpressionStmt(Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }


    @Override
    public Object visitTernaryExpr(Ternary expr) {
        Object condition = evaluate(expr.condition);
        checkBooleanOperand(expr.operator, condition);

        return (boolean)condition ? evaluate(expr.left) : evaluate(expr.right);
    }

    private void checkBooleanOperand(Token operator, Object condition) {
        if (condition instanceof Boolean) return;

        throw new RuntimeError(operator, "Condition must be a boolean.");
    }

}
