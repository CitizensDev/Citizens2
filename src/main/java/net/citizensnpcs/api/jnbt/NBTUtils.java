package net.citizensnpcs.api.jnbt;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/*
 * JNBT License
 *
 * Copyright (c) 2010 Graham Edgecombe
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *     * Neither the name of the JNBT team nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * A class which contains NBT-related utility methods.
 * 
 * @author Graham Edgecombe
 * 
 */
public final class NBTUtils {

    /**
     * Default private constructor.
     */
    private NBTUtils() {

    }

    public static Tag createTag(String name, Object value) {
        Class<?> clazz = value.getClass();
        if (clazz == byte.class || clazz == Byte.class) {
            return new ByteTag(name, (Byte) value);
        } else if (clazz == short.class || clazz == Short.class) {
            return new ShortTag(name, (Short) value);
        } else if (clazz == int.class || clazz == Integer.class) {
            return new IntTag(name, (Integer) value);
        } else if (clazz == long.class || clazz == Long.class) {
            return new LongTag(name, (Long) value);
        } else if (clazz == float.class || clazz == Float.class) {
            return new FloatTag(name, (Float) value);
        } else if (clazz == double.class || clazz == Double.class) {
            return new DoubleTag(name, (Double) value);
        } else if (clazz == byte[].class) {
            return new ByteArrayTag(name, (byte[]) value);
        } else if (clazz == int[].class) {
            return new IntArrayTag(name, (int[]) value);
        } else if (clazz == String.class) {
            return new StringTag(name, (String) value);
        } else if (List.class.isAssignableFrom(clazz)) {
            List<?> list = (List<?>) value;
            if (list.isEmpty())
                throw new IllegalArgumentException("cannot set empty list");
            List<Tag> newList = Lists.newArrayList();
            Class<? extends Tag> tagClass = null;
            for (Object v : list) {
                Tag tag = createTag("", v);
                if (tag == null)
                    throw new IllegalArgumentException("cannot convert list value to tag");
                if (tagClass == null) {
                    tagClass = tag.getClass();
                } else if (tagClass != tag.getClass()) {
                    throw new IllegalArgumentException("list values must be of homogeneous type");
                }
                newList.add(tag);
            }
            return new ListTag(name, tagClass, newList);
        } else if (Map.class.isAssignableFrom(clazz)) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            if (map.isEmpty())
                throw new IllegalArgumentException("cannot set empty list");
            Map<String, Tag> newMap = Maps.newHashMap();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Tag tag = createTag("", entry.getValue());
                if (tag == null)
                    throw new IllegalArgumentException(
                            "cannot convert map value with key " + entry.getKey() + " to tag");
                newMap.put(entry.getKey(), tag);
            }
            return new CompoundTag(name, newMap);
        }
        return null;
    }

    /**
     * Gets the class of a type of tag.
     * 
     * @param type
     *            The type.
     * @return The class.
     * @throws IllegalArgumentException
     *             if the tag type is invalid.
     */
    public static Class<? extends Tag> getTypeClass(int type) {
        switch (type) {
            case NBTConstants.TYPE_END:
                return EndTag.class;
            case NBTConstants.TYPE_BYTE:
                return ByteTag.class;
            case NBTConstants.TYPE_SHORT:
                return ShortTag.class;
            case NBTConstants.TYPE_INT:
                return IntTag.class;
            case NBTConstants.TYPE_LONG:
                return LongTag.class;
            case NBTConstants.TYPE_FLOAT:
                return FloatTag.class;
            case NBTConstants.TYPE_DOUBLE:
                return DoubleTag.class;
            case NBTConstants.TYPE_BYTE_ARRAY:
                return ByteArrayTag.class;
            case NBTConstants.TYPE_STRING:
                return StringTag.class;
            case NBTConstants.TYPE_LIST:
                return ListTag.class;
            case NBTConstants.TYPE_COMPOUND:
                return CompoundTag.class;
            case NBTConstants.TYPE_INT_ARRAY:
                return IntArrayTag.class;
            default:
                throw new IllegalArgumentException("Invalid tag type : " + type + ".");
        }
    }

    /**
     * Gets the type code of a tag class.
     * 
     * @param clazz
     *            The tag class.
     * @return The type code.
     * @throws IllegalArgumentException
     *             if the tag class is invalid.
     */
    public static int getTypeCode(Class<? extends Tag> clazz) {
        if (clazz.equals(ByteArrayTag.class)) {
            return NBTConstants.TYPE_BYTE_ARRAY;
        } else if (clazz.equals(ByteTag.class)) {
            return NBTConstants.TYPE_BYTE;
        } else if (clazz.equals(CompoundTag.class)) {
            return NBTConstants.TYPE_COMPOUND;
        } else if (clazz.equals(DoubleTag.class)) {
            return NBTConstants.TYPE_DOUBLE;
        } else if (clazz.equals(EndTag.class)) {
            return NBTConstants.TYPE_END;
        } else if (clazz.equals(FloatTag.class)) {
            return NBTConstants.TYPE_FLOAT;
        } else if (clazz.equals(IntTag.class)) {
            return NBTConstants.TYPE_INT;
        } else if (clazz.equals(ListTag.class)) {
            return NBTConstants.TYPE_LIST;
        } else if (clazz.equals(LongTag.class)) {
            return NBTConstants.TYPE_LONG;
        } else if (clazz.equals(ShortTag.class)) {
            return NBTConstants.TYPE_SHORT;
        } else if (clazz.equals(StringTag.class)) {
            return NBTConstants.TYPE_STRING;
        } else if (clazz.equals(IntArrayTag.class)) {
            return NBTConstants.TYPE_INT_ARRAY;
        } else {
            throw new IllegalArgumentException("Invalid tag classs (" + clazz.getName() + ").");
        }
    }

    /**
     * Gets the type name of a tag.
     * 
     * @param clazz
     *            The tag class.
     * @return The type name.
     */
    public static String getTypeName(Class<? extends Tag> clazz) {
        if (clazz.equals(ByteArrayTag.class)) {
            return "TAG_Byte_Array";
        } else if (clazz.equals(ByteTag.class)) {
            return "TAG_Byte";
        } else if (clazz.equals(CompoundTag.class)) {
            return "TAG_Compound";
        } else if (clazz.equals(DoubleTag.class)) {
            return "TAG_Double";
        } else if (clazz.equals(EndTag.class)) {
            return "TAG_End";
        } else if (clazz.equals(FloatTag.class)) {
            return "TAG_Float";
        } else if (clazz.equals(IntTag.class)) {
            return "TAG_Int";
        } else if (clazz.equals(ListTag.class)) {
            return "TAG_List";
        } else if (clazz.equals(LongTag.class)) {
            return "TAG_Long";
        } else if (clazz.equals(ShortTag.class)) {
            return "TAG_Short";
        } else if (clazz.equals(StringTag.class)) {
            return "TAG_String";
        } else if (clazz.equals(IntArrayTag.class)) {
            return "TAG_Int_Array";
        } else {
            throw new IllegalArgumentException("Invalid tag classs (" + clazz.getName() + ").");
        }
    }
}
