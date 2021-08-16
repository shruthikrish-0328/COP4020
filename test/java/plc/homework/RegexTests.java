package plc.homework;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Contains JUnit tests for {@link Regex}. Test structure for steps 1 & 2 are
 * provided, you must create this yourself for step 3.
 *
 * To run tests, either click the run icon on the left margin, which can be used
 * to run all tests or only a specific test. You should make sure your tests are
 * run through IntelliJ (File > Settings > Build, Execution, Deployment > Build
 * Tools > Gradle > Run tests using <em>IntelliJ IDEA</em>). This ensures the
 * name and inputs for the tests are displayed correctly in the run window.
 */
public class RegexTests {

    /**
     * This is a parameterized test for the {@link Regex#EMAIL} regex. The
     * {@link ParameterizedTest} annotation defines this method as a
     * parameterized test, and {@link MethodSource} tells JUnit to look for the
     * static method {@link #testEmailRegex()}.
     *
     * For personal preference, I include a test name as the first parameter
     * which describes what that test should be testing - this is visible in
     * IntelliJ when running the tests (see above note if not working).
     */
    @ParameterizedTest
    @MethodSource
    public void testEmailRegex(String test, String input, boolean success) {
        test(input, Regex.EMAIL, success);
    }

    /**
     * This is the factory method providing test cases for the parameterized
     * test above - note that it is static, takes no arguments, and has the same
     * name as the test. The {@link Arguments} object contains the arguments for
     * each test to be passed to the function above.
     */
    public static Stream<Arguments> testEmailRegex() {
        return Stream.of(
                //Professors test cases
                Arguments.of("Alphanumeric", "thelegend27@gmail.com", true),
                Arguments.of("UF Domain", "otherdomain@ufl.edu", true),
                Arguments.of("Missing Domain Dot", "missingdot@gmailcom", false),
                Arguments.of("Symbols", "symbols#$%@gmail.com", false),

                //Working Examples
                Arguments.of("Email", "shruthikrish@.com",true),
                Arguments.of("Email", "ShRuThI.KrIsH_0328@YAHOO.com",true),
                Arguments.of("Email", "Skrishnapuram_0328@u4sd.org",true),
                Arguments.of("Email", "-.@ufl23.com",true),
                Arguments.of("Email", "krishnSHR.2019@123.com",true),
                //Failing Examples
                Arguments.of("Unique Characters", "Shruthikrish!#19@123.com",false),
                Arguments.of("Number in domain", "ShRuThI.KrIsH_0328@ufl.edu123",false),
                Arguments.of("Capitalized org", "Skrishnapuram23@.ORG",false),
                Arguments.of("Too many characters after domain dot", "-.@ufl23.commm",false),
                Arguments.of("Capitalized domain", "krishnSHR.2019@GMAIL.COM",false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testEvenStringsRegex(String test, String input, boolean success) {
        test(input, Regex.EVEN_STRINGS, success);
    }

    public static Stream<Arguments> testEvenStringsRegex() {
        return Stream.of(
                //Professors test cases
                //what has ten letters and starts with gas?
                Arguments.of("10 Characters", "automobile", true),
                Arguments.of("14 Characters", "i<3pancakes10!", true),
                Arguments.of("6 Characters", "6chars", false),
                Arguments.of("13 Characters", "i<3pancakes9!", false),

                //Working Examples
                Arguments.of("10 Characters", "Florida8&!", true),
                Arguments.of("12 Characters", "Money$123456", true),
                Arguments.of("14 Characters", "ShruthiK@#%+=\\", true),
                Arguments.of("16 Characters", "LayaK<>?()11182003", true),
                Arguments.of("20 Characters", "Mississippi-=!@#$%^&", true),
                //Failing Examples
                Arguments.of("1 Character", "1", false),
                Arguments.of("8 Characters", "UF2023!!", false),
                Arguments.of("15 Characters", "COP4020isHard:(", false),
                Arguments.of("22 Characters", "Mississippi-=!@#$%^&()", false),
                Arguments.of("11 Characters", "fiveIS>four", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testIntegerListRegex(String test, String input, boolean success) {
        test(input, Regex.INTEGER_LIST, success);
    }

    public static Stream<Arguments> testIntegerListRegex() {
        return Stream.of(
                //Professors test cases
                Arguments.of("Single Element", "[1]", true),
                Arguments.of("Multiple Elements", "[1,2,3]", true),
                Arguments.of("Missing Brackets", "1,2,3", false),
                Arguments.of("Missing Commas", "[1 2 3]", false),

                //Working Examples
                Arguments.of("Single Element", "[1]", true),
                Arguments.of("Multiple Elements", "[1,2,3]", true),
                Arguments.of("No Elements", "[]", true),
                Arguments.of("Space", "[1,2, 3]", true),
                Arguments.of("Multi Digit Number", "[123]", true),
                //Failing Examples
                Arguments.of("Starting Comma", "[,]", false),
                Arguments.of("Ending Comma", "[1,]", false),
                Arguments.of("Multiple Spaces", "[1,2  ,3]", false),
                Arguments.of("Missing Commas", "[1 2 3]", false),
                Arguments.of("Multiple Commas", "[1,,2]", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testNumberRegex(String test, String input, boolean success) {
        //throw new UnsupportedOperationException(); //TODO
        test(input, Regex.NUMBER, success);
    }

    public static Stream<Arguments> testNumberRegex() {
        //throw new UnsupportedOperationException(); //TODO
        return Stream.of(
                //Working Examples
                Arguments.of("Single Digit", "1", true),
                Arguments.of("Multiple Digits", "1234", true),
                Arguments.of("Decimal", "120.18271", true),
                Arguments.of("Trailing Zeros", "145.0000", true),
                Arguments.of("Positive Sign", "+12.8271", true),
                //Failing Examples
                Arguments.of("Starting w Decimal", ".50", false),
                Arguments.of("Ending w Decimal", "11.", false),
                Arguments.of("Multiple Decimals", "11.00.12", false),
                Arguments.of("Trailing Decimals", "123......", false),
                Arguments.of("Plus and Minus", "+-20", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testStringRegex(String test, String input, boolean success) {
        //throw new UnsupportedOperationException(); //TODO
        test(input, Regex.STRING, success);
    }

    public static Stream<Arguments> testStringRegex() {
        //throw new UnsupportedOperationException(); //TODO
        return Stream.of(

                //Working Examples
                Arguments.of("Regular String", "\"ShruthiUF2023\"", true),
                Arguments.of("Backslash t & b", "\"COP4020\\t\\bWooo\"", true),
                Arguments.of("Empty String", "\"\"", true),
                Arguments.of("Backslash b", "\"145.0\\b00\\\"0ahs&\"", true),
                Arguments.of("Backslash n", "\"UnivFlorida\\'2023\\n\"", true),
                //Failing Examples
                Arguments.of("Invalid Escape", "\"invalid\\escape\"", false),
                Arguments.of("Only Backslash", "\"\\\"", false),
                Arguments.of("Backslash %", "\"100\\%Effort\"", false),
                Arguments.of("Backslash j", "\"backslash\\j\"", false),
                Arguments.of("Backslash +", "\"backslash\\+\"", false)
        );
    }

    /**
     * Asserts that the input matches the given pattern. This method doesn't do
     * much now, but you will see this concept in future assignments.
     */
    private static void test(String input, Pattern pattern, boolean success) {
        Assertions.assertEquals(success, pattern.matcher(input).matches());
    }
}