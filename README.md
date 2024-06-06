# ViaRewind
[![Latest Release](https://img.shields.io/github/v/release/ViaVersion/ViaRewind)](https://github.com/ViaVersion/ViaRewind/releases)
[![Build Status](https://github.com/ViaVersion/ViaRewind/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/ViaVersion/ViaRewind/actions)
[![Discord](https://img.shields.io/badge/chat-on%20discord-blue.svg)](https://viaversion.com/discord)

**ViaBackwards addon to allow 1.8.x and 1.7.x clients on newer server versions.** <br>
Runs on 1.9-latest servers or 1.8 clients and lower.

**You can override the included version in [ViaFabric](https://modrinth.com/mod/viafabric) or [ViaProxy](https://github.com/ViaVersion/ViaProxy):**
- in **ViaFabric**, put ViaRewind into the `mods` folder
- in **ViaProxy**, put ViaRewind into the `jars` folder

Note: when using ViaFabric <= 1.16.5 or J8 ViaProxy builds, you need [J8 ViaRewind](https://ci.viaversion.com/view/ViaRewind/job/ViaRewind-Java8/) builds.

**Requires [ViaVersion](https://hangar.papermc.io/ViaVersion/ViaVersion) and [ViaBackwards](https://hangar.papermc.io/ViaVersion/ViaBackwards) to be installed..**

Releases/Dev Builds
-
You can find releases in the following places:

- **Hangar (for our plugins)**: https://hangar.papermc.io/ViaVersion/ViaRewind
- **Modrinth (for our mods)**: https://modrinth.com/mod/viarewind
- **GitHub**: https://github.com/ViaVersion/ViaRewind/releases

Dev builds for **all** of our projects are on our Jenkins server:

- **Jenkins**: https://ci.viaversion.com/view/ViaRewind/

Other Links
-
**Maven:** https://repo.viaversion.com/

**Issue tracker:** https://github.com/ViaVersion/ViaRewind/issues

**List of contributors:** https://github.com/ViaVersion/ViaRewind/graphs/contributors

Building
-
After cloning this repository, build the project with Gradle by running `./gradlew build` and take the created jar out
of the `universal/build/libs` directory.

You need JDK 17 or newer to build ViaRewind.

License
-
This project is licensed under the [GNU General Public License Version 3](LICENSE).
