package io.pravega.example.flink.demo;

import io.pravega.client.stream.Serializer;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * Create by Anna
 * Date 2021-08-17
 * Pravega Serializer for ImageData
 */
public class ImageDataSerializer implements Serializer<ImageData>, Serializable {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public ImageData deserialize(ByteBuffer serializedValue) {
        final ByteArrayInputStream bin = new ByteArrayInputStream(
                serializedValue.array(),
                serializedValue.position(),
                serializedValue.remaining());
        try {
            return mapper.readValue(bin, ImageData.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ByteBuffer serialize(ImageData element) {
        try {
            return ByteBuffer.wrap(mapper.writeValueAsBytes(element));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
