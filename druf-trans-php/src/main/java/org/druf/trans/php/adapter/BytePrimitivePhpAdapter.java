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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.util.Trees;

import org.druf.trans.VisitResult;
import org.druf.trans.php.PhpTransVisitor;
import org.druf.trans.php.adapter.PrimitiveTypePhpAdapter;


public class BytePrimitivePhpAdapter extends PrimitiveTypePhpAdapter
{
	@Override
	public VisitResult		processTypeCast(Class<?> clazz, VisitResult expression, TypeCastTree node, Trees trees)
	{
		if (node.getExpression() instanceof LiteralTree) {
			return new VisitResult(String.valueOf(((((Number)((LiteralTree)node.getExpression()).getValue()).longValue()+0x80)&0xff)-0x80));
		}
		return new VisitResult("(((("+expression.getContent()+")+0x80)&0xff)-0x80)");
	}

	@Override
	public VisitResult		processAssignment(VisitResult variable, VisitResult value, AssignmentTree node, Trees trees)
	{
		if (node.getVariable() instanceof ArrayAccessTree) {
			ArrayAccessTree aaNode = (ArrayAccessTree)node.getVariable();
			VisitResult arrayResult = this.visitor.scan(aaNode.getExpression(), trees);
			VisitResult indexResult = this.visitor.scan(aaNode.getIndex(), trees);
			StringBuilder sb = new StringBuilder();
			sb.append(arrayResult.getContent());
			sb.append("[").append(indexResult.getContent()).append("]");
			sb.append(" = chr(");
			sb.append(value.getContent());
			sb.append(")");
			return new VisitResult(sb);
		}
		else {
			return super.processAssignment(variable, value, node, trees);
		}
	}
}
