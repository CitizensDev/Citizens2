Citizens2 README
================

Citizens is an NPC plugin for the Bukkit API. It was first released on March 5, 2011, and has since seen numerous updates. Citizens provides an API which developers can use to create their own NPC characters. More information on the API can be found on the API page of the Citizens Wiki (https://wiki.citizensnpcs.co/API).

Compatible with:
* Minecraft (for specific compatible version information, see https://wiki.citizensnpcs.co/Versions for info)
* CitizensAPI (for compiling purposes only)

Building
=================
The pom.xml requires you to have craftbukkit versions `1.8`, `1.10`, `1.11`, `1.12`, `1.13`, `1.14` and `1.15` versions 
installed in maven to compile. There is a handy shell script to automatically install all these versions, in the 
`/scripts` folder. _This process may take awhile_.

Therefore to compile
1. Ensure all craftbukkit versions are installed running the script `buildtools.sh` in the `scripts` directory.
2. Run `mvn clean package` in the base directory.

Extra information
=================

Javadoc: http://jd.citizensnpcs.co

Spigot page: https://www.spigotmc.org/resources/citizens.13811

Developmental builds: https://ci.citizensnpcs.co/job/Citizens2/

For questions/help join our discord at: https://discord.gg/Q6pZGSR
