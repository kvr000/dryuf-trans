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
package net.dryuf.trans.php.adapter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.util.Trees;

import net.dryuf.trans.BasicTransClassAdapter;
import net.dryuf.trans.FormatUtil;
import net.dryuf.trans.TransVisitor;
import net.dryuf.trans.VisitResult;


public class BasicPhpAdapter extends BasicTransClassAdapter
{
	@Override
	public VisitResult		processAnnotation(Class<?> clazz, AnnotationTree node, Trees trees)
	{
		String targetType = clazz.getName();
		if ((targetType = transformClass(targetType)) == null) {
			return null;
		}
		try {
			StringBuilder sb = new StringBuilder("@").append(this.visitor.translateClassIdentifier(targetType));
			List<? extends ExpressionTree> args = node.getArguments();
			if (args != null && args.size() > 0) {
				sb.append("(");
				for (ExpressionTree expr: args) {
					if (!(expr instanceof AssignmentTree))
						throw new RuntimeException("not assign format for this annotation");
					AssignmentTree assign = (AssignmentTree) expr;
					sb.append(assign.getVariable().toString());
					sb.append(" = ").append(this.visitor.visitExpression(assign.getExpression(), trees).getFinal()).append(", ");
				}
				FormatUtil.removeSbEndSafe(sb, ", ");
				sb.append(")");
			}
			return new VisitResult(sb.toString());
		}
		catch (Exception ex) {
			getLogger().error(ex.toString(), ex);
			return new VisitResult(this.visitor.surroundFix(node.toString(), ex));
		}
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
					if (isPrimitive && !isChar && (result.getResultClass() == char.class || result.getResultClass() == Character.class)) {
						r = "ord("+r+")";
					}
					else if (isPrimitive && isChar && (result.getResultClass() != char.class && result.getResultClass() != Character.class)) {
						r = "chr("+r+")";
					}
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
			return new VisitResult("array()");
		}
		else if (node.getDimensions().size() == 1 && node.getDimensions().get(0) instanceof LiteralTree) {
			StringBuilder sb = new StringBuilder("array_fill(0, ");
			sb.append(((LiteralTree)node.getDimensions().get(0)).getValue()).append(", ");
			sb.append(isPrimitive ? "0" : "null");
			sb.append(")");
			return new VisitResult(sb);
		}
		else {
			StringBuilder sb = new StringBuilder("\\net\\dryuf\\core\\Dryuf::allocArray(");
			sb.append(isPrimitive ? "0" : "null");
			for (ExpressionTree dimension: node.getDimensions()) {
				sb.append(", ").append(this.visitor.scan(dimension, trees).getContent());
			}
			sb.append(")");
			return new VisitResult(sb);
		}
	}

	@Override
	public VisitResult		processFieldAccess(VisitResult path, Field field, MemberSelectTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder();
		if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
			sb.append(this.visitor.translateClassIdentifier(transformClass(field.getDeclaringClass().getName()))).append("::");
			if (
					!(java.lang.reflect.Modifier.isFinal(field.getModifiers()) && field.getType().isPrimitive()) &&
					!(field.getDeclaringClass().isEnum())
					)
				sb.append("$");
			sb.append(field.getName());
		}
		else {
			Class<?> lc = path.getResultClass();
			sb.append(path.getContent()).append("->").append(node.getIdentifier());
		}
		return new VisitResult(sb);
	}

	@Override
	public VisitResult		processSuperConstructorInvocation(VisitResult path, Constructor<?> constructor, List<VisitResult> arguments, MethodInvocationTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("parent::__construct(");
		appendArguments(sb, constructor, node.getArguments(), trees);
		sb.append(")");
		return new VisitResult(sb);
	}

	@Override
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
			/*
			else if (count == parameters.length-1 && parameters[count].isArray() && parameters[count].getComponentType().isAssignableFrom(this.visitor.getCurrentResult())) {
				startedVar = true;
				sb.append("array(");
			}
			*/
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
			sb.append(")");
	}

	@Override
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
			/*
			else if (count == parameters.length-1 && parameters[count].isArray() && parameters[count].getComponentType().isAssignableFrom(this.visitor.getCurrentResult())) {
				startedVar = true;
				sb.append("array(");
			}
			*/
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
			sb.append(")");
	}

	@Override
	public VisitResult		checkCommonMethodInvocation(VisitResult path, Method method, List<VisitResult> arguments, MethodInvocationTree node, Trees trees)
	{
		return this.visitor.checkCommonMethodInvocation(path, method, arguments, node, trees);
	}

	public VisitResult		processCommonMethodInvocationOrError(VisitResult path, Method method, List<VisitResult> arguments, MethodInvocationTree node, Trees trees)
	{
		VisitResult out;
		if ((out = checkCommonMethodInvocation(path, method, arguments, node, trees)) != null)
			return out;
		return new VisitResult(this.visitor.surroundFix(node.toString(), null));
	}
}
