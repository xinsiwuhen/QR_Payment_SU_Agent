package com.chinaepay.wx.client;

import com.chinaepay.wx.common.CommonTool;

public class TestTech {

	public static void main(String[] args) {
		
		System.out.println(CommonTool.getRandomString(32));
		
		
		final SyncObj syncObj = new SyncObj();
		
		// showA
		new Thread(new Runnable() {
			@Override
			public void run() {
				syncObj.showA();
			}
		}).start();
		
		
		// showB
		new Thread(new Runnable() {
			@Override
			public void run() {
				syncObj.showB();
			}
		}).start();
		
		
		// showC
		new Thread(new Runnable() {
			@Override
			public void run() {
				syncObj.showC();
			}
		}).start();
		
		
		
		// showD
		new Thread(new Runnable() {
			@Override
			public void run() {
				syncObj.showD();
			}
		}).start();
	}
}

class SyncObj {
	public synchronized void showA() {
		System.out.println("showA 1 ...");
		try {
			Thread.sleep(6 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("showA 2 ...");
	}
	
	public void showB() {
		synchronized (SyncObj.class) {
			System.out.println("showB ...");
			try {
				Thread.sleep(3 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void showC() {
		synchronized (SyncObj.class) {
//			try {
//				Thread.sleep(3 * 1000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
			System.out.println("showC ...");
		}
	}
	
	public void showD() {
		System.out.println("showD ...");
	}
}
