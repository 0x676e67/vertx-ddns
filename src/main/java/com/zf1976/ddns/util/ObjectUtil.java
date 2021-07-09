package com.zf1976.ddns.util;


import com.zf1976.ddns.annotation.Nullable;

import java.lang.reflect.Array;
import java.util.*;
/**
 * @author mac
 * @date 2021/7/7
 */
@SuppressWarnings({"unchecked", "unused", "SimplifyOptionalCallChains"})
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

    public static boolean isCompatibleWithThrowsClause(Throwable ex, @Nullable Class<?>... declaredExceptions) {
        if (!isCheckedException(ex)) {
            return true;
        } else {
            if (declaredExceptions != null) {
                Class[] var2 = declaredExceptions;
                int var3 = declaredExceptions.length;

                for(int var4 = 0; var4 < var3; ++var4) {
                    Class<?> declaredException = var2[var4];
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

    @SuppressWarnings("SimplifiableConditionalExpression")
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
            Object[] var2 = array;
            int var3 = array.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                Object arrayEle = var2[var4];
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
                Class<?> wrapperType = Array.get(source, 0).getClass();
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
            return Arrays.equals(o1, o2);
        } else if (o1 instanceof boolean[] && o2 instanceof boolean[]) {
            return Arrays.equals(o1, o2);
        } else if (o1 instanceof byte[] && o2 instanceof byte[]) {
            return Arrays.equals(o1, o2);
        } else if (o1 instanceof char[] && o2 instanceof char[]) {
            return Arrays.equals(o1, o2);
        } else if (o1 instanceof double[] && o2 instanceof double[]) {
            return Arrays.equals(o1, o2);
        } else if (o1 instanceof float[] && o2 instanceof float[]) {
            return Arrays.equals(o1, o2);
        } else if (o1 instanceof int[] && o2 instanceof int[]) {
            return Arrays.equals(o1, o2);
        } else if (o1 instanceof long[] && o2 instanceof long[]) {
            return Arrays.equals(o1, o2);
        } else {
            return o1 instanceof short[] && o2 instanceof short[] && Arrays.equals(o1, o2);
        }
    }

    public static int nullSafeHashCode(@Nullable Object obj) {
        if (obj == null) {
            return 0;
        } else {
            if (obj.getClass().isArray()) {
                if (obj instanceof Object[]) {
                    return nullSafeHashCode(obj);
                }

                if (obj instanceof boolean[]) {
                    return nullSafeHashCode(obj);
                }

                if (obj instanceof byte[]) {
                    return nullSafeHashCode(obj);
                }

                if (obj instanceof char[]) {
                    return nullSafeHashCode(obj);
                }

                if (obj instanceof double[]) {
                    return nullSafeHashCode(obj);
                }

                if (obj instanceof float[]) {
                    return nullSafeHashCode(obj);
                }

                if (obj instanceof int[]) {
                    return nullSafeHashCode(obj);
                }

                if (obj instanceof long[]) {
                    return nullSafeHashCode(obj);
                }

                if (obj instanceof short[]) {
                    return nullSafeHashCode(obj);
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
            Object[] var2 = array;
            int var3 = array.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                Object element = var2[var4];
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
            boolean[] var2 = array;
            int var3 = array.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                boolean element = var2[var4];
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
            byte[] var2 = array;
            int var3 = array.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                byte element = var2[var4];
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
            char[] var2 = array;
            int var3 = array.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                char element = var2[var4];
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
            double[] var2 = array;
            int var3 = array.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                double element = var2[var4];
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
            float[] var2 = array;
            int var3 = array.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                float element = var2[var4];
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
            int[] var2 = array;
            int var3 = array.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                int element = var2[var4];
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
            long[] var2 = array;
            int var3 = array.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                long element = var2[var4];
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
            short[] var2 = array;
            int var3 = array.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                short element = var2[var4];
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
            return nullSafeToString(obj);
        } else if (obj instanceof boolean[]) {
            return nullSafeToString(obj);
        } else if (obj instanceof byte[]) {
            return nullSafeToString(obj);
        } else if (obj instanceof char[]) {
            return nullSafeToString(obj);
        } else if (obj instanceof double[]) {
            return nullSafeToString(obj);
        } else if (obj instanceof float[]) {
            return nullSafeToString(obj);
        } else if (obj instanceof int[]) {
            return nullSafeToString(obj);
        } else if (obj instanceof long[]) {
            return nullSafeToString(obj);
        } else if (obj instanceof short[]) {
            return nullSafeToString(obj);
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
                Object[] var3 = array;
                int var4 = array.length;

                for(int var5 = 0; var5 < var4; ++var5) {
                    Object o = var3[var5];
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
                boolean[] var3 = array;
                int var4 = array.length;

                for(int var5 = 0; var5 < var4; ++var5) {
                    boolean b = var3[var5];
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
                byte[] var3 = array;
                int var4 = array.length;

                for(int var5 = 0; var5 < var4; ++var5) {
                    byte b = var3[var5];
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
                char[] var3 = array;
                int var4 = array.length;

                for(int var5 = 0; var5 < var4; ++var5) {
                    char c = var3[var5];
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
                double[] var3 = array;
                int var4 = array.length;

                for(int var5 = 0; var5 < var4; ++var5) {
                    double d = var3[var5];
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
                float[] var3 = array;
                int var4 = array.length;

                for(int var5 = 0; var5 < var4; ++var5) {
                    float f = var3[var5];
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
                int[] var3 = array;
                int var4 = array.length;

                for(int var5 = 0; var5 < var4; ++var5) {
                    int i = var3[var5];
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
                long[] var3 = array;
                int var4 = array.length;

                for(int var5 = 0; var5 < var4; ++var5) {
                    long l = var3[var5];
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
                short[] var3 = array;
                int var4 = array.length;

                for(int var5 = 0; var5 < var4; ++var5) {
                    short s = var3[var5];
                    stringJoiner.add(String.valueOf(s));
                }

                return stringJoiner.toString();
            }
        }
    }
}
