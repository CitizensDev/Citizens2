Citizens welcomes contributors and pull requests. Feel free to join the [Discord](https://discord.gg/Q6pZGSR) for tips and tricks when writing your first pull request.

Before you get started, bear in mind that we do require a Contributor's License Agreement (CLA) to prevent licensing issues in the future.
Sign the CLA [here](https://cla-assistant.io/CitizensDev/Citizens2)

Development environment setup
=============================
1. Clone Citizens2 and CitizensAPI repos to your machine
2. Import the maven project to your IDE of choice. Citizens targets Java 8 - Java 9+ code should not be used.

Citizens is structured using an overarching maven project with several submodules for different parts of the codebase
`api` - the API, with a loosely defined boundary but mainly containing interfaces and events that are agnostic to the lifecycle of a Spigot plugin
`main` - the main Citizens codebase which implements commands and Spigot plugin-specific code
`dist` - the distribution subproject which will actually build your Citizens JAR with relevant submodules

3. Next, run Spigot's BuildTools.jar for all supported Minecraft versions to install the Spigot JARs to your local maven repo (see `dist/pom.xml` for the current list)
We recommend using the `dev` maven build profile which will only build for the latest Minecraft version. This saves you having to install 10 different old versions.
Try to build your first Citizens2 JAR using `mvn install dist/pom.xml -P dev`

Now you're ready to start creating a pull request!

Creating a pull request
=======================
1. Pull request your changes to the relevant Citizens repo using Github's pull request feature
2. Sign the CLA and make sure you own the rights to all of your contributions
3. Include JavaDocs for your code that other people might use, such as API methods
4. There are no specific style requirements at present