#!/usr/bin/env bash
# Generates a 2048-bit RSA key pair for JWT RS256 signing.
# Usage: bash scripts/generate-rsa-keys.sh [output-dir]
#   output-dir defaults to /run/secrets (recommended for Docker secret mounts)
#
# Example (local dev):
#   bash scripts/generate-rsa-keys.sh /tmp/jwt-keys
#   Then set in environment:
#     JWT_RSA_PRIVATE_KEY_PATH=/tmp/jwt-keys/private.pem
#     JWT_RSA_PUBLIC_KEY_PATH=/tmp/jwt-keys/public.pem

set -euo pipefail

OUT_DIR="${1:-/run/secrets}"
mkdir -p "$OUT_DIR"

# Generate RSA 2048 private key (PKCS#8 format — required by Java KeyFactory)
openssl genrsa -out "${OUT_DIR}/rsa-private-raw.pem" 2048 2>/dev/null
openssl pkcs8 -topk8 -nocrypt -in "${OUT_DIR}/rsa-private-raw.pem" -out "${OUT_DIR}/private.pem"
rm -f "${OUT_DIR}/rsa-private-raw.pem"

# Derive the public key
openssl rsa -in "${OUT_DIR}/private.pem" -pubout -out "${OUT_DIR}/public.pem" 2>/dev/null

chmod 600 "${OUT_DIR}/private.pem"
chmod 644 "${OUT_DIR}/public.pem"

echo "Keys generated:"
echo "  Private key : ${OUT_DIR}/private.pem"
echo "  Public key  : ${OUT_DIR}/public.pem"
echo ""
echo "Set these environment variables before starting the service:"
echo "  export JWT_RSA_PRIVATE_KEY_PATH=file:${OUT_DIR}/private.pem"
echo "  export JWT_RSA_PUBLIC_KEY_PATH=file:${OUT_DIR}/public.pem"
