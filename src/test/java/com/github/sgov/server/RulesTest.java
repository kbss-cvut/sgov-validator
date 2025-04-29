package com.github.sgov.server;

import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.util.SHACLPreferences;
import org.topbraid.shacl.validation.ValidationReport;
import org.topbraid.shacl.vocabulary.SH;

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

    @BeforeEach
    public void init() {
        SHACLPreferences.setProduceFailuresMode(true);
    }

    @ParameterizedTest(name = "Rule {0} for {1} (should be {2})")
    @CsvFileSource(resources = "/test-cases.csv", numLinesToSkip = 1)
    public void testShaclRule(String rule, String output, String outcome) throws IOException {
        testModel(loadRuleset(Set.of(getClass().getResource("/rules/" + rule))), output,
                  Outcome.valueOf(outcome));
    }

    private static Model loadRuleset(Set<URL> ruleSet) throws IOException {
        final Model shapesModel = JenaUtil.createMemoryModel();
        for (URL r : ruleSet) {
            shapesModel.read(r.openStream(), null, FileUtils.langTurtle);
        }
        return shapesModel;
    }

    private void testModel(Model shapesModel, String data, Outcome outcome) {
        final Model dataModel =
                JenaUtil.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF, null);

        OntDocumentManager.getInstance().setProcessImports(false);
        dataModel.read(RulesTest.class.getResourceAsStream("/" + data), "urn:dummy",
                       FileUtils.langTurtle);

        final Validator validator = new Validator();
        final ValidationReport r = validator.validate(dataModel, shapesModel);

        r.results().forEach(result -> System.out.println((MessageFormat
                .format("[{0}] Node {1} failing for value {2} with message: {3} ",
                        result.getSeverity().getLocalName(), result.getFocusNode(), result.getValue(),
                        result.getMessage()))));

        if (r.conforms()) {
            Assertions.assertEquals(outcome, Outcome.Pass);
        } else {
            Assertions.assertTrue(
                    r.results().stream().anyMatch(a -> a.getSeverity().equals(outcome.url)));
        }
    }

    enum Outcome {
        Info(SH.Info), Warning(SH.Warning), Violation(SH.Violation), Pass(null);
        final Resource url;

        Outcome(Resource url) {
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
        final Model shapesModel = JenaUtil.createMemoryModel();
        shapesModel.read(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), null,
                         FileUtils.langTurtle);
        testModel(shapesModel, output, Outcome.valueOf(outcome));
    }
}
