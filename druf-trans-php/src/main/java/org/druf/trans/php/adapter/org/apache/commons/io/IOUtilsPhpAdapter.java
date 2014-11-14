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

import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.List;

import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.Trees;

import org.druf.trans.VisitResult;
import org.druf.trans.php.adapter.BasicPhpAdapter;
import org.druf.trans.php.PhpTransVisitor;


public class IOUtilsPhpAdapter extends BasicPhpAdapter
{
	@Override
	public VisitResult		processMethodInvocation(VisitResult path, Method method, List<VisitResult> arguments, MethodInvocationTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder();
		if (method.getName().equals("copy") && method.getParameterTypes().length == 2) {
			sb.append("stream_copy_to_stream(");
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append(")");
			return new VisitResult(sb.toString());
		}
		else if (method.getName().equals("copyLarge") && method.getParameterTypes().length == 4 && method.getParameterTypes()[2] == long.class && method.getParameterTypes()[3] == long.class) {
			sb.append("stream_copy_to_stream(");
			appendArgument(sb, true, node.getArguments().get(0), trees);
			appendArgument(sb, false, node.getArguments().get(1), trees);
			appendArgument(sb, false, node.getArguments().get(3), trees);
			appendArgument(sb, false, node.getArguments().get(2), trees);
			sb.append(")");
			return new VisitResult(sb.toString());
		}
		else if (method.getName().equals("toByteArray")) {
			sb.append("stream_get_contents(");
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append(")");
			return new VisitResult(sb.toString());
		}
		else if (method.getName().equals("toString")) {
			sb.append("stream_get_contents(");
			appendArgumentsDirect(sb, node.getArguments(), trees);
			sb.append(")");
			return new VisitResult(sb.toString());
		}
		else if (method.getName().equals("write") && (method.getParameterTypes().length == 2 || method.getParameterTypes().length == 3) && (
				method.getParameterTypes()[0] == String.class && method.getParameterTypes()[1] == OutputStream.class
			)) {
			sb.append("stream_put_contents(");
			appendArgument(sb, true, node.getArguments().get(1), trees);
			appendArgument(sb, false, node.getArguments().get(0), trees);
			sb.append(")");
			return new VisitResult(sb.toString());
		}
		return super.processCommonMethodInvocationOrError(path, method, arguments, node, trees);
	}
}
