#!/usr/bin/env python3
"""Copy Loom packages into the NeoForge tree, rewrite them, and compile.

Packages that do not compile stay in src/staged/java, which is excluded from
the main mod compile. Promote a package into src/main once none of its files
error. Javac lines stay under tools/port-errors/.
"""
import argparse
import os
import re
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
LOOM = Path("/tmp/neep-loom/remappedSrc/com/neep/neepmeat")
SRC = ROOT / "src/main/java/com/neep/neepmeat"
STAGE = ROOT / "src/staged/java/com/neep/neepmeat"
MEATLIB = ROOT / "src/main/java/com/neep/meatlib"
ERRORS = ROOT / "tools/port-errors"
sys.path.insert(0, str(ROOT / "tools/loom-migrated"))
from apply_121_drift import drift  # noqa: E402

IMP_RE = re.compile(r"^import (?:static )?([a-zA-Z0-9_.]+);", re.M)
ERR_RE = re.compile(r"^(.+\.java):(\d+): error: (.*)$")
ALLOWED = (
    "net.fabricmc.fabric.api.transfer.v1",
    "net.fabricmc.api.EnvType",
    "net.fabricmc.api.Environment",
    "net.fabricmc.fabric.api.util.NbtType",
)


def packages(root):
    found = {}
    if not root.exists():
        return found
    for dirpath, dirs, files in os.walk(root):
        javas = [f for f in files if f.endswith(".java")]
        if javas:
            rel = Path(dirpath).relative_to(root).as_posix()
            found["(root)" if rel == "." else rel] = [Path(dirpath) / f for f in javas]
    return found


def package_of(imp, prefix):
    rest = imp[len(prefix):]
    parts = rest.split(".")
    if parts and parts[-1][:1].isupper():
        parts = parts[:-1]
    return "/".join(parts) if parts else "(root)"


def select_soft(files_by, present, meatlib_pkgs):
    missing = set(files_by) - present
    info = {}
    for key, paths in files_by.items():
        if key not in missing:
            continue
        hard = False
        deps = set()
        meatlib_missing = False
        for path in paths:
            text = path.read_text(errors="replace")
            for match in IMP_RE.finditer(text):
                imp = match.group(1)
                if (imp.startswith("net.fabricmc") or imp.startswith((
                        "dev.", "software.", "folk.", "me.shedaniel", "org.spongepowered"))) and not imp.startswith(ALLOWED):
                    hard = True
                if imp.startswith("com.neep.neepmeat."):
                    pkg = package_of(imp, "com.neep.neepmeat.")
                    if pkg != key and pkg in missing:
                        deps.add(pkg)
                if imp.startswith("com.neep.meatlib."):
                    pkg = package_of(imp, "com.neep.meatlib.")
                    if pkg not in meatlib_pkgs:
                        meatlib_missing = True
        info[key] = (hard, deps, meatlib_missing)
    soft = {key for key, (hard, deps, meatlib_missing) in info.items() if not hard and not meatlib_missing}
    return sorted(key for key in soft if info[key][1] <= soft)


def copy_packages(names, files_by, destination=SRC):
    copied = {}
    for name in names:
        targets = []
        for source in files_by[name]:
            relative = source.relative_to(LOOM)
            if (SRC / relative).exists():
                continue
            target = destination / relative
            if target.exists():
                targets.append(target)
                continue
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_text(drift(source.read_text(errors="replace")))
            targets.append(target)
        if targets:
            copied[name] = targets
    return copied


def compile_errors(task="compileJava"):
    log = ERRORS / "compile.log"
    ERRORS.mkdir(parents=True, exist_ok=True)
    result = subprocess.run(
        ["./gradlew", task, "--offline", "--console=plain"],
        cwd=ROOT, text=True, capture_output=True)
    log.write_text(result.stdout + "\n" + result.stderr)
    per_file = {}
    for line in (result.stdout + "\n" + result.stderr).splitlines():
        match = ERR_RE.match(line)
        if match:
            per_file.setdefault(match.group(1), []).append(line)
    return result.returncode == 0, per_file


def select_leaves(files_by, present):
    """Packages whose neepmeat imports are already ported. They cannot break each other."""
    missing = set(files_by) - present
    banned = (
        "org.spongepowered", "dev.engine_room", "dev.emi", "me.shedaniel",
        "net.fabricmc.fabric.api.renderer", "net.fabricmc.fabric.api.datagen",
        "net.fabricmc.fabric.api.client", "software.bernie", "dev.onyxstudios",
        "folk.sisby", "com.neep.meatweapons", "com.neep.neepbus.block",
        "com.neep.neepbus.util.NeepBus", "net.fabricmc.fabric.api.networking",
        "net.fabricmc.fabric.api.lookup", "net.fabricmc.fabric.api.object",
        "net.fabricmc.fabric.api.screenhandler", "net.fabricmc.fabric.api.event",
        "net.fabricmc.loader",
    )
    chosen = []
    for key in sorted(missing):
        blocked = False
        for path in files_by[key]:
            text = path.read_text(errors="replace")
            for match in IMP_RE.finditer(text):
                imp = match.group(1)
                if imp.startswith(banned):
                    blocked = True
                if imp.startswith("com.neep.neepmeat."):
                    pkg = package_of(imp, "com.neep.neepmeat.")
                    if pkg != key and pkg in missing:
                        blocked = True
        if not blocked:
            chosen.append(key)
    return chosen


