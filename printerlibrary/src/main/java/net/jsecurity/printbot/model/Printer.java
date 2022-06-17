package net.jsecurity.printbot.model;

public class Printer {
    private String id;
    private String name;
    private String ipAddress;

    public Printer() {
        this.id = "";
        this.name = "";
        this.ipAddress = "";
    }

    public Printer(String id, String name, String ipAddress) {
        this.id = id;
        this.name = name;
        this.ipAddress = ipAddress;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
