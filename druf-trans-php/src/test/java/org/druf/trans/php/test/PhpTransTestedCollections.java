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

/**
 * Comments to the class, more comments on next line including some multibyte unicode characters:
 * Žluťoučký kůň úpěl ďábelské ódy.
 *
 * @author
 * 	rat
 * @since
 * 	2015-06-11
 */
public class PhpTransTestedCollections extends Object
{
	/**
	 * Constructor comments.
	 */
	public				PhpTransTestedCollections()
	{
		super();
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

	public void			dataStructuresTest()
	{
		blackHole(new HashMap<Thread, String>());
		blackHole(new LinkedList<Thread>());
		blackHole(new HashSet<Thread>());
	}

	public void			nativeDataStructuresTest()
	{
		blackHole(new HashMap<Integer, String>());
		blackHole(new LinkedHashMap<String, String>());
	}

	public int			collectionMethods()
	{
		new HashSet<String>().forEach((String value) -> value.length());
		return 0;
	}

	public int			mapMethods()
	{
		new HashMap<String, String>().forEach((String key, String value) -> value.length());
		return 0;
	}
}
