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

import net.dryuf.trans.BoundType;
import net.dryuf.trans.VisitResult;
import net.dryuf.trans.php.adapter.BasicPhpAdapter;

import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.Trees;


public class ClassPhpAdapter extends BasicPhpAdapter
{
	@Override
	public VisitResult		processMethodInvocation(VisitResult path, Method method, List<VisitResult> arguments, MethodInvocationTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder();
		if (method.getName().equals("getName")) {
			String[] m;
			if (path.getResultIndicator() == VisitResult.RI_DotClass) {
				return new VisitResult(path.getFinal());
			}
			else {
				return new VisitResult(sb.append(path.getContent()).toString());
			}
		}
		else if (method.getName().equals("forName")) {
			return arguments.get(0);
		}
		else if (method.getName().equals("newInstance")) {
			return new VisitResult(sb.append("\\net\\dryuf\\core\\Dryuf::createClassArg0(").append(path.getContent()).append(")"));
		}
		else if (method.getName().equals("getAnnotation")) {
			String[] m;
			return new VisitResult(sb.append("\\net\\dryuf\\core\\Dryuf::getClassAnnotation(").append(path.getContent()).append(", ").append(arguments.get(0).getFinal()).append(")"))
				.updateResultBound(arguments.get(0).getDefaultBound(0, BoundType.createRaw(Object.class)));
		}
		return super.processCommonMethodInvocationOrError(path, method, arguments, node, trees);
	}
}
