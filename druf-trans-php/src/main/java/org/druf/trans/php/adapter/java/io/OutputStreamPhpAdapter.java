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
package org.druf.trans.php.adapter.java.io;

import java.lang.reflect.Method;
import java.util.List;

import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.Trees;

import org.druf.trans.VisitResult;
import org.druf.trans.php.PhpTransVisitor;
import org.druf.trans.php.adapter.BasicPhpAdapter;


public class OutputStreamPhpAdapter extends BasicPhpAdapter
{
	@Override
	public VisitResult		processInstanceOf(Class<?> className, InstanceOfTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("\\org\\druf\\io\\IoUtil::isStream(");
		sb.append(this.visitor.visitExpression(node.getExpression(), trees).getContent());
		sb.append(")");
		return new VisitResult(sb.toString());
	}

	@Override
	public VisitResult		processMethodInvocation(VisitResult path, Method method, List<VisitResult> arguments, MethodInvocationTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder();
		if (method.getName().equals("close")) {
			sb.append("fclose(").append(path.getContent());
			sb.append(")");
		}
		else if (method.getName().equals("flush")) {
			sb.append("fflush(").append(path.getContent());
			sb.append(")");
		}
		else if (method.getName().equals("write") && method.getParameterTypes().length == 1 && method.getParameterTypes()[0] == byte[].class) {
			sb.append("fwrite(").append(path.getContent());
			this.visitor.appendArgument(sb, false, arguments.get(0));
			sb.append(")");
		}
		else {
			return super.processCommonMethodInvocationOrError(path, method, arguments, node, trees);
		}
		return new VisitResult(sb);
	}
}
