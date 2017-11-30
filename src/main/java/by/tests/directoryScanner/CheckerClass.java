package by.tests.directoryScanner;

import by.tests.directoryScanner.Exceptions.InvalidLocaleException;
import by.tests.directoryScanner.Exceptions.SyntaxException;
import by.tests.directoryScanner.enums.CommandsEnum;
import by.tests.directoryScanner.enums.ConstantsEnum;
import by.tests.directoryScanner.enums.LocaleEnum;
import by.tests.directoryScanner.enums.OptionsEnum;

import java.util.*;
import java.util.stream.Collectors;

public class CheckerClass {

    private static CheckerClass himself;
    private ResourceBundle messages;

    private CheckerClass() {
        messages = ResourceBundle.getBundle("messages", Locale.getDefault());
    }

    /**
     * singleton
     *
     * @return
     */
    public static CheckerClass getCheckerClass() {
        if (himself == null) himself = new CheckerClass();
        return himself;
    }

    /**
     * method reload locale for correct messages
     */
    public void reloadLocale() {
        messages = ResourceBundle.getBundle("messages", Locale.getDefault());
    }

    /**
     * command validation
     *
     * @param command
     * @throws SyntaxException if user entered not valid data with description
     */
    public void checkCommandValidate(Optional<String> command) throws SyntaxException {
        command
                .filter(c -> {
                    for (CommandsEnum s : CommandsEnum.values()) {
                        if (s.name().equals(c)) return true;
                    }
                    return false;
                }).orElseThrow(() -> new SyntaxException(messages.getString("messages.verification.command.notcorrect")))
        ;
    }

    /**
     * options validation on empty value and incorrect option name
     *
     * @param command
     * @param options
     * @throws SyntaxException
     */
    public void checkOptionsValidate(Optional<String> command, Map<String, String> options) throws SyntaxException {
        // find options without value
        List<String> optionsWithoutValue = options.entrySet().stream()
                .filter(entry -> ConstantsEnum.empty.name().equals(entry.getValue()))
                .map(entry -> entry.getKey())
                .collect(Collectors.toList());
        // notification of the user about empty options
        checkExistenceOfInvalidData(optionsWithoutValue, ConstantsEnum.empty.name());

        //options validation
        List<String> invalidOptions = options.entrySet().stream()
                .map(entry -> entry.getKey())
                .filter(option -> {
                    for (OptionsEnum s : OptionsEnum.values()) {
                        if (s.name().equals(option)) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
        // notification of the user about invalid options
        checkExistenceOfInvalidData(invalidOptions, ConstantsEnum.invalid.name());

        //check availability of necessary options for scanner
        if (command.filter(s -> CommandsEnum.scan.name().equals(s)).isPresent()) {
            // Is there an inputDir option among options
            options.entrySet().stream()
                    .filter(entry -> OptionsEnum.inputDir.name().equals(entry.getKey()))
                    .findFirst()
                    .orElseThrow(() -> new SyntaxException(messages.getString("messages.verification.inputdir.notexist")));

            // Is there an outputDir option among options
            options.entrySet().stream()
                    .filter(entry -> OptionsEnum.outputDir.name().equals(entry.getKey()))
                    .findFirst()
                    .orElseThrow(() -> new SyntaxException(messages.getString("messages.verification.outputdir.notexist")));
        }

        // check value waitInterval option
        if (options.containsKey(OptionsEnum.waitInterval.name())) {
            Optional<String> value = Optional.ofNullable(options.get(OptionsEnum.waitInterval.name()));
            value.orElseThrow(() -> new SyntaxException(messages.getString("messages.verification.options.waitinternal.notcorrect")));
            try {
                if (Long.parseLong(value.get()) < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                throw new SyntaxException(
                        messages.getString("messages.verification.options.waitinternal.notcorrect")
                );
            }
        }

        //check availability of necessary options for close scanner
        if (command.filter(s -> CommandsEnum.closeScan.name().equals(s)).isPresent()) {
            // Is there an scanner`s id option among options
            options.entrySet().stream()
                    .filter(entry -> OptionsEnum.idScan.name().equals(entry.getKey()))
                    .map(s -> s.getValue())
                    .map(s -> {
                        try {
                            long i = Long.parseLong(s);
                            if (i > 0) return i;
                        } catch (NumberFormatException e) {
                        }
                        return Optional.empty();
                    })
                    .findFirst()
                    .orElseThrow(() -> new SyntaxException(messages.getString("messages.verification.idsanner.notcorrect")));
        }
    }

    /**
     * method notification of the user about invalid or empty options
     *
     * @param list should be checked
     * @param text type of check for users message
     * @throws SyntaxException
     */
    private void checkExistenceOfInvalidData(List list, String text) throws SyntaxException {
        if (!list.isEmpty()) {
            throw new SyntaxException(messages.getString("messages.verification.options.notcorrect") + text + ": "
                    + list.stream()
                    .map(Objects::toString)
                    .collect(Collectors.joining(", ")));
        }
    }

    /**
     * validation of locale option
     *
     * @param line - option
     * @throws InvalidLocaleException
     */
    public void checkLocaleOption(Optional<String> line) throws InvalidLocaleException {
        line.filter(s -> OptionsEnum.locale.name().equals(s))
                .orElseThrow(() -> new InvalidLocaleException(messages.getString("messages.verification.locale.notcorrect")));
    }

    /**
     * validation of locale option value
     *
     * @param locale
     * @throws InvalidLocaleException
     */
    public void checkLocaleOptionValue(Optional<Locale> locale) throws InvalidLocaleException {
        locale.filter(s -> {
            for (LocaleEnum localeEnum : LocaleEnum.values()) {
                if (localeEnum.name().equals(s.getLanguage())) return true;
            }
            return false;
        }).orElseThrow(() -> new InvalidLocaleException(messages.getString("messages.verification.locale.defoult")));
    }

}
