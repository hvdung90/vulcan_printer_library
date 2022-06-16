package net.jsecurity.printbot.chooser;

public class Option implements Comparable<Option> {
    private String data;
    private String name;
    private String path;
    private String type;

    public Option(String n, String d, String p, String t) {
        this.name = n;
        this.data = d;
        this.path = p;
        this.type = t;
    }

    public String getName() {
        return this.name;
    }

    public String getData() {
        return this.data;
    }

    public String getPath() {
        return this.path;
    }

    public String getType() {
        return this.type;
    }

    public int compareTo(Option o) {
        if (this.name != null) {
            return this.name.toLowerCase().compareTo(o.getName().toLowerCase());
        }
        throw new IllegalArgumentException();
    }
}
