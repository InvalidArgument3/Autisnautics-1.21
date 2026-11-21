package dev.wyseman.autisupdatersetup;

import java.util.ArrayList;
import java.util.List;

/**
 * Edits one JSON value in place. The rest of the file, including a compact
 * multi-megabyte array, stays byte for byte the same.
 */
final class JsonText {
    private JsonText() {}

    static final class Span {
        final int start;
        final int end;

        Span(int start, int end) {
            this.start = start;
            this.end = end;
        }

        String raw(String s) {
            return s.substring(start, end);
        }
    }

    static String quote(String value) {
        StringBuilder out = new StringBuilder(value.length() + 2);
        out.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        out.append('"');
        return out.toString();
    }

    static String unquote(String literal) {
        if (literal.length() < 2 || literal.charAt(0) != '"') {
            throw new IllegalArgumentException("not a json string");
        }
        StringBuilder out = new StringBuilder(literal.length() - 2);
        for (int i = 1; i < literal.length() - 1; i++) {
            char c = literal.charAt(i);
            if (c != '\\') {
                out.append(c);
                continue;
            }
            if (i + 1 >= literal.length() - 1) {
                break;
            }
            char n = literal.charAt(++i);
            switch (n) {
                case '"', '\\', '/' -> out.append(n);
                case 'n' -> out.append('\n');
                case 'r' -> out.append('\r');
                case 't' -> out.append('\t');
                case 'b' -> out.append('\b');
                case 'f' -> out.append('\f');
                case 'u' -> {
                    if (i + 4 >= literal.length() - 1) {
                        throw new IllegalArgumentException("bad unicode escape");
                    }
                    int cp = Integer.parseInt(literal.substring(i + 1, i + 5), 16);
                    out.append((char) cp);
                    i += 4;
                }
                default -> out.append(n);
            }
        }
        return out.toString();
    }

    static int objectEnd(String s, int openBrace) {
        Cursor c = new Cursor(s, openBrace);
        c.skipValue();
        return c.i;
    }

    static List<int[]> topLevelObjects(String s) {
        Cursor c = new Cursor(s, 0);
        c.skipWs();
        if (c.i >= s.length() || s.charAt(c.i) != '[') {
            throw new IllegalArgumentException("expected a json array");
        }
        c.i++;
        List<int[]> objects = new ArrayList<>();
        while (true) {
            c.skipWs();
            if (c.i >= s.length()) {
                throw new IllegalArgumentException("unterminated array");
            }
            if (s.charAt(c.i) == ']') {
                return objects;
            }
            if (s.charAt(c.i) != '{') {
                throw new IllegalArgumentException("array element is not an object at " + c.i);
            }
            int start = c.i;
            c.skipValue();
            objects.add(new int[] {start, c.i});
            c.skipWs();
            if (c.i < s.length() && s.charAt(c.i) == ',') {
                c.i++;
            }
        }
    }

    /** Value of {@code key} on this object only, not inside nested objects. */
    static Span findKey(String s, int objStart, int objEnd, String key) {
        Cursor c = new Cursor(s, objStart);
        if (c.i >= s.length() || s.charAt(c.i) != '{') {
            throw new IllegalArgumentException("expected an object");
        }
        c.i++;
        while (c.i < objEnd) {
            c.skipWs();
            if (c.i >= objEnd) {
                break;
            }
            char ch = s.charAt(c.i);
            if (ch == '}') {
                return null;
            }
            if (ch != '"') {
                throw new IllegalArgumentException("expected a key at " + c.i);
            }
            int keyStart = c.i;
            c.skipString();
            String name = unquote(s.substring(keyStart, c.i));
            c.skipWs();
            if (c.i >= s.length() || s.charAt(c.i) != ':') {
                throw new IllegalArgumentException("expected colon at " + c.i);
            }
            c.i++;
            c.skipWs();
            int valueStart = c.i;
            c.skipValue();
            int valueEnd = c.i;
            if (name.equals(key)) {
                return new Span(valueStart, valueEnd);
            }
            c.skipWs();
            if (c.i < s.length() && s.charAt(c.i) == ',') {
                c.i++;
            }
        }
        return null;
    }

