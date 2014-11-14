/*
 * druf library
 *
 * druf multiplatform development toolkit
 *
 * ----------------------------------------------------------------------------------
 *
 * Copyright (C) 2013-2015 Zbyněk Vyškovský
 *
 * ----------------------------------------------------------------------------------
 *
 * LICENSE:
 *
 * This file is part of druf
 *
 * druf is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * druf is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with druf; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * @author	2013-2015 Zbyněk Vyškovský
 * @link	mailto:kvr@matfyz.cz
 * @link	http://kvr.matfyz.cz/software/java/druf/trans/
 * @license	http://www.gnu.org/licenses/lgpl.txt GNU Lesser General Public License v3
 */
package org.druf.trans.php.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.validation.constraints.NotNull;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import org.druf.trans.meta.DynamicDefaults;
import org.druf.trans.meta.NoDynamic;
import org.apache.commons.io.IOUtils;

/**
 * Comments to the class, more comments on next line including some multibyte unicode characters:
 * Žluťoučký kůň úpěl ďábelské ódy.
 *
 * @author
 * 	rat
 * @since
 * 	2013-03-12
 */
public class PhpTransTested extends java.lang.Object implements java.lang.Comparable<PhpTransTested>
{
	/**
	 * Constructor comments.
	 */
	public				PhpTransTested()
	{
		super();
		uninit = false;
	}

	/**
	 * Black hole servers as a way to avoid unused warnings.
	 *
	 * @param obj
	 * 	object to be formally used
	 */
	public void			blackHole(Object obj)
	{
	}

	public int			compareTo(PhpTransTested s)
	{
		return name.compareTo(s.name);
	}

	/**
	 * Method comments.
	 */
	public String			packageTest()
	{
		return org.druf.trans.php.test.PhpTransTested.staticAccessTest()+org.druf.trans.php.test.PhpTransTested.str;
	}

	/*
	 * No doc method comments.
	 */
	public void			assignment()
	{
		magic = 11;
	}

	public static int		addNumbers(int x, int y)
	{
		int tmp = x+y;
		{
			int adding = y;
			tmp -= adding;
			tmp = tmp == 0 ? tmp*2 : tmp;
		}
		return tmp;
	}

	public int			addMagic(int arg)
	{
		this.assignment();
		return addNumbers(this.magic, arg);
	}

	public String			superTest()
	{
		return super.toString();
	}

	public String			thisTest()
	{
		return toString();
	}

	public Object			newClassTest()
	{
		typedField = new LinkedList<String>();
		return new PhpTransTested().addMagic(0);
	}

	public Object			newArrayTest()
	{
		return new byte[]{ 0, 1, 4, 8 };
	}

	public int			arrayAccessTest(int i)
	{
		return list[i-1];
	}

	@SuppressWarnings("fallthrough")
	public void			switchTest(int x)
	{
		switch (x) {
		case 0:
			assignment();
			break;
		case 1:
			addMagic(0);
			// fall through
		default:
			addNumbers(4, 5);
			break;
		}
	}

	public void			ifTest(int x)
	{
		if (x == 0)
			x = 1;
		if (x == 1) {
			x++;
			x += 2;
		}
		if (x == 0)
			x = 1;
		else if (x == 1)
			x = 2;
		else
			x = 3;
		if (x == 0) {
			x = 1;
		}
		else if (x == 3) {
			x = 2;
		}
		else {
			x = 0;
		}
	}

	public void			forTest(int x)
	{
		for (;;)
			break;
		for (int i = 0; i < 5; i++) ;
		for (int i = 0; i < 4; i++) {
			i++;
			continue;
		}
	}

	public void			whileTest(int x)
	{
		while (x != 0)
			x /= 2;
		while (x < 0) {
			x++;
			break;
		}
		while (x < 0) {
			x++;
			continue;
		}
	}

	public void			doWhileTest(int x)
	{
		do
			x++;
		while (x < 0);
		do {
			x++;
		} while (x < 0);
	}

