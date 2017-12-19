package cool.warrior.dubbo.demo.provider;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.dubbo.config.annotation.Service;

@Service
public class Provider {

	public static void main(String[] args) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "dubbo-demo-provider.xml" });
		context.start();
		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
