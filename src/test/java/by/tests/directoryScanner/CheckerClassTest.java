package by.tests.directoryScanner;


import by.tests.directoryScanner.Exceptions.InvalidLocaleException;
import by.tests.directoryScanner.Exceptions.SyntaxException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class CheckerClassTest {

    private CheckerClass checkerClass;

    @Rule
    public ExpectedException thrown = ExpectedException.none();


    @Before
    public void setUp() {
        checkerClass = CheckerClass.getCheckerClass();
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testCheckCommandValidate() throws Exception {

        Optional<String> inputLine = Optional.of("scan");
        checkerClass.checkCommandValidate(inputLine);
        inputLine = Optional.of("exit");
        checkerClass.checkCommandValidate(inputLine);
        inputLine = Optional.of("skip");
        checkerClass.checkCommandValidate(inputLine);
        inputLine = Optional.of("showScans");
        checkerClass.checkCommandValidate(inputLine);
        inputLine = Optional.of("closeScan");
        checkerClass.checkCommandValidate(inputLine);
        inputLine = Optional.of("failCommand");

        thrown.expect(SyntaxException.class);
        checkerClass.checkCommandValidate(inputLine);
    }

    @Test
    public void testCheckOptionsValidate() throws Exception {
        Optional<String> command = Optional.of("scan");
        Map<String, String> options = new HashMap<>();

        thrown.expect(SyntaxException.class);
        checkerClass.checkOptionsValidate(command, options);

        options.put("inputDir", "path");
        thrown.expect(SyntaxException.class);
        checkerClass.checkOptionsValidate(command, options);

        options.put("outputDir", "path2");
        checkerClass.checkOptionsValidate(command, options);

        options.put("waitInterval", "drfjdff");

        thrown.expect(SyntaxException.class);
        checkerClass.checkOptionsValidate(command, options);

        options.put("waitInterval", "-600");

        thrown.expect(SyntaxException.class);
        checkerClass.checkOptionsValidate(command, options);

        options.put("waitInterval", "600");
        options.put("mask", ".*");
        options.put("includeSubfolders", "yes");
        options.put("autoDelete", "true");
        checkerClass.checkOptionsValidate(command, options);

    }

    @Test
    public void testCheckLocaleOption() throws Exception {
        Optional<String> inputLine = Optional.of("locale");
        checkerClass.checkLocaleOption(inputLine);


        inputLine = Optional.of("incorelocale");
        thrown.expect(InvalidLocaleException.class);
        checkerClass.checkLocaleOption(inputLine);

    }

    @Test
    public void testCheckLocaleOptionValue() throws Exception {
        Optional locale = Optional.of(new Locale("en"));
        checkerClass.checkLocaleOptionValue(locale);

        locale = Optional.of(new Locale("xx"));
        thrown.expect(InvalidLocaleException.class);
        checkerClass.checkLocaleOptionValue(locale);
    }


}
