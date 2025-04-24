package com.github.sgov.server;

/**
 * Represents a validation rule.
 *
 * @param name    Name of the rule, typically its filename
 * @param content Content of the rule, usually as a Turtle string
 */
public record Rule(String name, String content) {

    Rule withContent(String content) {
        return new Rule(name, content);
    }
}
