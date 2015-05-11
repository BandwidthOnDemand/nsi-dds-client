package net.es.nsi.dds.client;

/**
 *
 * @author hacksaw
 */
public enum Command {
    LS("ls", "List summary of resources."),
    LIST("list", "Detailed list summary of resources."),
    DETAILS("details", "Details of resources."),
    DELETE("delete", "Delete resource.");

    private final String name;
    private final String description;

    private Command(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
