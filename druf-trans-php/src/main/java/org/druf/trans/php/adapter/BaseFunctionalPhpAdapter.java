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
package org.druf.trans.php.adapter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import java.util.Map;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.Trees;

import org.druf.trans.CatchingIdentifierStore;
import org.druf.trans.FormatUtil;
import org.druf.trans.IdentifierDef;
import org.druf.trans.VisitResult;


public class BaseFunctionalPhpAdapter extends BasicPhpAdapter
{
	public VisitResult		processNewInstanceFunctional(Class<?> clazz, Constructor<?> constructor, List<VisitResult> arguments, NewClassTree node, Trees trees)
	{
		CatchingIdentifierStore catched = new CatchingIdentifierStore(this.visitor.getIdentifiers());
		this.visitor.pushIdentifiersSpecial(catched);
		this.visitor.pushIdentifiers();
		try {
			ClassTree body;
			MethodTree methodTree;
			if ((body = node.getClassBody()) != null &&
					body.getMembers().size() == 1 &&
					body.getMembers().get(0) instanceof MethodTree) {
				methodTree = (MethodTree)body.getMembers().get(0);
				StringBuilder sb = new StringBuilder("function (");
				{
					int count = 0;
					for (VariableTree par: methodTree.getParameters()) {
						if (count++ != 0)
							sb.append(", ");
						this.visitor.addIdentifier(IdentifierDef.createVariable(par.getName().toString(), this.visitor.resolveBoundType(par.getType())));
						sb.append("$").append(methodTree.getParameters().get(0).getName());
					}
				}
				sb.append(") ");
				String bodyStr = this.visitor.scan(methodTree.getBody(), trees).getContent();
				StringBuilder uses = new StringBuilder();
				for (Map.Entry<String, IdentifierDef> idef: catched.getCatched().entrySet()) {
					switch (idef.getValue().getType()) {
					case IT_Variable:
						uses.append("$").append(idef.getKey()).append(", ");
						break;
					default:
						break;
					}
				}
				if (uses.length() > 0) {
					uses.replace(uses.length()-2, uses.length(), "");
					sb.append("use (").append(uses).append(") ");
				}
				sb.append(bodyStr);
				return new VisitResult(sb);
			}
			return super.processNewInstance(clazz, constructor, arguments, node, trees);
		}
		finally {
			this.visitor.popIdentifiers();
			this.visitor.popIdentifiers();
		}
	}

	@Override
	public VisitResult		processInstanceOf(Class<?> className, InstanceOfTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("is_callable(");
		sb.append(this.visitor.visitExpression(node.getExpression(), trees).getContent());
		sb.append(")");
		return new VisitResult(sb);
	}

	@Override
	public VisitResult		processMethodInvocation(VisitResult path, Method method, List<VisitResult> arguments, MethodInvocationTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder();
		VisitResult out;
		if (method.getName().equals("apply")) {
			sb.append("call_user_func(").append(path.getContent()).append(", ");
			appendArgumentsDirect(sb, node.getArguments(), trees);
			FormatUtil.removeSbEndSafe(sb, ", ");
			sb.append(")");
		}
		else if ((out = checkCommonMethodInvocation(path, method, arguments, node, trees)) != null) {
			return out;
		}
		else {
			return new VisitResult(this.visitor.surroundFix(super.processMethodInvocation(path, method, arguments, node, trees).getContent(), null));
		}
		return new VisitResult(sb);
	}
}
