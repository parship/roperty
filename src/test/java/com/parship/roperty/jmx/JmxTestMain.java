package com.parship.roperty.jmx;

import com.parship.roperty.Roperty;

import java.io.IOException;


/**
 * @author mfinsterwalder
 * @since 2013-05-28 13:33
 */
public class JmxTestMain {

	public static void main(String[] args) throws IOException {
		Roperty r1 = new Roperty("dom1", "dom2");
		r1.set("key R1", "value a", null);
		Roperty r2 = new Roperty("dom1", "dom2");
		r2.set("key R2", "value b", null);
		r2.set("key2 R2", "value x", null);
		Roperty r3 = new Roperty("dom1", "dom2");
		r3.set("key R3", "value c", null);
		System.in.read();
	}
}
