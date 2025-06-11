package edu.kit.kastel.vads.compiler.frontend.parser;

import edu.kit.kastel.vads.compiler.frontend.lexer.Identifier;
import edu.kit.kastel.vads.compiler.frontend.lexer.Keyword;
import edu.kit.kastel.vads.compiler.frontend.lexer.KeywordType;
import edu.kit.kastel.vads.compiler.frontend.lexer.NumberLiteral;
import edu.kit.kastel.vads.compiler.frontend.lexer.Operator;
import edu.kit.kastel.vads.compiler.frontend.lexer.Operator.OperatorType;
import edu.kit.kastel.vads.compiler.frontend.lexer.Separator;
import edu.kit.kastel.vads.compiler.frontend.lexer.Separator.SeparatorType;
import edu.kit.kastel.vads.compiler.Span;
import edu.kit.kastel.vads.compiler.frontend.lexer.Token;
import edu.kit.kastel.vads.compiler.frontend.parser.ast.*;
import edu.kit.kastel.vads.compiler.frontend.parser.symbol.Name;
import edu.kit.kastel.vads.compiler.frontend.parser.type.BasicType;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final TokenSource tokenSource;

    public Parser(TokenSource tokenSource) {
        this.tokenSource = tokenSource;
    }

    public ProgramTree parseProgram() {
        ProgramTree programTree = new ProgramTree(List.of(parseFunction()));
        if (this.tokenSource.hasMore()) {
            throw new ParseException("expected end of input but got " + this.tokenSource.peek());
        }
        return programTree;
    }

    private FunctionTree parseFunction() {
        Keyword returnType = this.tokenSource.expectKeyword(KeywordType.INT);
        Identifier identifier = this.tokenSource.expectIdentifier();

        if (!identifier.value().equals("main")) { // Pass test 'Misspelled main function'
            throw new ParseException("expected identifier 'main' for main method but got " + identifier.value());
        }

        this.tokenSource.expectSeparator(SeparatorType.PAREN_OPEN);
        this.tokenSource.expectSeparator(SeparatorType.PAREN_CLOSE);
        BlockTree body = parseBlock();
        return new FunctionTree(
            new TypeTree(BasicType.INT, returnType.span()),
            name(identifier),
            body
        );
    }

    private BlockTree parseBlock() {
        Separator bodyOpen = this.tokenSource.expectSeparator(SeparatorType.BRACE_OPEN);
        List<StatementTree> statements = new ArrayList<>();
        while (!(this.tokenSource.peek() instanceof Separator sep && sep.type() == SeparatorType.BRACE_CLOSE)) {
            statements.add(parseStatement());
        }
        Separator bodyClose = this.tokenSource.expectSeparator(SeparatorType.BRACE_CLOSE);
        return new BlockTree(statements, bodyOpen.span().merge(bodyClose.span()));
    }

    private StatementTree parseStatement() {
        StatementTree statement;
        if (this.tokenSource.peek().isSeparator(SeparatorType.BRACE_OPEN)) {
            statement = parseBlock();
        } else if (this.tokenSource.peek().isOneOfKeywords(
                KeywordType.IF, KeywordType.WHILE, KeywordType.FOR,
                KeywordType.CONTINUE, KeywordType.BREAK, KeywordType.RETURN)) {
            statement = parseControl();
        } else {
            statement = parseSimple();
            this.tokenSource.expectSeparator(SeparatorType.SEMICOLON);
        }
        return statement;
    }

    private ControlTree parseControl() {
        Token next = tokenSource.peek();
        if (next.isKeyword(KeywordType.IF)) {
            return parseIf();
        } else if (next.isKeyword(KeywordType.WHILE)) {
            return parseWhile();
        } else if (next.isKeyword(KeywordType.FOR)) {
            return parseFor();
        } else if (next.isKeyword(KeywordType.CONTINUE)) {
            return parseContinue();
        } else if (next.isKeyword(KeywordType.BREAK)) {
            return parseBreak();
        } else if (next.isKeyword(KeywordType.RETURN)) {
            return parseReturn();
        }

        throw new ParseException("expected to parse control statement but " + this.tokenSource.peek() + "is not a valid begin of a control statement");
    }

    private ForTree parseFor() {
        Keyword forKeyword = this.tokenSource.expectKeyword(KeywordType.FOR);
        this.tokenSource.expectSeparator(SeparatorType.PAREN_OPEN);

        SimpleStatementTree initializationStatementTree = null;
        // Check if initialization statement is present
        if (!this.tokenSource.peek().isSeparator(SeparatorType.SEMICOLON)) {
            initializationStatementTree = parseSimple();
        }
        this.tokenSource.expectSeparator(SeparatorType.SEMICOLON);

        ExpressionTree conditionExpressionTree = parseExpression();
        this.tokenSource.expectSeparator(SeparatorType.SEMICOLON);

        SimpleStatementTree postIterationStatementTree = null;
        // Check if post iteration statement is present
        if (!this.tokenSource.peek().isSeparator(SeparatorType.PAREN_CLOSE)) {
            postIterationStatementTree = parseSimple();
        }
        this.tokenSource.expectSeparator(SeparatorType.PAREN_CLOSE);

        StatementTree body = parseStatement();
        return new ForTree(
                initializationStatementTree,
                conditionExpressionTree,
                postIterationStatementTree,
                body,
                forKeyword.span().start());
    }

    private IfTree parseIf() {
        Keyword ifKeyword = this.tokenSource.expectKeyword(KeywordType.IF);
        this.tokenSource.expectSeparator(SeparatorType.PAREN_OPEN);

        ExpressionTree conditionExpressionTree = parseExpression();
        this.tokenSource.expectSeparator(SeparatorType.PAREN_CLOSE);

        StatementTree bodyTree = parseStatement();

        ElseTree elseTree = null;
        if (this.tokenSource.peek().isKeyword(KeywordType.ELSE)) {
            elseTree = parseElse();
        }

        return new IfTree(
                conditionExpressionTree,
                bodyTree,
                elseTree,
                ifKeyword.span().start());
    }

    private ElseTree parseElse() {
        Keyword elseKeyword = this.tokenSource.expectKeyword(KeywordType.ELSE);

        StatementTree bodyTree = parseStatement();

        return new ElseTree(
                bodyTree,
                elseKeyword.span().start());
    }

    private WhileTree parseWhile() {
        Keyword whileKeyword = this.tokenSource.expectKeyword(KeywordType.WHILE);
        this.tokenSource.expectSeparator(SeparatorType.PAREN_OPEN);

        ExpressionTree conditionExpressionTree = parseExpression();

        this.tokenSource.expectSeparator(SeparatorType.PAREN_CLOSE);

        StatementTree bodyTree = parseStatement();

        return new WhileTree(
                conditionExpressionTree,
                bodyTree,
                whileKeyword.span().start());
    }

    private ContinueTree parseContinue() {
        Keyword continueKeyword = this.tokenSource.expectKeyword(KeywordType.CONTINUE);
        this.tokenSource.expectSeparator(SeparatorType.SEMICOLON);

        return new ContinueTree(continueKeyword.span());
    }

    private BreakTree parseBreak() {
        Keyword breakKeyword = this.tokenSource.expectKeyword(KeywordType.BREAK);
        this.tokenSource.expectSeparator(SeparatorType.SEMICOLON);

        return new BreakTree(breakKeyword.span());
    }

    private ReturnTree parseReturn() {
        Keyword ret = this.tokenSource.expectKeyword(KeywordType.RETURN);
        ExpressionTree expression = parseExpression();
        this.tokenSource.expectSeparator(SeparatorType.SEMICOLON);

        return new ReturnTree(expression, ret.span().start());
    }


    private SimpleStatementTree parseSimple() {
        // If next token is a type this must be a declaration.
        if (this.tokenSource.peek().isType()) {
            return parseDeclaration();
        }
        // Else this must be an assignment
        return parseAssignment();
    }

    private SimpleStatementTree parseDeclaration() {
        Keyword type = this.tokenSource.expectKeywords(KeywordType.INT, KeywordType.BOOL);
        Identifier ident = this.tokenSource.expectIdentifier();
        ExpressionTree expr = null;
        if (this.tokenSource.peek().isOperator(OperatorType.ASSIGN)) {
            this.tokenSource.expectOperator(OperatorType.ASSIGN);
            expr = parseExpression();
        }

        TypeTree typeTree = switch (type.type()) {
            case KeywordType.INT -> new TypeTree(BasicType.INT, type.span());
            case KeywordType.BOOL -> new TypeTree(BasicType.BOOL, type.span());
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
        return new DeclarationTree(typeTree, name(ident), expr);
    }

    private SimpleStatementTree parseAssignment() {
        LValueTree lValue = parseLValue();
        Operator assignmentOperator = parseAssignmentOperator();
        ExpressionTree expression = parseExpression();
        return new AssignmentTree(lValue, assignmentOperator, expression);
    }



    private Operator parseAssignmentOperator() {
        if (this.tokenSource.peek() instanceof Operator op) {
            return switch (op.type()) {
                case ASSIGN, ASSIGN_DIV, ASSIGN_MINUS, ASSIGN_MOD, ASSIGN_MUL, ASSIGN_PLUS,
                     ASSIGN_BITWISE_OR, ASSIGN_BITWISE_AND, ASSIGN_BITWISE_XOR,
                     ASSIGN_LEFT_SHIFT, ASSIGN_RIGHT_SHIFT -> {
                    this.tokenSource.consume();
                    yield op;
                }
                default -> throw new ParseException("expected assignment but got " + op.type());
            };
        }
        throw new ParseException("expected assignment but got " + this.tokenSource.peek());
    }

    private LValueTree parseLValue() {
        if (this.tokenSource.peek().isSeparator(SeparatorType.PAREN_OPEN)) {
            this.tokenSource.expectSeparator(SeparatorType.PAREN_OPEN);
            LValueTree inner = parseLValue();
            this.tokenSource.expectSeparator(SeparatorType.PAREN_CLOSE);
            return inner;
        }
        Identifier identifier = this.tokenSource.expectIdentifier();
        return new LValueIdentTree(name(identifier));
    }

    private ExpressionTree parseExpression() {
        return parseExpression(0);
    }

    private ExpressionTree parseExpression(int minPrecedenceLevel) {
        ExpressionTree lhs = parseFactor();

        while (true) {
            Token next = this.tokenSource.peek();
            if (!(next instanceof Operator(OperatorType type, _)) || type == OperatorType.TERNARY)
                break;

            // If next operator has lower precedence level (is less tight binding) then don't continue
            if (type.precedenceLevel() < minPrecedenceLevel)
                break;

            if (type == OperatorType.QUESTION) {
                this.tokenSource.consume();
                ExpressionTree thenExpression = parseExpression(0);

                tokenSource.expectOperator(OperatorType.TERNARY);
                ExpressionTree elseExpression = parseExpression(0);

                lhs = new ConditionalExpressionTree(
                        lhs,
                        thenExpression,
                        elseExpression,
                        lhs.span().merge(elseExpression.span()));
            } else {
                // TODO: Only allow binary operators
                this.tokenSource.consume();

                int nextMinPrecedenceLevel = type.associativity() == Operator.OperatorAssociativity.LEFT
                        ? type.precedenceLevel() + 1
                        : type.precedenceLevel();

                ExpressionTree rhs = parseExpression(nextMinPrecedenceLevel);
                lhs = new BinaryOperationTree(lhs, rhs, type);
            }
        }

        return lhs;
    }

    private ExpressionTree parseFactor() {
        return switch (this.tokenSource.peek()) {
            case Separator(var type, _) when type == SeparatorType.PAREN_OPEN -> {
                this.tokenSource.consume();
                ExpressionTree expression = parseExpression();
                this.tokenSource.expectSeparator(SeparatorType.PAREN_CLOSE);
                yield expression;
            }
            case Operator(var type, _) when type == OperatorType.MINUS -> {
                Span span = this.tokenSource.consume().span();
                yield new NegateTree(parseFactor(), type, span);
            }
            case Operator(var type, _) when type == OperatorType.BITWISE_NOT -> {
                Span span = this.tokenSource.consume().span();
                yield new NegateTree(parseFactor(), type, span);
            }
            case Operator(var type, _) when type == OperatorType.LOGICAL_NOT -> {
                Span span = this.tokenSource.consume().span();
                yield new NegateTree(parseFactor(), type, span);
            }
            case Identifier ident -> {
                this.tokenSource.consume();
                yield new IdentExpressionTree(name(ident));
            }
            case NumberLiteral(String value, int base, Span span) -> {
                this.tokenSource.consume();
                yield new IntLiteralTree(value, base, span);
            }
            case Keyword(KeywordType type, Span span) when type == KeywordType.TRUE -> {
                this.tokenSource.consume();
                yield new BoolLiteralTree(true, span);
            }
            case Keyword(KeywordType type, Span span) when type == KeywordType.FALSE -> {
                this.tokenSource.consume();
                yield new BoolLiteralTree(false, span);
            }
            case Token t -> throw new ParseException("invalid factor " + t);
        };
    }

    private static NameTree name(Identifier ident) {
        return new NameTree(Name.forIdentifier(ident), ident.span());
    }
}
