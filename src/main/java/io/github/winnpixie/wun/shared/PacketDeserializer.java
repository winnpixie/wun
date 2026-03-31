package io.github.winnpixie.wun.shared;

import java.io.DataInputStream;
import java.io.IOException;

@FunctionalInterface
public interface PacketDeserializer<R extends Packet> {
    R deserialize(DataInputStream input) throws IOException;
}
