package utilities;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.testng.Assert;

/**
 * Reads element locators from src/test/resources/object-repository. Every page
 * object has one file named after its class, e.g. LoginPage.properties, with
 * one entry per element written as {@code key=strategy:value}:
 *
 * <pre>
 * usernameField=id:username
 * loginButton=css:button[type='submit']
 * sportRadio=css:input[name='sport'][value='{0}']
 * </pre>
 *
 * The strategies are listed in {@link #STRATEGIES}. Placeholders {0}, {1}, ...
 * are replaced by the arguments given to {@link #get(String, String, Object...)}
 * (a plain text replacement, so single quotes need no escaping). The value is
 * split on its first colon only, so CSS and XPath values may contain colons.
 *
 * Each file is read once per JVM and cached; the cache is safe to use from the
 * parallel data provider threads. Nothing is logged when a locator is read
 * successfully. Any problem (missing file or key, unknown strategy, wrong
 * number of arguments) is written as an Allure step and fails the test.
 */
public class ObjectRepository {

    /** Folder holding the repository files, relative to the project root. */
    private static final Path FOLDER = Paths.get("src", "test", "resources", "object-repository");

    /**
     * Supported strategy prefixes, in the locator preference order of CLAUDE.md,
     * mapped to the Selenium factory method that builds the locator.
     */
    private static final Map<String, Function<String, By>> STRATEGIES = new LinkedHashMap<>();
    static {
        STRATEGIES.put("id", By::id);
        STRATEGIES.put("name", By::name);
        STRATEGIES.put("css", By::cssSelector);
        STRATEGIES.put("xpath", By::xpath);
        STRATEGIES.put("class", By::className);
        STRATEGIES.put("tag", By::tagName);
        STRATEGIES.put("linkText", By::linkText);
        STRATEGIES.put("partialLinkText", By::partialLinkText);
    }

    /** Matches a placeholder such as {0} and captures its index. */
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{(\\d+)\\}");

    /** Repository files already read, keyed by page name. */
    private static final Map<String, Properties> CACHE = new ConcurrentHashMap<>();

    private ObjectRepository() {
    }

    /**
     * Returns the locator stored under the given key in the given page's
     * repository file, with its placeholders replaced by the arguments.
     *
     * @param page the page name, i.e. the simple class name of the page object
     *             (also the file name without .properties)
     * @param key  the entry name, e.g. "usernameField"
     * @param args the values for the placeholders {0}, {1}, ... in order; there
     *             must be exactly one per placeholder index
     * @return the locator
     */
    public static By get(String page, String key, Object... args) {
        String entry = load(page).getProperty(key);
        if (entry == null) {
            fail("Key '" + key + "' not found in " + fileName(page) + ".", null);
        }
        String[] parts = split(entry);
        if (parts == null) {
            fail("Entry '" + key + "' in " + fileName(page)
                    + " must be written as strategy:value with one of the strategies " + STRATEGIES.keySet()
                    + ", but is: " + entry + ".", null);
        }
        int expectedArguments = placeholderCount(parts[1]);
        if (args.length != expectedArguments) {
            fail("Entry '" + key + "' in " + fileName(page) + " has " + expectedArguments
                    + " placeholder(s) but " + args.length + " argument(s) were given. Entry: " + entry + ".", null);
        }
        return STRATEGIES.get(parts[0]).apply(fillPlaceholders(parts[1], args));
    }

    /**
     * Checks every entry of a page's repository file without building any
     * locator, for the repository validation test. An entry is valid when it
     * uses a known strategy, has a non-empty value and numbers its placeholders
     * from {0} without gaps.
     *
     * @param page the page name (the file name without .properties)
     * @return one message per invalid entry; empty when the file is valid
     */
    public static List<String> validate(String page) {
        Properties properties = load(page);
        List<String> problems = new ArrayList<>();
        for (String key : new TreeSet<>(properties.stringPropertyNames())) {
            String entry = properties.getProperty(key);
            String[] parts = split(entry);
            if (parts == null) {
                problems.add(fileName(page) + ": '" + key + "' is not strategy:value with one of "
                        + STRATEGIES.keySet() + ": " + entry);
                continue;
            }
            TreeSet<Integer> indexes = new TreeSet<>();
            Matcher matcher = PLACEHOLDER.matcher(parts[1]);
            while (matcher.find()) {
                indexes.add(Integer.parseInt(matcher.group(1)));
            }
            if (!indexes.isEmpty() && indexes.last() != indexes.size() - 1) {
                problems.add(fileName(page) + ": '" + key + "' placeholders must be numbered from {0} without gaps: "
                        + entry);
            }
        }
        return problems;
    }

    /**
     * Returns the cached contents of a page's repository file, reading it on
     * first use. The file is read as UTF-8 so link texts may contain any
     * character.
     */
    private static Properties load(String page) {
        // A failed read throws out of computeIfAbsent, so nothing is cached and the
        // next call reports the problem again.
        return CACHE.computeIfAbsent(page, name -> {
            Path path = FOLDER.resolve(name + ".properties");
            Properties properties = new Properties();
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                properties.load(reader);
            } catch (NoSuchFileException noSuchFileException) {
                fail("Object repository file " + path + " does not exist. Every page object needs a file named after its class.",
                        noSuchFileException);
            } catch (IOException ioException) {
                fail("Object repository file " + path + " could not be read.", ioException);
            } catch (IllegalArgumentException illegalArgumentException) {
                fail("Object repository file " + path
                        + " contains a malformed Unicode escape sequence (a '\\u' not followed by four hexadecimal digits).",
                        illegalArgumentException);
            }
            return properties;
        });
    }

    /**
     * Splits an entry into its strategy and its trimmed value at the first colon.
     *
     * @return {strategy, value}, or null when the strategy is unknown or the
     *         value is missing
     */
    private static String[] split(String entry) {
        int colon = entry.indexOf(':');
        if (colon < 0) {
            return null;
        }
        String strategy = entry.substring(0, colon).trim();
        // Trimmed because java.util.Properties keeps trailing spaces in values.
        String value = entry.substring(colon + 1).trim();
        if (!STRATEGIES.containsKey(strategy) || value.isEmpty()) {
            return null;
        }
        return new String[] { strategy, value };
    }

    /** Returns the number of arguments a value needs: its highest placeholder index plus one. */
    private static int placeholderCount(String value) {
        int count = 0;
        Matcher matcher = PLACEHOLDER.matcher(value);
        while (matcher.find()) {
            count = Math.max(count, Integer.parseInt(matcher.group(1)) + 1);
        }
        return count;
    }

    /** Replaces every {n} in the value by the n-th argument, taken literally. */
    private static String fillPlaceholders(String value, Object[] args) {
        Matcher matcher = PLACEHOLDER.matcher(value);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String argument = String.valueOf(args[Integer.parseInt(matcher.group(1))]);
            matcher.appendReplacement(result, Matcher.quoteReplacement(argument));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /** Returns the repository file name of a page, for error messages. */
    private static String fileName(String page) {
        return "object-repository/" + page + ".properties";
    }

    /** Reports a problem as an Allure step and fails the test. */
    private static void fail(String message, Throwable cause) {
        AllureWrapper.step(message);
        if (cause == null) {
            Assert.fail(message);
        }
        Assert.fail(message, cause);
    }
}
