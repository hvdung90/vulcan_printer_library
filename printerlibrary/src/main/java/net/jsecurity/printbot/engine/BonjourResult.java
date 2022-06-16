package net.jsecurity.printbot.engine;

import java.net.InetAddress;

class BonjourResult {
    private int port;
    private String rp;
    private InetAddress source;
    private String type;

    public BonjourResult(InetAddress source2, int port2, String rp2, String type2) {
        this.source = source2;
        this.port = port2;
        this.rp = rp2;
        this.type = type2;
    }

    public InetAddress getSource() {
        return this.source;
    }

    public int getPort() {
        return this.port;
    }

    public String getRp() {
        return this.rp;
    }

    public String getType() {
        return this.type;
    }
}
