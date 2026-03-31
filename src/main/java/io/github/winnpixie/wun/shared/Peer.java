package io.github.winnpixie.wun.shared;

import java.net.InetSocketAddress;

public record Peer(int id,
                   InetSocketAddress address) {
}
