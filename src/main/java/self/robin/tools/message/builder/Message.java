package self.robin.tools.message.builder;

/**
 * 这是个定义模板的板面。
 * 里面可根据自己的需要定义许多模板，@see {@link Template}.
 * 模板中的变量占位符，应形如 ${var}，运行时会自动做变量替换。
 * 如果变量是集合，自动将集合以逗号拼接展开。如果是对象则会调用对象的{@code toString}方法
 *
 * @Author Li Yalei - Robin
 * @since 2021/4/23 11:18
 */
public interface Message {
}
