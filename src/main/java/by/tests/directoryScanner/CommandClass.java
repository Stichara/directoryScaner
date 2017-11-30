package by.tests.directoryScanner;

import by.tests.directoryScanner.Exceptions.ExitException;
import by.tests.directoryScanner.enums.CommandsEnum;
import by.tests.directoryScanner.enums.ConstantsEnum;
import by.tests.directoryScanner.enums.OptionsEnum;
import org.apache.logging.log4j.Logger;

import java.util.*;


public class CommandClass {
    private static CommandClass himself;
    private HashMap<String, Thread> pool = new HashMap<>();
    private ShowClass showClass;
    private long id = 0;
    private ResourceBundle messages;

    private CommandClass() {
        showClass = ShowClass.getShow();
        messages = ResourceBundle.getBundle("messages", Locale.getDefault());
    }

    public static CommandClass getAssistent() {
        if (himself == null) himself = new CommandClass();
        return himself;
    }

    /**
     * method is command
     *
     * @param command
     * @param options
     * @throws ExitException
     */
    public void executeCommand(Optional<String> command, Map<String, String> options) throws ExitException {

        if (CommandsEnum.exit.name().equals(command.get())) {
            closeAllScans();
            throw new ExitException();
        }
        if (CommandsEnum.scan.name().equals(command.get())) {
            scan(options);
        }
        if (CommandsEnum.showScans.name().equals(command.get())) {
            showScans();
        }
        if (CommandsEnum.closeScan.name().equals(command.get())) {
            closeScan(options);
        }

    }

    /**
     * method create scan thread
     *
     * @param options
     */
    public void scan(Map<String, String> options) {
        Runnable runnable = new ScanThread(options);
        Thread thread = new Thread(runnable, "Scan."+(++id));
        thread.start();
        if (options.containsKey(OptionsEnum.waitInterval.name())) {
            pool.put(options.get(OptionsEnum.inputDir.name()), thread);
        }
    }

    /**
     * method print all running scanners into stdout
     */
    private void showScans() {
        showClass.writeMessage(messages.getString("message.command.showscans"));
        pool.entrySet()
                .forEach(s -> showClass.writeMessage(s.getKey() + ": " + s.getValue().getId())  );
    }


    /**
     * method stops the selected scanner
     * @param options - key with id thread
     */
    private void closeScan(Map<String, String> options) {

        long idThread = Long.parseLong(options.get(OptionsEnum.idScan.name()));
        pool.entrySet().stream()
                .filter(s -> s.getValue().getId() == idThread)
                .findFirst()
                .ifPresent(s -> {
                    s.getValue().interrupt();
                    pool.remove(s.getKey());
                });
    }

    /**
     * method stops all scanners
     */
    private void closeAllScans(){
        pool.entrySet().
                forEach(s -> {
                    s.getValue().interrupt();
                    pool.remove(s.getKey());
                });
    }
}
