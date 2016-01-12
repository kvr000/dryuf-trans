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
package net.dryuf.trans.php.adapter.org.apache.commons.lang3;

import java.lang.reflect.Method;
import java.util.List;

import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.Trees;

import net.dryuf.trans.VisitResult;
import net.dryuf.trans.php.adapter.BasicPhpAdapter;
import net.dryuf.trans.php.PhpTransVisitor;


public class StringUtilsPhpAdapter extends BasicPhpAdapter
{
	@Override
	public VisitResult		processMethodInvocation(VisitResult path, Method method, List<VisitResult> arguments, MethodInvocationTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder();
		if (method.getName().equals("isEmpty")) {
			sb.append("((");
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append(") == ");
			sb.append("null)");
			return new VisitResult(sb.toString());
		}
		else if (method.getName().equals("defaultString")) {
			sb.append("\\net\\dryuf\\core\\Dryuf::defvalue(");
			appendArgumentsDirect(sb, node.getArguments(), trees);
			if (arguments.size() == 1)
				sb.append(", ''");
			sb.append(")");
			return new VisitResult(sb.toString());
		}
		else if (method.getName().equals("defaultIfEmpty")) {
			sb.append("\\net\\dryuf\\core\\StringUtil::defaultIfEmpty(");
			appendArgumentsDirect(sb, node.getArguments(), trees);
			if (arguments.size() == 1)
				sb.append(", ''");
			sb.append(")");
			return new VisitResult(sb.toString());
		}
		return super.processCommonMethodInvocationOrError(path, method, arguments, node, trees);
	}
}
