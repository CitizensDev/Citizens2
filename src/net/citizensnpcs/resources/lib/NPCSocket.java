package net.citizensnpcs.resources.lib;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class NPCSocket extends Socket {
    @Override
    public InputStream getInputStream() {
        return new InputStream() {
            @Override
            public int read() {
                return 0; // NOP
            }
        };
    }

    @Override
    public OutputStream getOutputStream() {
        return new OutputStream() {
            @Override
            public void write(int b) {
                // NOP
            }
        };
    }
}