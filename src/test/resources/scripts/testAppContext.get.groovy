def greeting = applicationContext.getBean("greeting");

return greeting.sayHi(name)