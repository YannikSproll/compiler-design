package edu.kit.kastel.vads.compiler.frontend.parser;

import edu.kit.kastel.vads.compiler.frontend.lexer.Identifier;
import edu.kit.kastel.vads.compiler.frontend.lexer.Keyword;
import edu.kit.kastel.vads.compiler.frontend.lexer.KeywordType;
import edu.kit.kastel.vads.compiler.frontend.lexer.Lexer;
import edu.kit.kastel.vads.compiler.frontend.lexer.Operator;
import edu.kit.kastel.vads.compiler.frontend.lexer.Operator.OperatorType;
import edu.kit.kastel.vads.compiler.frontend.lexer.Separator;
import edu.kit.kastel.vads.compiler.frontend.lexer.Separator.SeparatorType;
import edu.kit.kastel.vads.compiler.frontend.lexer.Token;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TokenSource {
    private final List<Token> tokens;
    private int idx;

    public TokenSource(Lexer lexer) {
        this.tokens = Stream.generate(lexer::nextToken)
            .takeWhile(Optional::isPresent)
            .map(Optional::orElseThrow)
            .toList();
    }

    TokenSource(List<Token> tokens) {
        this.tokens = List.copyOf(tokens);
    }

    public Token peek() {
        expectHasMore();
        return this.tokens.get(this.idx);
    }

    public Keyword expectKeyword(KeywordType type) {
        Token token = peek();
        if (!(token instanceof Keyword kw) || kw.type() != type) {
            throw new ParseException("expected keyword '" + type + "' but got " + token);
        }
        this.idx++;
        return kw;
    }

    public Keyword expectKeywords(KeywordType... types) {
        Token token = peek();
        if (!(token instanceof Keyword kw) || Arrays.stream(types).noneMatch(x -> kw.type() == x)) {
            throw new ParseException("expected one of the keywords '"
                    + Arrays.stream(types).map(KeywordType::toString).collect(Collectors.joining())
                    + "' but got "
                    + token);
        }
        this.idx++;
        return kw;
    }

    public Separator expectSeparator(SeparatorType type) {
        Token token = peek();
        if (!(token instanceof Separator sep) || sep.type() != type) {
            throw new ParseException("expected separator '" + type + "' but got " + token);
        }
        this.idx++;
        return sep;
    }

    public Operator expectOperator(OperatorType type) {
        Token token = peek();
        if (!(token instanceof Operator op) || op.type() != type) {
            throw new ParseException("expected operator '" + type + "' but got " + token);
        }
        this.idx++;
        return op;
    }
    public Identifier expectIdentifier() {
        Token token = peek();
        if (!(token instanceof Identifier ident)) {
            throw new ParseException("expected identifier but got " + token);
        }
        this.idx++;
        return ident;
    }

    public Token consume() {
        Token token = peek();
        this.idx++;
        return token;
    }

    public boolean hasMore() {
        return this.idx < this.tokens.size();
    }

    private void expectHasMore() {
        if (this.idx >= this.tokens.size()) {
            throw new ParseException("reached end of file");
        }
    }
}
