package io.github.winnpixie.wun.shared.packets.client;

import io.github.winnpixie.wun.shared.IOHelper;
import io.github.winnpixie.wun.shared.Packet;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ClientMessagePacket extends Packet {
    private final String message;

    public ClientMessagePacket(ByteBuffer buffer) throws IOException {
        this.message = IOHelper.getString(buffer);
    }

    public ClientMessagePacket(String message) {
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
