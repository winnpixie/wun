package io.github.winnpixie.wun.program;

import io.github.winnpixie.wun.program.client.Client;
import io.github.winnpixie.wun.program.server.Server;
import io.github.winnpixie.wun.shared.Host;

import java.io.IOException;

public class Entrypoint {
    public static void main(String[] args) {
        if (args.length < 1) {
            return;
        }

        try {
            Host host = new Host();

            switch (args[0].toLowerCase()) {
                case "--server":
                    new Server(host).start();

                    break;
                case "--client":
                    new Client(host).join();

                    break;
                default:
                    System.out.println("No!");

                    host.stop();
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
