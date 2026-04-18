package io.github.winnpixie.wun.shared.packets.server;

import io.github.winnpixie.wun.shared.Packet;

import java.nio.ByteBuffer;

public class ServerConfigurationPacket extends Packet {
    private final int heartBeatInterval;

    public ServerConfigurationPacket(ByteBuffer buffer) {
        this.heartBeatInterval = buffer.getInt();
    }

    public ServerConfigurationPacket(int heartBeatInterval) {
        this.heartBeatInterval = heartBeatInterval;
    }

    public int getHeartBeatInterval() {
        return heartBeatInterval;
    }

    @Override
    public void serialize(ByteBuffer buffer) {
        buffer.putInt(heartBeatInterval);
    }
}
