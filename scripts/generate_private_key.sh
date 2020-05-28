#!/bin/bash

# Generates a prime256v1 EC private key.
# This script requires openssl to be installed, see here:
## https://www.openssl.org/source/

pushd "$(dirname "${BASH_SOURCE[0]}")" > /dev/null || exit

rm -rf keys
mkdir keys
pushd keys > /dev/null || exit

# Generate a prime256v1 EC private key
# $1 = OUT Private key file
generate_private_key()
{
  openssl ecparam                             \
    -name prime256v1                          \
    -genkey                                   \
    -out "$1"                                 \
    -noout
}

generate_private_key private.pem

popd > /dev/null || exit
popd > /dev/null || exit