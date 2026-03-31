package io.github.winnpixie.wun.shared.packets.server;

import io.github.winnpixie.wun.shared.Packet;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ServerConfigurePeerPacket extends Packet {
    private final int newPeerId;
    private final long heartBeatInterval;

    public ServerConfigurePeerPacket(ByteBuffer buffer) throws IOException {
        this.newPeerId = buffer.getInt();
        this.heartBeatInterval = buffer.getLong();
    }

    public ServerConfigurePeerPacket(int newPeerId, long heartBeatInterval) {
        this.newPeerId = newPeerId;
        this.heartBeatInterval = heartBeatInterval;
    }

    public int getNewPeerId() {
        return newPeerId;
    }

    public long getHeartBeatInterval() {
        return heartBeatInterval;
    }

    @Override
    public void serialize(ByteBuffer buffer) throws IOException {
        buffer.putInt(newPeerId);
        buffer.putLong(heartBeatInterval);
    }
}
