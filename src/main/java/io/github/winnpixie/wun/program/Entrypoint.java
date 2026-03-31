package io.github.winnpixie.wun.program;

import io.github.winnpixie.wun.program.client.ClientEntrypoint;
import io.github.winnpixie.wun.program.server.ServerEntrypoint;

public class Entrypoint {
    public static void main(String[] args) {
        if (args.length < 1) {
            return;
        }

        switch (args[0].toLowerCase()) {
            case "--server":
                ServerEntrypoint.run();
                break;
            case "--client":
                ClientEntrypoint.run();
                break;
            default:
                System.out.println("No!");
                break;
        }
    }
}
