package io.github.winnpixie.wun.shared;

import java.nio.ByteBuffer;

public abstract class Packet {
    protected Packet(ByteBuffer buffer) {
    }

    protected Packet() {
    }

    public abstract void serialize(ByteBuffer buffer);
}
