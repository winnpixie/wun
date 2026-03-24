package io.github.winnpixie.wun.client;

import io.github.winnpixie.wun.shared.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ClientConnectPacket extends Packet {
    @Override
    public void serialize(DataOutputStream output) throws IOException {
    }

    public static ClientConnectPacket deserialize(DataInputStream input) throws IOException {
        return new ClientConnectPacket();
    }
}
