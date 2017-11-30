package by.tests.directoryScanner;


import by.tests.directoryScanner.Exceptions.ExitException;
import by.tests.directoryScanner.Exceptions.InvalidLocaleException;
import by.tests.directoryScanner.Exceptions.SyntaxException;
import by.tests.directoryScanner.enums.ConstantsEnum;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;


public class Main {

    private static Logger logger = getLogger() ;

    private static Logger getLogger(){
        Thread.currentThread().setName("Scan."+ConstantsEnum.app);
        ThreadContext.put("scan", Thread.currentThread().getName() );
        return AssistantClass.getLogger(Thread.currentThread().getName());
    }

    public static void main(String[] args) {
        //loading the required classes
        ShowClass showClass = ShowClass.getShow();
        AssistantClass assistantClass = AssistantClass.getAssistent();
        CommandClass commandClass = CommandClass.getAssistent();
        CheckerClass checkerClass = CheckerClass.getCheckerClass();

        // verifies and sets up the user locale
        if (args.length > 0 ){
            Optional locale = assistantClass.getLocaleOption(args[0]);
            Optional localeValue = assistantClass.getLocaleOptionValue(args[0]);
            try {
                checkerClass.checkLocaleOption(locale);
                checkerClass.checkLocaleOptionValue(localeValue);
                Locale.setDefault((Locale) localeValue.get());
                checkerClass.reloadLocale();
                showClass.reloadLocale();
            } catch (InvalidLocaleException e) {
                showClass.writeMessage(e.getMessage());
            }
        }

        showClass.Hello();
        boolean exit = false;
        // execution of user commands
        do {
            showClass.suggestEnterCommand();
            Optional userInStream = showClass.readInputData();
            try {
                // definition and verification of the entered command and options
                Optional<String> command = assistantClass.getCommand(userInStream);
                Map options = assistantClass.getOptions(userInStream);
                checkerClass.checkCommandValidate(command);
                checkerClass.checkOptionsValidate(command, options);
                // execution
                commandClass.executeCommand(command, options);
            } catch (SyntaxException e) {
                showClass.writeMessage(e.getMessage());
            } catch (ExitException e){
                exit = true;
            }
        } while (!exit);

    }


}
