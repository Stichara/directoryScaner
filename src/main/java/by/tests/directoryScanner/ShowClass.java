package by.tests.directoryScanner;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class ShowClass {

    static private ShowClass himself;
    private ResourceBundle messages ;

    private ShowClass(){
        messages = ResourceBundle.getBundle("messages", Locale.getDefault());
    }

    /**
     * pattern singleton
     * @return
     */
    public static ShowClass getShow(){
        if (himself == null) himself = new ShowClass();
        return himself;
    }

    /**
     * method reload locale for correct messages
     */
    public void reloadLocale(){
        messages = ResourceBundle.getBundle("messages", Locale.getDefault());
    }

    /**
     * Method write message in stdout
     * @param message
     */
    public void writeMessage(String message){
        System.out.println("Directory Scaner: " + message);
    }

    /**
     * method read user input
     * @return
     */
    public Optional<String> readInputData(){

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        Optional<String> command;
        try {
            command = Optional.ofNullable(reader.readLine());
        } catch (IOException e) {
            command = Optional.empty();
        }
        return command;
    }

    /**
     * welcome message
     */
    public void Hello(){
        writeMessage(messages.getString("messages.display.hello"));
    }

    /**
     *  command request
     */
    public void suggestEnterCommand(){
        writeMessage(messages.getString("messages.display.wait.command"));
    }



}
