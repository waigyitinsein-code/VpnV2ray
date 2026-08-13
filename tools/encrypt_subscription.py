#!/usr/bin/env python3
"""Encrypt a UTF-8 V2Ray/Xray subscription file for VPN V2Ray.

The output is a text-only AES-256-GCM envelope, not a Base64 subscription:
    v1:<12-byte-nonce-in-hex>:<ciphertext-and-16-byte-auth-tag-in-hex>

Usage:
    python3 tools/encrypt_subscription.py \
      --key-hex <64-hex-character-key> \
      --input subscription/plaintext.example.txt \
      --output subscription/subscription.enc
"""

from __future__ import annotations

import argparse
import os
from pathlib import Path

from cryptography.hazmat.primitives.ciphers.aead import AESGCM

VERSION = "v1"
NONCE_SIZE = 12
KEY_SIZE = 32


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Encrypt a V2Ray/Xray subscription with AES-256-GCM."
    )
    parser.add_argument("--key-hex", required=True, help="AES-256 key as exactly 64 hex characters")
    parser.add_argument("--input", type=Path, required=True, help="UTF-8 plaintext subscription file")
    parser.add_argument("--output", type=Path, required=True, help="Destination .enc file")
    return parser.parse_args()


def parse_key(key_hex: str) -> bytes:
    try:
        key = bytes.fromhex(key_hex.strip())
    except ValueError as exc:
        raise SystemExit("--key-hex must contain hexadecimal characters only") from exc
    if len(key) != KEY_SIZE:
        raise SystemExit("--key-hex must contain exactly 64 hexadecimal characters (32 bytes)")
    return key


def main() -> None:
    args = parse_args()
    key = parse_key(args.key_hex)
    plaintext = args.input.read_bytes()
    if not plaintext.strip():
        raise SystemExit("Input subscription file is empty")

    nonce = os.urandom(NONCE_SIZE)
    encrypted = AESGCM(key).encrypt(nonce, plaintext, associated_data=None)
    envelope = f"{VERSION}:{nonce.hex()}:{encrypted.hex()}\n"

    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(envelope, encoding="utf-8", newline="\n")
    print(f"Encrypted {args.input} -> {args.output} ({len(plaintext)} plaintext bytes)")


if __name__ == "__main__":
    main()
