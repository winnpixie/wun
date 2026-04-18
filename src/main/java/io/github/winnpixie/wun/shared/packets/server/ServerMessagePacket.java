package io.github.winnpixie.wun.shared.packets.server;

import io.github.winnpixie.wun.shared.IOHelper;
import io.github.winnpixie.wun.shared.Packet;

import java.nio.ByteBuffer;

public class ServerMessagePacket extends Packet {
    private final String message;

    public ServerMessagePacket(ByteBuffer buffer) {
        this.message = IOHelper.getString(buffer);
    }

    public ServerMessagePacket(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void serialize(ByteBuffer buffer) {
        IOHelper.putString(buffer, message);
    }
}
