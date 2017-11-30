package by.tests.directoryScanner;

import by.tests.directoryScanner.enums.ConstantsEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AssistantClass {

    private static AssistantClass himself;

    private AssistantClass() {
    }

    public static AssistantClass getAssistent() {
        if (himself == null) himself = new AssistantClass();
        return himself;
    }

    /**
     * synchronized method of obtaining a logger
     * @return - logger object
     */
    synchronized static public Logger getLogger(String name){
        return LogManager.getLogger(name);
    }

    /**
     * method return only command
     *
     * @param line user-entered data
     * @return Option object with command
     */
    public Optional getCommand(Optional<String> line) {
        return Arrays.stream(line.get().trim().split(" ")).findFirst();
    }

    /**
     * create map with input options and value
     *
     * @param line user-entered data
     * @return Map with options
     */
    public Map<String, String> getOptions(Optional<String> line) {
        return Arrays.stream(line.get().trim().split(" -"))
                .filter(s -> !s.isEmpty())
                .skip(1)
                .map(s -> s.split(" "))
                .collect(
                        Collectors.toMap(
                                e -> e[0].trim(), e -> e.length > 1 ? e[1].trim() : ConstantsEnum.empty.name()
                        )
                );

    }

    /**
     * get first option from command line at startup
     * @param line
     * @return Optional with String
     */
    public Optional<String> getLocaleOption(String line) {
        return Arrays.stream(line.substring(1,line.length()).split("="))
                .findFirst();
    }

    /**
     * get locale value
     * @param line
     * @return Optional with Locale.class or with null
     */
    public Optional<Locale> getLocaleOptionValue(String line) {
        return Arrays.stream(line.split("="))
                .skip(1)
                .map(s -> new Locale(s))
//                .map(s -> {
//                    Stream stream = Arrays.stream(s.split(" "));
//                    return new Locale((String) stream.findFirst().orElse("")
//                            ,(String) stream.findAny().orElse(""));
//                })
                .findFirst();
    }
}
