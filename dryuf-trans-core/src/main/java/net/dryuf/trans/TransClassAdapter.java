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
package net.dryuf.trans;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.util.Trees;


public interface TransClassAdapter
{
	public void			setTransVisitor(TransVisitor transVisitor);

	public void			init();

	public String			transformClass(String className);

	public VisitResult		processInstanceOf(Class<?> className, InstanceOfTree node, Trees trees);

	public VisitResult		processTypeCast(Class<?> clazz, VisitResult expression, TypeCastTree node, Trees trees);

	public VisitResult		processNewInstance(Class<?> clazz, Constructor<?> constructor, List<VisitResult> arguments, NewClassTree node, Trees trees);

	public VisitResult		processNewArray(Class<?> clazz, List<VisitResult> dimensions, List<VisitResult> elements, NewArrayTree node, Trees trees);

	public VisitResult		processArrayAccess(VisitResult array, VisitResult index, ArrayAccessTree node, Trees trees);

	public VisitResult		processFieldAccess(VisitResult left, Field field, MemberSelectTree node, Trees trees);

	public VisitResult		processSuperConstructorInvocation(VisitResult path, Constructor<?> constructor, List<VisitResult> arguments, MethodInvocationTree node, Trees trees);

	public VisitResult		processMethodInvocation(VisitResult path, Method method, List<VisitResult> arguments, MethodInvocationTree node, Trees trees);

	public VisitResult		processAnnotation(Class<?> clazz, AnnotationTree node, Trees trees);

	public VisitResult		processAssignment(VisitResult variable, VisitResult value, AssignmentTree node, Trees trees);
}
