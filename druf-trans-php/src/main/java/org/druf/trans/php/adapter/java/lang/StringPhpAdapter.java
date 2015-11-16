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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.List;

import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.util.Trees;

import org.druf.trans.VisitResult;
import org.druf.trans.php.PhpTransVisitor;


public class StringPhpAdapter extends ScalarPhpAdapter
{
	@Override
	public VisitResult		processNewInstance(Class<?> clazz, Constructor<?> constructor, List<VisitResult> arguments, NewClassTree node, Trees trees)
	{
		if ((arguments.size() == 1 && arguments.get(0).getResultClass() == byte[].class) ||
				(arguments.size() == 2 && arguments.get(0).getResultClass() == byte[].class && arguments.get(1).getResultClass() == Charset.class)) {
			StringBuilder sb = new StringBuilder("(");
			sb.append(arguments.get(0).getContent());
			sb.append(")");
			return new VisitResult(sb);
		}
		else if (arguments.size() <= 1) {
			StringBuilder sb = new StringBuilder("strval");
			sb.append("(");
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append(")");
			return new VisitResult(sb.toString());
		}
		else {
			StringBuilder sb = new StringBuilder("new String");
			sb.append("(");
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append(")");
			return new VisitResult(visitor.surroundFixSb(sb, null));
		}
	}

	@Override
	public VisitResult		processInstanceOf(Class<?> className, InstanceOfTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("is_string(");
		sb.append(this.visitor.visitExpression(node.getExpression(), trees).getContent());
		sb.append(")");
		return new VisitResult(sb.toString())
			.updateExpressionPriority(visitor.getPriorityCall());
	}

	@Override
	public VisitResult		processMethodInvocation(VisitResult path, Method method, List<VisitResult> arguments, MethodInvocationTree node, Trees trees)
	{
		int priority = visitor.getPriorityCall();
		StringBuilder sb = new StringBuilder();
		if (method.getName().equals("compareTo")) {
			sb.append("strcmp(").append(path.getContent()).append(", ");
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append(")");
		}
		else if (method.getName().equals("substring")) {
			sb.append("strval(substr(").append(path.getContent()).append(", ");
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append("))");
		}
		else if (method.getName().equals("charAt")) {
			sb.append("\\org\\druf\\core\\StringWrap::charAt(");
			sb.append(path.getContent()).append(", ");
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append(")");
		}
		else if (method.getName().equals("format")) {
			sb.append("sprintf(");
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append(")");
		}
		else if (method.getName().equals("intern")) {
			sb.append(path.getContent());
		}
		else if (method.getName().equals("isEmpty")) {
			visitor.surroundLeftPrioritized(path, visitor.getTargetOperator("php==="));
			sb.append(path.getContent()).append(" === \"\"");
			priority = visitor.getOperatorPriority("php===");
		}
		else if (method.getName().equals("length")) {
			sb.append("strlen(");
			sb.append(path.getContent());
			sb.append(")");
		}
		else if (method.getName().equals("toLowerCase")) {
			sb.append("strtolower(");
			sb.append(path.getContent());
			sb.append(")");
		}
		else if (method.getName().equals("toUpperCase")) {
			sb.append("strtoupper(");
			sb.append(path.getContent());
			sb.append(")");
		}
		else if (method.getName().equals("valueOf")) {
			sb.append("strval(");
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append(")");
		}
		else if (method.getName().equals("replace")) {
			sb.append("str_replace(");
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append(", ").append(path.getContent());
			sb.append(")");
		}
		else if (method.getName().equals("replaceAll")) {
			sb.append("\\org\\druf\\core\\StringWrap::replaceRegExp(");
			sb.append(path.getContent()).append(", ");
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append(")");
		}
		else if (method.getName().equals("matches")) {
			sb.append("\\org\\druf\\core\\StringWrap::matchRegExp(");
			sb.append(path.getContent()).append(", ");
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append(")");
		}
		else if (method.getName().equals("indexOf")) {
			sb.append("\\org\\druf\\core\\StringWrap::indexOf(");
			sb.append(path.getContent()).append(", ");
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append(")");
		}
		else if (method.getName().equals("lastIndexOf")) {
			sb.append("\\org\\druf\\core\\StringWrap::lastIndexOf(");
			sb.append(path.getContent()).append(", ");
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append(")");
		}
		else if (method.getName().equals("getBytes")) {
			sb.append(path.getContent());
			priority = path.getExpressionPriority();
		}
		else if (method.getName().equals("startsWith") && method.getParameterTypes().length == 1) {
			sb.append("substr(").append(path.getContent()).append(", 0, strlen(");
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append(")) == ");
			sb.append(visitor.surroundRightPrioritized(arguments.get(0), visitor.getTargetOperator("==")).getContent());
			priority = visitor.getOperatorPriority("==");
		}
		else if (method.getName().equals("endsWith") && method.getParameterTypes().length == 1) {
			sb.append("substr(").append(path.getContent()).append(", -strlen(");
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append(")) == ");
			sb.append(visitor.surroundRightPrioritized(arguments.get(0), visitor.getTargetOperator("==")).getContent());
			priority = visitor.getOperatorPriority("php===");
		}
		else if (method.getName().equals("trim")) {
			sb.append("trim(");
			sb.append(path.getContent());
			sb.append(")");
		}
		else if (method.getName().equals("split")) {
			sb.append("\\org\\druf\\core\\StringWrap::splitRegExp(");
			sb.append(path.getContent()).append(", ");
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append(")");
		}
		else {
			return super.processMethodInvocation(path, method, arguments, node, trees);
		}
		return new VisitResult(sb).updateExpressionPriority(priority);
	}
}
