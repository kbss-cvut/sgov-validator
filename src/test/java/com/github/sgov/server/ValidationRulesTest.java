package com.github.sgov.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValidationRulesTest {

    @Test
    void rulesReturnsAllRules() {
        assertEquals(14, ValidationRules.glossaryRules("en").size());
        assertEquals(7, ValidationRules.modelRules("cs").size());
        assertEquals(3, ValidationRules.vocabularyRules("en").size());
    }
}