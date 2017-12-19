package cool.warrior.dubbo.demo;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.dubbo.config.annotation.Service;

@Service
public class Consumer {
	public static void main(String[] args) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"dubbo-demo-consumer.xml"});
		context.start();
		DemoService demoService = (DemoService) context.getBean("demoService");
		String hello = demoService.sayHello("world");
		System.out.println(hello);
	}
}
