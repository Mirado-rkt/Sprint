package mg.itu.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class JsonFormatter {

    private JsonFormatter() {
    }

    public static String toJson(Object value) {
        return serialize(value, java.util.Collections.newSetFromMap(new IdentityHashMap<>()));
    }

    private static String serialize(Object value, Set<Object> visited) {
        if (value == null) {
            return "null";
        }

        if (value instanceof String || value instanceof Character) {
            return '"' + escape(value.toString()) + '"';
        }

        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }

        if (value instanceof Enum<?>) {
            Enum<?> enumValue = (Enum<?>) value;
            return '"' + escape(enumValue.name()) + '"';
        }

        if (value instanceof TemporalAccessor) {
            return '"' + escape(value.toString()) + '"';
        }

        if (value.getClass().isArray()) {
            if (visited.contains(value)) {
                return "null";
            }
            visited.add(value);

            StringBuilder builder = new StringBuilder();
            builder.append('[');
            int length = Array.getLength(value);
            for (int index = 0; index < length; index++) {
                if (index > 0) {
                    builder.append(',');
                }
                builder.append(serialize(Array.get(value, index), visited));
            }
            builder.append(']');
            return builder.toString();
        }

        if (value instanceof Collection<?>) {
            Collection<?> collection = (Collection<?>) value;
            if (visited.contains(value)) {
                return "null";
            }
            visited.add(value);

            StringBuilder builder = new StringBuilder();
            builder.append('[');
            boolean first = true;
            for (Object element : collection) {
                if (!first) {
                    builder.append(',');
                }
                builder.append(serialize(element, visited));
                first = false;
            }
            builder.append(']');
            return builder.toString();
        }

        if (value instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) value;
            if (visited.contains(value)) {
                return "null";
            }
            visited.add(value);

            StringBuilder builder = new StringBuilder();
            builder.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) {
                    builder.append(',');
                }
                builder.append('"').append(escape(String.valueOf(entry.getKey()))).append('"');
                builder.append(':');
                builder.append(serialize(entry.getValue(), visited));
                first = false;
            }
            builder.append('}');
            return builder.toString();
        }

        if (visited.contains(value)) {
            return "null";
        }
        visited.add(value);

        Map<String, Object> properties = new LinkedHashMap<>();
        Class<?> type = value.getClass();
        while (type != null && type != Object.class) {
            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                    continue;
                }

                try {
                    field.setAccessible(true);
                    properties.put(field.getName(), field.get(value));
                } catch (IllegalAccessException | RuntimeException ignored) {
                }
            }
            type = type.getSuperclass();
        }

        StringBuilder builder = new StringBuilder();
        builder.append('{');
        boolean first = true;
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            if (!first) {
                builder.append(',');
            }
            builder.append('"').append(escape(entry.getKey())).append('"');
            builder.append(':');
            builder.append(serialize(entry.getValue(), visited));
            first = false;
        }
        builder.append('}');
        return builder.toString();
    }

    private static String escape(String input) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < input.length(); index++) {
            char c = input.charAt(index);
            switch (c) {
                case '"':
                    builder.append("\\\"");
                    break;
                case '\\':
                    builder.append("\\\\");
                    break;
                case '\b':
                    builder.append("\\b");
                    break;
                case '\f':
                    builder.append("\\f");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        builder.append(String.format("\\u%04x", (int) c));
                    } else {
                        builder.append(c);
                    }
                    break;
            }
        }
        return builder.toString();
    }
}