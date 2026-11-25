import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Streaming multi-pass HPROF analyzer that traces large primitive arrays (>= minBytes)
 * up through collections (HashMap, ArrayList, Object[], etc.) to their owning mod/class & field.
 *
 * Usage: java testing/HprofReader.java <file.hprof> [minBytes=4096] [topN=35]
 */
public class HprofReader {
    static int idSize;
    static final Map<Long, String> strings = new HashMap<>(250_000);
    static final Map<Long, String> classNames = new HashMap<>(100_000);

    static class FieldDef {
        final String name;
        final byte type;
        FieldDef(String name, byte type) { this.name = name; this.type = type; }
    }

    static class ClassInfo {
        long superId;
        int instanceSize;
        List<FieldDef> instanceFields = Collections.emptyList();
        List<FieldDef> allFields; // cached flattened hierarchy (this -> super -> ...)
    }

    static final Map<Long, ClassInfo> classes = new HashMap<>(100_000);

    // Large arrays: arrayId -> [typeByte, totalBytes]
    static final Map<Long, long[]> largeArrays = new HashMap<>(200_000);
    static final long[] totalPrimBytes = new long[16];
    static final long[] totalPrimCount = new long[16];
    static final long[] largePrimBytes = new long[16];
    static long totalObjArrayBytes = 0;
    static long totalInstanceBytes = 0;

    static class ParentRef {
        final long parentId;
        final long parentClassId;
        final String field;
        ParentRef(long parentId, long parentClassId, String field) {
            this.parentId = parentId;
            this.parentClassId = parentClassId;
            this.field = field;
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: HprofReader <file.hprof> [minBytes=4096] [topN=35]");
            System.exit(1);
        }
        String path = args[0];
        int minBytes = args.length >= 2 ? Integer.parseInt(args[1]) : 4096;
        int topN = args.length >= 3 ? Integer.parseInt(args[2]) : 35;

        long t0 = System.currentTimeMillis();
        System.out.println("Pass 1: scanning classes & arrays >= " + minBytes + " B...");
        pass1(path, minBytes);
        System.out.printf("Pass 1 done in %.1fs: %d classes, %d large arrays%n",
                (System.currentTimeMillis() - t0) / 1000.0, classes.size(), largeArrays.size());

        // Flatten field layouts
        for (Map.Entry<Long, ClassInfo> e : classes.entrySet()) {
            buildAllFields(e.getValue());
        }

        // Multi-pass parent resolution through containers
        Map<Long, ParentRef> resolved = new HashMap<>(largeArrays.size() * 2);
        Set<Long> frontier = new HashSet<>(largeArrays.keySet());
        for (int pass = 2; pass <= 5 && !frontier.isEmpty(); pass++) {
            long tp = System.currentTimeMillis();
            Map<Long, ParentRef> found = passFindParents(path, frontier);
            resolved.putAll(found);
            Set<Long> nextFrontier = new HashSet<>();
            for (ParentRef pr : found.values()) {
                if (pr.parentId != 0 && !resolved.containsKey(pr.parentId) && isContainerClass(pr.parentClassId)) {
                    nextFrontier.add(pr.parentId);
                }
            }
            System.out.printf("Pass %d done in %.1fs: resolved %d refs, next container frontier %d%n",
                    pass, (System.currentTimeMillis() - tp) / 1000.0, found.size(), nextFrontier.size());
            frontier = nextFrontier;
        }

        printReport(minBytes, topN, resolved);
    }

    static boolean isContainerClass(long classId) {
        if (classId == 0) return true; // Object[]
        String n = classNames.getOrDefault(classId, "");
        return n.startsWith("[")
                || n.startsWith("java.util.")
                || n.startsWith("it.unimi.dsi.fastutil.")
                || n.startsWith("com.google.common.collect.")
                || n.equals("java.lang.Object");
    }

    static List<FieldDef> buildAllFields(ClassInfo ci) {
        if (ci.allFields != null) return ci.allFields;
        List<FieldDef> list = new ArrayList<>(ci.instanceFields);
        if (ci.superId != 0) {
            ClassInfo sup = classes.get(ci.superId);
            if (sup != null) list.addAll(buildAllFields(sup));
        }
        ci.allFields = list;
        return list;
    }

