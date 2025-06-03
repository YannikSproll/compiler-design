package edu.kit.kastel.vads.compiler.frontend.semantic;

import edu.kit.kastel.vads.compiler.frontend.lexer.Operator;
import edu.kit.kastel.vads.compiler.frontend.parser.ast.*;
import edu.kit.kastel.vads.compiler.frontend.parser.symbol.Name;
import edu.kit.kastel.vads.compiler.frontend.parser.type.BasicType;
import edu.kit.kastel.vads.compiler.frontend.parser.type.Type;
import edu.kit.kastel.vads.compiler.frontend.parser.visitor.NoOpPreVisitor;
import edu.kit.kastel.vads.compiler.frontend.parser.visitor.NoOpVisitor;
import edu.kit.kastel.vads.compiler.frontend.parser.visitor.Unit;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Stack;

/*
class TypeCheckingAnalysis implements
        NoOpVisitor<TypeCheckingAnalysis.ScopeStack>,
        NoOpPreVisitor<TypeCheckingAnalysis.ScopeStack> {

    static class Scope {
        private final HashMap<Tree, Type> typesOfTrees;
        private final HashMap<Name, Type> typesOfVariables;
        private final @Nullable Scope parent;

        public Scope() {
            this.typesOfTrees = new HashMap<>();
            this.typesOfVariables = new HashMap<>();
            this.parent = null;
        }

        public Scope(Scope parent) {
            this.typesOfTrees = new HashMap<>();
            this.typesOfVariables = new HashMap<>();
            this.parent = parent;
        }

        public void putType(Tree tree, Type type) {
            typesOfTrees.put(tree, type);
        }

        public void putType(Name name, Type type) {typesOfVariables.put(name, type);}

        public Type typeOf(Tree tree) {
            boolean containsType = typesOfTrees.containsKey(tree);
            if (!containsType) {
                if (parent == null) {
                    throw new IllegalStateException("Type not found during type checking analysis of " + tree);
                }
                return parent.typeOf(tree);
            }
            return typesOfTrees.get(tree);
        }

        public Type typeOf(Name name) {
            boolean containsType = typesOfVariables.containsKey(name);
            if (!containsType) {
                if (parent == null) {
                    throw new IllegalStateException("Type not found during type checking analysis of " + name);
                }
                return parent.typeOf(name);
            }
            return typesOfVariables.get(name);
        }
    }

    static class ScopeStack {
        private final Stack<Scope> scopes;

        public ScopeStack() {
            this.scopes = new Stack<>();
        }

        public Scope getCurrentScope() {
            return scopes.peek();
        }

        public void pushScope() {
            if (!scopes.isEmpty()) {
                scopes.push(new Scope(scopes.peek()));
            } else {
                scopes.push(new Scope());
            }
        }

        public void popScope() {
            scopes.pop();
        }
    }

    @Override
    public Unit visit(AssignmentTree assignmentTree, ScopeStack data) {
        return Unit.INSTANCE;
    }

    @Override
    public Unit visit(BinaryOperationTree binaryOperationTree, ScopeStack data) {
        Type lhsExpression = data.getCurrentScope().typeOf(binaryOperationTree.lhs());
        Type rhsExpression = data.getCurrentScope().typeOf(binaryOperationTree.rhs());

        Type binaryOperationTreeType = switch (binaryOperationTree.operatorType()) {
            case MUL, DIV, MOD, MINUS, PLUS, LEFT_SHIFT, RIGHT_SHIFT, BITWISE_NOT, BITWISE_XOR, BITWISE_OR, BITWISE_AND -> {
                if (lhsExpression != BasicType.INT || rhsExpression != BasicType.INT) {
                    throw new SemanticException("Invalid expression for operator: " + binaryOperationTree.operatorType());
                }
                yield lhsExpression;
            }
            case LOGICAL_AND, LOGICAL_OR -> {
                if (lhsExpression != BasicType.BOOL || rhsExpression != BasicType.BOOL) {
                    throw new SemanticException("Invalid expression for operator: " + binaryOperationTree.operatorType());
                }
                yield lhsExpression;
            }
            case EQUAL_TO, UNEQUAL_TO -> {
                if (lhsExpression != rhsExpression) {
                    throw new SemanticException("Invalid expression for operator: " + binaryOperationTree.operatorType());
                }
                yield BasicType.BOOL;
            }
            case LESS_THAN, GREATER_THAN, LESS_OR_EQUAL, GREATER_OR_EQUAL -> {
                if (lhsExpression != BasicType.INT || rhsExpression != BasicType.INT) {
                    throw new SemanticException("Invalid expression for operator: " + binaryOperationTree.operatorType());
                }
                yield BasicType.BOOL;
            }
            case ASSIGN_MINUS, ASSIGN_PLUS, ASSIGN_MUL, ASSIGN_DIV, ASSIGN_MOD, ASSIGN_BITWISE_AND, ASSIGN_BITWISE_OR, ASSIGN_BITWISE_XOR, ASSIGN_LEFT_SHIFT, ASSIGN_RIGHT_SHIFT -> {
                if (lhsExpression != BasicType.INT || rhsExpression != BasicType.INT) {
                    throw new SemanticException("Invalid expression for operator: " + binaryOperationTree.operatorType());
                }
                yield null; // This can only happen in assignments; not in real expressions
            }
            default -> throw new IllegalArgumentException("The operator " + binaryOperationTree.operatorType() + " is not supported for binary operations");

        };

        if (binaryOperationTreeType != null) {
            data.getCurrentScope().putType(binaryOperationTree, binaryOperationTreeType);
        }

        return Unit.INSTANCE;
    }

    private List<Type> supportedOperatorTypes(Operator.OperatorType operatorType) {
        return switch (operatorType) {
            case MUL, DIV, MOD, MINUS, PLUS -> List.of(BasicType.INT);
            case BITWISE_AND, BITWISE_OR, BITWISE_XOR, BITWISE_NOT -> List.of(BasicType.INT);
            case LEFT_SHIFT, RIGHT_SHIFT -> List.of(BasicType.INT);
            case EQUAL_TO, UNEQUAL_TO -> List.of(BasicType.INT, BasicType.BOOL);
            default -> List.of();
        };
    }

    @Override
    public Unit preVisit(BlockTree blockTree, ScopeStack data) {
        data.pushScope();
        return Unit.INSTANCE;
    }

    @Override
    public Unit visit(BlockTree blockTree, ScopeStack data) {
        data.popScope();
        return Unit.INSTANCE;
    }

    @Override
    public Unit visit(DeclarationTree declarationTree, ScopeStack data) {
        data.getCurrentScope().putType(declarationTree.name().name(), declarationTree.type().type());

        if (declarationTree.initializer() != null) {
            Type initializerType = data.getCurrentScope().typeOf(declarationTree.initializer());
            Type declaredType = data.getCurrentScope().typeOf(declarationTree.type());

            if (initializerType != declaredType) {
                throw new IllegalStateException("Type mismatch in initializer of type " + initializerType + " and " + declaredType);
            }
        }
        return Unit.INSTANCE;
    }

    @Override
    public Unit preVisit(FunctionTree functionTree, ScopeStack data) {
        data.pushScope();
        return Unit.INSTANCE;
    }

    @Override
    public Unit visit(FunctionTree functionTree, ScopeStack data) {
        return Unit.INSTANCE;
    }

    @Override
    public Unit visit(IdentExpressionTree identExpressionTree, ScopeStack data) {
        data.getCurrentScope().putType(identExpressionTree, data.getCurrentScope().typeOf(identExpressionTree.name().name()));
        return Unit.INSTANCE;
    }

    @Override
    public Unit visit(IntLiteralTree intLiteralTree, ScopeStack data) {
        data.getCurrentScope().putType(intLiteralTree, BasicType.INT);
        return Unit.INSTANCE;
    }

    @Override
    public Unit visit(LValueIdentTree lValueIdentTree, ScopeStack data) {
        return Unit.INSTANCE;
    }

    @Override
    public Unit visit(NameTree nameTree, ScopeStack data) { return Unit.INSTANCE; }

    @Override
    public Unit visit(NegateTree negateTree, ScopeStack data) {
        data.getCurrentScope().putType(negateTree, data.getCurrentScope().typeOf(negateTree.expression()));
        return Unit.INSTANCE;
    }

    @Override
    public Unit visit(ProgramTree programTree, ScopeStack data) {
        return Unit.INSTANCE;
    }

    @Override
    public Unit visit(ReturnTree returnTree, ScopeStack data) {
        return Unit.INSTANCE;
    }

    @Override
    public Unit visit(TypeTree typeTree, ScopeStack data) {
        data.getCurrentScope().putType(typeTree, typeTree.type());
        return Unit.INSTANCE;
    }

    @Override
    public Unit visit(ConditionalExpressionTree conditionalExpressionTree, ScopeStack data) {
        if (data.getCurrentScope().typeOf(conditionalExpressionTree.conditionTree()) != BasicType.BOOL) {
            throw new SemanticException("Condition of conditional expression must be of type BOOL.");
        }

        Type thenType = data.getCurrentScope().typeOf(conditionalExpressionTree.thenTree());
        Type elseType = data.getCurrentScope().typeOf(conditionalExpressionTree.elseTree());

        if (thenType != elseType) {
            throw new SemanticException("Then expression and else tree must have the same type.");
        }

        data.getCurrentScope().putType(conditionalExpressionTree, thenType);

        return Unit.INSTANCE; }

    @Override
    public Unit visit(BoolLiteralTree boolLiteralTree, ScopeStack data) {
        data.getCurrentScope().putType(boolLiteralTree, BasicType.BOOL);
        return Unit.INSTANCE;
    }

    @Override
    public Unit visit(BreakTree breakTree, ScopeStack data) {
        return Unit.INSTANCE;
    }

    @Override
    public Unit visit(ContinueTree continueTree, ScopeStack data) {
        return Unit.INSTANCE;
    }

    @Override
    public Unit visit(ForTree forTree, ScopeStack data) {
        return Unit.INSTANCE;
    }

    @Override
    public Unit visit(IfTree ifTree, ScopeStack data) {
        return Unit.INSTANCE;
    }

    @Override
    public Unit visit(ElseTree elseTree, ScopeStack data) {
        return Unit.INSTANCE;
    }

    @Override
    public Unit visit(WhileTree whileTree, ScopeStack data) {
        return Unit.INSTANCE;
    }
}
*/