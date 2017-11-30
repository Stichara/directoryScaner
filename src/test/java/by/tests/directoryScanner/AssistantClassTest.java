package by.tests.directoryScanner;


import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

//@RunWith(MockitoJUnitRunner.class)
public class AssistantClassTest {

    private AssistantClass assistantClass;

    @Before
    public void setUp(){
        assistantClass = AssistantClass.getAssistent();
    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void testGetCommand() throws Exception{
        Optional<String> inputLine = Optional.of("scan -key1 value1 -key2 value2");
        Optional<String> command = assistantClass.getCommand(inputLine);
        Assert.assertTrue(command.isPresent());
        Assert.assertEquals("scan",command.get());
    }

    @Test
    public void testGetOptions() throws Exception{
        Optional<String> inputLine = Optional.of("scan -key1 value1 -key2 value2");
        Map<String, String> mapOptions = assistantClass.getOptions(inputLine);
        Assert.assertFalse(mapOptions.isEmpty());
        Assert.assertEquals(mapOptions.size(),2);
        mapOptions.entrySet()
                .stream()
                .forEach(s->{
                    Assert.assertFalse(s.getKey().isEmpty());
                    Assert.assertTrue(s.getKey().startsWith("key"));
                    Assert.assertFalse(s.getValue().isEmpty());
                    Assert.assertTrue(s.getValue().startsWith("value"));
                });
    }

    @Test
    public void testGetLocaleOption() throws Exception{
        String inputLine = new String("-locale=en");
        Optional<String> localeKey = assistantClass.getLocaleOption(inputLine);
        Assert.assertTrue(localeKey.isPresent());
        Assert.assertEquals("locale",localeKey.get());
    }

    @Test
    public void testGetLocaleOptionValue() throws Exception{
        String inputLine = new String("-locale=en");
        Optional<Locale> locale = assistantClass.getLocaleOptionValue(inputLine);
        Assert.assertTrue(locale.isPresent());
        Assert.assertEquals("en",locale.get().getLanguage());
    }







}


