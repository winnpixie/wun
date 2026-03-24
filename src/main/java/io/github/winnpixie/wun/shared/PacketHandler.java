package io.github.winnpixie.wun.shared;

import java.io.DataInputStream;
import java.io.IOException;

@FunctionalInterface
public interface PacketHandler<R extends Packet> {
    R process(DataInputStream input) throws IOException;
}
