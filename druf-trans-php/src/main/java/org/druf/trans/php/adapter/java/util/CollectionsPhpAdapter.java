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

import java.lang.reflect.Method;
import java.util.List;

import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.Trees;

import org.druf.trans.VisitResult;
import org.druf.trans.php.adapter.BasicPhpAdapter;
import org.druf.trans.php.PhpTransVisitor;


public class CollectionsPhpAdapter extends BasicPhpAdapter
{
	@Override
	public VisitResult		processMethodInvocation(VisitResult path, Method method, List<VisitResult> arguments, MethodInvocationTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder();
		if (method.getName().equals("reverse")) {
			appendArgumentsDirect(sb, node.getArguments(), trees);
			String args = sb.toString();
			sb.append(" = ");
			sb.append("\\org\\druf\\util\\Collections::reverse(");
			sb.append(args);
			sb.append(")");
			return new VisitResult(sb.toString());
		}
		else if (method.getName().equals("emptyMap")) {
			sb.append("new \\org\\druf\\util\\HashMap()");
			return new VisitResult(sb.toString()).updateExpressionPriority(visitor.getPriorityNew());
		}
		else if (method.getName().equals("reverseOrder")) {
			sb.append("function ($a, $b) { return \\org\\druf\\core\\Druf::compareToObject($b, $a); }");
			return new VisitResult(sb.toString());
		}
		else if (method.getName().equals("sort")) {
			sb.append("\\org\\druf\\util\\Collections::sort(");
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append(")");
			return new VisitResult(sb.toString());
		}
		else if (method.getName().equals("singletonList")) {
			sb.append("\\org\\druf\\util\\Collections::singletonList(");
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append(")");
			return new VisitResult(sb.toString());
		}
		return super.processCommonMethodInvocationOrError(path, method, arguments, node, trees);
	}
}
