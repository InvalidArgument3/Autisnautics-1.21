package dev.wyseman.autisupdatersetup;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Writes a file by temp file plus rename, and keeps one backup. */
final class AtomicFiles {
    private AtomicFiles() {}

    static void write(Path dest, byte[] bytes) throws IOException {
        Path tmp = dest.resolveSibling(dest.getFileName().toString() + ".autis-tmp");
        Files.createDirectories(dest.getParent());
        Files.write(tmp, bytes);
        try {
            Files.move(tmp, dest, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tmp, dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            Files.move(tmp, dest, StandardCopyOption.REPLACE_EXISTING);
        }
        if (Files.exists(tmp)) {
            Files.deleteIfExists(tmp);
        }
    }

    /** Copies {@code file} to {@code file.pre-updater.bak} only when that backup is absent. */
    static void backupOnce(Path file) throws IOException {
        Path bak = file.resolveSibling(file.getFileName().toString() + ".pre-updater.bak");
        if (Files.exists(bak) || !Files.isRegularFile(file)) {
            return;
        }
        Files.copy(file, bak);
    }
}
