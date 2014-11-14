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


import java.lang.reflect.Array;
import java.util.Arrays;

public class BoundType extends java.lang.Object
{
	public static BoundType		createRaw(Class<?> rawClass)
	{
		return new BoundType(rawClass, new BoundType[0]);
	}

	public static BoundType		createArray(BoundType elementClass)
	{
		return new BoundType(Array.newInstance(elementClass.getRawClass(), 0).getClass(), elementClass.getTypeArguments());
	}

	public static BoundType		createTyped(Class<?> rawClass, BoundType[] typeArguments)
	{
		return new BoundType(rawClass, typeArguments);
	}

	public boolean			isArray()
	{
		return rawClass.isArray();
	}

	public BoundType		getElementBound()
	{
		if (!isArray())
			throw new IllegalArgumentException("Calling getElementBound() on non-array");
		return new BoundType(rawClass.getComponentType(), getTypeArguments());
	}

	public				BoundType(Class<?> rawClass, BoundType[] typeArguments)
	{
		this.rawClass = rawClass;
		this.typeArguments = typeArguments;
	}

	public String			toString()
	{
		StringBuilder sb = new StringBuilder(rawClass.getName());
		if (typeArguments.length != 0) {
			sb.append("<");
			for (BoundType bt: typeArguments)
				sb.append(bt.toString()).append(",");
			sb.replace(sb.length()-1, sb.length(), ">");
		}
		return sb.toString();
	}

	protected Class<?>		rawClass;

	public Class<?>			getRawClass()
	{
		return this.rawClass;
	}

	protected BoundType[]		typeArguments;

	public BoundType[]		getTypeArguments()
	{
		return this.typeArguments;
	}
}
