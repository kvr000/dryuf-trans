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
package org.druf.trans.proxy;


import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.TreeVisitor;

import javax.lang.model.element.Name;
import java.util.List;


public class IdentifierTreeProxy implements IdentifierTree
{
	public				IdentifierTreeProxy(IdentifierTree target)
	{
		this.target = target;
	}

	@Override
	public Name			getName()
	{
		return target.getName();
	}

	@Override
	public Kind			getKind()
	{
		return target.getKind();
	}

	@Override
	public <R, D> R			accept(TreeVisitor<R, D> treeVisitor, D d)
	{
		return treeVisitor.visitIdentifier(this, d);
	}

	protected IdentifierTree		target;
}
