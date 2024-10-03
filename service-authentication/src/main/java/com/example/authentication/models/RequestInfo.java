package com.example.authentication.models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 5921990572289947287L;
    private String requestId;
    private String requestUri;
    private String referer;

    public byte[] toBytes() {
        var baos = new ByteArrayOutputStream();
        try {
            var oos = new ObjectOutputStream(baos);
            oos.writeObject(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }

    public static RequestInfo newRequestInfo(byte[] data) {
        try {
            var ois = new ObjectInputStream(new ByteArrayInputStream(data));
            return (RequestInfo) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
