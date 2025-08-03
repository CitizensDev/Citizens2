/*
 * This file is part of adventure-platform, licensed under the MIT License.
 *
 * Copyright (c) 2018-2020 KyoriPowered
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.citizensnpcs.api.util;

import static java.lang.invoke.MethodHandles.insertArguments;
import static java.util.Objects.requireNonNull;
import static net.kyori.adventure.platform.bukkit.BukkitComponentSerializer.gson;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;

/**
 * A component serializer for {@code net.minecraft.server.<version>.IChatBaseComponent}.
 *
 * <p>
 * Due to Bukkit version namespaces, the return type does not reflect the actual type.
 * </p>
 *
 * <p>
 * Color downsampling will be performed as necessary for the running server version.
 * </p>
 *
 * <p>
 * If not {@link #isSupported()}, an {@link UnsupportedOperationException} will be thrown on any serialize or
 * deserialize operations.
 * </p>
 *
 * @see #get()
 * @since 4.0.0
 */
@ApiStatus.Experimental // due to direct server implementation references
public final class TemporaryMinecraftComponentSerializer implements ComponentSerializer<Component, Component, Object> {
    @Override
    public @NotNull Component deserialize(final @NotNull Object input) {
        if (!SUPPORTED)
            throw INITIALIZATION_ERROR.get();

        try {
            final Object element;
            if (TEXT_SERIALIZER_SERIALIZE_TREE != null) {
                element = TEXT_SERIALIZER_SERIALIZE_TREE.invoke(input);
            } else if (MC_TEXT_GSON != null) {
                element = ((Gson) MC_TEXT_GSON).toJsonTree(input);
            } else if (COMPONENTSERIALIZATION_CODEC_ENCODE != null && CREATE_SERIALIZATION_CONTEXT != null) {
                final Object serializationContext = CREATE_SERIALIZATION_CONTEXT.bindTo(REGISTRY_ACCESS)
                        .invoke(JSON_OPS_INSTANCE);
                final Object result = COMPONENTSERIALIZATION_CODEC_ENCODE.invoke(input, serializationContext, null);
                final Method getOrThrow = result.getClass().getMethod("getOrThrow", java.util.function.Function.class);
                final Object jsonElement = getOrThrow.invoke(result,
                        (java.util.function.Function<Throwable, RuntimeException>) RuntimeException::new);
                return gson().serializer().fromJson(jsonElement.toString(), Component.class);
            } else {
                return gson().deserialize((String) TEXT_SERIALIZER_SERIALIZE.invoke(input));
            }
            return gson().serializer().fromJson(element.toString(), Component.class);
        } catch (final Throwable error) {
            throw new UnsupportedOperationException(error);
        }
    }

    @Override
    public @NotNull Object serialize(final @NotNull Component component) {
        if (!SUPPORTED)
            throw INITIALIZATION_ERROR.get();

        if (TEXT_SERIALIZER_DESERIALIZE_TREE != null || MC_TEXT_GSON != null) {
            final JsonElement json = gson().serializer().toJsonTree(component);
            try {
                if (TEXT_SERIALIZER_DESERIALIZE_TREE != null) {
                    final Object unRelocatedJsonElement = PARSE_JSON.invoke(JSON_PARSER_INSTANCE, json.toString());
                    return TEXT_SERIALIZER_DESERIALIZE_TREE.invoke(unRelocatedJsonElement);
                }
                return ((Gson) MC_TEXT_GSON).fromJson(json, CLASS_CHAT_COMPONENT);
            } catch (final Throwable error) {
                throw new UnsupportedOperationException(error);
            }
        } else {
            try {
                if (COMPONENTSERIALIZATION_CODEC_DECODE != null && CREATE_SERIALIZATION_CONTEXT != null) {
                    final Object serializationContext = CREATE_SERIALIZATION_CONTEXT.bindTo(REGISTRY_ACCESS)
                            .invoke(JSON_OPS_INSTANCE);
                    final Object result = COMPONENTSERIALIZATION_CODEC_DECODE.invoke(serializationContext,
                            gson().serializeToTree(component));
                    final Method getOrThrow = result.getClass().getMethod("getOrThrow",
                            java.util.function.Function.class);
                    final Object pair = getOrThrow.invoke(result,
                            (java.util.function.Function<Throwable, RuntimeException>) RuntimeException::new);
                    final Method getFirst = pair.getClass().getMethod("getFirst");
                    return getFirst.invoke(pair);
                }
                return TEXT_SERIALIZER_DESERIALIZE.invoke(gson().serialize(component));
            } catch (final Throwable error) {
                throw new UnsupportedOperationException(error);
            }
        }
    }

