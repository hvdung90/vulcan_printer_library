package net.jsecurity.printbot.model;

import java.util.List;
import net.jsecurity.printbot.UIUtil;
import net.jsecurity.printbot.prefs.SettingsHelper;

public class PrintBotInfo {
    private String bonjourKey;
    private String driver;
    private String host;
    private int ix;
    private String manufacturer;
    private String pageSize;
    private String password;
    private boolean portrait;
    private String protocol;
    private String queue;
    private String resolution;
    private List<KeyValuePair> resolutions;
    private String user;

    public PrintBotInfo() {
        this.pageSize = GUIConstants.PAGE_SIZES[0];
        this.portrait = true;
        this.ix = -1;
    }

    public PrintBotInfo(int ix2) {
        this.pageSize = GUIConstants.PAGE_SIZES[0];
        this.portrait = true;
        this.ix = ix2;
    }

    public List<KeyValuePair> getResolutions() {
        return this.resolutions;
    }

    public void setResolutions(List<KeyValuePair> resolutions2) {
        this.resolutions = resolutions2;
    }

    public void setProtocol(String protocol2) {
        this.protocol = protocol2;
    }

    public void setHost(String host2) {
        this.host = host2;
    }

    public void setQueue(String queue2) {
        this.queue = queue2;
    }

    public void setManufacturer(String manufacturer2) {
        this.manufacturer = manufacturer2;
    }

    public void setDriver(String driver2) {
        this.driver = driver2;
    }

    public void setResolution(String resolution2) {
        this.resolution = resolution2;
    }

    public void setUser(String user2) {
        this.user = user2;
    }

    public String getNetworkName() {
        if (GUIConstants.PROTOCOL_BONJOUR.equals(this.protocol)) {
            return SettingsHelper.getBonjourName(this.bonjourKey);
        }
        return UIUtil.formatManufacturer(this.manufacturer) + " " + UIUtil.formatModel(this.driver) + " @ " + this.host;
    }

    public String getNetworkUrl() {
        if (this.protocol == null) {
            return "-";
        }
        if (GUIConstants.PROTOCOL_BONJOUR.equals(this.protocol)) {
            return "zeroconf:" + getBonjourKey();
        }
        if (!GUIConstants.PROTOCOL_IPP.equals(this.protocol)) {
            return this.protocol + "://" + this.host + "/" + this.queue;
        }
        String hostPort = this.host;
        if (hostPort.indexOf(58) < 0) {
            hostPort = hostPort + ":631";
        }
        if (this.queue == null || !this.queue.startsWith("/")) {
            return this.protocol + "://" + hostPort + "/" + GUIConstants.IPP_PATH + this.queue;
        }
        return this.protocol + "://" + hostPort + this.queue;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public String getHost() {
        return this.host;
    }

    public String getQueue() {
        return this.queue;
    }

    public String getUser() {
        return this.user;
    }

    public String getPassword() {
        return this.password;
    }

    public String getManufacturer() {
        return this.manufacturer;
    }

    public String getDriver() {
        return this.driver;
    }

    public String getResolution() {
        return this.resolution;
    }

    public void setPageSize(String pageSize2) {
        this.pageSize = pageSize2;
    }

    public String getPageSize() {
        return this.pageSize;
    }

    public boolean isPortrait() {
        return this.portrait;
    }

    public void setPortrait(boolean portrait2) {
        this.portrait = portrait2;
    }

    public String getBonjourKey() {
        return this.bonjourKey;
    }

    public void setBonjourKey(String bonjourKey2) {
        this.bonjourKey = bonjourKey2;
    }

    public void setPassword(String password2) {
        this.password = password2;
    }

    public int getIndex() {
        if (this.ix >= 0) {
            return this.ix;
        }
        throw new IllegalArgumentException("Printer index not set");
    }

    public String toString() {
        return "PrinterInfo [protocol=" + this.protocol + ", network=" + getNetworkName() + ", driver=" + this.driver + ", resolution=" + this.resolution + "]";
    }
}
