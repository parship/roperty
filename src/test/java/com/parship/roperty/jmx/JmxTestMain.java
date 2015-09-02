/*
 * Roperty - An advanced property management and retrival system
 * Copyright (C) 2013 PARSHIP GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.parship.roperty.jmx;

import com.parship.roperty.Roperty;
import com.parship.roperty.RopertyImpl;

import java.io.IOException;


/**
 * @author mfinsterwalder
 * @since 2013-05-28 13:33
 */
public class JmxTestMain {

	public static void main(String[] args) throws IOException {
		Roperty r1 = new RopertyImpl("dom1", "dom2");
		r1.set("key R1", "value a", null);
		Roperty r2 = new RopertyImpl("dom1", "dom2");
		r2.set("key R2", "value b", null);
		r2.set("key2 R2", "value x", null);
		Roperty r3 = new RopertyImpl("dom1", "dom2");
		r3.set("key R3", "value c", null);
		System.in.read();
	}
}
