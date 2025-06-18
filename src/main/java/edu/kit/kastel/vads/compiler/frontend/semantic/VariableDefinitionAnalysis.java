package edu.kit.kastel.vads.compiler.frontend.semantic;

import edu.kit.kastel.vads.compiler.frontend.semantic.hir.*;

import java.util.*;
import java.util.stream.Collectors;

/*
 * Checks that variables are:
 *   - Declared before they are defined/used
 *   - Defined before they are used
 * */
class VariableDefinitionAnalysis implements TypedResultVisitor<VariableDefinitionAnalysis.VariableDefinitionContext, List<Symbol>> {

    @Override
    public List<Symbol> visit(TypedAssignment assignment, VariableDefinitionContext context) {
        assignment.initializer().accept(this, context);

        if (!context.isDeclared(assignment.lValue().asVariable().symbol())) {
            throw new SemanticException("Can not assign variable that is not declared: " + assignment.lValue().asVariable().symbol());
        }

        if (!context.isDefined(assignment.lValue().asVariable().symbol())) {
            context.defineSymbol(assignment.lValue().asVariable().symbol());
            //assignment.lValue().asVariable().symbol().markAsDefined(assignment);
            return List.of(assignment.lValue().asVariable().symbol());
        }

        return List.of();
    }

    @Override
    public List<Symbol> visit(TypedBinaryOperation operation, VariableDefinitionContext context) {
        operation.lhsExpression().accept(this, context);
        operation.rhsExpression().accept(this, context);

        return List.of();
    }

    @Override
    public List<Symbol> visit(TypedBlock block, VariableDefinitionContext context) {
        if (block.declaredScope().isPresent()) {
            context.pushScope();
        }

        HashSet<Symbol> definedSymbols = new HashSet<>();
        for (TypedStatement statement : block.statements()) {
            List<Symbol> definedByStatement = statement.accept(this, context);
            for (Symbol symbol : definedByStatement) {
                context.defineSymbol(symbol);
            }
            definedSymbols.addAll(definedByStatement);
        }

        if (block.declaredScope().isPresent()) {
            context.popScope();

            return definedSymbols
                    .stream()
                    .filter(context::isDeclared)
                    .toList();
        }

        return definedSymbols
                .stream()
                .toList();
    }

    @Override
    public List<Symbol> visit(TypedBoolLiteral literal, VariableDefinitionContext context) {
        return List.of();
    }

    @Override
    public List<Symbol> visit(TypedBreak breakStatement, VariableDefinitionContext context) {
        return context.getAllDeclaredSymbols();
    }

    @Override
    public List<Symbol> visit(TypedConditionalExpression conditionalExpression, VariableDefinitionContext context) {
        conditionalExpression.conditionExpression().accept(this, context);
        conditionalExpression.thenExpression().accept(this, context);
        conditionalExpression.elseExpression().accept(this, context);

        return List.of();
    }

    @Override
    public List<Symbol> visit(TypedContinue continueStatement, VariableDefinitionContext context) {
        return context.getAllDeclaredSymbols();
    }

    @Override
    public List<Symbol> visit(TypedDeclaration declaration, VariableDefinitionContext context) {
        // All declarations are valid because this analysis runs after elaboration
        if (declaration.initializer().isPresent()) {
            declaration.initializer().get().accept(this, context);
        }

        context.declareSymbol(declaration.symbol());

        if (declaration.initializer().isPresent()) {
            context.defineSymbol(declaration.symbol());
            //declaration.symbol().markAsDefined(declaration);
            return List.of(declaration.symbol());
        }

        return List.of();
    }

    @Override
    public List<Symbol> visit(TypedFile file, VariableDefinitionContext context) {
        context.pushScope();

        for (TypedFunction f : file.functions()) {
            f.accept(this, context);
        }

        context.popScope();

        return List.of();
    }

    @Override
    public List<Symbol> visit(TypedFunction function, VariableDefinitionContext context) {
        context.pushScope();

        function.body().accept(this, context);

        context.popScope();

        return List.of();
    }

    @Override
    public List<Symbol> visit(TypedFunctionCall functionCall, VariableDefinitionContext variableDefinitionContext) {
        return List.of();
    }

    @Override
    public List<Symbol> visit(TypedArgument argument, VariableDefinitionContext variableDefinitionContext) {
        return List.of();
    }

