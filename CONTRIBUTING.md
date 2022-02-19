Setup
=====
Clone the CitizensAPI and Citizens2 repositories.
Citizens2 uses Maven subprojects for backwards compatibility with old Minecraft versions.

Contributing
============
Citizens welcomes open source contributions. Make sure you sign the CLA when making a pull request.

Building
======== 
Make sure you have installed all prerequisite Spigot versions using `java BuildTools.jar --rev <1.12.2|1.13.2|1.14.4|1.15.2...>`.
Build the Citizens JAR by running `maven install` in the main directory; the output JAR will be located at `dist/target/Citizens-xxx.jar`.