    static void pass1(String path, int minBytes) throws Exception {
        try (DataInputStream in = open(path)) {
            while (in.available() > 0 || true) {
                int tag;
                try { tag = in.readUnsignedByte(); } catch (EOFException eof) { break; }
                in.readInt(); // ts
                long len = Integer.toUnsignedLong(in.readInt());
                if (tag == 0x01) { // STRING
                    long id = readId(in);
                    byte[] b = in.readNBytes((int) (len - idSize));
                    strings.put(id, new String(b, StandardCharsets.UTF_8));
                } else if (tag == 0x02) { // LOAD_CLASS
                    in.readInt(); // serial
                    long cid = readId(in);
                    in.readInt(); // stack
                    long nameId = readId(in);
                    classNames.put(cid, strings.getOrDefault(nameId, "?").replace('/', '.'));
                } else if (tag == 0x0C || tag == 0x1C) { // HEAP_DUMP / SEGMENT
                    readHeapSegmentPass1(in, len, minBytes);
                } else {
                    skipFully(in, len);
                }
            }
        }
    }

    static void readHeapSegmentPass1(DataInputStream in, long segLen, int minBytes) throws Exception {
        long end = segLen;
        long read = 0;
        while (read < end) {
            int sub = in.readUnsignedByte();
            read += 1;
            switch (sub) {
                case 0xFF: read += skip(in, idSize); break;
                case 0x01: read += skip(in, idSize * 2); break;
                case 0x02: case 0x03: read += skip(in, idSize + 8); break;
                case 0x04: read += skip(in, idSize + 4); break;
                case 0x05: case 0x07: read += skip(in, idSize); break;
                case 0x06: read += skip(in, idSize + 4); break;
                case 0x08: read += skip(in, idSize + 8); break;
                case 0x20: { // CLASS_DUMP
                    long cid = readId(in); read += idSize;
                    skipFully(in, 4); read += 4;
                    long sup = readId(in); read += idSize;
                    skipFully(in, idSize * 5); read += idSize * 5;
                    int instSize = in.readInt(); read += 4;
                    int cpCount = in.readUnsignedShort(); read += 2;
                    for (int i = 0; i < cpCount; i++) {
                        skipFully(in, 2); read += 2;
                        int t = in.readUnsignedByte(); read += 1;
                        int vs = typeSize(t);
                        skipFully(in, vs); read += vs;
                    }
                    int sfCount = in.readUnsignedShort(); read += 2;
                    for (int i = 0; i < sfCount; i++) {
                        skipFully(in, idSize); read += idSize;
                        int t = in.readUnsignedByte(); read += 1;
                        int vs = typeSize(t);
                        skipFully(in, vs); read += vs;
                    }
                    int ifCount = in.readUnsignedShort(); read += 2;
                    List<FieldDef> fds = new ArrayList<>(ifCount);
                    for (int i = 0; i < ifCount; i++) {
                        long nid = readId(in); read += idSize;
                        byte t = in.readByte(); read += 1;
                        fds.add(new FieldDef(strings.getOrDefault(nid, "?"), t));
                    }
                    ClassInfo ci = new ClassInfo();
                    ci.superId = sup;
                    ci.instanceSize = instSize;
                    ci.instanceFields = fds;
                    classes.put(cid, ci);
                    break;
                }
                case 0x21: { // INSTANCE_DUMP
                    skipFully(in, idSize + 4 + idSize); read += idSize + 4 + idSize;
                    int n = in.readInt(); read += 4;
                    totalInstanceBytes += n;
                    skipFully(in, n); read += n;
                    break;
                }
                case 0x22: { // OBJECT_ARRAY_DUMP
                    skipFully(in, idSize + 4); read += idSize + 4;
                    int count = in.readInt(); read += 4;
                    skipFully(in, idSize); read += idSize;
                    long bytes = (long) count * idSize;
                    totalObjArrayBytes += bytes;
                    skipFully(in, bytes); read += bytes;
                    break;
                }
                case 0x23: { // PRIMITIVE_ARRAY_DUMP
                    long arrId = readId(in); read += idSize;
                    in.readInt(); read += 4;
                    int count = in.readInt(); read += 4;
                    int elemType = in.readUnsignedByte(); read += 1;
                    long bytes = (long) count * typeSize(elemType);
                    totalPrimBytes[elemType] += bytes;
                    totalPrimCount[elemType]++;
                    if (bytes >= minBytes) {
                        largeArrays.put(arrId, new long[]{elemType, bytes});
                        largePrimBytes[elemType] += bytes;
                    }
                    skipFully(in, bytes); read += bytes;
                    break;
                }
                default:
                    throw new IOException("Unknown HPROF subtag: 0x" + Integer.toHexString(sub));
            }
        }
    }

    static Map<Long, ParentRef> passFindParents(String path, Set<Long> targets) throws Exception {
        Map<Long, ParentRef> found = new HashMap<>(targets.size());
        try (DataInputStream in = open(path)) {
            while (true) {
                int tag;
                try { tag = in.readUnsignedByte(); } catch (EOFException eof) { break; }
                in.readInt();
                long len = Integer.toUnsignedLong(in.readInt());
                if (tag == 0x0C || tag == 0x1C) {
                    readHeapSegmentParents(in, len, targets, found);
                } else {
                    skipFully(in, len);
                }
            }
        }
        return found;
    }

