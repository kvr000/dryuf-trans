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
package net.dryuf.trans.php.adapter.net.dryuf.core;

import java.lang.reflect.Method;
import java.util.List;

import net.dryuf.trans.BoundType;
import net.dryuf.trans.VisitResult;
import net.dryuf.trans.php.adapter.BasicPhpAdapter;

import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.Trees;


public class DryufPhpAdapter extends BasicPhpAdapter
{
	@Override
	public VisitResult		processMethodInvocation(VisitResult path, Method method, List<VisitResult> arguments, MethodInvocationTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder();
		if (method.getName().equals("getMandatoryAnnotation")) {
			BoundType resultBound = BoundType.createRaw(Object.class);
			if (arguments.get(1).getOptionalBound(0) != null) {
				resultBound = arguments.get(1).getMandatoryBound(0);
			}
			if (arguments.get(1).getResultClass() == Class.class) {
				return new VisitResult(sb.append("\\net\\dryuf\\core\\Dryuf::getClassMandatoryAnnotation(").append(arguments.get(0).getContent()).append(", ").append(arguments.get(1).getFinal()).append(")"))
					.updateResultBound(resultBound);
			}
			else {
				return new VisitResult(this.visitor.surroundFix(node.toString(), null))
					.updateResultBound(resultBound);
			}
		}
		return super.processMethodInvocation(path, method, arguments, node, trees);
	}
}