    /**
     * Gets a class by the first name available.
     *
     * @param classNames
     *            an array of class names to try in order
     * @return a class or {@code null} if not found
     */
    public static @Nullable Class<?> findClass(final @Nullable String @NotNull... classNames) {
        for (final String clazz : classNames) {
            if (clazz == null)
                continue;

            try {
                final Class<?> classObj = Class.forName(clazz);
                return classObj;
            } catch (final ClassNotFoundException e) {
            }
        }
        return null;
    }

    /**
     * Gets a handle for a class constructor.
     *
     * @param holderClass
     *            a class
     * @param parameterClasses
     *            an array of method parameter classes
     * @return a method handle or {@code null} if not found
     */
    public static @Nullable MethodHandle findConstructor(final @Nullable Class<?> holderClass,
            final @Nullable Class<?>... parameterClasses) {
        if (holderClass == null)
            return null;
        for (final Class<?> parameterClass : parameterClasses) {
            if (parameterClass == null)
                return null;
        }
        try {
            return LOOKUP.findConstructor(holderClass, MethodType.methodType(void.class, parameterClasses));
        } catch (final NoSuchMethodException | IllegalAccessException e) {
            return null;
        }
    }

    /**
     * Gets a {@code org.bukkit.craftbukkit} class.
     *
     * @param className
     *            a class name, without the {@code org.bukkit.craftbukkit} prefix
     * @return a class or {@code null} if not found
     */
    public static @Nullable Class<?> findCraftClass(final @NotNull String className) {
        final String craftClassName = findCraftClassName(className);
        if (craftClassName == null) {
            return null;
        }
        return findClass(craftClassName);
    }

    /**
     * Gets a {@code org.bukkit.craftbukkit} class.
     *
     * @param className
     *            a class name, without the {@code org.bukkit.craftbukkit} prefix
     * @param superClass
     *            a super class
     * @param <T>
     *            a super type
     * @return a class or {@code null} if not found
     */
    public static <T> @Nullable Class<? extends T> findCraftClass(final @NotNull String className,
            final @NotNull Class<T> superClass) {
        final Class<?> craftClass = findCraftClass(className);
        if (craftClass == null || !requireNonNull(superClass, "superClass").isAssignableFrom(craftClass)) {
            return null;
        }
        return craftClass.asSubclass(superClass);
    }

    /**
     * Gets a {@code org.bukkit.craftbukkit} class name.
     *
     * @param className
     *            a class name, without the {@code org.bukkit.craftbukkit} prefix
     * @return a class name or {@code null} if not found
     */
    public static @Nullable String findCraftClassName(final @NotNull String className) {
        return isCraftBukkit() ? PREFIX_CRAFTBUKKIT + VERSION + className : null;
    }

    /**
     * Gets an enum value.
     *
     * @param enumClass
     *            an enum class
     * @param enumName
     *            an enum name
     * @return an enum value or {@code null} if not found
     */
    public static @Nullable Object findEnum(final @Nullable Class<?> enumClass, final @NotNull String enumName) {
        return findEnum(enumClass, enumName, Integer.MAX_VALUE);
    }

