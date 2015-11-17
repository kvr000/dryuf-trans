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
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.util.Trees;
import org.apache.logging.log4j.Logger;


public class BasicTransClassAdapter implements TransClassAdapter
{
	public				BasicTransClassAdapter()
	{
	}

	@Override
	public void			setTransVisitor(TransVisitor visitor)
	{
		this.visitor = visitor;
	}

	@Override
	public void			init()
	{
	}

	public Logger			getLogger()
	{
		return visitor.getLogger();
	}

	@Override
	public VisitResult		processAnnotation(Class<?> clazz, AnnotationTree node, Trees tree)
	{
		return null;
	}

	@Override
	public VisitResult		processInstanceOf(Class<?> className, InstanceOfTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(this.visitor.visitExpression(node.getExpression(), trees).getContent());
		sb.append(" instanceof ");
		sb.append(this.visitor.translateClassIdentifier(transformClass(className.getName())));
		return new VisitResult(sb);
	}

	@Override
	public VisitResult		processTypeCast(Class<?> clazz, VisitResult expression, TypeCastTree node, Trees trees)
	{
		return new VisitResult(expression.getContent());
	}

	@Override
	public VisitResult		processNewInstance(Class<?> clazz, Constructor<?> constructor, List<VisitResult> arguments, NewClassTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder("new ");
		sb.append(this.visitor.translateClassIdentifier(transformClass(clazz.getName())));
		sb.append("(");
		appendArgumentsDirect(sb, node.getArguments(), trees);
		sb.append(")");
		if (node.getClassBody() != null) {
			sb.append(this.visitor.surroundFix(node.getClassBody().toString(), null));
		}
		return new VisitResult(sb);
	}

	@Override
	public VisitResult		processNewArray(Class<?> elementClazz, List<VisitResult> dimensions, List<VisitResult> initializers, NewArrayTree node, Trees trees)
	{
		boolean isPrimitive = elementClazz != null && elementClazz.isPrimitive();
		boolean isChar = elementClazz != null && (elementClazz.equals(char.class) || elementClazz.equals(Character.class));
		if (node.getInitializers() != null) {
			StringBuilder sb = new StringBuilder(this.visitor.getContext() == TransVisitor.CodeContext.CCtx_Annotated ? "{" : "array(");
			try {
				int counter = 0;
				boolean newLine = false;
				for (ExpressionTree element: node.getInitializers()) {
					if (!(element instanceof LiteralTree)) {
						newLine = true;
					}
					if (counter++ != 0) {
						FormatUtil.removeSbNls(sb);
						sb.append(",");
					}
					sb.append(newLine ? "\n" : " ");
					VisitResult result = this.visitor.scan(element, trees);
					String r = result.getContent();
					if (newLine)
						r = FormatUtil.indentString(r).toString();
					sb.append(r);
				}
				FormatUtil.removeSbEndSafe(sb, "\n");
				sb.append(newLine ? "\n" : " ");
				sb.append(this.visitor.getContext() == TransVisitor.CodeContext.CCtx_Annotated ? "}" : ")");
			}
			finally {
			}
			return new VisitResult(sb);
		}
		else if (node.getDimensions().size() > 0 && node.getDimensions().get(0) instanceof LiteralTree && ((LiteralTree)node.getDimensions().get(0)).getValue().equals(0)) {
			return new VisitResult("[]");
		}
		else if (node.getDimensions().size() == 1 && node.getDimensions().get(0) instanceof LiteralTree) {
			StringBuilder sb = new StringBuilder("new Object[");
			sb.append(((LiteralTree)node.getDimensions().get(0)).getValue()).append(", ");
			sb.append(isPrimitive ? "0" : "null");
			sb.append("]");
			return new VisitResult(sb);
		}
		else {
			StringBuilder sb = new StringBuilder("new Object[");
			for (ExpressionTree dimension: node.getDimensions()) {
				sb.append(this.visitor.scan(dimension, trees).getContent()).append(", ");
			}
			sb.append(isPrimitive ? "0" : "null");
			sb.append("]");
			return new VisitResult(sb);
		}
	}

	@Override
	public VisitResult		processArrayAccess(VisitResult array, VisitResult index, ArrayAccessTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(array.getContent());
		sb.append("[");
		sb.append(index.getContent());
		sb.append("]");
		return new VisitResult(sb);
	}

