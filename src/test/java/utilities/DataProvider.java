package utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.Assert;
import io.qameta.allure.model.Status;
import testbase.TestBase;

/**
 * Builds test data for TestNG data providers from CSV files under
 * src/test/resources/test-data. Not related to the TestNG
 * {@code @DataProvider} annotation, which tests use to call these methods.
 */
public class DataProvider {

    /**
     * Reads a CSV file from a specified folder within the test-data folder in
     * resources.
     * Returns a 2-dimensional Object array suitable for a TestNG DataProvider.
     * Each CSV row becomes one test iteration and each column one test parameter.
     * Fails the test if the file is missing, unreadable or has no data rows.
     *
     * @param folderPath The subfolder within test-data.
     * @param fileName   The CSV file name.
     * @return 2D Object array of CSV data (skips header).
     */
    public Object[][] csvReader(String folderPath, String fileName) {

        // Prepares the file path. Also sanitizes the path using .normalize() method.
        String path = Paths.get("src", "test", "resources", "test-data", folderPath, fileName).normalize().toString();

        try {
            // Reads the CSV file. The first line is treated as the header and skipped,
            // and surrounding whitespace is trimmed from every value.
            Reader reader = new FileReader(path);
            CSVParser csvParser = CSVParser.builder().setReader(reader).setFormat(CSVFormat.DEFAULT.builder()
                    .setHeader().setSkipHeaderRecord(true).setIgnoreHeaderCase(true).setTrim(true).get()).get();

            List<CSVRecord> records = csvParser.getRecords();
            // Checks if the file is empty.
            if (records.size() == 0) {
                String errorMessage = "The test data CSV file is empty. Folder path: " + folderPath + ". File name: "
                        + fileName + ".";
                AllureWrapper.step(errorMessage);
                Assert.fail(errorMessage);
            }

            // Store the data from the test data file into the Object array. The column
            // count is taken from the first row and assumed to be the same for all rows.
            int rowCount = records.size();
            int columnCount = records.getFirst().size();
            Object[][] data = new Object[rowCount][columnCount];
            for (int rowIteration = 0; rowIteration < rowCount; rowIteration++) {
                for (int columnIteration = 0; columnIteration < columnCount; columnIteration++) {
                    data[rowIteration][columnIteration] = records.get(rowIteration).get(columnIteration);
                }
            }
            return data;

        } catch (FileNotFoundException fileNotFoundException) {
            String errorMessage = "The specified test data CSV file either does not exist, is a directory instead of a file, or can't be read for some other reason. Folder path: "
                    + folderPath + ". File name: " + fileName + ".";
            AllureWrapper.step(errorMessage);
            Assert.fail(errorMessage, fileNotFoundException);
        } catch (IOException ioException) {
            String errorMessage = "An IO exception occurred while parsing the test data CSV file. Folder path: "
                    + folderPath + ". File name: " + fileName + ".";
            AllureWrapper.step(errorMessage);
            Assert.fail(errorMessage, ioException);
        }

        // This code should be inaccessible. An error is placed in case of any
        // unexpected scenarios.
        String errorMessage = "An unexpected error appeared while reading test data CSV file. Folder path: "
                + folderPath + ". File name: " + fileName + ".";
        AllureWrapper.step(errorMessage);
        Assert.fail(errorMessage);
        return null;
    }

