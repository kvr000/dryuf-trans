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
package org.druf.trans.php.adapter.org.druf.core;

import java.lang.reflect.Method;
import java.util.List;

import org.druf.trans.BoundType;
import org.druf.trans.VisitResult;
import org.druf.trans.php.adapter.BasicPhpAdapter;

import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.Trees;


public class DrufPhpAdapter extends BasicPhpAdapter
{
	@Override
	public VisitResult		processMethodInvocation(VisitResult path, Method method, List<VisitResult> arguments, MethodInvocationTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder();
		if (method.getName().equals("getMandatoryAnnotation")) {
			BoundType resultBound = BoundType.createRaw(Object.class);
			if (arguments.get(1).getOptionalBound(0) != null) {
				resultBound = arguments.get(1).getMandatoryBound(0);
			}
			if (arguments.get(1).getResultClass() == Class.class) {
				return new VisitResult(sb.append("\\org\\druf\\core\\Druf::getClassMandatoryAnnotation(").append(arguments.get(0).getContent()).append(", ").append(arguments.get(1).getFinal()).append(")"))
					.updateResultBound(resultBound);
			}
			else {
				return new VisitResult(this.visitor.surroundFix(node.toString(), null))
					.updateResultBound(resultBound);
			}
		}
		else if (method.getName().equals("createHashMap")) {
			if (arguments.get(0).getResultClass() == String.class || visitor.isClassIntegerish(arguments.get(0).getResultClass())) {
				sb.append("\\org\\druf\\core\\Druf::createNativeHashMap");
			}
			else {
				sb.append("\\org\\druf\\core\\Druf::createHashMap");
			}
			sb.append("(");
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append(")");
			return new VisitResult(sb);
		}
		else if (method.getName().equals("createLinkedHashMap")) {
			if (arguments.get(0).getResultClass() == String.class) {
				sb.append("\\org\\druf\\core\\Druf::createStringNativeHashMap");
			}
			else if (visitor.isClassIntegerish(arguments.get(0).getResultClass())) {
				sb.append("\\org\\druf\\core\\Druf::createNativeHashMap");
			}
			else {
				sb.append("\\org\\druf\\core\\Druf::createLinkedMap");
			}
			sb.append("(");
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append(")");
			return new VisitResult(sb);
		}
		else if (method.getName().equals("createHashSet")) {
			if (arguments.get(0).getResultClass() == String.class || visitor.isClassIntegerish(arguments.get(0).getResultClass())) {
				sb.append("\\org\\druf\\core\\Druf::createNativeHashSet");
			}
			else {
				sb.append("\\org\\druf\\core\\Druf::createHashSet");
			}
			sb.append("(");
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append(")");
			return new VisitResult(sb);
		}
		return super.processMethodInvocation(path, method, arguments, node, trees);
	}
}
