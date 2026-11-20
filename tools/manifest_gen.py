#!/usr/bin/env python3
"""Build the client update manifest for an Autisnautics release.

Reads pakku-lock.json (the resolved pack), writes manifest.json with one entry
per client-side file: relative path, download URL, sha1, size. Optionally packs
override directories into a zip and references it.

  manifest_gen.py --lock pakku-lock.json --version 0.1.40 \\
      --overrides-dir .pakku/client-overrides --overrides-dir .pakku/overrides \\
      --overrides-zip client-overrides-0.1.40.zip \\
      --overrides-url https://github.com/.../client-overrides-0.1.40.zip \\
      --changelog-file CHANGELOG_EXCERPT.md \\
      --out manifest.json
"""
from __future__ import annotations

import argparse
import hashlib
import json
import pathlib
import sys
import zipfile

# pakku project type -> directory inside the instance's minecraft/ folder
TYPE_DIRS = {
    "MOD": "mods",
    "RESOURCE_PACK": "resourcepacks",
    "SHADER": "shaderpacks",
    "DATAPACK": "datapacks",
}

CLIENT_SIDES = {"BOTH", "CLIENT", None}
# mirrors are the same artifact; prefer the one that never needs an API key
SOURCE_PREFERENCE = ("modrinth", "curseforge")

# files the updater must never overwrite, even if a pack zip contains them
OVERRIDE_SKIP_TOP = {
    "options.txt", "servers.dat", "saves", "logs", "screenshots",
    "crash-reports", "usercache.json", "usernamecache.json", ".autis",
}
OVERRIDE_SKIP_NAMES = {"autis-updater.jar"}
# build noise that must not ship to clients
OVERRIDE_SKIP_DIRS = {".git", "node_modules", ".cache", "dx", "exported", ".gradle", "run"}
OVERRIDE_SKIP_SUFFIXES = (".log", ".lock~", ".autis-part")


def pick_file(files: list[dict]) -> dict | None:
    for source in SOURCE_PREFERENCE:
        for f in files:
            if f.get("type") == source and f.get("url") and (f.get("hashes") or {}).get("sha1"):
                return f
    for source in SOURCE_PREFERENCE:
        for f in files:
            if f.get("type") == source and f.get("url"):
                return f
    for f in files:
        if f.get("url"):
            return f
    return None


def build_entries(lock: dict) -> tuple[list[dict], dict]:
    entries: list[dict] = []
    seen: set[str] = set()
    stats = {"projects": 0, "skipped_side": 0, "skipped_type": 0, "no_file": 0, "dupes": 0}

    for project in lock.get("projects", []):
        stats["projects"] += 1
        if project.get("side") not in CLIENT_SIDES:
            stats["skipped_side"] += 1
            continue
        kind = project.get("type", "MOD")
        directory = TYPE_DIRS.get(kind)
        if directory is None:
            stats["skipped_type"] += 1
            continue
        chosen = pick_file(project.get("files") or [])
        if not chosen:
            stats["no_file"] += 1
            continue
        rel = f"{directory}/{chosen['file_name']}"
        if rel in seen:
            stats["dupes"] += 1
            continue
        seen.add(rel)
        sha1 = (chosen.get("hashes") or {}).get("sha1")
        if not sha1:
            stats.setdefault("no_hash", 0)
            stats["no_hash"] += 1
            print(f"warn: no sha1 for {rel} — size-only verification", file=sys.stderr)
        entries.append({
            "path": rel,
            "url": chosen["url"],
            "sha1": (sha1 or "").lower(),
            "size": int(chosen.get("size") or 0),
        })

    entries.sort(key=lambda e: e["path"])
    return entries, stats


def sha1_file(path: pathlib.Path) -> str:
    h = hashlib.sha1()
    with path.open("rb") as fh:
        for chunk in iter(lambda: fh.read(1 << 20), b""):
            h.update(chunk)
    return h.hexdigest()


def build_overrides_zip(dirs: list[pathlib.Path], out: pathlib.Path) -> tuple[int, list[str]]:
    count = 0
    written: list[str] = []
    seen: set[str] = set()
    out.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(out, "w", zipfile.ZIP_DEFLATED, compresslevel=6) as zf:
        for root in dirs:
            if not root.is_dir():
                print(f"warn: overrides dir missing, skipping: {root}", file=sys.stderr)
                continue
            for path in sorted(root.rglob("*")):
                if not path.is_file():
                    continue
                arc = path.relative_to(root).as_posix()
                parts = arc.split("/")
                top = parts[0]
                if top in OVERRIDE_SKIP_TOP or path.name in OVERRIDE_SKIP_NAMES:
                    continue
                if any(p in OVERRIDE_SKIP_DIRS for p in parts[:-1]):
                    continue
                if path.name.endswith(OVERRIDE_SKIP_SUFFIXES):
                    continue
                if arc in seen:
                    continue
                seen.add(arc)
                zf.write(path, arc)
                written.append(arc)
                count += 1
    written.sort()
    return count, written


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--lock", default="pakku-lock.json")
    ap.add_argument("--version", required=True)
    ap.add_argument("--mc", default=None, help="defaults to lockfile mc_versions[0]")
    ap.add_argument("--loader", default=None, help="defaults to '<loader>-<version>' from lockfile")
    ap.add_argument("--changelog", default="")
    ap.add_argument("--changelog-file", default=None)
    ap.add_argument("--overrides-dir", action="append", default=[])
    ap.add_argument("--overrides-zip", default=None)
    ap.add_argument("--overrides-url", default=None)
    ap.add_argument("--out", default="manifest.json")
    args = ap.parse_args()

    lock_path = pathlib.Path(args.lock)
    lock = json.loads(lock_path.read_text(encoding="utf-8"))

    mc = args.mc or (lock.get("mc_versions") or ["?"])[0]
    loader = args.loader
    if not loader:
        loaders = lock.get("loaders") or {}
        loader = "-".join(f"{k}-{v}" for k, v in loaders.items()) or "unknown"

    entries, stats = build_entries(lock)

    changelog = args.changelog
    if args.changelog_file:
        changelog = pathlib.Path(args.changelog_file).read_text(encoding="utf-8")

    manifest = {
        "version": args.version,
        "mc": mc,
        "loader": loader,
        "changelog": changelog.strip()[:8000],
        "files": entries,
    }

    if args.overrides_zip and args.overrides_dir:
        dirs = [pathlib.Path(d) for d in args.overrides_dir]
        out_zip = pathlib.Path(args.overrides_zip)
        n, written = build_overrides_zip(dirs, out_zip)
        url = args.overrides_url or out_zip.name
        manifest["overrides"] = {
            "url": url,
            "sha1": sha1_file(out_zip),
            "size": out_zip.stat().st_size,
            "files": written,
        }
        print(f"overrides: {n} files, {out_zip.stat().st_size / 1024 / 1024:.1f} MB -> {out_zip}",
              file=sys.stderr)

    pathlib.Path(args.out).write_text(json.dumps(manifest, indent=1) + "\n", encoding="utf-8")
    total = sum(e["size"] for e in entries)
    print(f"manifest: {args.version} mc={mc} loader={loader} | {len(entries)} files "
          f"({total / 1024 / 1024:.1f} MB) | {stats}", file=sys.stderr)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
