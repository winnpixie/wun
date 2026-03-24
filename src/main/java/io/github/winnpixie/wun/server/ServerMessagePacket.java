package io.github.winnpixie.wun.server;

import io.github.winnpixie.wun.shared.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ServerMessagePacket extends Packet {
    private final String message;

    public ServerMessagePacket(String message) {
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

    public static ServerMessagePacket deserialize(DataInputStream input) throws IOException {
        return new ServerMessagePacket(input.readUTF());
    }
}
