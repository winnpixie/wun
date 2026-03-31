package io.github.winnpixie.wun.shared.packets.client;

import io.github.winnpixie.wun.shared.Packet;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ClientHeartbeatPacket extends Packet {
    public ClientHeartbeatPacket(ByteBuffer buffer) throws IOException {
    }

    public ClientHeartbeatPacket() {
    }

    @Override
    public void serialize(ByteBuffer buffer) throws IOException {
    }
}
