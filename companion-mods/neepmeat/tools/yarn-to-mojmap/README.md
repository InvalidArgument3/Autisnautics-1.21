Yarn 1.20.1 to Mojmap 1.20.1 source rewrite.

Download into /tmp/remap, then:

1. yarn-1.20.1+build.10-v2.jar from maven.fabricmc.net, mappings/mappings.tiny as yarn.tiny
2. intermediary-1.20.1-v2.jar, mappings/mappings.tiny as intermediary.tiny
3. The 1.20.1 client mappings from the Mojang version manifest, as client-1.20.1.txt

python3 build_maps.py
python3 members.py
python3 rewrite.py <yarn-source-dir> <output-dir>

The rewrite changes Minecraft class names and method names that have one Mojmap target. It does not apply 1.20.1 to 1.21.1 API changes. Do not commit the Mojang mapping file.
