package io.github.winnpixie.wun.server;

import io.github.winnpixie.wun.shared.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ServerDisconnectPacket extends Packet {
    @Override
    public void serialize(DataOutputStream output) throws IOException {
    }

    public static ServerDisconnectPacket deserialize(DataInputStream input) throws IOException {
        return new ServerDisconnectPacket();
    }
}
