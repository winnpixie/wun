package io.github.winnpixie.wun.shared.packets.client;

import io.github.winnpixie.wun.shared.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ClientHeartbeatPacket extends Packet {
    public ClientHeartbeatPacket(DataInputStream input) throws IOException {
    }

    public ClientHeartbeatPacket() {
    }

    @Override
    public void serialize(DataOutputStream output) throws IOException {
    }
}