    /**
     * Reads the first sheet of an XLSX file from a specified folder within the
     * test-data folder in resources.
     * Returns a 2-dimensional Object array suitable for a TestNG DataProvider.
     * Each row becomes one test iteration and each column one test parameter. The
     * first row is treated as a header and skipped. Every cell value is returned
     * as a String, formatted the way Excel displays it, so the result matches
     * {@link #csvReader(String, String)}. Fails the test if the file is missing,
     * unreadable or has no data rows.
     *
     * @param folderPath The subfolder within test-data.
     * @param fileName   The XLSX file name.
     * @return 2D Object array of XLSX data (skips header).
     */
    public Object[][] xlsxReader(String folderPath, String fileName) {

        // Prepares the file path. Also sanitizes the path using .normalize() method.
        String path = Paths.get("src", "test", "resources", "test-data", folderPath, fileName).normalize().toString();

        try (FileInputStream fileInputStream = new FileInputStream(path);
                Workbook workbook = new XSSFWorkbook(fileInputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            // DataFormatter converts every cell type (numeric, date, boolean, formula,
            // blank) to the String Excel displays, so no cell type causes an error.
            DataFormatter dataFormatter = new DataFormatter();
            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();

            // Checks if the sheet has any data rows. Row numbers are 0-based and the
            // header is row 0, so a last row number of 0 means there is no data.
            if (sheet.getLastRowNum() < 1) {
                String errorMessage = "The test data XLSX file is empty. Folder path: " + folderPath + ". File name: "
                        + fileName + ".";
                AllureWrapper.step(errorMessage);
                Assert.fail(errorMessage);
            }

            // The column count is taken from the header row and assumed to be the same
            // for all rows.
            int columnCount = sheet.getRow(0).getLastCellNum();
            int rowCount = sheet.getLastRowNum();
            Object[][] data = new Object[rowCount][columnCount];
            for (int rowIteration = 0; rowIteration < rowCount; rowIteration++) {
                Row row = sheet.getRow(rowIteration + 1);
                for (int columnIteration = 0; columnIteration < columnCount; columnIteration++) {
                    // Missing rows or cells are read as empty strings and values are trimmed,
                    // like the CSV reader.
                    Cell cell = (row == null) ? null : row.getCell(columnIteration);

                    // Warns about any non-text cell (numeric, boolean, formula, error) without
                    // stopping the test. Blank cells are ignored.
                    if (cell != null && cell.getCellType() != CellType.STRING
                            && cell.getCellType() != CellType.BLANK) {
                        AllureWrapper.step("Warning: cell " + cell.getAddress().formatAsString() + " in the test data XLSX file is of type "
                                + cell.getCellType() + " instead of text. It was converted to the text Excel displays ("
                                + dataFormatter.formatCellValue(cell, formulaEvaluator).trim()
                                + "). Change the cell format to Text to use the exact value. Folder path: "
                                + folderPath + ". File name: " + fileName + ".", Status.BROKEN);
                    }

                    data[rowIteration][columnIteration] = (cell == null) ? ""
                            : dataFormatter.formatCellValue(cell, formulaEvaluator).trim();
                }
            }
            return data;

        } catch (FileNotFoundException fileNotFoundException) {
            String errorMessage = "The specified test data XLSX file either does not exist, is a directory instead of a file, or can't be read for some other reason. Folder path: "
                    + folderPath + ". File name: " + fileName + ".";
            AllureWrapper.step(errorMessage);
            Assert.fail(errorMessage, fileNotFoundException);
        } catch (IOException ioException) {
            String errorMessage = "An IO exception occurred while parsing the test data XLSX file. Folder path: "
                    + folderPath + ". File name: " + fileName + ".";
            AllureWrapper.step(errorMessage);
            Assert.fail(errorMessage, ioException);
        }

        // This code should be inaccessible. An error is placed in case of any
        // unexpected scenarios.
        String errorMessage = "An unexpected error appeared while reading test data XLSX file. Folder path: "
                + folderPath + ". File name: " + fileName + ".";
        AllureWrapper.step(errorMessage);
        Assert.fail(errorMessage);
        return null;
    }

    /**
     * Reads a CSV file like {@link #csvReader(String, String)} and runs every row
     * once for each browser enabled in configuration.properties. The browser name
     * is added as the first column, so a test method takes the browser as its first
     * parameter followed by the CSV columns.
     *
     * @param testBase   the TestBase whose configuration decides the browsers
     * @param folderPath The subfolder within test-data.
     * @param fileName   The CSV file name.
     * @return 2D Object array with one row per CSV row per enabled browser
     */
    public Object[][] csvReaderWithBrowser(TestBase testBase, String folderPath, String fileName) {
        // Reads data from the CSV file using the csvReader method.
        return addBrowserColumn(testBase, csvReader(folderPath, fileName));
    }

    /**
     * Reads an XLSX file like {@link #xlsxReader(String, String)} and runs every
     * row once for each browser enabled in configuration.properties. The browser
     * name is added as the first column, so a test method takes the browser as its
     * first parameter followed by the XLSX columns.
     *
     * @param testBase   the TestBase whose configuration decides the browsers
     * @param folderPath The subfolder within test-data.
     * @param fileName   The XLSX file name.
     * @return 2D Object array with one row per XLSX row per enabled browser
     */
    public Object[][] xlsxReaderWithBrowser(TestBase testBase, String folderPath, String fileName) {
        // Reads data from the XLSX file using the xlsxReader method.
        return addBrowserColumn(testBase, xlsxReader(folderPath, fileName));
    }

    /**
     * Reads a JSON file from a specified folder within the test-data folder in
     * resources. The file must contain an array of objects. Each object becomes one
     * test iteration and each key one test parameter. The key order of the first
     * object decides the column order, and a key missing from a later object is
     * read as an empty string.
     *
     * Scalar values (text, numbers, booleans) are returned as Strings. Nested
     * objects and arrays are returned unchanged as Jackson {@link JsonNode}s, so the
     * test method can declare a {@code JsonNode} parameter for that column and
     * navigate it (for example {@code node.get("address").get("city").asText()}).
     * Fails the test if the file is missing, unreadable, invalid or is not a
     * non-empty array of objects.
     *
     * @param folderPath The subfolder within test-data.
     * @param fileName   The JSON file name.
     * @return 2D Object array of JSON data.
     */
    public Object[][] jsonReader(String folderPath, String fileName) {

        // Prepares the file path. Also sanitizes the path using .normalize() method.
        String path = Paths.get("src", "test", "resources", "test-data", folderPath, fileName).normalize().toString();

        try {
            JsonNode root = new ObjectMapper().readTree(new File(path));

            // Checks the file is a non-empty array whose first element is an object.
            if (root == null || !root.isArray() || root.isEmpty() || !root.get(0).isObject()) {
                String errorMessage = "The test data JSON file is empty or is not an array of objects. Folder path: "
                        + folderPath + ". File name: " + fileName + ".";
                AllureWrapper.step(errorMessage);
                Assert.fail(errorMessage);
            }

            // The column order is taken from the keys of the first object.
            List<String> columns = new ArrayList<>();
            root.get(0).fieldNames().forEachRemaining(columns::add);

            int rowCount = root.size();
            int columnCount = columns.size();
            Object[][] data = new Object[rowCount][columnCount];
            for (int rowIteration = 0; rowIteration < rowCount; rowIteration++) {
                JsonNode rowNode = root.get(rowIteration);
                for (int columnIteration = 0; columnIteration < columnCount; columnIteration++) {
                    JsonNode value = rowNode.get(columns.get(columnIteration));
                    if (value == null || value.isNull()) {
                        // Missing or null values are read as empty strings, like the other readers.
                        data[rowIteration][columnIteration] = "";
                    } else if (value.isContainerNode()) {
                        // Nested objects and arrays are passed on as they are.
                        data[rowIteration][columnIteration] = value;
                    } else {
                        data[rowIteration][columnIteration] = value.asText().trim();
                    }
                }
            }
            return data;

        } catch (IOException ioException) {
            // Jackson's parse exceptions are IOExceptions, so this covers a missing file,
            // an unreadable file and invalid JSON.
            String errorMessage = "The test data JSON file does not exist, can't be read or is not valid JSON. Folder path: "
                    + folderPath + ". File name: " + fileName + ".";
            AllureWrapper.step(errorMessage);
            Assert.fail(errorMessage, ioException);
        }

        // This code should be inaccessible. An error is placed in case of any
        // unexpected scenarios.
        String errorMessage = "An unexpected error appeared while reading test data JSON file. Folder path: "
                + folderPath + ". File name: " + fileName + ".";
        AllureWrapper.step(errorMessage);
        Assert.fail(errorMessage);
        return null;
    }

    /**
     * Reads a JSON file like {@link #jsonReader(String, String)} and runs every
     * object once for each browser enabled in configuration.properties. The
     * browser name is added as the first column, so a test method takes the
     * browser as its first parameter followed by the JSON values.
     *
     * @param testBase   the TestBase whose configuration decides the browsers
     * @param folderPath The subfolder within test-data.
     * @param fileName   The JSON file name.
     * @return 2D Object array with one row per JSON object per enabled browser
     */
    public Object[][] jsonReaderWithBrowser(TestBase testBase, String folderPath, String fileName) {
        return addBrowserColumn(testBase, jsonReader(folderPath, fileName));
    }

    /**
     * Repeats every row of the given data once per enabled browser, with the
     * browser name added as the first column.
     */
    private Object[][] addBrowserColumn(TestBase testBase, Object[][] csvData) {
        // Check which browsers are to be executed according to the
        // configuration.properties file.
        ArrayList<String> browserList = new ArrayList<String>();
        if (testBase.configuration.browserBrowserExecutionChrome) {
            browserList.add("chrome");
        }
        if (testBase.configuration.browserBrowserExecutionFirefox) {
            browserList.add("firefox");
        }
        if (testBase.configuration.browserBrowserExecutionEdge) {
            browserList.add("edge");
        }
        if (testBase.configuration.browserBrowserExecutionSafari) {
            browserList.add("safari");
        }

        // Takes the size of plain CSV data and size of an object array to be created.
        int oldRowCount = csvData.length;
        int newRowCount = browserList.size() * oldRowCount;
        int oldColumnCount = csvData[0].length;
        int newColumnCount = oldColumnCount + 1;
        Object[][] dataWithBrowsers = new Object[newRowCount][newColumnCount];

        // Creates a new object array with browser names to be executed in the 1st
        // column. Additional rows / iterations are created for each browser to run.
        int currentRow = 0;
        for (int rowIteration = 0; rowIteration < oldRowCount; rowIteration++) {
            for (String browserName : browserList) {
                dataWithBrowsers[currentRow][0] = browserName;
                // Copy the CSV columns after the browser name.
                for (int columnIteration = 0; columnIteration < oldColumnCount; columnIteration++) {
                    dataWithBrowsers[currentRow][columnIteration + 1] = csvData[rowIteration][columnIteration];
                }
                currentRow++;
            }
        }

        return dataWithBrowsers;
    }

}
