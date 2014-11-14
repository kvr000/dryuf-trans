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
import java.util.Map;

import javax.validation.constraints.NotNull;


public class CatchingIdentifierStore extends java.lang.Object implements IdentifierStore
{
	public				CatchingIdentifierStore(@NotNull IdentifierStore parent)
	{
		this.parent = parent;
	}

	@Override
	public IdentifierDef		findIdentifier(String name)
	{
		IdentifierDef idef = parent.findIdentifier(name);
		if (idef != null && !catched.containsKey(name)) {
			catched.put(name, idef);
		}
		return idef;
	}

	@Override
	public void			addIdentifier(IdentifierDef idef)
	{
		throw new UnsupportedOperationException("addIdentifier not supported on catcher");
	}

	public Map<String, IdentifierDef> getCatched()
	{
		return catched;
	}

	@Override
	public IdentifierStore		getParent()
	{
		return parent;
	}

	Map<String, IdentifierDef>	catched = new LinkedHashMap<String, IdentifierDef>();
	IdentifierStore			parent;
}
