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
package net.dryuf.trans;

import java.util.LinkedHashSet;
import java.util.Set;


public class ClassUtil extends java.lang.Object
{
	public static Iterable<Class<?>> listClassHierarchyClassFirst(Class<?> clazz)
	{
		Set<Class<?>> result = new LinkedHashSet<>();
		result.add(clazz);
		for (Class<?> current = clazz.getSuperclass(); current != null; current = current.getSuperclass()) {
			result.add(current);
		}
		for (Class<?> current = clazz; current != null; current = current.getSuperclass()) {
			collectClassInterfaces(result, current);
		}
		return result;
	}

	protected static void		collectClassInterfaces(Set<Class<?>> result, Class<?> clazz)
	{
		for (Class<?> iface: clazz.getInterfaces()) {
			result.add(iface);
		}
		for (Class<?> iface: clazz.getInterfaces()) {
			collectClassInterfaces(result, iface);
		}
	}
}
