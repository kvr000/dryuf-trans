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
package net.dryuf.trans.php.adapter.java.lang;

import java.lang.reflect.Method;
import java.util.List;

import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.Trees;

import net.dryuf.trans.VisitResult;
import net.dryuf.trans.php.PhpTransVisitor;


public class NumberPhpAdapter extends ScalarPhpAdapter
{
	@Override
	public VisitResult		processInstanceOf(Class<?> className, InstanceOfTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("is_numeric(");
		sb.append(this.visitor.visitExpression(node.getExpression(), trees).getContent());
		sb.append(")");
		return new VisitResult(sb.toString())
			.updateExpressionPriority(visitor.getPriorityCall());
	}

	@Override
	public VisitResult		processMethodInvocation(VisitResult path, Method method, List<VisitResult> arguments, MethodInvocationTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder();
		if (method.getName().equals("boolValue")) {
			return new VisitResult(sb.append("\\net\\dryuf\\core\\Dryuf::toBool(").append(path).append(")").toString());
		}
		else if (method.getName().equals("byteValue")) {
			return new VisitResult(sb.append("intval(").append(path.getContent()).append(")").toString());
		}
		else if (method.getName().equals("shortValue")) {
			return new VisitResult(sb.append("intval(").append(path.getContent()).append(")").toString());
		}
		else if (method.getName().equals("intValue")) {
			return new VisitResult(sb.append("intval(").append(path.getContent()).append(")").toString());
		}
		else if (method.getName().equals("longValue")) {
			return new VisitResult(sb.append("intval(").append(path.getContent()).append(")").toString());
		}
		else if (method.getName().equals("floatValue")) {
			return new VisitResult(sb.append("floatval(").append(path.getContent()).append(")").toString());
		}
		else if (method.getName().equals("doubleValue")) {
			return new VisitResult(sb.append("floatval(").append(path.getContent()).append(")").toString());
		}
		else {
			return super.processMethodInvocation(path, method, arguments, node, trees);
		}
	}
}
