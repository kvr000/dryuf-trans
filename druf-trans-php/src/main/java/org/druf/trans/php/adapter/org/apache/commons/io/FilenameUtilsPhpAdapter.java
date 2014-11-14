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
package org.druf.trans.php.adapter.org.apache.commons.io;

import java.lang.reflect.Method;
import java.util.List;

import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.Trees;

import org.druf.trans.VisitResult;
import org.druf.trans.php.adapter.BasicPhpAdapter;
import org.druf.trans.php.PhpTransVisitor;


public class FilenameUtilsPhpAdapter extends BasicPhpAdapter
{
	@Override
	public VisitResult		processMethodInvocation(VisitResult path, Method method, List<VisitResult> arguments, MethodInvocationTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder();
		if (method.getName().equals("getExtension")) {
			sb.append("pathinfo(");
			appendArgument(sb, true, node.getArguments().get(0), trees);
			sb.append(", PATHINFO_EXTENSION");
			sb.append(")");
			return new VisitResult(sb.toString());
		}
		else if (method.getName().equals("removeExtension")) {
			sb.append("preg_replace(',\\.[^/]*,', ''");
			appendArgument(sb, false, node.getArguments().get(0), trees);
			sb.append(")");
			return new VisitResult(sb.toString());
		}
		return super.processCommonMethodInvocationOrError(path, method, arguments, node, trees);
	}
}
