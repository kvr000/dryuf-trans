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
package org.druf.trans;

import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TransUtil extends java.lang.Object
{
	public static String[]		matchText(String regexp, String text)
	{
		Pattern regExp = Pattern.compile(regexp);
		Matcher matcher = regExp.matcher(text);

		if (matcher != null && matcher.matches()) {
			String[] groups = new String[(matcher.groupCount()+1)];
			for (int i = 0; i <= matcher.groupCount(); i++) {
				groups[i] = matcher.group(i);
			}
			return groups;
		}
		return null;
	}

	public static <K, V> LinkedHashMap<K, V> createLinkedHashMap(K k0, V v0, Object... params)
	{
		LinkedHashMap<K, V> map = new LinkedHashMap<K, V>();
		map.put(k0, v0);
		for (int i = 0; i < params.length; ) {
			@SuppressWarnings("unchecked")
			K k = (K)params[i++];
			@SuppressWarnings("unchecked")
			V v = (V)params[i++];
			map.put(k, v);
		}
		return map;
	}
}
