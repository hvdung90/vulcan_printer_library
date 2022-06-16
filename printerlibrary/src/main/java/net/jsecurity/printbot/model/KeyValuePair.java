package net.jsecurity.printbot.model;

public class KeyValuePair {
    private String key;
    private String value;

    public KeyValuePair(String key2, String value2) {
        this.key = key2;
        this.value = value2;
    }

    public String getKey() {
        return this.key;
    }

    public String toString() {
        return this.value;
    }
}
