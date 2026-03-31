package io.github.winnpixie.wun.shared.packets.client;

import io.github.winnpixie.wun.shared.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ClientConnectPacket extends Packet {
    public ClientConnectPacket(DataInputStream input) throws IOException {
    }

    public ClientConnectPacket() {
    }

    @Override
    public void serialize(DataOutputStream output) throws IOException {
    }
}
