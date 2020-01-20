#!/bin/bash

#
# The original version of this code belongs to EssentialsX
# https://github.com/EssentialsX/Essentials/tree/2e818ac7090626a6682b19f2b8ce6fe385dbc170/scripts
#

mkdir -p .buildtools
pushd .buildtools

is_installed() {
    mvn dependency:get -q -Dartifact=$1 -DremoteRepositories=file://$HOME/.m2/repository 1>/dev/null 2>&1
    return $?
}

ensure_buildtools() {
    if [ ! -f "BuildTools.jar" ]; then
        echo "Downloading BuildTools..."
        if [[ "$OSTYPE" == "darwin"* ]]; then
            curl https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar -o BuildTools.jar
        else
            wget -O BuildTools.jar https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar
        fi
    fi
}

run_buildtools() {
    ensure_buildtools
    # Check if env var isnt empty, then run with xmx flag
    if [ ! -z "$BUILDTOOLS_XMX" ]; then
        echo "BUILDTOOLS_XMX Environment variable found. Running BuildTools with -Xmx$BUILDTOOLS_XMX"
        java -Xmx$BUILDTOOLS_XMX -jar BuildTools.jar --rev $1 --compile CRAFTBUKKIT
    else
        java -jar BuildTools.jar --rev $1 --compile CRAFTBUKKIT
    fi
    if [ $? -ne 0 ]; then
        echo "Running BuildTools for CB $1 failed! Aborting."
        popd
        exit 255
    else
        echo "Successfully built version $1"
    fi
}

# Check CB 1.8.8
is_installed org.bukkit:craftbukkit:1.8.8-R0.1-SNAPSHOT
is_18=$? # 0 = present, 1 = not present

# Check CB 1.10.2
is_installed org.bukkit:craftbukkit:1.10.2-R0.1-SNAPSHOT
is_110=$? # 0 = present, 1 = not present

# Check CB 1.11.2
is_installed org.bukkit:craftbukkit:1.11.2-R0.1-SNAPSHOT
is_111=$? # 0 = present, 1 = not present

# Check CB 1.12.2
is_installed org.bukkit:craftbukkit:1.12.2-R0.1-SNAPSHOT
is_112=$? # 0 = present, 1 = not present

# Check CB 1.13.2
is_installed org.bukkit:craftbukkit:1.13.2-R0.1-SNAPSHOT
is_113=$? # 0 = present, 1 = not present

# Check CB 1.14.4
is_installed org.bukkit:craftbukkit:1.14.4-R0.1-SNAPSHOT
is_114=$? # 0 = present, 1 = not present

# Check CB 1.15.1
is_installed org.bukkit:craftbukkit:1.15.1-R0.1-SNAPSHOT
is_115=$? # 0 = present, 1 = not present


if [ $is_18 -ne 0 ]; then
    echo "Installing CraftBukkit 1.8.8..."
    run_buildtools 1.8.8
else
    echo "CraftBukkit 1.8.8 installed; skipping BuildTools..."
fi

if [ $is_110 -ne 0 ]; then
    echo "Installing CraftBukkit 1.10.2..."
    run_buildtools 1.10.2
else
    echo "CraftBukkit 1.10.2 installed; skipping BuildTools..."
fi

if [ $is_111 -ne 0 ]; then
    echo "Installing CraftBukkit 1.11.2..."
    run_buildtools 1.11.2
else
    echo "CraftBukkit 1.11.2 installed; skipping BuildTools..."
fi

if [ $is_112 -ne 0 ]; then
    echo "Installing CraftBukkit 1.12.2..."
    run_buildtools 1.12.2
else
    echo "CraftBukkit 1.12.2 installed; skipping BuildTools..."
fi

if [ $is_113 -ne 0 ]; then
    echo "Installing CraftBukkit 1.13.2..."
    run_buildtools 1.13.2
else
    echo "CraftBukkit 1.13.2 installed; skipping BuildTools..."
fi

if [ $is_114 -ne 0 ]; then
    echo "Installing CraftBukkit 1.14.4..."
    run_buildtools 1.14.4
else
    echo "CraftBukkit 1.14.4 installed; skipping BuildTools..."
fi

if [ $is_115 -ne 0 ]; then
    echo "Installing CraftBukkit 1.15.1..."
    run_buildtools 1.15.1
else
    echo "CraftBukkit 1.15.1 installed; skipping BuildTools..."
fi

popd
