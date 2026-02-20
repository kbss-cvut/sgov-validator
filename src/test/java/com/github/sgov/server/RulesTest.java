package com.github.sgov.server;

import org.apache.jena.ontapi.OntModelFactory;
import org.apache.jena.ontapi.OntSpecification;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.validation.Severity;
import org.apache.jena.util.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Set;
import java.util.stream.Collectors;

public class RulesTest {

    @ParameterizedTest(name = "Rule {0} for {1} (should be {2})")
    @CsvFileSource(resources = "/test-cases.csv", numLinesToSkip = 1)
    public void testShaclRule(String rule, String output, String outcome) throws IOException {
        testModel(loadRuleset(Set.of(getClass().getResource("/rules/" + rule))), output,
                  Outcome.valueOf(outcome));
    }

    private static Model loadRuleset(Set<URL> ruleSet) throws IOException {
        final Model shapesModel = ModelFactory.createDefaultModel();
        for (URL r : ruleSet) {
            shapesModel.read(r.openStream(), null, FileUtils.langTurtle);
        }
        return shapesModel;
    }

    private void testModel(Model shapesModel, String data, Outcome outcome) {
        final Model dataModel = OntModelFactory.createModel(OntSpecification.OWL1_DL_MEM_RDFS_INF);

        OntDocumentManager.getInstance().setProcessImports(false);
        dataModel.read(RulesTest.class.getResourceAsStream("/" + data), "urn:dummy",
                       FileUtils.langTurtle);

        final Validator validator = new Validator();
        final ValidationReport r = validator.validate(dataModel, shapesModel);

        r.getEntries().forEach(result -> System.out.println((MessageFormat
                .format("[{0}] Node {1} failing for value {2} with message: {3} ",
                        result.severity(), result.focusNode(), result.value(),
                        result.message()))));

        if (r.conforms()) {
            Assertions.assertEquals(Outcome.Pass, outcome);
        } else {
            Assertions.assertTrue(
                    r.getEntries().stream().anyMatch(a -> a.severity().equals(outcome.url)));
        }
    }

    enum Outcome {
        Info(Severity.Info), Warning(Severity.Warning), Violation(Severity.Violation), Pass(null);
        final Severity url;

        Outcome(Severity url) {
            this.url = url;
        }
    }

    @ParameterizedTest(name = "Rule {0} for {1} (should be {2})")
    @CsvFileSource(resources = "/localized-test-cases.csv", numLinesToSkip = 1)
    public void testLangShaclRule(String rule, String output, String outcome) throws IOException {
        String content;
        try (final BufferedReader reader = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream("/rule-templates/" + rule)))) {
            content = reader.lines().collect(Collectors.joining("\n"));
        }
        content = content.replace("${lang}", "cs");
        final Model shapesModel = ModelFactory.createDefaultModel();
        shapesModel.read(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), null,
                         FileUtils.langTurtle);
        testModel(shapesModel, output, Outcome.valueOf(outcome));
    }
}
