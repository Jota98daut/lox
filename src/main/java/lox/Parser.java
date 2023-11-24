package lox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static lox.TokenType.*;

class Parser {
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();

        while (!isAtEnd())
            statements.add(statement());

        return statements;
    }

    private Expr expression() {
        if (match(COMMA, QUESTION)) {
            Token operator = previous();
            equality();
            throw error(operator, "Expect expression before '" + operator.lexeme + "'.");
        }
        Expr expr = equality();

        if (match(COMMA)) {
            Token operator = previous();
            Expr right = expression();
            expr = new Expr.Binary(expr, operator, right);
        }

        if (match(QUESTION)) {
            if (match(COLON)) {
                Token operator = previous();
                expression();
                throw error(operator, "Expect expression before ','.");
            }
            Token operator = previous();
            Expr right = expression();
            
            if (match(COLON)) {
                Expr right = expression();
                expr = new Expr.Ternary(operator, expr, left, right);
            }
            else throw error(peek(), "Expect ':' after expression.");
        }

        return expr;
    }

    private Stmt statement() {
        if (match(PRINT)) return printStatement();

        return expressionStatement();
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private Expr equality() {
        if (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            comparison();
            throw error(operator, "Expect expression before '" + operator.lexeme + "'.");
        }
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        if (match(LESS, LESS_EQUAL, GREATER, GREATER_EQUAL)) {
            Token operator = previous();
            term();
            throw error(operator, "Expect expression before '" + operator.lexeme + "'.");
        }
        Expr expr = term();

        while (match(LESS, LESS_EQUAL, GREATER, GREATER_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        if (match(PLUS)) {
            Token operator = previous();
            factor();
            throw error(operator, "Expect expression before '" + operator.lexeme + "'.");
        }
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        if (match(SLASH, STAR)) {
            Token operator = previous();
            unary();
            throw error(operator, "Expect expression before '" + operator.lexeme + "'.");
        }
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING)) return new Expr.Literal(previous().literal);

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }

    /**
     * Consume the current token if it matches the given type, throw an error otherwise.
    */
    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;

                default: break;
            }

            advance();
        }
    }

    private boolean match(TokenType ...types) {
        boolean doesMatch = Arrays.asList(types).stream().anyMatch(t -> check(t));
        if (doesMatch) advance();
        return doesMatch;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }
}
