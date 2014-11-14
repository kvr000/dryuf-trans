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
package org.druf.trans.php.adapter.java.util;

import java.lang.reflect.Constructor;
import java.util.List;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.Trees;

import org.druf.trans.VisitResult;
import org.druf.trans.php.adapter.BasicPhpAdapter;
import org.druf.trans.php.PhpTransVisitor;


public class PhpHashSetPhpAdapter extends BasicPhpAdapter
{
	@Override
	public VisitResult		processNewInstance(Class<?> clazz, Constructor<?> constructor, List<VisitResult> arguments, NewClassTree node, Trees trees)
	{
		String className = clazz.getName().replaceAll("^java\\.", "org.druf.");
		ExpressionTree identifier = node.getIdentifier();
		if (identifier instanceof ParameterizedTypeTree) {
			List<? extends Tree> typeArguments = ((ParameterizedTypeTree)identifier).getTypeArguments();
			if (typeArguments != null && typeArguments.size() >= 1) {
				Class<?> keyType = visitor.resolveType(typeArguments.get(0));
				if (keyType == String.class || keyType == Boolean.class || keyType == Short.class || keyType == Byte.class || keyType == Character.class || keyType == Integer.class || keyType == Long.class)
					className = className.replaceAll("[^.]*HashSet$", keyType == String.class ? "php.StringNativeHashSet" : "php.NativeHashSet");
			}
		}
		StringBuilder sb = new StringBuilder("new ");
		sb.append(this.visitor.translateClassIdentifier(className));
		sb.append("(");
		appendArgumentsDirect(sb, node.getArguments(), trees);
		sb.append(")");
		if (node.getClassBody() != null) {
			sb.append(this.visitor.surroundFix(node.getClassBody().toString(), null));
		}
		return new VisitResult(sb);
	}
}