def select_light(files_by, present):
    """Small packages with no mixin, renderer, datagen, or compat imports."""
    missing = set(files_by) - present
    banned = (
        "org.spongepowered", "dev.engine_room", "dev.emi", "me.shedaniel",
        "net.fabricmc.fabric.api.renderer", "net.fabricmc.fabric.api.datagen",
        "net.fabricmc.fabric.api.client.model", "software.bernie", "dev.onyxstudios",
        "folk.sisby",
    )
    info = {}
    for key in missing:
        if len(files_by[key]) > 6:
            continue
        hard = False
        deps = set()
        for path in files_by[key]:
            text = path.read_text(errors="replace")
            for match in IMP_RE.finditer(text):
                imp = match.group(1)
                if imp.startswith(banned):
                    hard = True
                if imp.startswith("com.neep.neepmeat."):
                    pkg = package_of(imp, "com.neep.neepmeat.")
                    if pkg != key and pkg in missing:
                        deps.add(pkg)
        info[key] = (hard, deps)
    chosen = {key for key, (hard, deps) in info.items() if not hard}
    return sorted(key for key in chosen if info[key][1] <= chosen)


def drop_until_clean(copied):
    remaining = {name: list(paths) for name, paths in copied.items()}
    for attempt in range(8):
        ok, per_file = compile_errors()
        if ok:
            print(f"compile clean, kept {len(remaining)} packages")
            return 0
        failed = set()
        for path, lines in per_file.items():
            for name, targets in remaining.items():
                if any(str(target) == path for target in targets):
                    failed.add(name)
                    (ERRORS / (name.replace("/", "__") + ".txt")).write_text("\n".join(lines) + "\n")
        if not failed:
            print(f"errors are outside the batch: {len(per_file)} files")
            for path in list(per_file)[:8]:
                print(path)
            return 1
        print(f"drop {len(failed)} packages on attempt {attempt + 1}: {', '.join(sorted(failed)[:12])}")
        for name in failed:
            for target in remaining.pop(name):
                if target.exists():
                    target.unlink()
            directory = SRC / name
            if directory.exists() and not any(directory.glob("*.java")):
                try:
                    directory.rmdir()
                except OSError:
                    pass
    print("gave up")
    return 1


def stage_missing(files_by):
    present = set(packages(SRC))
    names = sorted(set(files_by) - present)
    copied = copy_packages(names, files_by, STAGE)
    print(f"staged {sum(len(v) for v in copied.values())} files in {len(copied)} packages")
    return copied


def promote(copied):
    ok, per_file = compile_errors("compileStagedJava")
    failed = set()
    for path, lines in per_file.items():
        for name, targets in copied.items():
            if any(str(target) == path for target in targets):
                failed.add(name)
                (ERRORS / (name.replace("/", "__") + ".txt")).write_text("\n".join(lines) + "\n")
    if not ok and not failed:
        print("staged compile failed and no package was matched; nothing promoted")
        return []
    promoted = []
    for name, targets in copied.items():
        if name in failed:
            continue
        for target in targets:
            relative = target.relative_to(STAGE)
            destination = SRC / relative
            destination.parent.mkdir(parents=True, exist_ok=True)
            shutil.move(str(target), destination)
        directory = STAGE / name
        if directory.exists() and not any(directory.rglob("*.java")):
            shutil.rmtree(directory, ignore_errors=True)
        promoted.append(name)
    print(f"promoted {len(promoted)} packages, staged failures {len(failed)}, compile {'clean' if ok else 'has errors'}")
    return promoted


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--packages", nargs="*")
    parser.add_argument("--soft", action="store_true")
    parser.add_argument("--light", action="store_true")
    parser.add_argument("--leaves", action="store_true")
    parser.add_argument("--drop-failures", action="store_true")
    parser.add_argument("--stage-missing", action="store_true")
    parser.add_argument("--promote", action="store_true")
    args = parser.parse_args()
    files_by = packages(LOOM)
    if args.stage_missing:
        copied = stage_missing(files_by)
        if args.promote:
            promote(copied)
        return 0
    present = set(packages(SRC))
    meatlib_pkgs = set(packages(MEATLIB))
    if args.packages:
        names = args.packages
    elif args.leaves:
        names = select_leaves(files_by, present)
    elif args.light:
        names = select_light(files_by, present)
    elif args.soft:
        names = select_soft(files_by, present, meatlib_pkgs)
    else:
        names = []
    if not names:
        print("no packages selected")
        return 1
    copied = copy_packages(names, files_by)
    print(f"copied {sum(len(v) for v in copied.values())} files in {len(copied)} packages")
    if args.drop_failures:
        return drop_until_clean(copied)
    ok, per_file = compile_errors()
    print("compile clean" if ok else f"errors in {len(per_file)} files")
    return 0 if ok else 1


if __name__ == "__main__":
    raise SystemExit(main())
