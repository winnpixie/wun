package io.github.winnpixie.wun.shared.packets.client;

import io.github.winnpixie.wun.shared.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ClientDisconnectPacket extends Packet {
    public ClientDisconnectPacket(DataInputStream input) throws IOException {
    }

    public ClientDisconnectPacket() {
    }

    @Override
    public void serialize(DataOutputStream output) throws IOException {
    }
}
