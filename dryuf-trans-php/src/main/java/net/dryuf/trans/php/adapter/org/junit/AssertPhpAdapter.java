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
package net.dryuf.trans.php.adapter.org.junit;

import java.lang.reflect.Method;
import java.util.List;

import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.Trees;

import net.dryuf.trans.VisitResult;
import net.dryuf.trans.php.adapter.WrongPhpAdapter;


public class AssertPhpAdapter extends WrongPhpAdapter
{
	public VisitResult		processMethodInvocation(VisitResult path, Method method, List<VisitResult> arguments, MethodInvocationTree node, Trees trees)
	{
		Class<?>[] methodParams = method.getParameterTypes();
		StringBuilder sb = new StringBuilder();
		if ((method.getName().equals("assertEquals") || method.getName().equals("assertNotEquals")) && (
				(methodParams.length == 3 && ((methodParams[0] == double.class && methodParams[2] == double.class) || (methodParams[0] == float.class && methodParams[2] == float.class))) ||
				(methodParams.length == 4 && methodParams[0] == String.class && ((methodParams[1] == double.class && methodParams[3] == double.class) || (methodParams[1] == float.class && methodParams[3] == float.class)))
				)) {
			sb.append("\\net\\dryuf\\tenv\\DAssert::").append(method.getName()).append("Percent1");
			sb.append("(");
			if (arguments.size() == 4 && methodParams.length == 4 && methodParams[0] == String.class) {
				appendArgument(sb, true, node.getArguments().get(1), trees);
				appendArgument(sb, false, node.getArguments().get(2), trees);
				appendArgument(sb, false, node.getArguments().get(0), trees);
			}
			else {
				appendArgument(sb, true, node.getArguments().get(0), trees);
				appendArgument(sb, false, node.getArguments().get(1), trees);
			}
			sb.append(")");
			return new VisitResult(sb.toString());
		}
		else if ((method.getName().equals("assertEquals") || method.getName().equals("assertNotEquals") || method.getName().equals("assertSame") || method.getName().equals("assertNotSame"))) {
			sb.append("\\net\\dryuf\\tenv\\DAssert::").append(method.getName());
			sb.append("(");
			if (arguments.size() == 3 && methodParams.length == 3 && methodParams[0] == String.class) {
				appendArgument(sb, true, node.getArguments().get(1), trees);
				appendArgument(sb, false, node.getArguments().get(2), trees);
				appendArgument(sb, false, node.getArguments().get(0), trees);
			}
			else {
				appendArguments(sb, method, node.getArguments(), trees);
			}
			sb.append(")");
			return new VisitResult(sb.toString());
		}
		else if ((method.getName().equals("assertArrayEquals"))) {
			sb.append("\\net\\dryuf\\tenv\\DAssert::");
			if (arguments.get(0).getResultClass() == byte[].class) {
				sb.append("assertEquals");
			}
			else {
				sb.append(method.getName());
			}
			sb.append("(");
			if (arguments.size() == 3) {
				appendArgument(sb, true, node.getArguments().get(1), trees);
				appendArgument(sb, false, node.getArguments().get(2), trees);
				appendArgument(sb, false, node.getArguments().get(0), trees);
			}
			else {
				appendArguments(sb, method, node.getArguments(), trees);
			}
			sb.append(")");
			return new VisitResult(sb.toString());
		}
		else if ((method.getName().equals("assertNull") || method.getName().equals("assertNotNull") || method.getName().equals("assertFalse") || method.getName().equals("assertTrue"))) {
			sb.append("\\net\\dryuf\\tenv\\DAssert::").append(method.getName());
			sb.append("(");
			if (arguments.size() == 2) {
				appendArgument(sb, true, node.getArguments().get(1), trees);
				appendArgument(sb, false, node.getArguments().get(0), trees);
			}
			else {
				appendArguments(sb, method, node.getArguments(), trees);
			}
			sb.append(")");
		}
		else if ((method.getName().equals("fail"))) {
			sb.append("\\net\\dryuf\\tenv\\DAssert::").append(method.getName());
			sb.append("(");
			appendArguments(sb, method, node.getArguments(), trees);
			sb.append(")");
		}
		else {
			return this.processCommonMethodInvocationOrError(path, method, arguments, node, trees);
		}
		return new VisitResult(sb.toString());
	}
}
