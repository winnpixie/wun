package io.github.winnpixie.wun.shared;

import java.net.SocketAddress;

public record QueuedPacket(Packet packet,
                           SocketAddress recipient) {
}
