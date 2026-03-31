package io.github.winnpixie.wun.shared.packets.server;

import io.github.winnpixie.wun.shared.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ServerConfigurePeerPacket extends Packet {
    private final int newPeerId;
    private final long heartBeatInterval;

    public ServerConfigurePeerPacket(DataInputStream input) throws IOException {
        this.newPeerId = input.readInt();
        this.heartBeatInterval = input.readLong();
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
    public void serialize(DataOutputStream output) throws IOException {
        output.writeInt(newPeerId);
        output.writeLong(heartBeatInterval);
    }
}
