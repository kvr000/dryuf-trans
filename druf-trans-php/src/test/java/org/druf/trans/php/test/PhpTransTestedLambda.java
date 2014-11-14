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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Class to test parameterized types translation.
 *
 * @author
 * 	Zbyněk Vyškovský
 * @since
 * 	2015-06-10
 */
public class PhpTransTestedLambda extends java.lang.Object
{
	public int			matchSimple()
	{
		int i = 0;
		i += Lists.transform(stringList, (String s) -> s.length()).get(0);
		i += Lists.transform(stringList, (String s) -> s + "x").get(0).length();
		i += Sets.filter(stringSet, (String s) -> s.length() > 0).size();
		i += Sets.filter(stringSet, (String s) -> s.length() > 0).size();
		return i;
	}

	protected List<String>		stringList = new LinkedList<>();

	protected Set<String> stringSet = new HashSet<>();
}
