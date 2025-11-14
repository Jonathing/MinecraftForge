package net.minecraftforge.forge.build.tasks;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public record InheritanceData(
    String name,
    int access,
    String superName,
    List<String> interfaces,
    Map<String, Method> methods,
    Map<String, Field> fields,
    List<Annotation> annotations
) implements InheritanceDataAnnotatable {
    public record Method(
        int access,
        String override,
        List<Annotation> annotations
    ) implements InheritanceDataAnnotatable { }

    public record Field(
        int access,
        String desc,
        List<Annotation> annotations
    ) implements InheritanceDataAnnotatable { }

    public record Annotation(
        String desc
    ) { }

    private static final Gson GSON = new Gson();
    public static Map<String, InheritanceData> parse(File file) {
        try (var reader = new FileReader(file)) {
            return GSON.fromJson(reader, new TypeToken<>() {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
