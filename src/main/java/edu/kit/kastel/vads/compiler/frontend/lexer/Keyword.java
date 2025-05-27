package edu.kit.kastel.vads.compiler.frontend.lexer;

import edu.kit.kastel.vads.compiler.Span;

import java.util.Arrays;

public record Keyword(KeywordType type, Span span) implements Token {
    @Override
    public boolean isKeyword(KeywordType keywordType) {
        return type() == keywordType;
    }


    public boolean isOneOfKeywords(KeywordType... keywordTypes) {
        return Arrays.stream(keywordTypes).anyMatch(this::isKeyword);
    }

    @Override
    public boolean isType() {
        return type() == KeywordType.BOOL || type() == KeywordType.INT;
    }

    @Override
    public String asString() {
        return type().keyword();
    }
}