    static String replace(String s, Span span, String rawToken) {
        return s.substring(0, span.start) + rawToken + s.substring(span.end);
    }

    /** Inserts a key before the object's closing brace. */
    static String insert(String s, int objStart, int objEnd, String key, String rawToken) {
        int close = objEnd - 1;
        while (close > objStart && Character.isWhitespace(s.charAt(close))) {
            close--;
        }
        if (s.charAt(close) != '}') {
            throw new IllegalArgumentException("object does not end at " + objEnd);
        }
        boolean empty = true;
        for (int i = objStart + 1; i < close; i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                empty = false;
                break;
            }
        }
        String insertion = quote(key) + ":" + rawToken;
        if (!empty) {
            insertion = "," + insertion;
        }
        return s.substring(0, close) + insertion + s.substring(close);
    }

    static boolean stringArrayContains(String s, Span array, String needle) {
        if (!array.raw(s).startsWith("[")) {
            return false;
        }
        Cursor c = new Cursor(s, array.start);
        c.i++;
        while (c.i < array.end) {
            c.skipWs();
            if (c.i >= array.end || s.charAt(c.i) == ']') {
                return false;
            }
            if (s.charAt(c.i) == '"') {
                int start = c.i;
                c.skipString();
                if (unquote(s.substring(start, c.i)).contains(needle)) {
                    return true;
                }
            } else {
                c.skipValue();
            }
            c.skipWs();
            if (c.i < s.length() && s.charAt(c.i) == ',') {
                c.i++;
            }
        }
        return false;
    }

    /** Appends one JSON string to an array span. The span must be an array. */
    static String appendString(String s, Span array, String value) {
        int close = array.end - 1;
        while (close > array.start && Character.isWhitespace(s.charAt(close))) {
            close--;
        }
        if (s.charAt(close) != ']') {
            throw new IllegalArgumentException("not an array");
        }
        boolean empty = true;
        for (int i = array.start + 1; i < close; i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                empty = false;
                break;
            }
        }
        String insertion = (empty ? "" : ",") + quote(value);
        return s.substring(0, close) + insertion + s.substring(close);
    }

    private static final class Cursor {
        final String s;
        int i;

        Cursor(String s, int i) {
            this.s = s;
            this.i = i;
        }

        void skipWs() {
            while (i < s.length()) {
                char c = s.charAt(i);
                if (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
                    i++;
                } else {
                    break;
                }
            }
        }

        void skipValue() {
            skipWs();
            if (i >= s.length()) {
                throw new IllegalArgumentException("unexpected end");
            }
            char c = s.charAt(i);
            if (c == '"') {
                skipString();
            } else if (c == '{') {
                skipContainer('{', '}');
            } else if (c == '[') {
                skipContainer('[', ']');
            } else {
                while (i < s.length()) {
                    char n = s.charAt(i);
                    if (n == ',' || n == '}' || n == ']' || Character.isWhitespace(n)) {
                        break;
                    }
                    i++;
                }
            }
        }

        void skipString() {
            if (s.charAt(i) != '"') {
                throw new IllegalArgumentException("expected string at " + i);
            }
            i++;
            while (i < s.length()) {
                char c = s.charAt(i++);
                if (c == '\\') {
                    if (i >= s.length()) {
                        throw new IllegalArgumentException("bad escape");
                    }
                    if (s.charAt(i) == 'u') {
                        i += 5;
                    } else {
                        i++;
                    }
                } else if (c == '"') {
                    return;
                }
            }
            throw new IllegalArgumentException("unterminated string");
        }

        void skipContainer(char open, char close) {
            int depth = 0;
            boolean inString = false;
            while (i < s.length()) {
                char c = s.charAt(i++);
                if (inString) {
                    if (c == '\\') {
                        i++;
                    } else if (c == '"') {
                        inString = false;
                    }
                    continue;
                }
                if (c == '"') {
                    inString = true;
                } else if (c == open) {
                    depth++;
                } else if (c == close) {
                    depth--;
                    if (depth == 0) {
                        return;
                    }
                }
            }
            throw new IllegalArgumentException("unterminated container");
        }
    }
}
