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

import java.util.LinkedHashMap;
import java.util.Map;


public abstract class AbstractIdentifierStore extends java.lang.Object implements IdentifierStore
{
	public				AbstractIdentifierStore()
	{
	}

	@Override
	public IdentifierDef		findIdentifier(String name)
	{
		IdentifierDef idef;
		if (identifiers.containsKey(name))
			return identifiers.get(name);
		int p;
		if ((p = name.indexOf('.')) >= 0 && (idef = identifiers.get(name.substring(0, p))) != null && idef.getType() == IdentifierDef.IdentifierType.IT_Class) {
			Class<?> current = idef.getRawClass();
			for (String rest = name.substring(p+1); rest != null;) {
				String next;
				if ((p = rest.indexOf('.')) >= 0) {
					next = rest.substring(0, p);
					rest = rest.substring(p + 1);
				}
				else {
					next = rest;
					rest = null;
				}
				boolean found = false;
				for (Class<?> clazz: current.getDeclaredClasses()) {
					if (clazz.getSimpleName().equals(next)) {
						current = clazz;
						found = true;
					}
				}
				if (!found) {
					current = null;
					break;
				}
			}
			if (current != null) {
				identifiers.put(name, idef = IdentifierDef.createClass(name, BoundType.createRaw(current)));
				return idef;
			}
		}
		return findFallback(name);
	}

	protected abstract IdentifierDef findFallback(String name);

	@Override
	public void			addIdentifier(IdentifierDef idef)
	{
		identifiers.put(idef.getName(), idef);
	}

	@Override
	public IdentifierStore		getParent()
	{
		return null;
	}

	Map<String, IdentifierDef>	identifiers = new LinkedHashMap<String, IdentifierDef>();
}
