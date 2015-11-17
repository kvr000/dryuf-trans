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


import java.lang.reflect.Type;

public class IdentifierDef
{
	public enum IdentifierType
	{
		IT_Package,
		IT_Class,
		IT_Typed,
		IT_This,
		IT_Super,
		IT_Field,
		IT_Method,
		IT_Variable,
	}

	public				IdentifierDef(IdentifierType it, String name, int modifiers, BoundType boundType)
	{
		this.type = it;
		this.name = name;
		this.modifiers = modifiers;
		this.boundType = boundType;
	}

	public static IdentifierDef	createPackage(String name)
	{
		return new IdentifierDef(IdentifierType.IT_Package, name, 0, null);
	}

	public static IdentifierDef	createClass(String name, BoundType boundType)
	{
		return new IdentifierDef(IdentifierType.IT_Class, name, 0, boundType);
	}

	public static IdentifierDef	createTyped(String name, BoundType boundType)
	{
		return new IdentifierDef(IdentifierType.IT_Typed, name, 0, boundType);
	}

	public static IdentifierDef	createThis(BoundType boundType)
	{
		return new IdentifierDef(IdentifierType.IT_This, "this", 0, boundType);
	}

	public static IdentifierDef	createSuper(BoundType boundType)
	{
		return new IdentifierDef(IdentifierType.IT_Super, "super", 0, boundType);
	}

	public static IdentifierDef	createField(String name, int modifiers, BoundType boundType)
	{
		return new IdentifierDef(IdentifierType.IT_Field, name, modifiers, boundType);
	}

	public static IdentifierDef	createMethod(String name, int modifiers, BoundType boundType)
	{
		return new IdentifierDef(IdentifierType.IT_Method, name, modifiers, boundType);
	}

	public static IdentifierDef	createVariable(String name, BoundType boundType)
	{
		return new IdentifierDef(IdentifierType.IT_Variable, name, 0, boundType);
	}

	public IdentifierType		getType()
	{
		return type;
	}

	public int			getModifiers()
	{
		return modifiers;
	}

	public String			getName()
	{
		return name;
	}

	public BoundType		getBoundType()
	{
		return boundType;
	}

	public Class<?>			getRawClass()
	{
		return boundType.getRawClass();
	}

	public IdentifierType		type;

	public int			modifiers;

	public String			name;

	public BoundType		boundType;
}
