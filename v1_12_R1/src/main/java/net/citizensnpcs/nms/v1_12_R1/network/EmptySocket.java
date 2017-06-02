package net.citizensnpcs.nms.v1_12_R1.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class EmptySocket extends Socket {
    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(EMPTY);
    }

    @Override
    public OutputStream getOutputStream() {
        return new ByteArrayOutputStream(10);
    }

    private static final byte[] EMPTY = new byte[50];
}