CitizensAPI README
==================
CitizensAPI is an API framework for developers to use. It provides methods for creating and maintaining NPCs, as well as attaching custom data to NPCs.

Compatible With:
- All versions of Minecraft with Spigot/Bukkit (http://wiki.citizensnpcs.co/Versions for more)

Maven
=====
Include CitizensAPI in your pom.xml like this (set the artifactId to citizens if you want to access the full plugin details of Citizens):
```
<repository>
            <id>everything</id>
            <url>http://repo.citizensnpcs.co/</url>
</repository>
<dependency>
	<groupId>net.citizensnpcs</groupId>
	<artifactId>citizensapi</artifactId>
	<version>CITIZENS_VERSION</version>
	<type>jar</type>
	<scope>compile</scope>
</dependency>
```

The correct CITIZENS_VERSION to use can depend on your minecraft version. A list can be found here http://repo.citizensnpcs.co/net/citizensnpcs/citizensapi/ - or you can use the version listed in the Citizens2 JAR you downloaded (e.g. `2.0.20-SNAPSHOT`).

Javadoc
=======
http://jd.citizensnpcs.co

Extra information
=================
Spigot page: https://www.spigotmc.org/resources/citizens.13811/
For questions/help join our discord at: https://discord.gg/Q6pZGSR
