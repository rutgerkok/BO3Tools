[Home](http://dev.bukkit.org/projects/bo3tools) | **Source** | [Commands and permissions](https://dev.bukkit.org/projects/bo3tools/pages/commands) | [Changelog](https://dev.bukkit.org/projects/bo3tools/pages/changelog)

Tools to help with the creation of BO3s. See [here](http://dev.bukkit.org/projects/bo3tools) for more information. Feel free to open a pull request (I like them!), just make sure to use no tabs (use spaces!), LF line endings and try to keep your code formatting like me.


## Compatibility

BO3Tools has been updated to work with OpenTerrainGenerator version "1.12.2 - v6", available on the [releases](/../../releases) tab. It is no longer compatible with TerrainControl.


## Build Instructions

BO3Tools depends on OpenTerrainGenerator, installed in the local Maven repository. Due to a problem with the Forge build at the time of writing, you are advised to follow the procedure below:

 1. Check out the OpenTerrainGenerator sources:
```
git clone https://github.com/PG85/OpenTerrainGenerator
```
 2. Change into the project root directory:
```
cd OpenTerrainGenerator
```
 3. Edit `settings.gradle` to exclude `'platforms:forge'`:
```
include 'common', 'platforms:bukkit', 'releases'    
// Was: include 'common', 'platforms:bukkit', 'platforms:forge', 'releases'
```
 4. Install OpenTerrainGenerator JAR files to the local Maven respository:
```
./gradlew install
```
 5. Now you are ready to build BO3Tools. Check out the sources:
```
cd ..
git clone https://github.com/totemo/BO3Tools
```
 6. Change into the project root directory and build with Maven:
```
cd BO3Tools
mvn
```
   * The JAR file will be created in the `target/` sub-directory.

 
## License

The BSD License

Copyright (c) 2013, Rutger Kok

All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. Neither the name of the owner nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