    static void readHeapSegmentParents(DataInputStream in, long segLen, Set<Long> targets, Map<Long, ParentRef> found) throws Exception {
        long read = 0;
        while (read < segLen) {
            int sub = in.readUnsignedByte();
            read += 1;
            switch (sub) {
                case 0xFF: read += skip(in, idSize); break;
                case 0x01: read += skip(in, idSize * 2); break;
                case 0x02: case 0x03: read += skip(in, idSize + 8); break;
                case 0x04: read += skip(in, idSize + 4); break;
                case 0x05: case 0x07: read += skip(in, idSize); break;
                case 0x06: read += skip(in, idSize + 4); break;
                case 0x08: read += skip(in, idSize + 8); break;
                case 0x20: { // CLASS_DUMP
                    skipFully(in, idSize + 4 + idSize * 6 + 4);
                    read += idSize + 4 + idSize * 6 + 4;
                    int cpCount = in.readUnsignedShort(); read += 2;
                    for (int i = 0; i < cpCount; i++) {
                        skipFully(in, 2); read += 2;
                        int t = in.readUnsignedByte(); read += 1;
                        read += skip(in, typeSize(t));
                    }
                    int sfCount = in.readUnsignedShort(); read += 2;
                    for (int i = 0; i < sfCount; i++) {
                        skipFully(in, idSize); read += idSize;
                        int t = in.readUnsignedByte(); read += 1;
                        read += skip(in, typeSize(t));
                    }
                    int ifCount = in.readUnsignedShort(); read += 2;
                    read += skip(in, (long) ifCount * (idSize + 1));
                    break;
                }
                case 0x21: { // INSTANCE_DUMP
                    long objId = readId(in); read += idSize;
                    in.readInt(); read += 4;
                    long cid = readId(in); read += idSize;
                    int n = in.readInt(); read += 4;
                    ClassInfo ci = classes.get(cid);
                    if (ci != null && ci.allFields != null) {
                        int pos = 0;
                        for (FieldDef fd : ci.allFields) {
                            if (fd.type == 2) { // OBJECT ref
                                long refId = readId(in);
                                pos += idSize;
                                if (refId != 0 && targets.contains(refId)) {
                                    found.putIfAbsent(refId, new ParentRef(objId, cid, fd.name));
                                }
                            } else {
                                int sz = typeSize(fd.type);
                                skipFully(in, sz);
                                pos += sz;
                            }
                        }
                        if (pos < n) skipFully(in, n - pos);
                    } else {
                        skipFully(in, n);
                    }
                    read += n;
                    break;
                }
                case 0x22: { // OBJECT_ARRAY_DUMP
                    long arrId = readId(in); read += idSize;
                    in.readInt(); read += 4;
                    int count = in.readInt(); read += 4;
                    readId(in); read += idSize; // elemClassId
                    for (int i = 0; i < count; i++) {
                        long refId = readId(in);
                        if (refId != 0 && targets.contains(refId)) {
                            found.putIfAbsent(refId, new ParentRef(arrId, 0, "[]"));
                        }
                    }
                    read += (long) count * idSize;
                    break;
                }
                case 0x23: { // PRIMITIVE_ARRAY_DUMP
                    skipFully(in, idSize + 4); read += idSize + 4;
                    int count = in.readInt(); read += 4;
                    int elemType = in.readUnsignedByte(); read += 1;
                    long bytes = (long) count * typeSize(elemType);
                    skipFully(in, bytes); read += bytes;
                    break;
                }
                default:
                    throw new IOException("Unknown HPROF subtag: 0x" + Integer.toHexString(sub));
            }
        }
    }

