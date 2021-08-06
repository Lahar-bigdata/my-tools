package self.robin.tools.message;


import self.robin.tools.message.builder.Message;
import self.robin.tools.message.builder.Template;

/**
 * 这是一个定义模板的板面，里面有很多模板，
 * 也可根据自己的需要，自定义一个模板，模板中的变量占位符，应形如 ${var}
 *
 * @author Li Yalei - Robin
 * @since 2021/2/24 16:08
 */
public interface MessageSample extends Message {

    @Template("hello ${userName}")
    String hello(String userName);

}
