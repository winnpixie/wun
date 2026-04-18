package io.github.winnpixie.wun.shared.packets.server;

import io.github.winnpixie.wun.shared.IOHelper;
import io.github.winnpixie.wun.shared.Packet;

import java.nio.ByteBuffer;

public class ServerDisconnectPacket extends Packet {
    private final String reason;

    public ServerDisconnectPacket(ByteBuffer buffer) {
        this.reason = IOHelper.getString(buffer);
    }

    public ServerDisconnectPacket(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public void serialize(ByteBuffer buffer) {
        IOHelper.putString(buffer, reason);
    }
}
