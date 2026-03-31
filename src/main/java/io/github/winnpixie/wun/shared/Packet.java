package io.github.winnpixie.wun.shared;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class Packet {
    protected Packet(ByteBuffer buffer) throws IOException {
    }

    protected Packet() {
    }

    public abstract void serialize(ByteBuffer buffer) throws IOException;
}
