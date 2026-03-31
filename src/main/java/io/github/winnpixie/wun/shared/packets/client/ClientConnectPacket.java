package io.github.winnpixie.wun.shared.packets.client;

import io.github.winnpixie.wun.shared.Packet;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ClientConnectPacket extends Packet {
    public ClientConnectPacket(ByteBuffer buffer) throws IOException {
    }

    public ClientConnectPacket() {
    }

    @Override
    public void serialize(ByteBuffer buffer) throws IOException {
    }
}
