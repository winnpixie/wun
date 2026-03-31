package io.github.winnpixie.wun.shared.packets.server;

import io.github.winnpixie.wun.shared.IOHelper;
import io.github.winnpixie.wun.shared.Packet;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ServerMessagePacket extends Packet {
    private final String message;

    public ServerMessagePacket(ByteBuffer buffer) throws IOException {
        this.message = IOHelper.getString(buffer);
    }

    public ServerMessagePacket(String message) {
        if (message.length() > 1024) {
            message = message.substring(0, 1024);
        }

        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void serialize(ByteBuffer buffer) throws IOException {
        IOHelper.putString(buffer, message);
    }
}
