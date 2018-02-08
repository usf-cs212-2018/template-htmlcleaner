import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

/**
 * A helper class with several static methods that will help fetch a webpage,
 * strip out all of the HTML, and parse the resulting plain text into words.
 * Meant to be used for the web crawler project.
 *
 * @see HTMLCleaner
 * @see HTMLCleanerTest
 */
@RunWith(Enclosed.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HTMLCleanerTest {

	public static final String format = "%nHTML:%n%s%n%nExpected:%n%s%n%nActual:%n%s%n%n";

	public static void test(String test, String expected, String actual) {
		String debug = String.format(format, test, expected, actual);
		Assert.assertEquals(debug, expected, actual);
	}

	@FixMethodOrder(MethodSorters.NAME_ASCENDING)
	public static class Test01Entities {
		/**
		 * Tests "2010&ndash;2011". (View Javadoc to see rendering.)
		 */
		@Test
		public void test01Named() {
			String test = "2010&ndash;2011";
			String expected = "2010 2011";
			String actual = HTMLCleaner.stripEntities(test);

			test(test, expected, actual);
		}

		/**
		 * Tests "2010&#8211;2011". (View Javadoc to see rendering.)
		 */
		@Test
		public void test02Numbered() {
			String test = "2010&#8211;2011";
			String expected = "2010 2011";
			String actual = HTMLCleaner.stripEntities(test);

			test(test, expected, actual);
		}

		/**
		 * Tests "2010&#x2013;2011". (View Javadoc to see rendering.)
		 */
		@Test
		public void test03Hexadecimal() {
			String test = "2010&#x2013;2011";
			String expected = "2010 2011";
			String actual = HTMLCleaner.stripEntities(test);

			test(test, expected, actual);
		}

		/**
		 * Tests "touche&#769;!". (View Javadoc to see rendering.)
		 */
		@Test
		public void test04Accent1() {
			String test = "touche&#769;!";
			String expected = "touche !";
			String actual = HTMLCleaner.stripEntities(test);

			test(test, expected, actual);
		}

		/**
		 * Tests "touch&eacute;!". (View Javadoc to see rendering.)
		 */
		@Test
		public void test05Accent2() {
			String test = "touch&eacute;!";
			String expected = "touch !";
			String actual = HTMLCleaner.stripEntities(test);

			test(test, expected, actual);
		}

		/**
		 * Tests "hello&mdash;good&dash;bye". (View Javadoc to see rendering.)
		 */
		@Test
		public void test06Multiple() {
			String test = "hello&mdash;good&dash;bye";
			String expected = "hello good bye";
			String actual = HTMLCleaner.stripEntities(test);

			test(test, expected, actual);
		}

		/**
		 * Tests "hello & good-bye".
		 */
		@Test
		public void test07Ampersand() {
			String test = "hello & good-bye";
			String expected = "hello & good-bye";
			String actual = HTMLCleaner.stripEntities(test);

			test(test, expected, actual);
		}

		/**
		 * Tests "hello & good-bye;".
		 */
		@Test
		public void test08AndSemicolon() {
			String test = "hello & good-bye;";
			String expected = "hello & good-bye;";
			String actual = HTMLCleaner.stripEntities(test);

			test(test, expected, actual);
		}
	}

	@FixMethodOrder(MethodSorters.NAME_ASCENDING)
	public static class Test02Comments {

		@Test
		public void test01Simple() {
			String test = "<!-- hello -->";
			String expected = " ";
			String actual = HTMLCleaner.stripComments(test);

			test(test, expected, actual);
		}

		@Test
		public void test02ABC() {
			String test = "A<!-- B -->C";
			String expected = "A C";
			String actual = HTMLCleaner.stripComments(test);

			test(test, expected, actual);
		}

		@Test
		public void test03NewLine() {
			String test = "A<!--\n B\n -->C";
			String expected = "A C";
			String actual = HTMLCleaner.stripComments(test);

			test(test, expected, actual);
		}

		@Test
		public void test04Tags() {
			String test = "A<!-- <b>B</b> -->C";
			String expected = "A C";
			String actual = HTMLCleaner.stripComments(test);

			test(test, expected, actual);
		}

		@Test
		public void test05Slashes() {
			String test = "A<!-- B //-->C";
			String expected = "A C";
			String actual = HTMLCleaner.stripComments(test);

			test(test, expected, actual);
		}

		@Test
		public void test06Multiple() {
			String test = "A<!-- B -->C D<!-- E -->F";
			String expected = "A C D F";
			String actual = HTMLCleaner.stripComments(test);

			test(test, expected, actual);
		}
	}

	@FixMethodOrder(MethodSorters.NAME_ASCENDING)
	public static class Test03Tags {

		/**
		 * View Javadoc to see HTML rendering of test case:
		 *
		 * <pre>
		 * <b>hello</b> world!
		 * </pre>
		 */
		@Test
		public void test01Simple() {
			String test = "<b>hello</b> world!";
			String expected = " hello  world!";
			String actual = HTMLCleaner.stripTags(test);

			test(test, expected, actual);
		}

		/**
		 * View Javadoc to see HTML rendering of test case:
		 *
		 * <pre>
		 * <b>hello
		 * </b> world!
		 * </pre>
		 */
		@Test
		public void test02SimpleNewLine() {
			String test = "<b>hello\n</b> world!";
			String expected = " hello\n  world!";
			String actual = HTMLCleaner.stripTags(test);

			test(test, expected, actual);
		}

		/**
		 * View Javadoc to see HTML rendering of test case:
		 *
		 * <pre>
		 * <a
		 *  name=toc>table of contents</a>
		 * </pre>
		 */
		@Test
		public void test03AttributeNewline() {
			String test = "<a \n name=toc>table of contents</a>";
			String expected = " table of contents ";
			String actual = HTMLCleaner.stripTags(test);

			test(test, expected, actual);
		}

		/**
		 * View Javadoc to see HTML rendering of test case:
		 *
		 * <pre>
		 * <p>Hello, <strong>world</strong>!</p>
		 * </pre>
		 */
		@Test
		public void test04NestedTags() {
			String test = "<p>Hello, <strong>world</strong>!</p>";
			String expected = " Hello,  world ! ";
			String actual = HTMLCleaner.stripTags(test);

			test(test, expected, actual);
		}

		/**
		 * View Javadoc to see HTML rendering of test case:
		 *
		 * <pre>
		 * <p>Hello, <br/>world!</p>
		 * </pre>
		 */
		@Test
		public void test05LineBreak() {
			String test = "<p>Hello, <br/>world!</p>";
			String expected = " Hello,  world! ";
			String actual = HTMLCleaner.stripTags(test);

			test(test, expected, actual);
		}
	}

	@FixMethodOrder(MethodSorters.NAME_ASCENDING)
	public static class Test04Elements {
		@Test
		public void test01Style() {
			String test = "<style type=\"text/css\">body { font-size: 10pt; }</style>";
			String expected = " ";
			String actual = HTMLCleaner.stripElement(test, "style");

			test(test, expected, actual);
		}

		@Test
		public void test02StyleNewline1() {
			String test = "<style type=\"text/css\">\nbody { font-size: 10pt; }\n</style>";
			String expected = " ";
			String actual = HTMLCleaner.stripElement(test, "style");

			test(test, expected, actual);
		}

		@Test
		public void test03StyleNewline2() {
			String test = "<style \n type=\"text/css\">\nbody { font-size: 10pt; }\n</style>";
			String expected = " ";
			String actual = HTMLCleaner.stripElement(test, "style");

			test(test, expected, actual);
		}

		@Test
		public void test04Multiple() {
			String test = "a<test>b</test>c<test>d</test>e";
			String expected = "a c e";
			String actual = HTMLCleaner.stripElement(test, "test");

			test(test, expected, actual);
		}

		@Test
		public void test05Mixed() {
			String test = "<title>Hello</title><script>potato</script> world";
			String expected = "<title>Hello</title>  world";
			String actual = HTMLCleaner.stripElement(test, "script");

			test(test, expected, actual);
		}
	}

	@FixMethodOrder(MethodSorters.NAME_ASCENDING)
	public static class Test05Clean {

		@Test
		public void test01NoHTML() {
			String test = "hello & good-bye;";
			String expected = "hello & good-bye;";
			String actual = HTMLCleaner.stripHTML(test);

			test(test, expected, actual);
		}

		@Test
		public void test02OneLine() {
			String test = "<b>hello</p>&amp;<script>potato</script>world";
			String expected = " hello   world";
			String actual = HTMLCleaner.stripHTML(test);

			test(test, expected, actual);
		}

		@Test
		public void test03SimplePage() {
			StringBuilder html = new StringBuilder();
			html.append("<!DOCTYPE html>\n");
			html.append("<html>\n");
			html.append("<head>\n");
			html.append("    <meta charset=\"utf-8\">\n");
			html.append("    <script type=\"text/javascript\" src=\"d3.v3.js\"></script>\n");
			html.append("    <style type=\"text/css\">\n");
			html.append("    body {\n");
			html.append("        font-size: 10pt;\n");
			html.append("        font-family: sans-serif;\n");
			html.append("    }\n");
			html.append("    </style>\n");
			html.append("</head>\n");
			html.append("<body>\n");
			html.append("Hello, world! &copy;2013\n");
			html.append("</body>\n");
			html.append("</html>\n");

			String expected = "Hello, world!  2013";
			String actual = HTMLCleaner.stripHTML(html.toString());

			// note trim to remove blank lines
			test(html.toString(), expected, actual.trim());
		}

		@Test
		public void test04Panagrams() throws IOException {
			Path htmlPath = Paths.get("test", "pangrams.html");
			Path textPath = Paths.get("test", "pangrams.txt");

			Assert.assertTrue(Files.isReadable(htmlPath));
			Assert.assertTrue(Files.isReadable(textPath));

			String html = new String(Files.readAllBytes(htmlPath));
			String expected = new String(Files.readAllBytes(textPath));
			String actual = HTMLCleaner.stripHTML(html);

			test(html.toString(), expected, actual);
		}
	}
}
