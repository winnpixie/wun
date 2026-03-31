package io.github.winnpixie.wun.shared.packets.server;

import io.github.winnpixie.wun.shared.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ServerDisconnectPacket extends Packet {
    private final String reason;

    public ServerDisconnectPacket(DataInputStream input) throws IOException {
        this.reason = input.readUTF();
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
    public void serialize(DataOutputStream output) throws IOException {
        output.writeUTF(reason);
    }
}
