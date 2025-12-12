package eu.ha3.presencefootsteps.config;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.function.Function;

public class ToStringAdapter <T> extends TypeAdapter<T> {
    private final Function<T, String> writer;
    private final Function<String, T> reader;

    public ToStringAdapter(Function<T, String> writer, Function<String, T> reader) {
        this.writer = writer;
        this.reader = reader;
    }


    @Override
    public void write(JsonWriter out, T value) throws IOException {
        out.value(this.writer.apply(value));
    }

    @Override
    public T read(JsonReader in) throws IOException {
        return this.reader.apply(in.nextString());
    }
}