	@Override
	public VisitResult		processFieldAccess(VisitResult path, Field field, MemberSelectTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder();
		Class<?> lc = path.getResultClass();
		if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
			sb.append(this.visitor.translateClassIdentifier(transformClass(lc.getName()))).append(".");
			sb.append(node.getIdentifier());
		}
		else {
			sb.append(path.getContent()).append(".").append(node.getIdentifier());
		}
		return new VisitResult(sb);
	}

	@Override
	public VisitResult		processMethodInvocation(VisitResult path, Method method, List<VisitResult> arguments, MethodInvocationTree node, Trees trees)
	{
		return null;
	}

	@Override
	public VisitResult		processAssignment(VisitResult variable, VisitResult value, AssignmentTree node, Trees trees)
	{
		return variable.appendString(" = ").append(value);
	}

	public void			appendArgumentsDirect(StringBuilder sb, List<? extends ExpressionTree> arguments, Trees trees)
	{
		int count = 0;
		boolean forceNl = false;
		for (ExpressionTree argument: arguments) {
			if (count++ != 0) {
				if (FormatUtil.checkRemoveSbEnd(sb, "\n"))
					forceNl = true;
				sb.append(", ");
			}
			String s = this.visitor.visitExpression(argument, trees).getFinal();
			if (s.indexOf("\n") >= 0) {
				forceNl = true;
			}
			if (forceNl) {
				sb.append("\n").append(FormatUtil.indentString(s));
			}
			else {
				sb.append(s);
			}
		}
	}

	public void			appendArgument(StringBuilder sb, boolean isFirst, ExpressionTree argument, Trees trees)
	{
		if (!isFirst) {
			if (sb.length() > 0 && sb.charAt(sb.length()-1) == '\n')
				sb.replace(sb.length()-1, sb.length(), ",\n\t");
			else
				sb.append(", ");
		}
		String content = this.visitor.visitExpression(argument, trees).getFinal();
		if (content.indexOf("\n") >= 0) {
			sb.append("\n").append(FormatUtil.indentString(content));
		}
		else {
			sb.append(content);
		}
	}

	public void			appendArguments(StringBuilder sb, Constructor<?> constructor, List<? extends ExpressionTree> arguments, Trees trees)
	{
		int count = 0;
		boolean startedVar = false;
		boolean forceNl = false;
		Class<?>[] parameters = constructor != null ? constructor.getParameterTypes() : new Class<?>[0];
		for (ExpressionTree argument: arguments) {
			if (count != 0) {
				if (FormatUtil.checkRemoveSbEnd(sb, "\n"))
					forceNl = true;
				sb.append(", ");
			}
			else if (sb.charAt(sb.length()-1) == '\n') {
				forceNl = true;
			}
			VisitResult result = this.visitor.visitExpression(argument, trees);
			String content = result.getFinal();
			if (count >= parameters.length) {
				// no special action
			}
			else if (count == parameters.length-1 && parameters[count].isArray() && !parameters[count].isAssignableFrom(result.getResultClass()) && parameters[count].getComponentType().isAssignableFrom(result.getResultClass())) {
				startedVar = true;
				sb.append("[");
			}
			if (content.indexOf("\n") >= 0) {
				forceNl = true;
				FormatUtil.removeSbEndSafe(sb, "\n");
			}
			if (forceNl) {
				sb.append("\n").append(FormatUtil.indentString(content));
			}
			else {
				sb.append(content);
			}
			count++;
		}
		if (startedVar)
			sb.append("]");
		if (forceNl)
			sb.append("\n");
	}

	public void			appendArguments(StringBuilder sb, Method method, List<? extends ExpressionTree> arguments, Trees trees)
	{
		int count = 0;
		boolean startedVar = false;
		boolean forceNl = false;
		Class<?>[] parameters = method != null ? method.getParameterTypes() : new Class<?>[0];
		for (ExpressionTree argument: arguments) {
			if (count != 0) {
				if (FormatUtil.checkRemoveSbEnd(sb, "\n"))
					forceNl = true;
				sb.append(", ");
			}
			else if (sb.charAt(sb.length()-1) == '\n') {
				forceNl = true;
			}
			VisitResult result = this.visitor.visitExpression(argument, trees);
			String content = result.getFinal();
			if (count >= parameters.length) {
				// no special action
			}
			else if (count == parameters.length-1 && parameters[count].isArray() && !parameters[count].isAssignableFrom(result.getResultClass()) && parameters[count].getComponentType().isAssignableFrom(result.getResultClass())) {
				startedVar = true;
				sb.append("[");
			}
			if (content.indexOf("\n") >= 0) {
				forceNl = true;
				FormatUtil.removeSbEndSafe(sb, "\n");
			}
			if (forceNl) {
				sb.append("\n").append(FormatUtil.indentString(content));
			}
			else {
				sb.append(content);
			}
			count++;
		}
		if (startedVar)
			sb.append("]");
		if (forceNl)
			sb.append("\n");
	}

	public StringBuilder		appendArgument(StringBuilder sb, boolean isFirst, VisitResult argument)
	{
		return this.visitor.appendArgument(sb, isFirst, argument);
	}

	public StringBuilder		appendArguments(StringBuilder sb, Method method, List<VisitResult> arguments)
	{
		return this.visitor.appendArguments(sb, method, arguments);
	}

	@Override
	public VisitResult		processSuperConstructorInvocation(VisitResult path, Constructor<?> constructor, List<VisitResult> arguments, MethodInvocationTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("super(");
		appendArguments(sb, constructor, node.getArguments(), trees);
		sb.append(")");
		return new VisitResult(sb);
	}

	public VisitResult		checkCommonMethodInvocation(VisitResult path, Method method, List<VisitResult> arguments, MethodInvocationTree node, Trees trees)
	{
		return null;
	}

	@Override
	public String			transformClass(String className)
	{
		return visitor.resolveConfiguredClassTransformation(className);
	}

	protected TransVisitor		visitor;
}
