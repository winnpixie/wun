package io.github.winnpixie.wun.program.server;

public class Connection {
    private String userName;
    private long heartBeat;

    Connection(String userName) {
        this.userName = userName;
        this.heartBeat = System.currentTimeMillis();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getHeartBeat() {
        return heartBeat;
    }

    public void setHeartBeat(long heartbeat) {
        this.heartBeat = heartbeat;
    }
}
