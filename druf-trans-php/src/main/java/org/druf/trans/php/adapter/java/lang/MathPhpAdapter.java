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
package org.druf.trans.php.adapter.java.lang;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.Trees;

import org.druf.trans.VisitResult;
import org.druf.trans.php.adapter.BasicPhpAdapter;


public class MathPhpAdapter extends BasicPhpAdapter
{
	public VisitResult		processFieldAccess(VisitResult path, Field field, MemberSelectTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder();
		if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
			if (field.getName().equals("PI")) {
				return new VisitResult(sb.append("M_PI").toString());
			}
		}
		return new VisitResult(this.visitor.surroundFix(super.processFieldAccess(path, field, node, trees).getContent(), null));
	}

	@SuppressWarnings("unused")
	public VisitResult		processMethodInvocation(VisitResult path, Method method, List<VisitResult> arguments, MethodInvocationTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder();
		String out;
		if (directTranslate.contains(method.getName())) {
			sb.append(method.getName()).append("(");
			appendArgumentsDirect(sb, node.getArguments(), trees);
			return new VisitResult(sb.append(")").toString());
		}
		else if (method.getName().equals("max") || method.getName().equals("min")) {
			sb.append(method.getName()).append("(array(");
			appendArgumentsDirect(sb, node.getArguments(), trees);
			return new VisitResult(sb.append("))").toString());
		}
		else {
			return processCommonMethodInvocationOrError(path, method, arguments, node, trees);
		}
	}

	protected static Set<String>	directTranslate = new LinkedHashSet<>(Arrays.asList("cos", "sin", "tan", "atan2", "atan", "asin", "acos", "log", "log10", "floor", "ceil", "sqrt", "abs"));
}
