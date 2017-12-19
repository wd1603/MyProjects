package cool.warrior.dubbo.demo.provider;

import cool.warrior.dubbo.demo.DemoService;

public class DemoServiceImpl implements DemoService {

	public String sayHello(String name) {
		return "hello " + name;
	}

}
