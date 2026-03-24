# wun

winnpixie's unreliable networking - a deep dive into working with UDP in Java

# Spec

## Packet

| Header        | Size (in bytes) |
|---------------|----------------:|
| Packet Id     |               1 |
| Peer Id       |               8 |
| Packet Length |               4 |
| Total         |              13 |
