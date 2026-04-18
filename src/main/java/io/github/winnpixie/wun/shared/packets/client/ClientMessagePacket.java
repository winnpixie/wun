package io.github.winnpixie.wun.shared.packets.client;

import io.github.winnpixie.wun.shared.IOHelper;
import io.github.winnpixie.wun.shared.Packet;

import java.nio.ByteBuffer;

public class ClientMessagePacket extends Packet {
    private final String message;

    public ClientMessagePacket(ByteBuffer buffer) {
        this.message = IOHelper.getString(buffer);
    }

    public ClientMessagePacket(String message) {
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
