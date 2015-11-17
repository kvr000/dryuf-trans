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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.util.Trees;

import net.dryuf.trans.VisitResult;
import net.dryuf.trans.php.PhpTransVisitor;


public class LongPhpAdapter extends NumberPhpAdapter
{
	@Override
	public VisitResult		processNewInstance(Class<?> clazz, Constructor<?> constructor, List<VisitResult> arguments, NewClassTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder("intval");
		sb.append("(");
		appendArgumentsDirect(sb, node.getArguments(), trees);
		sb.append(")");
		return new VisitResult(sb.toString());
	}

	@Override
	public VisitResult		processInstanceOf(Class<?> className, InstanceOfTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("is_int(");
		sb.append(this.visitor.visitExpression(node.getExpression(), trees).getContent());
		sb.append(")");
		return new VisitResult(sb.toString());
	}

	public VisitResult		processFieldAccess(VisitResult path, Field field, MemberSelectTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder();
		if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
			if (field.getName().equals("MAX_VALUE")) {
				return new VisitResult(sb.append("((1<<63)-1)").toString());
			}
			else if (field.getName().equals("MIN_VALUE")) {
				return new VisitResult(sb.append("(-(1<<63))").toString());
			}
		}
		return new VisitResult(this.visitor.surroundFix(super.processFieldAccess(path, field, node, trees).getContent(), null));
	}

	public VisitResult		processMethodInvocation(VisitResult path, Method method, List<VisitResult> arguments, MethodInvocationTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder();
		if (method.getName().equals("valueOf") && method.getParameterTypes().length == 1 && this.visitor.isClassNumberish(method.getParameterTypes()[0])) {
			sb.append("intval(");
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append(")");
			return new VisitResult(sb.toString());
		}
		else if (method.getName().equals("valueOf") || method.getName().equals("parseLong") || method.getName().equals("decode")) {
			sb.append("\\net\\dryuf\\core\\Dryuf::parseInt(");
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append(")");
			return new VisitResult(sb.toString());
		}
		else if (method.getName().equals("numberOfTrailingZeros")) {
			sb.append("intval(logger(");
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append(", 2))");
			return new VisitResult(sb.toString());
		}
		else if (method.getName().equals("toString") && arguments.size() == 1) {
			sb.append("strval(");
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append(")");
			return new VisitResult(sb.toString());
		}
		return super.processMethodInvocation(path, method, arguments, node, trees);
	}
}