    static void printReport(int minBytes, int topN, Map<Long, ParentRef> resolved) {
        System.out.println("\n=== HPROF PRIMITIVE ARRAY SUMMARY ===");
        String[] tNames = {"?", "?", "obj", "?", "boolean", "char", "float", "double", "byte", "short", "int", "long"};
        for (int t = 4; t <= 11; t++) {
            if (totalPrimBytes[t] > 0) {
                System.out.printf("  %-8s: %8.2f MB total (%,10d arrays) | %8.2f MB in arrays >= %d B%n",
                        tNames[t], totalPrimBytes[t] / (1024.0 * 1024.0), totalPrimCount[t],
                        largePrimBytes[t] / (1024.0 * 1024.0), minBytes);
            }
        }
        System.out.printf("  Object[] payloads : %8.2f MB | Instance payloads: %8.2f MB%n",
                totalObjArrayBytes / (1024.0 * 1024.0), totalInstanceBytes / (1024.0 * 1024.0));

        Map<String, long[]> byOwnerField = new HashMap<>();
        Map<String, long[]> byOwnerPkg = new HashMap<>();
        for (Map.Entry<Long, long[]> e : largeArrays.entrySet()) {
            long arrId = e.getKey();
            long bytes = e.getValue()[1];
            String owner = resolveOwner(arrId, resolved);
            byOwnerField.computeIfAbsent(owner, k -> new long[2]);
            byOwnerField.get(owner)[0] += bytes;
            byOwnerField.get(owner)[1]++;

            String pkg = groupPkg(owner);
            byOwnerPkg.computeIfAbsent(pkg, k -> new long[2]);
            byOwnerPkg.get(pkg)[0] += bytes;
            byOwnerPkg.get(pkg)[1]++;
        }

        List<Map.Entry<String, long[]>> pkgList = new ArrayList<>(byOwnerPkg.entrySet());
        pkgList.sort((a, b) -> Long.compare(b.getValue()[0], a.getValue()[0]));
        System.out.printf("%n--- Top %d Owners of Large Arrays (>= %d B) by Package ---%n", Math.min(topN, pkgList.size()), minBytes);
        for (int i = 0; i < Math.min(topN, pkgList.size()); i++) {
            Map.Entry<String, long[]> en = pkgList.get(i);
            System.out.printf("%2d. %8.2f MB | %,7d arrays | %s%n",
                    i + 1, en.getValue()[0] / (1024.0 * 1024.0), en.getValue()[1], en.getKey());
        }

        List<Map.Entry<String, long[]>> fldList = new ArrayList<>(byOwnerField.entrySet());
        fldList.sort((a, b) -> Long.compare(b.getValue()[0], a.getValue()[0]));
        System.out.printf("%n--- Top %d Owners of Large Arrays (>= %d B) by Class.Field ---%n", Math.min(topN, fldList.size()), minBytes);
        for (int i = 0; i < Math.min(topN, fldList.size()); i++) {
            Map.Entry<String, long[]> en = fldList.get(i);
            System.out.printf("%2d. %8.2f MB | %,7d arrays | %s%n",
                    i + 1, en.getValue()[0] / (1024.0 * 1024.0), en.getValue()[1], en.getKey());
        }
    }

    static String resolveOwner(long id, Map<Long, ParentRef> resolved) {
        long cur = id;
        ParentRef last = null;
        Set<Long> seen = new HashSet<>();
        for (int depth = 0; depth < 12; depth++) {
            ParentRef pr = resolved.get(cur);
            if (pr == null) break;
            last = pr;
            if (!isContainerClass(pr.parentClassId)) {
                return classNames.getOrDefault(pr.parentClassId, "?") + "." + pr.field;
            }
            if (!seen.add(pr.parentId)) break;
            cur = pr.parentId;
        }
        if (last != null) {
            return (last.parentClassId == 0 ? "Object[]" : classNames.getOrDefault(last.parentClassId, "?")) + "." + last.field;
        }
        return "<static/root/untraced>";
    }

    static String groupPkg(String owner) {
        int dot = owner.lastIndexOf('.');
        String cls = dot > 0 ? owner.substring(0, dot) : owner;
        String[] parts = cls.split("\\.");
        if (parts.length >= 3) return parts[0] + "." + parts[1] + "." + parts[2];
        return cls;
    }

    static DataInputStream open(String path) throws IOException {
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(path), 4 * 1024 * 1024));
        while (in.readByte() != 0) {} // header string
        idSize = in.readInt();
        in.readLong(); // timestamp
        return in;
    }

    static long readId(DataInputStream in) throws IOException {
        return idSize == 8 ? in.readLong() : Integer.toUnsignedLong(in.readInt());
    }

    static int typeSize(int t) {
        switch (t) {
            case 2: return idSize;
            case 4: case 8: return 1;
            case 5: case 9: return 2;
            case 6: case 10: return 4;
            case 7: case 11: return 8;
            default: throw new IllegalArgumentException("Bad HPROF type " + t);
        }
    }

    static long skip(DataInputStream in, long n) throws IOException {
        skipFully(in, n);
        return n;
    }

    static void skipFully(DataInputStream in, long n) throws IOException {
        long rem = n;
        while (rem > 0) {
            long s = in.skip(rem);
            if (s <= 0) {
                if (in.read() < 0) throw new EOFException();
                rem--;
            } else {
                rem -= s;
            }
        }
    }
}
