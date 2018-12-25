package com.ningze.util;

import java.io.Serializable;

public class UUIDHexGenerator extends AbstractUUIDGenerator {
	private String sep;

	public UUIDHexGenerator() {
		this.sep = "";
	}

	protected String format(int intval) {
		String formatted = Integer.toHexString(intval);
		StringBuffer buf = new StringBuffer("00000000");
		buf.replace(8 - formatted.length(), 8, formatted);
		return buf.toString();
	}

	protected String format(short shortval) {
		String formatted = Integer.toHexString(shortval);
		StringBuffer buf = new StringBuffer("0000");
		buf.replace(4 - formatted.length(), 4, formatted);
		return buf.toString();
	}

	public Serializable generate() {
		return format(getIP()) + this.sep + format(getJVM()) + this.sep
				+ format(getHiTime()) + this.sep + format(getLoTime())
				+ this.sep + format(getCount());
	}

	public void setSeparator(String separator) {
		this.sep = separator;
	}

	public static void main(String[] args) throws Exception {
		UUIDHexGenerator gen = new UUIDHexGenerator();
		for (int i = 0; i < 10; ++i) {
			String id = (String) gen.generate();
			System.out.println(id);
		}
	}
}
