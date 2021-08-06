package self.robin.tools.message.builder;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 信息的暂存区
 *
 * @Author Li Yalei - Robin
 * @since 2021/4/22 19:42
 */
public class AbstractBuilder {

    /**
     * 构建消息
     *
     * @param template 模板
     * @param varNames 参数名
     * @param args 参数值
     * @return 消息
     */
    protected String buildMessage(String template, String[] varNames, Object[] args) {
        String[] values = Arrays.asList(args).stream().map(arg -> {
            if (Collection.class.isAssignableFrom(arg.getClass())) {
                return ((Collection<?>) arg).stream().map(String::valueOf).collect(Collectors.joining(","));
            }
            if (arg.getClass().isArray()) {
                return Arrays.toString(((Object[]) arg));
            }
            return String.valueOf(arg);
        }).toArray(String[]::new);
        String[] vars = Arrays.asList(varNames).stream().map(v -> "${" + v + "}").toArray(String[]::new);
        String msg = StringUtils.replaceEach(template, vars, values);
        return msg;
    }

}
