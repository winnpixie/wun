package io.github.winnpixie.wun.shared.packets.client;

import io.github.winnpixie.wun.shared.IOHelper;
import io.github.winnpixie.wun.shared.Packet;

import java.nio.ByteBuffer;

public class ClientIdentityPacket extends Packet {
    private String userName;

    public ClientIdentityPacket(ByteBuffer buffer) {
        this.userName = IOHelper.getString(buffer);
    }

    public ClientIdentityPacket(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public void serialize(ByteBuffer buffer) {
        IOHelper.putString(buffer, userName);
    }
}
