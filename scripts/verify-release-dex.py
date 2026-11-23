#!/usr/bin/env python3
"""Verify that critical WorkManager/Room symbols survive R8 in a Release APK."""

from __future__ import annotations

import argparse
import glob
import os
import re
import subprocess
import sys
import tempfile
import zipfile
from pathlib import Path


def _version_key(path: str) -> list[int]:
    return [int(part) for part in re.findall(r"\d+", Path(path).parent.name)]


def find_dexdump(explicit_path: str | None) -> str:
    if explicit_path:
        if not os.path.isfile(explicit_path):
            raise FileNotFoundError(f"dexdump not found at: {explicit_path}")
        return explicit_path

    android_home = os.environ.get("ANDROID_HOME") or os.environ.get("ANDROID_SDK_ROOT")
    if not android_home:
        raise RuntimeError("ANDROID_HOME / ANDROID_SDK_ROOT is not set and --dexdump was not provided")

    candidates = glob.glob(os.path.join(android_home, "build-tools", "*", "dexdump"))
    if not candidates:
        raise FileNotFoundError(f"No dexdump found under {android_home}/build-tools")
    candidates.sort(key=_version_key)
    return candidates[-1]


def verify_apk(apk_path: Path, dexdump_bin: str) -> None:
    if not apk_path.is_file():
        raise FileNotFoundError(f"Release APK not found: {apk_path}")

    with tempfile.TemporaryDirectory(prefix="igngbbs-dex-") as tmp_dir:
        dex_files: list[Path] = []
        with zipfile.ZipFile(apk_path, "r") as zf:
            for member in sorted(zf.namelist()):
                if re.fullmatch(r"classes\d*\.dex", member):
                    out = Path(tmp_dir) / member
                    out.write_bytes(zf.read(member))
                    dex_files.append(out)

        if not dex_files:
            raise AssertionError(f"No classes*.dex entries found in {apk_path}")

        chunks: list[str] = []
        for dex in dex_files:
            proc = subprocess.run(
                [dexdump_bin, str(dex)],
                check=True,
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE,
                text=True,
                errors="replace",
            )
            chunks.append(proc.stdout)

    text = "\n".join(chunks)
    classes: dict[str, str] = {}
    for section in text.split("\nClass #")[1:]:
        for line in section.splitlines()[:5]:
            if "Class descriptor  : '" in line:
                classes[line.split("'")[1]] = section
                break

    wd = classes.get("Landroidx/work/impl/WorkDatabase_Impl;", "")
    if "name          : '<init>'" not in wd:
        raise AssertionError("WorkDatabase_Impl.<init>() was stripped by R8!")

    nw = classes.get("Lnet/igng/bbs/NotificationSyncWorker;", "")
    if "name          : '<init>'" not in nw:
        raise AssertionError("NotificationSyncWorker.<init>() was stripped by R8!")

    vm = nw.split("Virtual methods   -")[-1]
    if "name          :" not in vm:
        raise AssertionError("NotificationSyncWorker virtual methods (doWork) were stripped by R8!")

    print(
        f"Release DEX check passed ({apk_path.name}, {len(dex_files)} dex file(s)): "
        "WorkDatabase_Impl.<init>() and NotificationSyncWorker methods preserved."
    )


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("apk", help="Path to the Release APK file to inspect")
    parser.add_argument("--dexdump", help="Explicit path to Android build-tools dexdump binary")
    args = parser.parse_args()

    dexdump_bin = find_dexdump(args.dexdump)
    verify_apk(Path(args.apk), dexdump_bin)
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as exc:
        print(f"Release DEX check FAILED: {exc}", file=sys.stderr)
        raise SystemExit(1)
