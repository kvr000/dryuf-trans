/*
 * dryuf library
 *
 * dryuf multiplatform development toolkit
 *
 * ----------------------------------------------------------------------------------
 *
 * Copyright (C) 2013-2015 Zbyněk Vyškovský
 *
 * ----------------------------------------------------------------------------------
 *
 * LICENSE:
 *
 * This file is part of dryuf
 *
 * dryuf is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * dryuf is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with dryuf; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * @author	2013-2015 Zbyněk Vyškovský
 * @link	mailto:kvr@matfyz.cz
 * @link	http://kvr.matfyz.cz/software/java/dryuf/trans/
 * @license	http://www.gnu.org/licenses/lgpl.txt GNU Lesser General Public License v3
 */
package net.dryuf.trans.php.test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Comments to the class, more comments on next line including some multibyte unicode characters:
 * Žluťoučký kůň úpěl ďábelské ódy.
 *
 * @author
 *	Zbyněk Vyškovský
 * @since
 * 	2013-03-12
 */
public class PhpTransTestedTyped extends java.lang.Object
{
	/**
	 * Constructor comments.
	 */
	public				PhpTransTestedTyped()
	{
		super();
		listField.add("hello");
		mapField.put(0L, "world");
	}

	public int			constructorTest()
	{
		return new LinkedList<String>().get(0).length();
	}

	public int			fieldTest()
	{
		int s = listField.size();
		int l = listField.get(0).length();
		int v = mapField.get(0L).length();
		return s+l+v;
	}

	public int			paramTest(List<String> listParam, Map<Long, String> mapParam)
	{
		int s = listParam.size();
		int l = listParam.get(0).length();
		int v = mapParam.get(0L).length();
		return s+l+v;
	}

	public int			localTest()
	{
		List<String> listVar = new LinkedList<String>(); listField.add("hello");
		Map<Long, String> mapVar = new HashMap<>(); mapField.put(0L, "world");
		int s = listVar.size();
		int l = listVar.get(0).length();
		int v = mapVar.get(0L).length();
		return s+l+v;
	}

	public int			methodMatchTest() throws IllegalAccessException, InstantiationException
	{
		int i = 0;
		i += Integer.class.newInstance();
		i += String.class.newInstance().length();
		return i;
	}

	protected List<String>		listField = new LinkedList<String>();
	protected Map<Long, String>	mapField = new HashMap<>();
}
