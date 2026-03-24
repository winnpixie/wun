package io.github.winnpixie.wun.shared;

import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Packet {
    private long peerId;

    public long getPeerId() {
        return peerId;
    }

    public void setPeerId(long peerId) {
        this.peerId = peerId;
    }

    public abstract void serialize(DataOutputStream output) throws IOException;
}
