/*
 *
 *
 * MIT License
 *
 * Copyright (c) 2021 gngpp
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

package com.gngpp.ddns.util;


import com.gngpp.ddns.annotation.Nullable;

import java.lang.reflect.Array;
import java.util.*;
/**
 * @author mac
 * @date 2021/7/7
 */
@SuppressWarnings("unused")
public abstract class ObjectUtil {
    private static final int INITIAL_HASH = 7;
    private static final int MULTIPLIER = 31;
    private static final String EMPTY_STRING = "";
    private static final String NULL_STRING = "null";
    private static final String ARRAY_START = "{";
    private static final String ARRAY_END = "}";
    private static final String EMPTY_ARRAY = "{}";
    private static final String ARRAY_ELEMENT_SEPARATOR = ", ";
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    public ObjectUtil() {
    }

    public static boolean isCheckedException(Throwable ex) {
        return !(ex instanceof RuntimeException) && !(ex instanceof Error);
    }

    @SuppressWarnings("rawtypes")
    public static boolean isCompatibleWithThrowsClause(Throwable ex, @Nullable Class<?>... declaredExceptions) {
        if (!isCheckedException(ex)) {
            return true;
        } else {
            if (declaredExceptions != null) {
                int var3 = declaredExceptions.length;

                for(int var4 = 0; var4 < var3; ++var4) {
                    Class<?> declaredException = ((Class[]) declaredExceptions)[var4];
                    if (declaredException.isInstance(ex)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public static boolean isArray(@Nullable Object obj) {
        return obj != null && obj.getClass().isArray();
    }

    public static boolean isEmpty(@Nullable Object[] array) {
        return array == null || array.length == 0;
    }

    @SuppressWarnings({"SimplifiableConditionalExpression", "rawtypes", "SimplifyOptionalCallChains"})
    public static boolean isEmpty(@Nullable Object obj) {
        if (obj == null) {
            return true;
        } else if (obj instanceof Optional) {
            return !((Optional)obj).isPresent();
        } else if (obj instanceof CharSequence) {
            return ((CharSequence)obj).length() == 0;
        } else if (obj.getClass().isArray()) {
            return Array.getLength(obj) == 0;
        } else if (obj instanceof Collection) {
            return ((Collection)obj).isEmpty();
        } else {
            return obj instanceof Map ? ((Map)obj).isEmpty() : false;
        }
    }

    @SuppressWarnings("rawtypes")
    @Nullable
    public static Object unwrapOptional(@Nullable Object obj) {
        if (obj instanceof Optional) {
            Optional<?> optional = (Optional)obj;
            if (optional.isEmpty()) {
                return null;
            } else {
                Object result = optional.get();
                Assert.isTrue(!(result instanceof Optional), "Multi-level Optional usage not supported");
                return result;
            }
        } else {
            return obj;
        }
    }

    public static boolean containsElement(@Nullable Object[] array, Object element) {
        if (array == null) {
            return false;
        } else {
            int var3 = array.length;

            for (Object arrayEle : array) {
                if (nullSafeEquals(arrayEle, element)) {
                    return true;
                }
            }

            return false;
        }
    }

    public static boolean containsConstant(Enum<?>[] enumValues, String constant) {
        return containsConstant(enumValues, constant, false);
    }

    @SuppressWarnings("rawtypes")
    public static boolean containsConstant(Enum<?>[] enumValues, String constant, boolean caseSensitive) {
        int var4 = enumValues.length;
        int var5 = 0;

        while(true) {
            if (var5 >= var4) {
                return false;
            }

            Enum<?> candidate = ((Enum[]) enumValues)[var5];
            if (caseSensitive) {
                if (candidate.toString().equals(constant)) {
                    break;
                }
            } else if (candidate.toString().equalsIgnoreCase(constant)) {
                break;
            }

            ++var5;
        }

        return true;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <E extends Enum<?>> E caseInsensitiveValueOf(E[] enumValues, String constant) {
        int var3 = enumValues.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            E candidate = (E) ((Enum[]) enumValues)[var4];
            if (candidate.toString().equalsIgnoreCase(constant)) {
                return candidate;
            }
        }

        throw new IllegalArgumentException("Constant [" + constant + "] does not exist in enum type " + enumValues.getClass().getComponentType().getName());
    }

    @SuppressWarnings("unchecked")
    public static <A, O extends A> A[] addObjectToArray(@Nullable A[] array, @Nullable O obj) {
        Class<?> compType = Object.class;
        if (array != null) {
            compType = array.getClass().getComponentType();
        } else if (obj != null) {
            compType = obj.getClass();
        }

        int newArrLength = array != null ? array.length + 1 : 1;
        A[] newArr = (A[]) Array.newInstance(compType, newArrLength);
        if (array != null) {
            System.arraycopy(array, 0, newArr, 0, array.length);
        }

        newArr[newArr.length - 1] = obj;
        return newArr;
    }

    public static Object[] toObjectArray(@Nullable Object source) {
        if (source instanceof Object[]) {
            return (Object[]) source;
        } else if (source == null) {
            return EMPTY_OBJECT_ARRAY;
        } else if (!source.getClass().isArray()) {
            throw new IllegalArgumentException("Source is not an array: " + source);
        } else {
            int length = Array.getLength(source);
            if (length == 0) {
                return EMPTY_OBJECT_ARRAY;
            } else {
                Class<?> wrapperType = Array.get(source, 0)
                                            .getClass();
                Object[] newArray = (Object[]) Array.newInstance(wrapperType, length);

                for(int i = 0; i < length; ++i) {
                    newArray[i] = Array.get(source, i);
                }

                return newArray;
            }
        }
    }

    public static boolean nullSafeEquals(@Nullable Object o1, @Nullable Object o2) {
        if (o1 == o2) {
            return true;
        } else if (o1 != null && o2 != null) {
            if (o1.equals(o2)) {
                return true;
            } else {
                return o1.getClass()
                         .isArray() && o2.getClass()
                                         .isArray() && arrayEquals(o1, o2);
            }
        } else {
            return false;
        }
    }

    private static boolean arrayEquals(Object o1, Object o2) {
        if (o1 instanceof Object[] && o2 instanceof Object[]) {
            return Arrays.equals((Object[]) o1, (Object[]) o2);
        } else if (o1 instanceof boolean[] && o2 instanceof boolean[]) {
            return Arrays.equals((boolean[]) o1, (boolean[]) o2);
        } else if (o1 instanceof byte[] && o2 instanceof byte[]) {
            return Arrays.equals((byte[]) o1, (byte[]) o2);
        } else if (o1 instanceof char[] && o2 instanceof char[]) {
            return Arrays.equals((char[]) o1, (char[]) o2);
        } else if (o1 instanceof double[] && o2 instanceof double[]) {
            return Arrays.equals((double[]) o1, (double[]) o2);
        } else if (o1 instanceof float[] && o2 instanceof float[]) {
            return Arrays.equals((float[]) o1, (float[]) o2);
        } else if (o1 instanceof int[] && o2 instanceof int[]) {
            return Arrays.equals((int[]) o1, (int[]) o2);
        } else if (o1 instanceof long[] && o2 instanceof long[]) {
            return Arrays.equals((long[]) o1, (long[]) o2);
        } else {
            return o1 instanceof short[] && o2 instanceof short[] && Arrays.equals((short[]) o1, (short[]) o2);
        }
    }

    public static int nullSafeHashCode(@Nullable Object obj) {
        if (obj == null) {
            return 0;
        } else {
            if (obj.getClass().isArray()) {
                if (obj instanceof Object[]) {
                    return nullSafeHashCode((Object[]) obj);
                }

                if (obj instanceof boolean[]) {
                    return nullSafeHashCode((boolean[]) obj);
                }

                if (obj instanceof byte[]) {
                    return nullSafeHashCode((byte[]) obj);
                }

                if (obj instanceof char[]) {
                    return nullSafeHashCode((char[]) obj);
                }

                if (obj instanceof double[]) {
                    return nullSafeHashCode((double[]) obj);
                }

                if (obj instanceof float[]) {
                    return nullSafeHashCode((float[]) obj);
                }

                if (obj instanceof int[]) {
                    return nullSafeHashCode((int[]) obj);
                }

                if (obj instanceof long[]) {
                    return nullSafeHashCode((long[]) obj);
                }

                if (obj instanceof short[]) {
                    return nullSafeHashCode((short[]) obj);
                }
            }

            return obj.hashCode();
        }
    }

    public static int nullSafeHashCode(@Nullable Object[] array) {
        if (array == null) {
            return 0;
        } else {
            int hash = 7;

            for (Object element : array) {
                hash = 31 * hash + nullSafeHashCode(element);
            }

            return hash;
        }
    }

    public static int nullSafeHashCode(@Nullable boolean[] array) {
        if (array == null) {
            return 0;
        } else {
            int hash = 7;

            for (boolean element : array) {
                hash = 31 * hash + Boolean.hashCode(element);
            }

            return hash;
        }
    }

    public static int nullSafeHashCode(@Nullable byte[] array) {
        if (array == null) {
            return 0;
        } else {
            int hash = 7;

            for (byte element : array) {
                hash = 31 * hash + element;
            }

            return hash;
        }
    }

    public static int nullSafeHashCode(@Nullable char[] array) {
        if (array == null) {
            return 0;
        } else {
            int hash = 7;

            for (char element : array) {
                hash = 31 * hash + element;
            }

            return hash;
        }
    }

    public static int nullSafeHashCode(@Nullable double[] array) {
        if (array == null) {
            return 0;
        } else {
            int hash = 7;

            for (double element : array) {
                hash = 31 * hash + Double.hashCode(element);
            }

            return hash;
        }
    }

    public static int nullSafeHashCode(@Nullable float[] array) {
        if (array == null) {
            return 0;
        } else {
            int hash = 7;
            int var3 = array.length;

            for (float element : array) {
                hash = 31 * hash + Float.hashCode(element);
            }

            return hash;
        }
    }

    public static int nullSafeHashCode(@Nullable int[] array) {
        if (array == null) {
            return 0;
        } else {
            int hash = 7;
            int var3 = array.length;

            for (int element : array) {
                hash = 31 * hash + element;
            }

            return hash;
        }
    }

    public static int nullSafeHashCode(@Nullable long[] array) {
        if (array == null) {
            return 0;
        } else {
            int hash = 7;
            int var3 = array.length;

            for (long element : array) {
                hash = 31 * hash + Long.hashCode(element);
            }

            return hash;
        }
    }

    public static int nullSafeHashCode(@Nullable short[] array) {
        if (array == null) {
            return 0;
        } else {
            int hash = 7;
            int var3 = array.length;

            for (short element : array) {
                hash = 31 * hash + element;
            }

            return hash;
        }
    }

    /** @deprecated */
    @Deprecated
    public static int hashCode(boolean bool) {
        return Boolean.hashCode(bool);
    }

    /** @deprecated */
    @Deprecated
    public static int hashCode(double dbl) {
        return Double.hashCode(dbl);
    }

    /** @deprecated */
    @Deprecated
    public static int hashCode(float flt) {
        return Float.hashCode(flt);
    }

    /** @deprecated */
    @Deprecated
    public static int hashCode(long lng) {
        return Long.hashCode(lng);
    }

    public static String identityToString(@Nullable Object obj) {
        if (obj == null) {
            return "";
        } else {
            String className = obj.getClass().getName();
            String identityHexString = getIdentityHexString(obj);
            return className + '@' + identityHexString;
        }
    }

    public static String getIdentityHexString(Object obj) {
        return Integer.toHexString(System.identityHashCode(obj));
    }

    public static String getDisplayString(@Nullable Object obj) {
        return obj == null ? "" : nullSafeToString(obj);
    }

    public static String nullSafeClassName(@Nullable Object obj) {
        return obj != null ? obj.getClass().getName() : "null";
    }

    public static String nullSafeToString(@Nullable Object obj) {
        if (obj == null) {
            return "null";
        } else if (obj instanceof String) {
            return (String)obj;
        } else if (obj instanceof Object[]) {
            return nullSafeToString((Object[]) obj);
        } else if (obj instanceof boolean[]) {
            return nullSafeToString((boolean[]) obj);
        } else if (obj instanceof byte[]) {
            return nullSafeToString((byte[]) obj);
        } else if (obj instanceof char[]) {
            return nullSafeToString((char[]) obj);
        } else if (obj instanceof double[]) {
            return nullSafeToString((double[]) obj);
        } else if (obj instanceof float[]) {
            return nullSafeToString((float[]) obj);
        } else if (obj instanceof int[]) {
            return nullSafeToString((int[]) obj);
        } else if (obj instanceof long[]) {
            return nullSafeToString((long[]) obj);
        } else if (obj instanceof short[]) {
            return nullSafeToString((short[]) obj);
        } else {
            String str = obj.toString();
            return str != null ? str : "";
        }
    }

    public static String nullSafeToString(@Nullable Object[] array) {
        if (array == null) {
            return "null";
        } else {
            int length = array.length;
            if (length == 0) {
                return "{}";
            } else {
                StringJoiner stringJoiner = new StringJoiner(", ", "{", "}");
                int var4 = array.length;

                for (Object o : array) {
                    stringJoiner.add(String.valueOf(o));
                }

                return stringJoiner.toString();
            }
        }
    }

    public static String nullSafeToString(@Nullable boolean[] array) {
        if (array == null) {
            return "null";
        } else {
            int length = array.length;
            if (length == 0) {
                return "{}";
            } else {
                StringJoiner stringJoiner = new StringJoiner(", ", "{", "}");
                int var4 = array.length;

                for (boolean b : array) {
                    stringJoiner.add(String.valueOf(b));
                }

                return stringJoiner.toString();
            }
        }
    }

    public static String nullSafeToString(@Nullable byte[] array) {
        if (array == null) {
            return "null";
        } else {
            int length = array.length;
            if (length == 0) {
                return "{}";
            } else {
                StringJoiner stringJoiner = new StringJoiner(", ", "{", "}");
                int var4 = array.length;

                for (byte b : array) {
                    stringJoiner.add(String.valueOf(b));
                }

                return stringJoiner.toString();
            }
        }
    }

    public static String nullSafeToString(@Nullable char[] array) {
        if (array == null) {
            return "null";
        } else {
            int length = array.length;
            if (length == 0) {
                return "{}";
            } else {
                StringJoiner stringJoiner = new StringJoiner(", ", "{", "}");
                int var4 = array.length;

                for (char c : array) {
                    stringJoiner.add('\'' + String.valueOf(c) + '\'');
                }

                return stringJoiner.toString();
            }
        }
    }

    public static String nullSafeToString(@Nullable double[] array) {
        if (array == null) {
            return "null";
        } else {
            int length = array.length;
            if (length == 0) {
                return "{}";
            } else {
                StringJoiner stringJoiner = new StringJoiner(", ", "{", "}");
                int var4 = array.length;

                for (double d : array) {
                    stringJoiner.add(String.valueOf(d));
                }

                return stringJoiner.toString();
            }
        }
    }

    public static String nullSafeToString(@Nullable float[] array) {
        if (array == null) {
            return "null";
        } else {
            int length = array.length;
            if (length == 0) {
                return "{}";
            } else {
                StringJoiner stringJoiner = new StringJoiner(", ", "{", "}");
                int var4 = array.length;

                for (float f : array) {
                    stringJoiner.add(String.valueOf(f));
                }

                return stringJoiner.toString();
            }
        }
    }

    public static String nullSafeToString(@Nullable int[] array) {
        if (array == null) {
            return "null";
        } else {
            int length = array.length;
            if (length == 0) {
                return "{}";
            } else {
                StringJoiner stringJoiner = new StringJoiner(", ", "{", "}");
                int var4 = array.length;

                for (int i : array) {
                    stringJoiner.add(String.valueOf(i));
                }

                return stringJoiner.toString();
            }
        }
    }

    public static String nullSafeToString(@Nullable long[] array) {
        if (array == null) {
            return "null";
        } else {
            int length = array.length;
            if (length == 0) {
                return "{}";
            } else {
                StringJoiner stringJoiner = new StringJoiner(", ", "{", "}");
                int var4 = array.length;

                for (long l : array) {
                    stringJoiner.add(String.valueOf(l));
                }

                return stringJoiner.toString();
            }
        }
    }

    public static String nullSafeToString(@Nullable short[] array) {
        if (array == null) {
            return "null";
        } else {
            int length = array.length;
            if (length == 0) {
                return "{}";
            } else {
                StringJoiner stringJoiner = new StringJoiner(", ", "{", "}");
                int var4 = array.length;

                for (short s : array) {
                    stringJoiner.add(String.valueOf(s));
                }

                return stringJoiner.toString();
            }
        }
    }
}
