package io.github.winnpixie.wun.shared;

import java.io.IOException;
import java.nio.ByteBuffer;

@FunctionalInterface
public interface PacketDeserializer<R extends Packet> {
    R deserialize(ByteBuffer buffer) throws IOException;
}
