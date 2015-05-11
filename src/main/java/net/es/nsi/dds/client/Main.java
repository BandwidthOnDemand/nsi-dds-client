package net.es.nsi.dds.client;

import asg.cliche.ShellFactory;
import java.io.IOException;
import java.net.URL;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

/**
 *
 * @author hacksaw
 */
public class Main {

    public static void main(String[] args) {
        // Create Options object to hold our command line options.
        CommandLineOptions options = new CommandLineOptions(args);
        try {
            options.parse();
            URL ddsURL = options.getDdsUrl();
            boolean isDebug = options.isDebug();

            if (options.isShell()) {
                ShellFactory.createConsoleShell("dds",
                    "Welcome to the DDS command shell.  Enter ?list for available commands.",
                    new Root(ddsURL, isDebug)).commandLoop();
            }
            else {
                // Invoke command directly.
                Command inCommand = options.getCommand();
                String resource = options.getResource(inCommand);

                DdsClient command = new DdsClient(ddsURL, isDebug);
                command.invoke(inCommand, resource);
            }
        }
        catch (IOException | IllegalArgumentException ex) {
            System.err.println(ex);
            exitWithError(options.getOptions());
        }
    }

    static void exitWithError(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar ddscmd.jar -dds <dds server url> ...", options);
        System.exit(1);
    }
}
