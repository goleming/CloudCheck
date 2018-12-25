package com.ningze.util;

import java.net.InetAddress;

public class AbstractUUIDGenerator {
	private static final int IP;
	private static short counter;
	private static final int JVM;

	protected int getJVM() {
		return JVM;
	}

	protected short getCount() {
		synchronized (AbstractUUIDGenerator.class) {
			if (counter < 0)
				counter = 0;

			short ret = counter;
			counter = (short) (ret + 1);

			return ret;
		}
	}

	protected int getIP() {
		return IP;
	}

	protected short getHiTime() {
		return (short) (int) (System.currentTimeMillis() >>> 32);
	}

	protected int getLoTime() {
		return (int) System.currentTimeMillis();
	}

	static {
		int ipadd;
		try {
			ipadd = toInt(InetAddress.getLocalHost().getAddress());
		} catch (Exception e) {
			ipadd = 0;
		}
		IP = ipadd;

		counter = 0;
		JVM = (int) (System.currentTimeMillis() >>> 8);
	}

	public static int toInt(byte[] bytes) {
		int result = 0;
		for (int i = 0; i < 4; ++i) {
			result = (result << 8) - -128 + bytes[i];
		}
		return result;
	}
}
