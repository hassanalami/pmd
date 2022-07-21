/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.test.schema;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;

import org.hamcrest.MatcherAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.xml.sax.InputSource;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.PlainTextLanguage;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.rule.AbstractRule;

/**
 * @author Clément Fournier
 */
public class TestSchemaParserTest {

    @Rule
    public final SystemErrRule errStreamCaptor = new SystemErrRule().muteForSuccessfulTests();


    @Test
    public void testSchemaSimple() throws IOException {
        String file = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                      + "<test-data\n"
                      + "        xmlns=\"http://pmd.sourceforge.net/rule-tests\"\n"
                      + "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                      + "        xsi:schemaLocation=\"http://pmd.sourceforge.net/rule-tests net/sourceforge/pmd/test/schema/rule-tests_1_0_0.xsd\">\n"
                      + "    <test-code>\n"
                      + "        <description>equality operators with Double.NaN</description>\n"
                      + "        <expected-problems>4</expected-problems>\n"
                      + "        <code><![CDATA[\n"
                      + "            public class Foo {\n"
                      + "            }\n"
                      + "            ]]></code>\n"
                      + "    </test-code>\n"
                      + "    <test-code>\n"
                      + "        <description>equality operators with Float.NaN</description>\n"
                      + "        <expected-problems>4</expected-problems>\n"
                      + "        <code><![CDATA[\n"
                      + "            public class Foo {\n"
                      + "            }\n"
                      + "            ]]></code>\n"
                      + "    </test-code>\n"
                      + "</test-data>\n";

        RuleTestCollection parsed = parseFile(file);

        assertEquals(2, parsed.getTests().size());

    }

    @Test
    public void testSchemaDeprecatedAttr() throws IOException {
        String file = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                      + "<test-data\n"
                      + "        xmlns=\"http://pmd.sourceforge.net/rule-tests\"\n"
                      + "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                      + "        xsi:schemaLocation=\"http://pmd.sourceforge.net/rule-tests net/sourceforge/pmd/test/schema/rule-tests_1_0_0.xsd\">\n"
                      + "    <test-code regressionTest='false'>\n"
                      + "        <description>equality operators with Double.NaN</description>\n"
                      + "        <expected-problems>4</expected-problems>\n"
                      + "        <code><![CDATA[\n"
                      + "            public class Foo {\n"
                      + "            }\n"
                      + "            ]]></code>\n"
                      + "    </test-code>\n"
                      + "</test-data>\n";

        errStreamCaptor.enableLog();
        RuleTestCollection parsed = parseFile(file);

        assertEquals(1, parsed.getTests().size());
        MatcherAssert.assertThat(errStreamCaptor.getLog(), containsString(" 6|     <test-code regressionTest='false'>\n"
                                                                          + "                   ^^^^^^^^^^^^^^ Attribute 'regressionTest' is deprecated, use 'ignored' with inverted value\n"));
    }

    private RuleTestCollection parseFile(String file) throws IOException {
        MockRule mockRule = new MockRule();
        mockRule.setLanguage(PlainTextLanguage.getInstance());

        InputSource is = new InputSource();
        is.setSystemId("a/file.xml");
        is.setCharacterStream(new StringReader(file));

        return new TestSchemaParser().parse(mockRule, is);
    }

    public static final class MockRule extends AbstractRule {
        @Override
        public void apply(Node target, RuleContext ctx) {
            // do nothing
        }
    }

}
