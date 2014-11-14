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
package org.druf.trans.php.adapter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.util.Trees;

import org.druf.trans.VisitResult;
import org.druf.trans.php.PhpTransVisitor;
import org.druf.trans.php.adapter.PrimitiveTypePhpAdapter;


public class ByteArrayPhpAdapter extends PrimitiveArrayPhpAdapter
{
	@Override
	public VisitResult		processNewArray(Class<?> clazz, List<VisitResult> dimensions, List<VisitResult> initializers, NewArrayTree node, Trees trees)
	{
		return super.processNewArray(clazz, dimensions, initializers, node, trees)
			.prependString("implode(array_map('chr', ").appendString("))");
	}

	@Override
	public VisitResult		processArrayAccess(VisitResult array, VisitResult index, ArrayAccessTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("ord(");
		sb.append(array.getContent());
		sb.append("[");
		sb.append(index.getContent());
		sb.append("]");
		sb.append(")");
		return new VisitResult(sb);
	}

	@Override
	public VisitResult		processFieldAccess(VisitResult path, Field field, MemberSelectTree node, Trees trees)
	{
		if (node.getIdentifier().toString().equals("length")) {
			return new VisitResult("strlen("+path.getContent()+")");
		}
		else {
			return super.processFieldAccess(path, field, node, trees);
		}
	}
}
