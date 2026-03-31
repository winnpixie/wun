package io.github.winnpixie.wun.shared.packets.client;

import io.github.winnpixie.wun.shared.Packet;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ClientDisconnectPacket extends Packet {
    public ClientDisconnectPacket(ByteBuffer buffer) throws IOException {
    }

    public ClientDisconnectPacket() {
    }

    @Override
    public void serialize(ByteBuffer buffer) throws IOException {
    }
}
