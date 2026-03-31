package io.github.winnpixie.wun.shared.packets.server;

import io.github.winnpixie.wun.shared.IOHelper;
import io.github.winnpixie.wun.shared.Packet;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ServerDisconnectPacket extends Packet {
    private final String reason;

    public ServerDisconnectPacket(ByteBuffer buffer) throws IOException {
        this.reason = IOHelper.getString(buffer);
    }

    public ServerDisconnectPacket(String reason) {
        if (reason.length() > 1024) {
            reason = reason.substring(0, 1024);
        }

        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public void serialize(ByteBuffer buffer) throws IOException {
        IOHelper.putString(buffer, reason);
    }
}
