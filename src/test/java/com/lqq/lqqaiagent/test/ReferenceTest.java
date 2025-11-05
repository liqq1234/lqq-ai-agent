package com.lqq.lqqaiagent.test;

class Person {
    String name;

    Person(String name) {
        this.name = name;
    }
}

public class ReferenceTest {

    public static void main(String[] args) {
        // 创建一个 Person 对象
        Person p = new Person("张三");

        System.out.println("main 开始时 -> p.name = " + p.name);

        // 调用 change 方法
        change(p);

        // 再次输出
        System.out.println("main 结束时 -> p.name = " + p.name);
    }

    static void change(Person p) {
        System.out.println("进入 change() -> p.name = " + p.name);

        // 修改对象内部属性（外部可见）
        p.name = "李四";
        System.out.println("修改字段后 -> p.name = " + p.name);

        // 改变引用指向（外部不可见）
        p = new Person("王五");
        System.out.println("重新赋值后 -> p.name = " + p.name);

        System.out.println("退出 change() -> p.name = " + p.name);
    }
}