    /**
     * Gets an enum value.
     *
     * @param enumClass
     *            an enum class
     * @param enumName
     *            an enum name
     * @param enumFallbackOrdinal
     *            an enum ordinal, when the name is not found
     * @return an enum value or {@code null} if not found
     */
    @SuppressWarnings("unchecked")
    public static @Nullable Object findEnum(final @Nullable Class<?> enumClass, final @NotNull String enumName,
            final int enumFallbackOrdinal) {
        if (enumClass == null || !Enum.class.isAssignableFrom(enumClass)) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass.asSubclass(Enum.class), enumName);
        } catch (final IllegalArgumentException e) {
            final Object[] constants = enumClass.getEnumConstants();
            if (constants.length > enumFallbackOrdinal) {
                return constants[enumFallbackOrdinal];
            }
        }
        return null;
    }

    /**
     * Gets a class field if it exists and is of the appropriate type and makes it accessible.
     *
     * @param holderClass
     *            a class
     * @param expectedType
     *            the expected type of the field
     * @param fieldNames
     *            a field name
     * @return an accessible field
     */
    public static @Nullable Field findField(final @Nullable Class<?> holderClass, final @Nullable Class<?> expectedType,
            final @NotNull String... fieldNames) {
        if (holderClass == null)
            return null;

        Field field;
        for (final String fieldName : fieldNames) {
            try {
                field = holderClass.getDeclaredField(fieldName);
            } catch (final NoSuchFieldException ex) {
                continue;
            }
            field.setAccessible(true);
            if (expectedType != null && !expectedType.isAssignableFrom(field.getType())) {
                continue;
            }
            return field;
        }
        return null;
    }

    /**
     * Gets a class field if possible and makes it accessible.
     *
     * @param holderClass
     *            a class
     * @param fieldName
     *            a field name
     * @return an accessible field
     */
    public static @Nullable Field findField(final @Nullable Class<?> holderClass, final @NotNull String... fieldName) {
        return findField(holderClass, null, fieldName);
    }

    /**
     * Return a method handle that can get the value of the provided field.
     *
     * @param field
     *            the field to get
     * @return a handle, if accessible
     */
    public static @Nullable MethodHandle findGetterOf(final @Nullable Field field) {
        if (field == null)
            return null;

        try {
            return LOOKUP.unreflectGetter(field);
        } catch (final IllegalAccessException e) {
            return null;
        }
    }

    /**
     * Get a {@code net.minecraft} class.
     *
     * @param classNames
     *            a class name, without the {@code net.minecraft} prefix
     * @return a class name or {@code null} if not found
     */
    public static @Nullable Class<?> findMcClass(final @NotNull String... classNames) {
        for (final String clazz : classNames) {
            final String nmsClassName = findMcClassName(clazz);
            if (nmsClassName != null) {
                final Class<?> candidate = findClass(nmsClassName);
                if (candidate != null) {
                    return candidate;
                }
            }
        }
        return null;
    }

    /**
     * Gets a {@code net.minecraft} class name.
     *
     * @param className
     *            a class name, without the {@code net.minecraft} prefix
     * @return a class name or {@code null} if not found
     */
    public static @Nullable String findMcClassName(final @NotNull String className) {
        return isCraftBukkit() ? PREFIX_MC + className : null;
    }

    /**
     * Gets a handle for a class method.
     *
     * @param holderClass
     *            a class
     * @param methodName
     *            a method name
     * @param returnClass
     *            a method return class
     * @param parameterClasses
     *            an array of method parameter classes
     * @return a method handle or {@code null} if not found
     */
    public static @Nullable MethodHandle findMethod(final @Nullable Class<?> holderClass, final String methodName,
            final @Nullable Class<?> returnClass, final Class<?>... parameterClasses) {
        return findMethod(holderClass, new String[] { methodName }, returnClass, parameterClasses);
    }

    /**
     * Gets a handle for a class method.
     *
     * @param holderClass
     *            a class
     * @param methodNames
     *            a method name
     * @param returnClass
     *            a method return class
     * @param parameterClasses
     *            an array of method parameter classes
     * @return a method handle or {@code null} if not found
     */
    public static @Nullable MethodHandle findMethod(final @Nullable Class<?> holderClass,
            final @Nullable String @NotNull [] methodNames, final @Nullable Class<?> returnClass,
            final Class<?>... parameterClasses) {
        if (holderClass == null || returnClass == null)
            return null;
        for (final Class<?> parameterClass : parameterClasses) {
            if (parameterClass == null)
                return null;
        }
        for (final String methodName : methodNames) {
            if (methodName == null)
                continue;
            try {
                return LOOKUP.findVirtual(holderClass, methodName,
                        MethodType.methodType(returnClass, parameterClasses));
            } catch (final NoSuchMethodException | IllegalAccessException e) {
            }
        }
        return null;
    }

    /**
     * Get a {@code net.minecraft.server} class.
     *
     * @param className
     *            a class name, without the {@code net.minecraft.server} prefix
     * @return a class name or {@code null} if not found
     */
    public static @Nullable Class<?> findNmsClass(final @NotNull String className) {
        final String nmsClassName = findNmsClassName(className);
        if (nmsClassName == null) {
            return null;
        }
        return findClass(nmsClassName);
    }

    /**
     * Gets a {@code net.minecraft.server} class name.
     *
     * @param className
     *            a class name, without the {@code net.minecraft.server} prefix
     * @return a class name or {@code null} if not found
     */
    public static @Nullable String findNmsClassName(final @NotNull String className) {
        return isCraftBukkit() ? PREFIX_NMS + VERSION + className : null;
    }

    /**
     * Return a method handle that can set the value of the provided field.
     *
     * @param field
     *            the field to set
     * @return a handle, if accessible
     */
    public static @Nullable MethodHandle findSetterOf(final @Nullable Field field) {
        if (field == null)
            return null;

        try {
            return LOOKUP.unreflectSetter(field);
        } catch (final IllegalAccessException e) {
            return null;
        }
    }

    /**
     * Gets a handle for a class method.
     *
     * @param holderClass
     *            a class
     * @param methodNames
     *            a method name
     * @param returnClass
     *            a method return class
     * @param parameterClasses
     *            an array of method parameter classes
     * @return a method handle or {@code null} if not found
     */
    public static @Nullable MethodHandle findStaticMethod(final @Nullable Class<?> holderClass,
            final String methodNames, final @Nullable Class<?> returnClass, final Class<?>... parameterClasses) {
        return findStaticMethod(holderClass, new String[] { methodNames }, returnClass, parameterClasses);

    }

    /**
     * Gets a handle for a class method.
     *
     * @param holderClass
     *            a class
     * @param methodNames
     *            a method name
     * @param returnClass
     *            a method return class
     * @param parameterClasses
     *            an array of method parameter classes
     * @return a method handle or {@code null} if not found
     */
    public static @Nullable MethodHandle findStaticMethod(final @Nullable Class<?> holderClass,
            final String[] methodNames, final @Nullable Class<?> returnClass, final Class<?>... parameterClasses) {
        if (holderClass == null || returnClass == null)
            return null;
        for (final Class<?> parameterClass : parameterClasses) {
            if (parameterClass == null)
                return null;
        }
        for (final String methodName : methodNames) {
            try {
                return LOOKUP.findStatic(holderClass, methodName, MethodType.methodType(returnClass, parameterClasses));
            } catch (final NoSuchMethodException | IllegalAccessException e) {
            }
        }
        return null;
    }

    /**
     * Gets the component serializer.
     *
     * @return a component serializer
     * @since 4.0.0
     */
    public static @NotNull TemporaryMinecraftComponentSerializer get() {
        return INSTANCE;
    }

    /**
     * Gets whether a class is available.
     *
     * @param classNames
     *            an array of class names to try in order
     * @return if the class is loaded
     */
    public static boolean hasClass(final @NotNull String... classNames) {
        return findClass(classNames) != null;
    }

    /**
     * Gets whether a class has a method.
     *
     * @param holderClass
     *            a class
     * @param names
     *            field name candidates, will be checked in order
     * @param type
     *            the field type
     * @return if the method exists
     */
    public static boolean hasField(final @Nullable Class<?> holderClass, final Class<?> type, final String... names) {
        if (holderClass == null)
            return false;

        for (final String name : names) {
            try {
                final Field field = holderClass.getDeclaredField(name);
                if (field.getType() == type)
                    return true;
            } catch (final NoSuchFieldException e) {
                // continue
            }
        }
        return false;
    }

    /**
     * Gets whether a class has a method.
     *
     * @param holderClass
     *            a class
     * @param methodName
     *            a method name
     * @param parameterClasses
     *            an array of method parameter classes
     * @return if the method exists
     */
    public static boolean hasMethod(final @Nullable Class<?> holderClass, final String methodName,
            final Class<?>... parameterClasses) {
        return hasMethod(holderClass, new String[] { methodName }, parameterClasses);
    }

    /**
     * Gets whether a class has a method.
     *
     * @param holderClass
     *            a class
     * @param methodNames
     *            a method name
     * @param parameterClasses
     *            an array of method parameter classes
     * @return if the method exists
     */
    public static boolean hasMethod(final @Nullable Class<?> holderClass, final String[] methodNames,
            final Class<?>... parameterClasses) {
        if (holderClass == null)
            return false;
        for (final Class<?> parameterClass : parameterClasses) {
            if (parameterClass == null)
                return false;
        }
        for (final String methodName : methodNames) {
            try {
                holderClass.getMethod(methodName, parameterClasses);
                return true;
            } catch (final NoSuchMethodException e) {
            }
        }
        return false;
    }

    /**
     * Gets whether CraftBukkit is available.
     *
     * @return if CraftBukkit is available
     */
    public static boolean isCraftBukkit() {
        return VERSION != null;
    }

    /**
     * Gets whether this serializer is supported.
     *
     * @return if the serializer is supported.
     * @since 4.0.0
     */
    public static boolean isSupported() {
        return SUPPORTED;
    }

    /**
     * Gets the singleton method handle lookup.
     *
     * @return the method handle lookup
     */
    public static MethodHandles.@NotNull Lookup lookup() {
        return LOOKUP;
    }

    /**
     * Gets a {@code net.minecraft} class.
     *
     * @param className
     *            a class name, without the {@code net.minecraft} prefix
     * @return a class
     * @throws NullPointerException
     *             if the class was not found
     */
    public static @NotNull Class<?> needClass(final @Nullable String @NotNull... className) {
        return requireNonNull(findClass(className),
                "Could not find class from candidates" + Arrays.toString(className));
    }

    /**
     * Gets a {@code org.bukkit.craftbukkit} class.
     *
     * @param className
     *            a class name, without the {@code org.bukkit.craftbukkit} prefix
     * @return a class
     * @throws NullPointerException
     *             if the class was not found
     */
    public static @NotNull Class<?> needCraftClass(final @NotNull String className) {
        return requireNonNull(findCraftClass(className), "Could not find org.bukkit.craftbukkit class " + className);
    }

    /**
     * Gets a class field and makes it accessible.
     *
     * @param holderClass
     *            a class
     * @param fieldName
     *            a field name
     * @return an accessible field
     * @throws NoSuchFieldException
     *             when thrown by {@link Class#getDeclaredField(String)}
     */
    public static @NotNull Field needField(final @NotNull Class<?> holderClass, final @NotNull String fieldName)
            throws NoSuchFieldException {
        final Field field = holderClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }

    /**
     * Gets a {@code net.minecraft} class.
     *
     * @param className
     *            a class name, without the {@code net.minecraft} prefix
     * @return a class
     * @throws NullPointerException
     *             if the class was not found
     */
    public static @NotNull Class<?> needMcClass(final @NotNull String... className) {
        return requireNonNull(findMcClass(className),
                "Could not find net.minecraft class from candidates" + Arrays.toString(className));
    }

    /**
     * Gets a {@code net.minecraft.server} class.
     *
     * @param className
     *            a class name, without the {@code org.bukkit.craftbukkit} prefix
     * @return a class
     * @throws NullPointerException
     *             if the class was not found
     */
    public static @NotNull Class<?> needNmsClass(final @NotNull String className) {
        return requireNonNull(findNmsClass(className), "Could not find net.minecraft.server class " + className);
    }

    /**
     * Search a handle for a class method.
     *
     * @param holderClass
     *            a class
     * @param modifier
     *            method modifiers
     * @param methodName
     *            a method name
     * @param returnClass
     *            a method return class
     * @param parameterClasses
     *            an array of method parameter classes
     * @return a method handle or {@code null} if not found
     */
    public static MethodHandle searchMethod(final @Nullable Class<?> holderClass, final @Nullable Integer modifier,
            final String methodName, final @Nullable Class<?> returnClass, final Class<?>... parameterClasses) {
        return searchMethod(holderClass, modifier, new String[] { methodName }, returnClass, parameterClasses);
    }

    /**
     * Search a handle for a class method.
     *
     * @param holderClass
     *            a class
     * @param modifier
     *            method modifiers
     * @param methodNames
     *            a method names
     * @param returnClass
     *            a method return class
     * @param parameterClasses
     *            an array of method parameter classes
     * @return a method handle or {@code null} if not found
     */
    public static MethodHandle searchMethod(final @Nullable Class<?> holderClass, final @Nullable Integer modifier,
            final @Nullable String @NotNull [] methodNames, final @Nullable Class<?> returnClass,
            final Class<?>... parameterClasses) {
        if (holderClass == null || returnClass == null)
            return null;
        for (final Class<?> parameterClass : parameterClasses) {
            if (parameterClass == null)
                return null;
        }
        for (final String methodName : methodNames) {
            if (methodName == null)
                continue;
            try {
                if (modifier != null && Modifier.isStatic(modifier)) {
                    return LOOKUP.findStatic(holderClass, methodName,
                            MethodType.methodType(returnClass, parameterClasses));
                } else {
                    return LOOKUP.findVirtual(holderClass, methodName,
                            MethodType.methodType(returnClass, parameterClasses));
                }
            } catch (final NoSuchMethodException | IllegalAccessException e) {
            }
        }
        for (final Method method : holderClass.getDeclaredMethods()) {
            if ((modifier == null || (method.getModifiers() & modifier) == 0)
                    || !Arrays.equals(method.getParameterTypes(), parameterClasses))
                continue;
            try {
                if (Modifier.isStatic(modifier)) {
                    return LOOKUP.findStatic(holderClass, method.getName(),
                            MethodType.methodType(returnClass, parameterClasses));
                } else {
                    return LOOKUP.findVirtual(holderClass, method.getName(),
                            MethodType.methodType(returnClass, parameterClasses));
                }
            } catch (final NoSuchMethodException | IllegalAccessException e) {
            }
        }
        return null;
    }

    private static final @Nullable Class<?> CLASS_CHAT_COMPONENT;
    private static final @Nullable Class<?> CLASS_COMPONENT_SERIALIZATION;
    private static final @Nullable Class<?> CLASS_CRAFT_REGISTRY;
    private static final @Nullable Class<?> CLASS_HOLDERLOOKUP_PROVIDER;
    private static final @Nullable Class<?> CLASS_JSON_DESERIALIZER;
    private static final @Nullable Class<?> CLASS_JSON_ELEMENT = findClass("com.goo".concat("gle.gson.JsonElement"));
    private static final @Nullable Class<?> CLASS_JSON_OPS = findClass("com.mo".concat("jang.serialization.JsonOps"));
    private static final @Nullable Class<?> CLASS_JSON_PARSER = findClass("com.goo".concat("gle.gson.JsonParser"));
    private static final @Nullable Class<?> CLASS_REGISTRY_ACCESS;
    private static final MethodHandle COMPONENTSERIALIZATION_CODEC_DECODE;
    private static final MethodHandle COMPONENTSERIALIZATION_CODEC_ENCODE;
    private static final String CRAFT_SERVER = "CraftServer";
    private static final MethodHandle CREATE_SERIALIZATION_CONTEXT;
    private static final @Nullable MethodHandle GET_REGISTRY;
    private static final AtomicReference<RuntimeException> INITIALIZATION_ERROR = new AtomicReference<>(
            new UnsupportedOperationException());
    private static final TemporaryMinecraftComponentSerializer INSTANCE = new TemporaryMinecraftComponentSerializer();
    private static final Object JSON_OPS_INSTANCE;
    private static final Object JSON_PARSER_INSTANCE;
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final Object MC_TEXT_GSON;
    private static final @Nullable MethodHandle PARSE_JSON = findMethod(CLASS_JSON_PARSER, "parse", CLASS_JSON_ELEMENT,
            String.class);
    private static final String PREFIX_CRAFTBUKKIT = "org.bukkit.craftbukkit";
    private static final String PREFIX_MC = "net.minecraft.";
    private static final String PREFIX_NMS = "net.minecraft.server";
    private static final Object REGISTRY_ACCESS;
    private static final boolean SUPPORTED;
    private static final MethodHandle TEXT_SERIALIZER_DESERIALIZE;
    private static final MethodHandle TEXT_SERIALIZER_DESERIALIZE_TREE;
    private static final MethodHandle TEXT_SERIALIZER_SERIALIZE;
    private static final MethodHandle TEXT_SERIALIZER_SERIALIZE_TREE;

    private static final @Nullable String VERSION;

    static {
        final Class<?> serverClass = Bukkit.getServer().getClass(); // TODO: use reflection here too?
        if (!serverClass.getSimpleName().equals(CRAFT_SERVER)) {
            VERSION = null;
        } else if (serverClass.getName().equals(PREFIX_CRAFTBUKKIT + "." + CRAFT_SERVER)) {
            VERSION = ".";
        } else {
            String name = serverClass.getName();
            name = name.substring(PREFIX_CRAFTBUKKIT.length());
            name = name.substring(0, name.length() - CRAFT_SERVER.length());
            VERSION = name;
        }
        CLASS_JSON_DESERIALIZER = findClass("com.goo".concat("gle.gson.JsonDeserializer"));
        CLASS_REGISTRY_ACCESS = findClass(findMcClassName("core.IRegistryCustom"),
                findMcClassName("core.RegistryAccess"));
        CLASS_CRAFT_REGISTRY = findCraftClass("CraftRegistry");
        GET_REGISTRY = findStaticMethod(CLASS_CRAFT_REGISTRY, "getMinecraftRegistry", CLASS_REGISTRY_ACCESS);
        CLASS_CHAT_COMPONENT = findClass(findNmsClassName("IChatBaseComponent"),
                findMcClassName("network.chat.IChatBaseComponent"), findMcClassName("network.chat.Component"));
        CLASS_COMPONENT_SERIALIZATION = findClass(findMcClassName("network.chat.ComponentSerialization"));
        CLASS_HOLDERLOOKUP_PROVIDER = findClass(findMcClassName("core.HolderLookup$Provider"), // Paper mapping
                findMcClassName("core.HolderLookup$a") // Spigot mapping
        );
        Object gson = null;
        Object jsonOpsInstance = null;
        Object jsonParserInstance = null;
        Object registryAccessInstance = null;
        MethodHandle textSerializerDeserialize = null;
        MethodHandle textSerializerSerialize = null;
        MethodHandle textSerializerDeserializeTree = null;
        MethodHandle textSerializerSerializeTree = null;
        MethodHandle codecEncode = null;
        MethodHandle codecDecode = null;
        MethodHandle createContext = null;

        try {
            if (CLASS_JSON_OPS != null) {
                final Field instanceField = CLASS_JSON_OPS.getField("INSTANCE");
                instanceField.setAccessible(true);
                jsonOpsInstance = instanceField.get(null);
            }
            if (CLASS_JSON_PARSER != null) {
                jsonParserInstance = CLASS_JSON_PARSER.getDeclaredConstructor().newInstance();
            }
            if (CLASS_CHAT_COMPONENT != null) {
                final Object registryAccess = GET_REGISTRY != null ? GET_REGISTRY.invoke() : null;
                registryAccessInstance = registryAccess;
                // Chat serializer //
                final Class<?> chatSerializerClass = Arrays.stream(CLASS_CHAT_COMPONENT.getClasses()).filter(c -> {
                    if (CLASS_JSON_DESERIALIZER != null) {
                        return CLASS_JSON_DESERIALIZER.isAssignableFrom(c);
                    } else {
                        for (final Class<?> itf : c.getInterfaces()) {
                            if (itf.getSimpleName().equals("JsonDeserializer")) {
                                return true;
                            }
                        }
                        return false;
                    }
                }).findAny().orElse(findNmsClass("ChatSerializer")); // 1.7.10 compat
                if (chatSerializerClass != null) {
                    final Field gsonField = Arrays.stream(chatSerializerClass.getDeclaredFields())
                            .filter(m -> Modifier.isStatic(m.getModifiers()))
                            .filter(m -> m.getType().equals(Gson.class)).findFirst().orElse(null);
                    if (gsonField != null) {
                        gsonField.setAccessible(true);
                        gson = gsonField.get(null);
                    }
                }
                final List<Class<?>> candidates = new ArrayList<>();
                if (chatSerializerClass != null) {
                    candidates.add(chatSerializerClass);
                }
                candidates.addAll(Arrays.asList(CLASS_CHAT_COMPONENT.getClasses()));
                for (final Class<?> serializerClass : candidates) {
                    final Method[] declaredMethods = serializerClass.getDeclaredMethods();
                    final Method deserialize = Arrays.stream(declaredMethods)
                            .filter(m -> Modifier.isStatic(m.getModifiers()))
                            .filter(m -> CLASS_CHAT_COMPONENT.isAssignableFrom(m.getReturnType()))
                            .filter(m -> m.getParameterCount() == 1 && m.getParameterTypes()[0].equals(String.class))
                            .min(Comparator.comparing(Method::getName)) // prefer the #a method
                            .orElse(null);
                    final Method serialize = Arrays.stream(declaredMethods)
                            .filter(m -> Modifier.isStatic(m.getModifiers()))
                            .filter(m -> m.getReturnType().equals(String.class))
                            .filter(m -> m.getParameterCount() == 1
                                    && CLASS_CHAT_COMPONENT.isAssignableFrom(m.getParameterTypes()[0]))
                            .findFirst().orElse(null);
                    final Method deserializeTree = Arrays.stream(declaredMethods)
                            .filter(m -> Modifier.isStatic(m.getModifiers()))
                            .filter(m -> CLASS_CHAT_COMPONENT.isAssignableFrom(m.getReturnType()))
                            .filter(m -> m.getParameterCount() == 1
                                    && m.getParameterTypes()[0].equals(CLASS_JSON_ELEMENT))
                            .findFirst().orElse(null);
                    final Method serializeTree = Arrays.stream(declaredMethods)
                            .filter(m -> Modifier.isStatic(m.getModifiers()))
                            .filter(m -> m.getReturnType().equals(CLASS_JSON_ELEMENT))
                            .filter(m -> m.getParameterCount() == 1
                                    && CLASS_CHAT_COMPONENT.isAssignableFrom(m.getParameterTypes()[0]))
                            .findFirst().orElse(null);
                    final Method deserializeTreeWithRegistryAccess = Arrays.stream(declaredMethods)
                            .filter(m -> Modifier.isStatic(m.getModifiers()))
                            .filter(m -> CLASS_CHAT_COMPONENT.isAssignableFrom(m.getReturnType()))
                            .filter(m -> m.getParameterCount() == 2)
                            .filter(m -> m.getParameterTypes()[0].equals(CLASS_JSON_ELEMENT))
                            .filter(m -> m.getParameterTypes()[1].isInstance(registryAccess)).findFirst().orElse(null);
                    final Method serializeTreeWithRegistryAccess = Arrays.stream(declaredMethods)
                            .filter(m -> Modifier.isStatic(m.getModifiers()))
                            .filter(m -> m.getReturnType().equals(CLASS_JSON_ELEMENT))
                            .filter(m -> m.getParameterCount() == 2)
                            .filter(m -> CLASS_CHAT_COMPONENT.isAssignableFrom(m.getParameterTypes()[0]))
                            .filter(m -> m.getParameterTypes()[1].isInstance(registryAccess)).findFirst().orElse(null);
                    if (deserialize != null) {
                        textSerializerDeserialize = lookup().unreflect(deserialize);
                    }
                    if (serialize != null) {
                        textSerializerSerialize = lookup().unreflect(serialize);
                    }
                    if (deserializeTree != null) {
                        textSerializerDeserializeTree = lookup().unreflect(deserializeTree);
                    } else if (deserializeTreeWithRegistryAccess != null) {
                        deserializeTreeWithRegistryAccess.setAccessible(true);
                        textSerializerDeserializeTree = insertArguments(
                                lookup().unreflect(deserializeTreeWithRegistryAccess), 1, registryAccess);
                    }
                    if (serializeTree != null) {
                        textSerializerSerializeTree = lookup().unreflect(serializeTree);
                    } else if (serializeTreeWithRegistryAccess != null) {
                        serializeTreeWithRegistryAccess.setAccessible(true);
                        textSerializerSerializeTree = insertArguments(
                                lookup().unreflect(serializeTreeWithRegistryAccess), 1, registryAccess);
                    }
                }
                if (registryAccess != null && CLASS_HOLDERLOOKUP_PROVIDER != null) {
                    for (final Method m : CLASS_HOLDERLOOKUP_PROVIDER.getDeclaredMethods()) {
                        m.setAccessible(true);
                        if (m.getParameterCount() == 1 && m.getParameterTypes()[0].getSimpleName().equals("DynamicOps")
                                && m.getReturnType().getSimpleName().contains("RegistryOps")) {
                            createContext = lookup().unreflect(m);
                            break;
                        }
                    }
                }
                if (CLASS_COMPONENT_SERIALIZATION != null) {
                    for (final Field f : CLASS_COMPONENT_SERIALIZATION.getDeclaredFields()) {
                        if (Modifier.isStatic(f.getModifiers()) && f.getType().getSimpleName().equals("Codec")) {
                            f.setAccessible(true);
                            final Object codecInstance = f.get(null);
                            final Class<?> codecClass = codecInstance.getClass();
                            for (final Method m : codecClass.getDeclaredMethods()) {
                                if (m.getName().equals("decode")) {
                                    codecDecode = lookup().unreflect(m).bindTo(codecInstance);
                                } else if (m.getName().equals("encode")) {
                                    codecEncode = lookup().unreflect(m).bindTo(codecInstance);
                                }
                            }
                            break;
                        }
                    }
                }
            }
        } catch (final Throwable error) {
            INITIALIZATION_ERROR.set(new UnsupportedOperationException("Error occurred during initialization", error));
        }
        MC_TEXT_GSON = gson;
        JSON_OPS_INSTANCE = jsonOpsInstance;
        JSON_PARSER_INSTANCE = jsonParserInstance;
        TEXT_SERIALIZER_DESERIALIZE = textSerializerDeserialize;
        TEXT_SERIALIZER_SERIALIZE = textSerializerSerialize;
        TEXT_SERIALIZER_DESERIALIZE_TREE = textSerializerDeserializeTree;
        TEXT_SERIALIZER_SERIALIZE_TREE = textSerializerSerializeTree;
        COMPONENTSERIALIZATION_CODEC_ENCODE = codecEncode;
        COMPONENTSERIALIZATION_CODEC_DECODE = codecDecode;
        CREATE_SERIALIZATION_CONTEXT = createContext;
        REGISTRY_ACCESS = registryAccessInstance;
        SUPPORTED = MC_TEXT_GSON != null || (TEXT_SERIALIZER_DESERIALIZE != null && TEXT_SERIALIZER_SERIALIZE != null)
                || (TEXT_SERIALIZER_DESERIALIZE_TREE != null && TEXT_SERIALIZER_SERIALIZE_TREE != null)
                || (COMPONENTSERIALIZATION_CODEC_ENCODE != null && COMPONENTSERIALIZATION_CODEC_DECODE != null
                        && CREATE_SERIALIZATION_CONTEXT != null && JSON_OPS_INSTANCE != null);
    }
}