	public void			foreachTest(int x)
	{
		for (int v: new int[]{ 4, 5, 6})
			x *= v;
		for (int v: new int[]{ 4, 5, 6}) {
			x *= v;
			break;
		}
	}

	public boolean			instanceOfTest(@NotNull Object obj)
	{
		return obj instanceof PhpTransTested;
	}

	public char			tryTest()
	{
		try {
			str.charAt(0);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		try {
			return str.charAt(0);
		}
		finally {
			magic++;
		}
	}

	public String			concatStringTest(String x)
	{
		x = str+x;
		return x+str;
	}

	public String			selectTest(String x)
	{
		return PhpTransTested.str+this.concatStringTest(x)+this.magic;
	}

	public static String		staticAccessTest()
	{
		String r = "";
		r = r+org.druf.trans.php.test.PhpTransTested.str+org.druf.trans.php.test.PhpTransTested.addNumbers(0, 5);
		r = r+PhpTransTested.str+PhpTransTested.addNumbers(1, 2);
		r = r+str+addNumbers(3, 4);
		return r;
	}

	public PhpTransDep		createDep(@NotNull PhpTransDep passed)
	{
		return passed == null ? new PhpTransDep() : passed;
	}

	public int			parenthesisTest(int x, int y)
	{
		return (x+y)*magic;
	}

	public static int		constantTest()
	{
		return MYCONST+PhpTransTested.MYCONST;
	}

	public int			collectionsTest()
	{
		ArrayList<Integer> arrList = new ArrayList<Integer>();
		arrList.add(8);
		arrList.get(0);
		typedField.add("aa");
		return typedField.size();
	}

	public <T> T			getParametrized(T value)
	{
		return value;
	}

	public <T extends Number> T	getParametrizedExtend(T value)
	{
		return value;
	}

	public Collection<String>	testGCommonFunction(Collection<Integer> in, final int arg)
	{
		final int add = arg*magic;
		return Collections2.transform(in, new Function<Integer, String>() { public String apply(Integer v) { int t = arg+magic; return String.valueOf(v+t+add); } });
	}

	@NoDynamic
	public void			noDynamic()
	{
		magic = 5;
	}

	@DynamicDefaults(defaults = { "true", "1", "\"hello\"" })
	public void			dynamicDefaults(String mandatory, boolean b, int i, String s)
	{
		magic = b ? i : Integer.valueOf(s);
	}

	public Thread.State		enumReturn()
	{
		return Thread.State.RUNNABLE;
	}

	public Object			enumTranslate()
	{
		return Thread.State.TERMINATED;
	}

	public int			enumSwitch(Thread.State method)
	{
		switch (method) {
		case BLOCKED:
			return 0;

		case RUNNABLE:
			return 4;

		case WAITING:
			return 5;

		default:
			return -1;
		}
	}

	public void			testExactMethodMatch() throws IOException
	{
		IOUtils.write(staticAccessTest(), System.out);
	}

	public void			dataStructuresTest()
	{
		blackHole(new HashMap<Properties, String>());
		blackHole(new LinkedList<Properties>());
		blackHole(new HashSet<Properties>());
	}

	public void			nativeDataStructuresTest()
	{
		blackHole(new HashMap<Integer, String>());
		blackHole(new LinkedHashMap<String, String>());
	}

	protected String		name = "name";

	int[]				list = new int[5];
	int				magic = 6;
	public static String		str = "abcd";
	boolean				uninit;

	List<String>			typedField;

	public static final int		MYCONST = 11;

	/**
	 * Field comments.
	 */
	@NotNull
	Object				myObject;

	/*
	 * Field no doc comments.
	 */
	public static List<String>	complexStaticInitedOne = new LinkedList<String>();

	// Simple no doc comment.
	public static String[]		complexStaticInitedTwo = new String[]{ "a", null };

	public List<String>		complexInstanceInitedOne = new LinkedList<String>();

	public int[]			complexInstanceInitedTwo = new int[]{ 0, 1 };
}
