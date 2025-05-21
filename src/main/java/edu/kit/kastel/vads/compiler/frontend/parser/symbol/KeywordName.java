package edu.kit.kastel.vads.compiler.frontend.parser.symbol;

import edu.kit.kastel.vads.compiler.frontend.lexer.KeywordType;

record KeywordName(KeywordType type) implements Name {
    @Override
    public String asString() {
        return type().keyword();
    }
}
