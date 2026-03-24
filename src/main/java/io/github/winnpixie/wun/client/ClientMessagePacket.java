package io.github.winnpixie.wun.client;

import io.github.winnpixie.wun.shared.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ClientMessagePacket extends Packet {
    private final String message;

    public ClientMessagePacket(String message) {
        if (message.length() > 512) {
            message = message.substring(0, 512);
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

    public static ClientMessagePacket deserialize(DataInputStream input) throws IOException {
        return new ClientMessagePacket(input.readUTF());
    }
}
