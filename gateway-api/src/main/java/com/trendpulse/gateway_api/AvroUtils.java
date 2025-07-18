package com.trendpulse.gateway_api;

import org.apache.avro.io.*;
import org.apache.avro.specific.*;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class AvroUtils {

    public <T> byte[] serializeAvro(T record, Class<T> clazz) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            DatumWriter<T> writer = new SpecificDatumWriter<>(clazz);
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
            writer.write(record, encoder);
            encoder.flush();
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Avro serialization failed", e);
        }
    }

    public <T> T deserializeAvro(byte[] data, Class<T> clazz) {
        if (data == null) return null;
        try {
            DatumReader<T> reader = new SpecificDatumReader<>(clazz);
            BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            return reader.read(null, decoder);
        } catch (IOException e) {
            throw new RuntimeException("Avro deserialization failed", e);
        }
    }
}