    @Override
    public List<Symbol> visit(TypedIf ifStatement, VariableDefinitionContext context) {
        ifStatement.conditionExpression().accept(this, context);

        List<Symbol> definedByThenBranch = ifStatement.thenStatement().accept(this, context);

        if (ifStatement.elseStatement().isPresent()) {
            List<Symbol> definedByElseBranch = ifStatement.elseStatement().get().accept(this, context);

            List<Symbol> symbolDefinitionIntersection = definedByThenBranch
                    .stream()
                    .filter(definedByElseBranch::contains)
                    .toList();

            for (Symbol symbol : symbolDefinitionIntersection) {
                context.defineSymbol(symbol);
            }

            return symbolDefinitionIntersection;
        }

        return List.of();
    }

    @Override
    public List<Symbol> visit(TypedIntLiteral literal, VariableDefinitionContext context) {
        return List.of();
    }

    @Override
    public List<Symbol> visit(TypedLoop loop, VariableDefinitionContext context) {
        loop.conditionExpression().accept(this, context);

        List<Symbol> definedByBody = loop.body().accept(this, context);


        if (loop.postIterationStatement().isPresent()) {
            for (Symbol symbol : definedByBody) {
                context.defineSymbol(symbol);
            }

            loop.postIterationStatement().get().accept(this, context);
        }

        return List.of();
    }

    @Override
    public List<Symbol> visit(TypedReturn returnStatement, VariableDefinitionContext context) {
        returnStatement.returnExpression().accept(this, context);

        return context.getAllDeclaredSymbols();
    }

    @Override
    public List<Symbol> visit(TypedUnaryOperation operation, VariableDefinitionContext context) {
        operation.expression().accept(this, context);

        return List.of();
    }

    @Override
    public List<Symbol> visit(TypedVariable variable, VariableDefinitionContext context) {
        if (!context.isDeclared(variable.symbol())) {
            throw new SemanticException("The variable '" + variable.symbol() + "' is used before it is declared.");
        }

        if (!context.isDefined(variable.symbol())) {
            throw new SemanticException("The variable '" + variable.symbol() + "' is used before it is defined.");
        }

        return List.of();
    }

    static class VariableDefinitionContext {

        private final Stack<VariableDeclarationScope> scopeStack = new Stack<>();

        public void pushScope() {
            if (scopeStack.isEmpty()) {
                scopeStack.push(new VariableDeclarationScope());
            } else {
                scopeStack.push(new VariableDeclarationScope(scopeStack.peek()));
            }
        }

        private void popScope() {
            scopeStack.pop();
        }

        public void declareSymbol(Symbol symbol) {
            scopeStack.peek().declareSymbol(symbol);
        }

        public boolean isDeclared(Symbol symbol) {
            return scopeStack.peek().isDeclared(symbol);
        }

        public List<Symbol> getAllDeclaredSymbols() {
            return scopeStack.peek().getAllDeclaredSymbols();
        }

        public void defineSymbol(Symbol symbol) {
            scopeStack.peek().defineSymbol(symbol);
        }

        public boolean isDefined(Symbol symbol) {
            return scopeStack.peek().isDefined(symbol);
        }
    }

    static class VariableDeclarationScope {
        private final Optional<VariableDeclarationScope> parentScope;
        private final HashSet<Symbol> declaredSymbols;
        private final HashSet<Symbol> definedSymbols;

        public VariableDeclarationScope(VariableDeclarationScope parentScope) {
            this.parentScope = Optional.of(parentScope);
            this.declaredSymbols = new HashSet<>();
            this.definedSymbols = new HashSet<>();
        }

        public VariableDeclarationScope() {
            this.parentScope = Optional.empty();
            this.declaredSymbols = new HashSet<>();
            this.definedSymbols = new HashSet<>();
        }

        public void declareSymbol(Symbol symbol) {
            declaredSymbols.add(symbol);
        }

        public boolean isDeclared(Symbol symbol) {
            return declaredSymbols.contains(symbol)
                   || (parentScope.isPresent() && parentScope.get().isDeclared(symbol));
        }

        public List<Symbol> getAllDeclaredSymbols() {
            List<Symbol> allDeclaredSymbols = new ArrayList<>();
            allDeclaredSymbols = parentScope
                    .map(VariableDeclarationScope::getAllDeclaredSymbols)
                    .orElseGet(ArrayList::new);

            allDeclaredSymbols.addAll(declaredSymbols);
            return allDeclaredSymbols;
        }

        public void defineSymbol(Symbol symbol) {
            definedSymbols.add(symbol);
        }

        public boolean isDefined(Symbol symbol) {
            return definedSymbols.contains(symbol)
                    || (parentScope.isPresent() && parentScope.get().isDefined(symbol));
        }
    }
}
