package com.test;

public class Father implements TestInf {

	public void doEvent() {
		this.test();
	}
	
	@Override
	public void test() {
		System.out.println("this is [Father] class.");
	}
}
