package self.robin.tools;

import javassist.ClassPool;
import javassist.LoaderClassPath;

import java.util.concurrent.ConcurrentHashMap;

/**
 * ...
 *
 * @author: Li Yalei - Robin
 * @since 2021/4/22 17:28
 */
public class JavassistHelper {

    private final static ConcurrentHashMap<ClassLoader, ClassPool> CLASS_POOL_MAP = new ConcurrentHashMap<>();

    /**
     * 不同的ClassLoader返回不同的ClassPool
     *
     * @param loader
     * @return
     */
    public static ClassPool getClassPool(ClassLoader loader) {
        if (null == loader) {
            return ClassPool.getDefault();
        }

        ClassPool pool = CLASS_POOL_MAP.get(loader);
        if (null == pool) {
            pool = new ClassPool(true);
            pool.appendClassPath(new LoaderClassPath(loader));
            CLASS_POOL_MAP.put(loader, pool);
        }
        return pool;
    }

    public static ClassPool getDefaultClassPool() {
        ClassPool classPool = getClassPool(JavassistHelper.class.getClassLoader());
        classPool.childFirstLookup = true;
        return classPool;
    }


}
