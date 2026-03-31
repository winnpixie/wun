package io.github.winnpixie.wun.shared;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Packet {
    protected Packet(DataInputStream input) throws IOException {
    }

    protected Packet() {
    }

    public abstract void serialize(DataOutputStream output) throws IOException;
}
