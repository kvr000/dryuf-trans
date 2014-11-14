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
package org.druf.trans.php.adapter.java.util.concurrent;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.util.Trees;

import org.druf.trans.VisitResult;
import org.druf.trans.php.adapter.BaseFunctionalPhpAdapter;
import org.druf.trans.php.PhpTransVisitor;


public class CallablePhpAdapter extends BaseFunctionalPhpAdapter
{
	@Override
	public VisitResult		processNewInstance(Class<?> clazz, Constructor<?> constructor, List<VisitResult> arguments, NewClassTree node, Trees trees)
	{
		ClassTree body;
		MethodTree methodTree;
		if ((body = node.getClassBody()) != null &&
				body.getMembers().size() == 1 &&
				body.getMembers().get(0) instanceof MethodTree &&
				(methodTree = (MethodTree)body.getMembers().get(0)).getName().toString().equals("call") &&
				methodTree.getParameters().size() == 0) {
			return this.processNewInstanceFunctional(clazz, constructor, arguments, node, trees);
		}
		return super.processNewInstance(clazz, constructor, arguments, node, trees);
	}

	@Override
	public VisitResult		processMethodInvocation(VisitResult path, Method method, List<VisitResult> arguments, MethodInvocationTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder();
		if (method.getName().equals("call")) {
			return new VisitResult(sb.append("call_user_func(").append(path.getContent()).append(")"));
		}
		return super.processCommonMethodInvocationOrError(path, method, arguments, node, trees);
	}
}
