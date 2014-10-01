/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.es.nsi.dds.client;

import java.net.MalformedURLException;
import java.net.URL;
import asg.cliche.ShellFactory;
import java.io.IOException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author hacksaw
 */
public class DdsClient {
    public static final String DDS_SERVER = "dds";
    public static final String DEBUG = "debug";

    public static void main(String[] args) {
        // Create Options object to hold our command line options.
        Options options = new Options();

        // Configuration directory option.
        Option ddsServer = new Option(DDS_SERVER, true, "DDS server URL.");
        ddsServer.setRequired(true);
        options.addOption(ddsServer);

        // Configuration directory option.
        Option debug = new Option(DEBUG, false, "Enable debug.");
        options.addOption(debug);

        // Parse the command line options.
        CommandLineParser parser = new GnuParser();

        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException pe) {
            System.err.println("Error: You did not provide the correct arguments.");
            exitWithError(options);
            return;
        }

        String url = cmd.getOptionValue(DDS_SERVER);
        URL ddsURL;
        if(url == null) {
            System.err.println("Error: You must provide a DDS server URL.");
            exitWithError(options);
            return;
        }
        else {
            url = url.trim();
            try {
                ddsURL = new URL(url);
            } catch (MalformedURLException me) {
                System.err.println("Error: Malformed DDS URL.");
                exitWithError(options);
                return;
            }
        }

        boolean isDebug = cmd.hasOption(DEBUG);

        try {
            ShellFactory.createConsoleShell("dds", "Welcome to the DDS command shell.  Enter ?list for available commands.", new DdsCommands(ddsURL, isDebug)).commandLoop();
        } catch (IOException ex) {
            System.err.println("Error: Command shell failure.");
            exitWithError(options);
        }
    }

    static void exitWithError(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar ddscmd.jar -dds <dds server url>", options);
        System.exit(1);
    }
}
