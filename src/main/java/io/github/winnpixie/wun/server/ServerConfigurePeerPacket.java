package io.github.winnpixie.wun.server;

import io.github.winnpixie.wun.shared.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ServerConfigurePeerPacket extends Packet {
    private final long heartBeatInterval;
    private final long newPeerId;

    public ServerConfigurePeerPacket(long heartBeatInterval, long newPeerId) {
        this.heartBeatInterval = heartBeatInterval;
        this.newPeerId = newPeerId;
    }

    public long getHeartBeatInterval() {
        return heartBeatInterval;
    }

    public long getNewPeerId() {
        return newPeerId;
    }

    @Override
    public void serialize(DataOutputStream output) throws IOException {
        output.writeLong(heartBeatInterval);
        output.writeLong(newPeerId);
    }

    public static ServerConfigurePeerPacket deserialize(DataInputStream input) throws IOException {
        return new ServerConfigurePeerPacket(
                input.readLong(),
                input.readLong());
    }
}
