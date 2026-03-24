package io.github.winnpixie.wun.server;

import java.net.InetSocketAddress;

public class Peer {
    private final long id;
    private final InetSocketAddress address;

    private long heartBeat;

    public Peer(long id, InetSocketAddress address) {
        this.id = id;
        this.address = address;

        this.heartBeat = System.currentTimeMillis();
    }

    public long getId() {
        return id;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public long getHeartBeat() {
        return heartBeat;
    }

    public void setHeartBeat(long heartbeat) {
        this.heartBeat = heartbeat;
    }
}
