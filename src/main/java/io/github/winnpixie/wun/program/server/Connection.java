package io.github.winnpixie.wun.program.server;

import io.github.winnpixie.wun.shared.Peer;

public class Connection {
    private final Peer peer;

    private long heartBeat;

    public Connection(Peer peer) {
        this.peer = peer;

        this.heartBeat = System.currentTimeMillis();
    }

    public Peer getPeer() {
        return peer;
    }

    public long getHeartBeat() {
        return heartBeat;
    }

    public void setHeartBeat(long heartbeat) {
        this.heartBeat = heartbeat;
    }
}
