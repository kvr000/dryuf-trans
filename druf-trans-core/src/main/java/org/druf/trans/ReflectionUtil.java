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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ReflectionUtil extends Object
{
	public static Field		getClassPublicField(Class<?> cls, String fieldName)
	{
		try {
			return cls.getField(fieldName);
		}
		catch (Exception ex) {
			throw translateException(ex);
		}
	}

	public static RuntimeException	translateException(Throwable ex)
	{
		if (ex instanceof RuntimeException) {
			return (RuntimeException)ex;
		}
		else if (ex instanceof InvocationTargetException) {
			return translateCauseException(ex);
		}
		else {
			return new RuntimeException(ex);
		}
	}

	public static RuntimeException	translateCauseException(Throwable ex)
	{
		if (ex.getCause() != null)
			return translateException(ex.getCause());
		throw new RuntimeException(ex);
	}

	/**
	 * Translates primitive type to its wrapper class. Returns null if passed class is not primitive.
	 *
	 * @param orig
	 * 	original class
	 *
	 * @return null
	 * 	if the passed argument is not primitive
	 * @return
	 * 	wrapper class for its primitive
	 */
	public static Class<?>		translatePrimitiveToWrap(Class<?> orig)
	{
		return primitiveMap.get(orig);
	}

	protected static final Map<Class<?>, Class<?>> primitiveMap = TransUtil.createLinkedHashMap(
			(Class<?>)boolean.class,	(Class<?>)Boolean.class,
			char.class,			Character.class,
			byte.class,			Byte.class,
			short.class,			Short.class,
			int.class,			Integer.class,
			long.class,			Long.class,
			float.class,			Float.class,
			double.class,			Double.class
	);
}
