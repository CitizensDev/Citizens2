Citizens2 README
================

Citizens is an NPC plugin for the Bukkit API. It was first released on March 5, 2011, and has since seen numerous updates. Citizens provides an API which developers can use to create their own NPC characters. More information on the API can be found on the API page of the Citizens Wiki (https://wiki.citizensnpcs.co/API).

Compatible with:
* Minecraft (for specific compatible version information, see https://wiki.citizensnpcs.co/Versions for info)
* CitizensAPI (for compiling purposes only)

Extra information
=================

Javadoc: http://jd.citizensnpcs.co

Spigot page: https://www.spigotmc.org/resources/citizens.13811

Developmental builds: https://ci.citizensnpcs.co/job/Citizens2/

For questions/help join our discord at: https://discord.gg/Q6pZGSR

------------

CitizensAPI README
==================

CitizensAPI is an API framework for developers to use. It provides methods for creating and maintaining NPCs, as well as attaching custom data to NPCs.

Compatible With:
- All versions of Minecraft with Spigot/Bukkit (http://wiki.citizensnpcs.co/Versions for more)

Maven
=====

IF YOU ARE USING THE API (which you probably should **not** be if you're just integrating the Citizens plugin!), Include CitizensAPI in your pom.xml like this: (If you are integrating your own plugin with the Citizens plugin, see https://wiki.citizensnpcs.co/API for information on appropriate maven linkage.)
```xml
<repository>
            <id>citizens-repo</id>
            <url>https://maven.citizensnpcs.co/repo</url>
</repository>
<dependency>
	<groupId>net.citizensnpcs</groupId>
	<artifactId>citizensapi</artifactId>
	<version>CITIZENS_VERSION</version>
	<type>jar</type>
	<scope>compile</scope>
</dependency>
```

The correct CITIZENS_VERSION to use can depend on your minecraft version. A list can be found here https://maven.citizensnpcs.co/#/repo/net/citizensnpcs/ - or you can use the version listed in the Citizens2 JAR you downloaded (e.g. `2.0.30-SNAPSHOT`).

Javadoc
=======

http://jd.citizensnpcs.co

Extra information
=================

Spigot page: https://www.spigotmc.org/resources/citizens.13811/

For questions/help join our discord at: https://discord.gg/Q6pZGSR
