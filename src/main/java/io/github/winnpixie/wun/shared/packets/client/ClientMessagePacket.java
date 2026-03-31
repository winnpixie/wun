package io.github.winnpixie.wun.shared.packets.client;

import io.github.winnpixie.wun.shared.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ClientMessagePacket extends Packet {
    private final String message;

    public ClientMessagePacket(DataInputStream input) throws IOException {
        this.message = input.readUTF();
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
    public void serialize(DataOutputStream output) throws IOException {
        output.writeUTF(message);
    }
}
