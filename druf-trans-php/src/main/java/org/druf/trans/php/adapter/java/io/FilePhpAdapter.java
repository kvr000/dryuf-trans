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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.util.Trees;

import org.druf.trans.TargetOperator;
import org.druf.trans.VisitResult;
import org.druf.trans.php.adapter.BasicPhpAdapter;
import org.druf.trans.php.PhpTransVisitor;


public class FilePhpAdapter extends BasicPhpAdapter
{
	@Override
	public VisitResult		processNewInstance(Class<?> clazz, Constructor<?> constructor, List<VisitResult> arguments, NewClassTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder();
		if (arguments.size() == 1) {
			VisitResult argument = arguments.get(0);
			return new VisitResult(argument.getContent())
				.updateEmptyResultIndicator(VisitResult.RI_Expression)
				.updateEmptyExpressionPriority(argument.getExpressionPriority());
		}
		TargetOperator stringPlusOperator = visitor.getTargetOperator("string+");
		int trimLength = 0;
		for (int i = 0; i < arguments.size(); ++i) {
			VisitResult argument = arguments.get(i);
			visitor.surroundLeftPrioritized(argument, stringPlusOperator);
			appendArgument(sb, true, argument);
			trimLength = sb.length();
			sb.append(stringPlusOperator.getText()+"\"/\""+stringPlusOperator.getText());
		}
		return new VisitResult(sb.replace(trimLength, sb.length(), ""))
			.updateEmptyResultIndicator(VisitResult.RI_Expression)
			.updateEmptyExpressionPriority(stringPlusOperator.getPriority());
	}

	@Override
	public VisitResult		processInstanceOf(Class<?> className, InstanceOfTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("is_string(");
		sb.append(this.visitor.visitExpression(node.getExpression(), trees).getContent());
		sb.append(")");
		return new VisitResult(sb)
			.updateExpressionPriority(visitor.getPriorityCall());
	}

	@Override
	public VisitResult		processMethodInvocation(VisitResult path, Method method, List<VisitResult> arguments, MethodInvocationTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder();
		if (method.getName().equals("mkdirs")) {
			sb.append("\\org\\druf\\io\\DirUtil::mkpath(").append(path.getContent());
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append(")");
			return new VisitResult(sb.toString());
		}
		else if (method.getName().equals("exists")) {
			sb.append("file_exists(").append(path.getContent());
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append(")");
			return new VisitResult(sb.toString());
		}
		else if (method.getName().equals("isFile")) {
			sb.append("is_file(").append(path.getContent());
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append(")");
			return new VisitResult(sb.toString());
		}
		else if (method.getName().equals("isDirectory")) {
			sb.append("is_dir(").append(path.getContent());
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append(")");
			return new VisitResult(sb.toString());
		}
		else if (method.getName().equals("length")) {
			sb.append("filesize(").append(path.getContent());
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append(")");
			return new VisitResult(sb.toString());
		}
		else if (method.getName().equals("lastModified")) {
			sb.append("filemtime(").append(path.getContent());
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append(")*1000");
			return new VisitResult(sb.toString()).
				updateExpressionPriority(visitor.getOperatorPriority("*"));
		}
		else if (method.getName().equals("getName")) {
			sb.append("basename(").append(path.getContent());
			sb.append(")");
			return new VisitResult(sb.toString());
		}
		else if (method.getName().equals("getParent")) {
			sb.append("dirname(").append(path.getContent());
			sb.append(")");
			return new VisitResult(sb.toString());
		}
		else if (method.getName().equals("getParentFile")) {
			sb.append("dirname(").append(path.getContent());
			sb.append(")");
			return new VisitResult(sb.toString());
		}
		else if (method.getName().equals("delete")) {
			sb.append("unlink(").append(path.getContent());
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append(")");
			return new VisitResult(sb.toString());
		}
		return processCommonMethodInvocationOrError(path, method, arguments, node, trees);
	}
}
