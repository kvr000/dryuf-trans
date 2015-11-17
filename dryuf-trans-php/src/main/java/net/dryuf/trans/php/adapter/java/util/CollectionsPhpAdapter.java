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
package net.dryuf.trans.php.adapter.java.util;

import java.lang.reflect.Method;
import java.util.List;

import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.Trees;

import net.dryuf.trans.VisitResult;
import net.dryuf.trans.php.adapter.BasicPhpAdapter;
import net.dryuf.trans.php.PhpTransVisitor;


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
			sb.append("\\net\\dryuf\\util\\Collections::reverse(");
			sb.append(args);
			sb.append(")");
			return new VisitResult(sb.toString());
		}
		else if (method.getName().equals("emptyMap")) {
			sb.append("new \\net\\dryuf\\util\\HashMap()");
			return new VisitResult(sb.toString()).updateExpressionPriority(visitor.getPriorityNew());
		}
		else if (method.getName().equals("reverseOrder")) {
			sb.append("function ($a, $b) { return \\net\\dryuf\\core\\Dryuf::compareToObject($b, $a); }");
			return new VisitResult(sb.toString());
		}
		else if (method.getName().equals("sort")) {
			sb.append("\\net\\dryuf\\util\\Collections::sort(");
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append(")");
			return new VisitResult(sb.toString());
		}
		else if (method.getName().equals("singletonList")) {
			sb.append("\\net\\dryuf\\util\\Collections::singletonList(");
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append(")");
			return new VisitResult(sb.toString());
		}
		return super.processCommonMethodInvocationOrError(path, method, arguments, node, trees);
	}
}
