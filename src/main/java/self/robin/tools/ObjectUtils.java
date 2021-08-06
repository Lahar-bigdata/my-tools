package self.robin.tools;

import com.google.gson.Gson;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiFunction;

/**
 * ...
 *
 * @author Li Yalei - Robin
 * @since 2021/3/9 11:50
 */
public class ObjectUtils extends org.apache.commons.lang3.ObjectUtils {

    /**
     * 合并 o1, o2 两个对象
     * 当o1, o2其中有一个为null时，返回返回非null那个对象; 全为null时，返回null
     * 都不为null时，将o2的字段值合并到 o1中。
     * <p>
     * 合并规则：
     * 1. 值相同，保留o1中的字段值
     * 2. 值不同，a.有一个不为null，保留非null的那个值
     * b.都不为null，由用户决定如何合并（调用operationWhenConflict方法，方法返回值为合并后的值，将赋值给o1的当前字段）
     * <p>
     * 注意：合并的属性需要提供 set, get方法
     *
     * @param o1
     * @param o2
     * @param operationWhenConflict 当发生字段不一致时候用户的操作
     *                              Field：发生冲突的字段，Pair: o1,o2中对应的冲突字段值
     * @param <T>                   T: 待合并的对象，V:对象中的字段的值
     */
    public static <T, V> T merge(T o1, T o2, BiFunction<Field, Pair<V, V>, V> operationWhenConflict) throws IllegalAccessException, InvocationTargetException {

        if (o1 == null || o2 == null ? true : false) {
            return o1 == null ? o2 : o1;
        }
        Class<?> clazz = o1.getClass();
        //两者都不为null
        for (Field field : FieldUtils.getAllFieldsList(clazz)) {
            Method getMethod = getGetMethod(clazz, field.getName());
            Method setMethod = getSetMethod(clazz, field.getName(), new Class[]{field.getType()});
            if (getMethod == null || setMethod == null) {
                //没有set, get方法，跳过
                continue;
            }
            // 值不一致
            field.setAccessible(true);
            if (notEqual(field.get(o1), field.get(o2))) {
                Object v1 = field.get(o1);
                ;
                Object v2 = field.get(o2);
                if (v1 == null || v2 == null ? true : false) {
                    // 两者有且仅有一个为null
                    field.set(o1, v1 == null ? v2 : v1);
                    continue;
                }
                //两者都不为null
                field.set(o1, operationWhenConflict.apply(field, new ImmutablePair<>((V) v1, (V) v2)));
            }
        }
        return o1;
    }

    public static Method getGetMethod(Class<?> clazz, String fieldName) throws IllegalAccessException {
        return setGetMethod(clazz, fieldName, "get");
    }

    public static Method getSetMethod(Class<?> clazz, String fieldName, Class<?>[] args) throws IllegalAccessException {
        return setGetMethod(clazz, fieldName, "set", args);
    }

    private static Method setGetMethod(Class<?> clazz, String fieldName, String prefix, Class<?>... args) throws IllegalAccessException {
        if (fieldName == null || "".equals(fieldName)) {
            throw new IllegalAccessException("The filed name isn't present, name=" + fieldName);
        }
        String methodName = prefix;
        if (fieldName.length() == 1) {
            methodName += (fieldName.charAt(0) + "").toUpperCase();
        } else {
            methodName += (fieldName.charAt(0) + "").toUpperCase() + fieldName.substring(1);
        }
        Method method = MethodUtils.getAccessibleMethod(clazz, methodName, args);
        return method;
    }

    public static <T> T parse(String json, Class<T> clazz) {
        return new Gson().fromJson(json, clazz);
    }

}
