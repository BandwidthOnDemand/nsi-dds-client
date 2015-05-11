/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.es.nsi.dds.client;

import com.google.common.base.Strings;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author hacksaw
 */
public class CommandLineOptions {
    public static final String SERVER = "server";
    public static final String SHELL = "shell";
    public static final String DEBUG = "debug";

    private CommandLine clp;
    private final Options commandOptions;
    private final String[] args;

    public CommandLineOptions(String[] args) {
        this.args = args;
        commandOptions = getCommandOptions();
    }

    public void parse() throws IllegalArgumentException {
        // Parse the command line options.
        CommandLineParser parser = new GnuParser();

        try {
            clp = parser.parse(commandOptions, args);
        } catch (ParseException pe) {
            System.err.println("Error: You did not provide the correct arguments.");
            throw new IllegalArgumentException("Error: You did not provide the correct arguments.");
        }
    }

    private Options getCommandOptions() {
        // Create Options object to hold our command line options.
        Options options = new Options();

        // Configuration directory option.
        Option ddsServer = new Option(SERVER, true, "DDS server URL.");
        ddsServer.setRequired(true);
        options.addOption(ddsServer);

        Option shell = new Option(SHELL, false, "Start in interactive shell mode.");
        options.addOption(shell);

        // Command line options.
        for (Command command : Command.values()) {
            Option opt = new Option(command.getName(), true, command.getDescription());
            options.addOption(opt);
        }

        // Configuration directory option.
        Option debug = new Option(DEBUG, false, "Enable debug.");
        options.addOption(debug);

        return options;
    }

    public Options getOptions() {
        return commandOptions;
    }

    public URL getDdsUrl() throws IllegalArgumentException {
        // Get the DDS server URL.
        String url = clp.getOptionValue(SERVER);
        URL ddsURL;
        if(url == null) {
            System.err.println("Error: You must provide a DDS server URL.");
            throw new IllegalArgumentException("Error: You must provide a DDS server URL.");
        }
        else {
            url = url.trim();
            try {
                ddsURL = new URL(url);
            } catch (MalformedURLException me) {
                System.err.println("Error: Malformed DDS URL.");
                throw new IllegalArgumentException("Error: Malformed DDS URL.");
            }
        }

        return ddsURL;
    }

    public boolean isShell() {
        return clp.hasOption(SHELL);
    }

    public boolean isDebug() {
        return clp.hasOption(DEBUG);
    }

    public Command getCommand() throws IllegalArgumentException {
        for (Command command : Command.values()) {
            boolean result = clp.hasOption(command.getName());
            if (result) {
                return command;
            }
        }

        System.err.println("No command option provided");
        throw new IllegalArgumentException("No command option provided");
    }

    public String getResource(Command command) throws IllegalArgumentException {
        String result = clp.getOptionValue(command.getName());
        if (Strings.isNullOrEmpty(result)) {
            System.err.println("No command option provided");
            throw new IllegalArgumentException("No command option provided");
        }

        return result;
    }
}
