package com.github.sgov.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides access to the built-in ruleset.
 */
public final class ValidationRules {

    private static final String RULES_DIR = "/rules";
    private static final String RULE_TEMPLATES_DIR = "/rule-templates";
    private static final String GLOSSARY_RULES_PREFIX = "g";
    private static final String MODEL_RULES_PREFIX = "m";
    private static final String VOCABULARY_RULES_PREFIX = "s";

    private static final List<Rule> GLOSSARY_RULES = loadResources(RULES_DIR, GLOSSARY_RULES_PREFIX);
    private static final List<Rule> GLOSSARY_RULE_TEMPLATES = loadResources(RULE_TEMPLATES_DIR, GLOSSARY_RULES_PREFIX);
    private static final List<Rule> MODEL_RULES = loadResources(RULES_DIR, MODEL_RULES_PREFIX);
    private static final List<Rule> MODEL_RULE_TEMPLATES = loadResources(RULE_TEMPLATES_DIR, MODEL_RULES_PREFIX);
    private static final List<Rule> VOCABULARY_RULES = loadResources(RULES_DIR, VOCABULARY_RULES_PREFIX);
    private static final List<Rule> VOCABULARY_RULE_TEMPLATES =
            loadResources(RULE_TEMPLATES_DIR, VOCABULARY_RULES_PREFIX);

    private ValidationRules() {
        throw new AssertionError();
    }

    private static List<Rule> loadResources(String dir, String prefix) {
        int i = 1;
        final List<Rule> result = new ArrayList<>();
        while (i < Short.MAX_VALUE) {
            final String fileName = prefix + i + ".ttl";
            final InputStream stream = ValidationRules.class.getResourceAsStream(dir + "/" + fileName);
            if (stream == null) {
                break;
            }
            result.add(loadFile(fileName, stream));
            i++;
        }
        return result;
    }

    private static Rule loadFile(String fileName, InputStream stream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            return new Rule(fileName, reader.lines().collect(Collectors.joining("\n")));
        } catch (IOException e) {
            throw new RuntimeException("Unable to load file " + fileName, e);
        }
    }

    /**
     * Gets glossary validation rules for the specified language.
     *
     * @param language Language of data to validate
     * @return List of rules
     */
    public static List<Rule> glossaryRules(String language) {
        Objects.requireNonNull(language);
        return rulesAndTemplates(GLOSSARY_RULES, GLOSSARY_RULE_TEMPLATES, language);
    }

    private static List<Rule> rulesAndTemplates(List<Rule> rules, List<Rule> templates, String language) {
        return Stream.concat(rules.stream(),
                             templates.stream().map(r -> r.withContent(r.content().replace("${lang}", language))))
                     .collect(Collectors.toList());
    }

    /**
     * Gets model validation rules for the specified language.
     *
     * @param language Language of data to validate
     * @return List of rules
     */
    public static List<Rule> modelRules(String language) {
        Objects.requireNonNull(language);
        return rulesAndTemplates(MODEL_RULES, MODEL_RULE_TEMPLATES, language);
    }

    /**
     * Gets vocabulary validation rules for the specified language.
     *
     * @param language Language of data to validate
     * @return List of rules
     */
    public static List<Rule> vocabularyRules(String language) {
        Objects.requireNonNull(language);
        return rulesAndTemplates(VOCABULARY_RULES, VOCABULARY_RULE_TEMPLATES, language);
    }
}
