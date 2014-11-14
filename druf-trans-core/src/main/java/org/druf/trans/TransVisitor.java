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
package org.druf.trans;

import java.io.File;
import java.io.IOException;
import java.lang.Integer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.validation.constraints.NotNull;

import com.google.common.collect.Lists;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.LambdaExpressionTree;
import org.druf.trans.proxy.NewClassTreeProxy;
import org.druf.trans.proxy.ParameterizedTypeAndExpressionTreeProxy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.codec.Charsets;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.AssertTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.ErroneousTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WildcardTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public abstract class TransVisitor extends TreePathScanner<VisitResult, Trees>
{
	public enum CodeContext
	{
		CCtx_Package,
		CCtx_Annotated,
		CCtx_Class,
		CCtx_MethodBody,
		CCtx_Array,
		CCtx_Value,
	}

	public class ClassContext
	{
		public ClassTree		classTree;

		public Class<?>			clazz;

		public Class<?>			superClass;

		public String			packageName;

		public String			className;

		public String			fullName;

		public String			pathName;

		public StringBuilder		staticInit;

		public StringBuilder		instanceInit;

		public int			enumCounter;
	}

	public				TransVisitor(ProcessingEnvironment pe)
	{
		processingEnv = pe;

		arrayIds = new HashMap<String, String>();
		arrayIds.put("boolean", "Z");
		arrayIds.put("byte", "B");
		arrayIds.put("short", "S");
		arrayIds.put("int", "I");
		arrayIds.put("long", "J");
		arrayIds.put("float", "F");
		arrayIds.put("double", "D");
		arrayIds.put("char", "C");
	}

	public void			init()
	{
		if (logger == null)
			logger = LogManager.getLogger(this.getClass());
	}

	public void			setLogger(Logger logger)
	{
		this.logger = logger;
	}

	public Logger			getLogger()
	{
		return logger;
	}

	public String			surroundFix(String s, Exception ex)
	{
		return surroundFixSb(new StringBuilder(s), ex).toString();
	}

	public StringBuilder		surroundFixSb(StringBuilder s, Exception ex)
	{
		s.insert(0, FIX_STRING);
		if (ex != null) {
			s.insert(FIX_STRING.length()-1, " msg=\""+(ex.getMessage() == null ? "NULL" : ex.getMessage().replace("\"", "'"))+"\"");
		}
		s.append(XIF_STRING);
		return s;
	}

	public String			surroundParentheses(String expression)
	{
		return surroundParentheses(new StringBuilder(expression)).toString();
	}

	public StringBuilder		surroundParentheses(StringBuilder expression)
	{
		return expression.insert(0, "(").append(")");
	}

	public VisitResult		surroundParentheses(VisitResult expression)
	{
		if (expression.getResultIndicator() == VisitResult.RI_Null)
			return expression;
		return expression.prependString("(").appendString(")")
			.updateExpressionPriority(getPriorityParentheses())
			.updateResultIndicator(VisitResult.RI_Parenthesized);
	}

	public VisitResult		surroundLeftPrioritized(VisitResult expression, TargetOperator operatorDef)
	{
		if (expression.getExpressionPriority() > operatorDef.getPriority() || (operatorDef.getAssociativity() == 1 && expression.getExpressionPriority() == operatorDef.getPriority())) {
			surroundParentheses(expression);
		}
		return expression;
	}

	public VisitResult		surroundRightPrioritized(VisitResult expression, TargetOperator operatorDef)
	{
		if (expression.getExpressionPriority() > operatorDef.getPriority() || (operatorDef.getAssociativity() == -1 && expression.getExpressionPriority() == operatorDef.getPriority())) {
			surroundParentheses(expression);
		}
		return expression;
	}

	public TargetOperator		getTargetOperator(String operatorIdentifier)
	{
		TargetOperator operatorDef = targetOperators.get(operatorIdentifier);
		if (operatorDef == null)
			throw new IllegalArgumentException("Operator is unknown: "+operatorIdentifier);
		return operatorDef;
	}

	public int			getOperatorPriority(String operatorIdentifier)
	{
		return getTargetOperator(operatorIdentifier).getPriority();
	}

	public int			getPriorityLiteral()
	{
		return targetOperators.get("literal").getPriority();
	}

	public int			getPriorityVariable()
	{
		return targetOperators.get("literal").getPriority();
	}

	public int			getPriorityMember()
	{
		return targetOperators.get("member").getPriority();
	}

	public int			getPriorityStatic()
	{
		return targetOperators.get("static").getPriority();
	}

	public int			getPriorityParentheses()
	{
		return targetOperators.get("()").getPriority();
	}

	public int			getPriorityNew()
	{
		return targetOperators.get("new").getPriority();
	}

	public int			getPriorityCall()
	{
		return targetOperators.get("call").getPriority();
	}

	public int			getPriorityArrayAccess()
	{
		return targetOperators.get("[]").getPriority();
	}

	public int			getPriorityCast()
	{
		return targetOperators.get("cast").getPriority();
	}

	public StringBuilder		appendArgument(StringBuilder sb, boolean isFirst, VisitResult argument)
	{
		if (!isFirst) {
			if (sb.length() > 0 && sb.charAt(sb.length()-1) == '\n')
				sb.replace(sb.length()-1, sb.length(), ",\n\t");
			else
				sb.append(", ");
		}
		String content = argument.getFinal();
		if (content.indexOf("\n") >= 0) {
			sb.append("\n").append(FormatUtil.indentString(content));
		}
		else {
			sb.append(content);
		}
		return sb;
	}

	public StringBuilder		appendArguments(StringBuilder sb, Method method, List<VisitResult> arguments)
	{
		int count = 0;
		boolean startedVar = false;
		boolean forceNl = false;
		Class<?>[] parameters = method != null ? method.getParameterTypes() : new Class<?>[0];
		for (VisitResult argument: arguments) {
			if (count != 0) {
				if (FormatUtil.checkRemoveSbEnd(sb, "\n"))
					forceNl = true;
				sb.append(", ");
			}
			else if (sb.charAt(sb.length()-1) == '\n') {
				forceNl = true;
			}
			String content = argument.getFinal();
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
		return sb;
	}

	public Map<String, Class<?>>	getPrimitives()
	{
		if (primitives == null) {
			primitives = new HashMap<String, Class<?>>();
			for (Class<?> clazz: new Class<?>[]{ void.class, boolean.class, char.class, byte.class, short.class, int.class, long.class, float.class, double.class }) {
				primitives.put(clazz.getName(), clazz);
			}
		}
		return primitives;
	}

	public boolean			isPrimitive(String typename)
	{
		return getPrimitives().containsKey(typename);
	}

	public boolean			isClassCharacterish(Class<?> clazz)
	{
		return clazz == char.class ||
			clazz == Character.class;
	}

	public boolean			isClassIntegerish(Class<?> clazz)
	{
		return clazz == byte.class || clazz == short.class || clazz == int.class || clazz == long.class ||
			clazz == Byte.class || clazz == Short.class || clazz == Integer.class || clazz == Long.class;
	}

	public boolean			isClassFloatish(Class<?> clazz)
	{
		return clazz == float.class || clazz == double.class ||
			clazz == Float.class || clazz == Double.class;
	}

	public boolean			isClassNumberish(Class<?> clazz)
	{
		return clazz == byte.class || clazz == short.class || clazz == int.class || clazz == long.class || clazz == float.class || clazz == double.class ||
			clazz == Byte.class || clazz == Short.class || clazz == Integer.class || clazz == Long.class || clazz == float.class || clazz == double.class;
	}

	public Map<String, Class<?>>	getScalars()
	{
		if (scalars == null) {
			scalars = new HashMap<String, Class<?>>();
			for (Class<?> clazz: new Class<?>[]{ String.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class }) {
				scalars.put(clazz.getName(), clazz);
			}
		}
		return scalars;
	}

	public Class<?>			convertPotentialWrapperToPrimitive(Class<?> clazz)
	{
		if (clazz == Boolean.class)
			return boolean.class;

		if (clazz == Byte.class)
			return byte.class;

		if (clazz == Short.class)
			return short.class;

		if (clazz == Integer.class)
			return int.class;

		if (clazz == Long.class)
			return long.class;

		if (clazz == Float.class)
			return float.class;

		if (clazz == Double.class)
			return double.class;

		if (clazz == Character.class)
			return char.class;

		return clazz;
	}

	public Class<?>			convertPotentialPrimitiveToWrapper(Class<?> clazz)
	{
		if (clazz == boolean.class)
			return Boolean.class;

		if (clazz == byte.class)
			return Byte.class;

		if (clazz == short.class)
			return Short.class;

		if (clazz == int.class)
			return Integer.class;

		if (clazz == long.class)
			return Long.class;

		if (clazz == float.class)
			return Float.class;

		if (clazz == double.class)
			return Double.class;

		if (clazz == char.class)
			return Character.class;

		return clazz;
	}

	public boolean			checkAssignable(Class<?> target, Class<?> source)
	{
		if (target.isAssignableFrom(source))
			return true;
		if (!target.isPrimitive() && source == void.class)
			return true;
		if (convertPotentialWrapperToPrimitive(target).isAssignableFrom(convertPotentialWrapperToPrimitive(source)) ||
			convertPotentialPrimitiveToWrapper(target).isAssignableFrom(convertPotentialPrimitiveToWrapper(source)))
			return true;
		if (target == byte.class) {
			// nothing special here
		}
		else if (target == short.class) {
			if (source == byte.class || source == Byte.class)
				return true;
		}
		else if (target == int.class) {
			if (source == byte.class || source == Byte.class || source == short.class || source == Short.class || source == char.class || source == Character.class)
				return true;
		}
		else if (target == long.class) {
			if (source == byte.class || source == Byte.class || source == short.class || source == Short.class || source == int.class || source == Integer.class)
				return true;
		}
		else if (target == float.class) {
			if (source == byte.class || source == Byte.class || source == short.class || source == Short.class || source == int.class || source == Integer.class || source == long.class || source == Long.class)
				return true;
		}
		else if (target == double.class) {
			if (source == byte.class || source == Byte.class || source == short.class || source == Short.class || source == int.class || source == Integer.class || source == long.class || source == Long.class || source == float.class || source == Float.class)
				return true;
		}
		else if (target == char.class) {
			if (source == int.class || source == Integer.class)
				return true;
		}
		return false;
	}

	public boolean			checkTypesDirectMatch(Class<?> target, Class<?> source)
	{
		if (target == source)
			return true;
		if (!target.isPrimitive() && source == void.class)
			return true;
		if (target == int.class && convertPotentialWrapperToPrimitive(source) == char.class)
			return true;
		if (target == char.class && convertPotentialWrapperToPrimitive(source) == int.class)
			return true;
		return false;
	}

	public Class<?>			loadClassInternal(String typename) throws ClassNotFoundException
	{
		String dimensions = "";
		String suffix = "";
		String typeonly = typename;
		if (typeonly.endsWith("[]")) {
			while (typeonly.endsWith("[]")) {
				dimensions += "[";
				typeonly = typeonly.substring(0, typeonly.length()-2);
			}
			String arrayId;
			if ((arrayId = arrayIds.get(typeonly)) != null) {
				typeonly = arrayId;
				try {
					return Class.forName(dimensions+arrayId);
				}
				catch (ClassNotFoundException ex) {
					throw new RuntimeException(ex);
				}
			}
			else {
				dimensions += "L";
				suffix += ";";
			}
		}
		if (getPrimitives().containsKey(typeonly)) {
			return getPrimitives().get(typeonly);
		}
		try {
			return Class.forName(dimensions+typeonly+suffix);
		}
		catch (ClassNotFoundException e) {
			try {
				return Class.forName(dimensions+this.currentPackage+"."+typeonly+suffix);
			}
			catch (ClassNotFoundException e1) {
				int p;
				if ((p = typeonly.lastIndexOf('.')) >= 0) {
					try {
						return Class.forName(dimensions+loadClassInternal(typeonly.substring(0, p)).getName()+'$'+typeonly.substring(p+1)+suffix);
					}
					catch (ClassNotFoundException e3) {
						throw e;
					}
				}
				else {
					try {
						return Class.forName(dimensions+"java.lang"+"."+typeonly+suffix);
					}
					catch (ClassNotFoundException e2) {
						throw e;
					}
				}
			}
		}
	}

	public Class<?>			loadClass(String typename)
	{
		try {
			return loadClassInternal(typename);
		}
		catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public Field			lookupFieldInternal(Class<?> clazz, String fieldName) throws NoSuchFieldException
	{
		try {
			return clazz.getDeclaredField(fieldName);
		}
		catch (SecurityException e) {
			throw new RuntimeException(e);
		}
		catch (NoSuchFieldException e) {
			for (Class<?> superInterface: clazz.getInterfaces()) {
				try {
					return lookupFieldInternal(superInterface, fieldName);
				}
				catch (NoSuchFieldException ei) {
				}
			}
			Class<?> superClass = clazz.getSuperclass();
			if (superClass != null)
				return lookupFieldInternal(superClass, fieldName);
			throw e;
		}
	}

	public Field			lookupField(Class<?> clazz, String fieldName)
	{
		try {
			return lookupFieldInternal(clazz, fieldName);
		}
		catch (SecurityException e) {
			throw new RuntimeException(e);
		}
		catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Validates method according to provided arguments.
	 *
	 * Returns the ratio how much the arguments suit the method signature, the higher ratio, the better match.
	 *
	 * @param isVarArgs
	 * 	indicates varArgs method
	 * @param parameterTypes
	 * 	list of arguments types
	 * @param arguments
	 * 	list of arguments
	 *
	 * @return -1
	 * 	if the method does not match at all
	 * @return
	 * 	the matching ratio otherwise
	 */
	protected int			validateMethodArguments(boolean isVarArgs, Class<?>[] parameterTypes, List<VisitResult> arguments)
	{
		if (isVarArgs) {
			if (arguments.size() < parameterTypes.length-1)
				return -2;
			if (arguments.size() == parameterTypes.length && arguments.get(parameterTypes.length-1).getResultClass() == parameterTypes[parameterTypes.length-1]) {
				// ok
			}
			else {
				for (int i = parameterTypes.length-1; i < arguments.size(); ++i) {
					if (!checkAssignable(parameterTypes[parameterTypes.length-1].getComponentType(), arguments.get(i).getResultClass()))
						return -2;
				}
			}
		}
		else {
			if (arguments.size() != parameterTypes.length)
				return -1;
		}
		int exactMatch = -1;
		for (int i = 0; i < parameterTypes.length-(isVarArgs ? 1 : 0); ++i) {
			if (exactMatch < 0 && !checkTypesDirectMatch(parameterTypes[i], arguments.get(i).getResultClass())) {
				exactMatch = i;
			}
			if (!checkAssignable(parameterTypes[i], arguments.get(i).getResultClass()))
				return -1;
		}
		return exactMatch < 0 ? arguments.size() : exactMatch;
	}

	public Method			lookupMethod(Class<?> clazz, String methodName, List<VisitResult> arguments)
	{
		int bestRatio = -2;
		Method bestMatch = null;
		int accessLevel = clazz == this.currentClass ? Modifier.PUBLIC|Modifier.PROTECTED|Modifier.PRIVATE : clazz.isAssignableFrom(this.currentClass) ? Modifier.PUBLIC|Modifier.PROTECTED : Modifier.PUBLIC;
		try {
			for (Class<?> current = clazz; current != null; current = current.getSuperclass()) {
				for (Method method: current.getDeclaredMethods()) {
					if (method.getName().equals(methodName) && (method.getModifiers()&accessLevel) != 0) {
						int currentRatio;
						if ((currentRatio = validateMethodArguments(method.isVarArgs(), method.getParameterTypes(), arguments)) > bestRatio) {
							bestRatio = currentRatio;
							bestMatch = method;
						}
					}
				}
				for (Class<?> iface: current.getInterfaces()) {
					for (Method method: iface.getMethods()) {
						if (method.getName().equals(methodName) && (method.getModifiers()&accessLevel) != 0) {
							int currentRatio;
							if ((currentRatio = validateMethodArguments(method.isVarArgs(), method.getParameterTypes(), arguments)) > bestRatio) {
								bestRatio = currentRatio;
								bestMatch = method;
							}
						}
					}
				}
			}
			if (bestRatio < -1)
				throw new NoSuchMethodException("no method "+clazz.getName()+"."+methodName+"("+StringUtils.join(arguments.stream().map((VisitResult vr) -> { return vr.getResultClass().getName(); } ).iterator(), ", ")+")");
			if (bestRatio < 0) {
				getLogger().warn("Did not find sufficient match for "+clazz.getName()+"."+methodName+"("+StringUtils.join(arguments.stream().map((VisitResult vr) -> { return vr.getResultClass().getName(); } ).iterator(), ", ")+")");
			}
			return bestMatch;
		}
		catch (SecurityException e) {
			throw new RuntimeException(e);
		}
		catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	public Constructor<?>		lookupConstructor(Class<?> clazz, List<VisitResult> arguments)
	{
		int bestRatio = -2;
		Constructor<?> bestMatch = null;
		int accessLevel = clazz == this.currentClass ? Modifier.PUBLIC|Modifier.PROTECTED|Modifier.PRIVATE : clazz.isAssignableFrom(this.currentClass) ? Modifier.PUBLIC|Modifier.PROTECTED : Modifier.PUBLIC;
		try {
			for (Class<?> current = clazz; current != null; current = null) {
				for (Constructor<?> constructor: current.getDeclaredConstructors()) {
					if ((constructor.getModifiers()&accessLevel) != 0) {
						int currentRatio;
						if ((currentRatio = validateMethodArguments(constructor.isVarArgs(), constructor.getParameterTypes(), arguments)) > bestRatio) {
							bestRatio = currentRatio;
							bestMatch = constructor;
						}
					}
				}
			}
			if (bestRatio < -1)
				throw new NoSuchMethodException("no constructor "+clazz.getName()+"("+StringUtils.join(arguments.stream().map((VisitResult vr) -> { return vr.getResultClass().getName(); } ).iterator(), ", ")+")");
			if (bestRatio < 0) {
				getLogger().warn("Did not find sufficient match for "+clazz.getName()+"("+StringUtils.join(arguments.stream().map((VisitResult vr) -> { return vr.getResultClass().getName(); } ).iterator(), ", ")+")");
			}
			return bestMatch;
		}
		catch (SecurityException e) {
			throw new RuntimeException(e);
		}
		catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	public String			getDocCommentForTree(Tree tree, Trees trees)
	{
		String doc = processingEnv.getElementUtils().getDocComment(trees.getElement(trees.getPath(compilationUnitTree, tree)));
		return doc;
	}

	public void			process(Element element, TreePath tp, Trees trees)
	{
		this.compilationUnitTree = tp.getCompilationUnit();
		try {
			this.fileContent = IOUtils.toByteArray(compilationUnitTree.getSourceFile().openInputStream());
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		this.currentPackage = processingEnv.getElementUtils().getPackageOf(element).toString();
		this.currentPathName = this.currentPackage;

		this.contextStack = new Stack<TransVisitor.CodeContext>();
		this.pushContext(CodeContext.CCtx_Package);

		try {
			this.identifierStack = new GlobalIdentifierStore(this);
			if (tp.getCompilationUnit().getImports() != null) {
				for (ImportTree importTree: tp.getCompilationUnit().getImports()) {
					if (importTree.getQualifiedIdentifier().toString().endsWith(".*"))
						throw new RuntimeException("import * not supported");
					registerClass(importTree.getQualifiedIdentifier().toString());
				}
			}
			for (Tree typeDecl: tp.getCompilationUnit().getTypeDecls()) {
				if (typeDecl instanceof ClassTree) {
					registerClass(this.currentPackage+"."+((ClassTree)typeDecl).getSimpleName());
				}
			}

			this.scan(tp, trees);
		}
		catch (Exception ex) {
			logger.fatal("Failed to process "+compilationUnitTree.getSourceFile()+": "+ex.getMessage(), ex);
		}
		finally {
			this.popContext();
			this.contextStack = null;
			this.currentPackage = null;
			this.currentPathName = null;
			this.fileContent = null;
		}
	}

	public boolean			processOver(Trees trees)
	{
		for (Map.Entry<String, String> codeEntry: this.codeOutput.entrySet()) {
			String fileName = "trans/"+getSuffix()+"/_build/"+codeEntry.getKey().replaceAll("\\.java$", "").replace(".", "/")+"."+getSuffix();
			byte[] current = codeEntry.getValue().getBytes(Charsets.UTF_8);
			try {
				byte[] old = FileUtils.readFileToByteArray(new File(fileName));
				if (Arrays.equals(current, old))
					continue;
			}
			catch (IOException e) {
			}
			try {
				FileUtils.writeByteArrayToFile(new File(fileName), current);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return true;
	}

	public abstract String		getSuffix();

	protected void			pushContext(CodeContext context)
	{
		contextStack.push(context);
	}

	protected CodeContext		popContext()
	{
		return contextStack.pop();
	}

	public CodeContext		getContext()
	{
		return contextStack.peek();
	}

	public IdentifierStore		pushIdentifiers()
	{
		return (identifierStack = new HierarchicalIdentifierStore(identifierStack));
	}

	public IdentifierStore		pushIdentifiersSpecial(IdentifierStore store)
	{
		return (identifierStack = store);
	}

	public void			popIdentifiers()
	{
		identifierStack = identifierStack.getParent();
	}

	public IdentifierStore		getIdentifiers()
	{
		return identifierStack;
	}

	protected IdentifierDef		findIdentifier(String name)
	{
		return getIdentifiers().findIdentifier(name);
	}

	public IdentifierDef		addIdentifier(IdentifierDef idef)
	{
		getIdentifiers().addIdentifier(idef);
		return idef;
	}

	protected void			registerClass(String fullname)
	{
		Class<?> clazz = loadClass(fullname);
		addIdentifier(IdentifierDef.createClass(fullname.substring(fullname.lastIndexOf('.')+1), BoundType.createRaw(clazz)));
		addIdentifier(IdentifierDef.createClass(fullname, BoundType.createRaw(clazz)));
		//registerPackage(fullname.substring(0, fullname.lastIndexOf('.')));
	}

	protected void			registerPackage(String packageName)
	{
		for (String pkg = packageName; ; ) {
			addIdentifier(IdentifierDef.createPackage(pkg));
			if (pkg.lastIndexOf('.') < 0)
				break;
			pkg = pkg.substring(0, pkg.lastIndexOf('.'));
		}
	}

	public AnnotationTree		findAnnotation(ModifiersTree modifiers, Class<?> annotation)
	{
		for (AnnotationTree annotationTree: modifiers.getAnnotations()){
			if (resolveType(annotationTree.getAnnotationType()).equals(annotation))
				return annotationTree;
		}
		return null;
	}

	public Class<?>			resolveType(Tree type)
	{
		String typename = type.toString().replaceAll("<.*>", "");
		String dimensions = "";
		if (typename.indexOf('[') >= 0) {
			dimensions = typename.substring(typename.indexOf('['));
			typename = typename.substring(0, typename.indexOf('['));
		}
		IdentifierDef idef;
		if ((idef = findIdentifier(typename)) != null && (idef.type == IdentifierDef.IdentifierType.IT_Class || idef.type == IdentifierDef.IdentifierType.IT_Typed)) {
			return loadClass(idef.getRawClass().getName()+dimensions);
		}
		else if (typename.indexOf('.') >= 0 && (idef = findIdentifier(typename.substring(0, typename.indexOf('.')))) != null && idef.type == IdentifierDef.IdentifierType.IT_Class) {
			return loadClass(idef.getRawClass().getName()+typename.substring(typename.indexOf('.'))+dimensions);
		}
		else {
			Class<?> clazz = loadClass(typename);
			return loadClass(clazz.getName()+dimensions);
		}
	}

	public BoundType		resolveBoundType(Tree type)
	{
		switch (type.getKind()) {
		case PRIMITIVE_TYPE:
			{
				PrimitiveTypeTree pt = (PrimitiveTypeTree)type;
				return BoundType.createRaw(getPrimitives().computeIfAbsent(pt.toString(), (String key) -> { throw new IllegalArgumentException("Type "+key+" not found among primitives"); }));
			}

		case ARRAY_TYPE:
			{
				ArrayTypeTree at = (ArrayTypeTree)type;
				return BoundType.createRaw(Array.newInstance(resolveType(at.getType()), 0).getClass());
			}

		case PARAMETERIZED_TYPE:
			{
				ParameterizedTypeTree prt = (ParameterizedTypeTree)type;
				Class<?> rawType = resolveType(prt.getType());
				List<BoundType> paramBounds = new LinkedList<>();
				for (Tree paramTree: prt.getTypeArguments()) {
					paramBounds.add(resolveBoundType(paramTree));
				}
				return BoundType.createTyped(rawType, paramBounds.toArray(new BoundType[0]));
			}

		case UNBOUNDED_WILDCARD:
			{
				return BoundType.createRaw(Object.class);
			}

		case EXTENDS_WILDCARD:
			{
				return resolveBoundType(((WildcardTree) type).getBound());
			}

		case SUPER_WILDCARD:
			{
				return BoundType.createRaw(Object.class);
			}

		case IDENTIFIER:
		case MEMBER_SELECT:
			{
				IdentifierDef it = findIdentifier(type.toString());
				switch (it.getType()) {
				case IT_Class:
				case IT_Typed:
					return it.getBoundType();
				default:
					throw new RuntimeException("Cannot translate identifier "+type.toString()+" to bound type");
				}
			}

		default:
			{
				throw new RuntimeException("unexpected kind for type tree: "+type.getKind());
			}
		}
	}

	public static VisitResult	updateConstructorGenericResult(VisitResult result, Constructor<?> constructor, BoundType boundType, Map<String, BoundType> callBounds, List<VisitResult> arguments)
	{
		return result.updateResultBound(boundType);
	}

	public static VisitResult	updateMethodGenericResult(VisitResult result, Method method, BoundType ownerType, VisitResult path, Map<String, BoundType> callBounds, List<VisitResult> arguments)
	{
		List<BoundType> argumentsBounds = new ArrayList<>(Lists.transform(arguments, (VisitResult vr) -> vr.getResultBound()));
		if (method.isVarArgs() && argumentsBounds.size() >= method.getParameterCount() && !argumentsBounds.get(method.getParameterCount()-1).isArray())
			argumentsBounds.set(method.getParameterCount()-1, BoundType.createArray(argumentsBounds.get(method.getParameterCount()-1)));
		return result.updateResultBound(BoundTypeResolver.guessReturnTypeFromArguments(method, ownerType, callBounds, argumentsBounds));
	}

	public static VisitResult	updateFieldGenericResult(VisitResult result, Field field, BoundType ownerType)
	{
		return result.updateResultBound(BoundTypeResolver.replaceGenericTypes(field.getDeclaringClass(), field.getGenericType(), ownerType, new LinkedHashMap<String, BoundType>()));
	}

	public String			resolveConfiguredClassTransformation(String className)
	{
		return nativeClassMap.containsKey(className) ? nativeClassMap.get(className) : className;
	}

	protected String		transformClass(String clazz)
	{
		return getClassAdapter(clazz).transformClass(clazz);
	}

	protected String		transformClass(Class<?> clazz)
	{
		return getClassAdapter(clazz).transformClass(clazz.getName());
	}

	protected String		translateType(Tree type)
	{
		Class<?> typename = resolveType(type);
		return translateClassIdentifier(typename.getName());
	}

	public String			translateClassIdentifier(String className)
	{
		if (className == null)
			throw new IllegalArgumentException("className is null");
		return className;
	}

	public String			translateClassString(String className)
	{
		return className;
	}

	public String			translateClassString(Class<?> clazz)
	{
		return translateClassString(clazz.getName());
	}

	public String			translateResult(VisitResult result)
	{
		switch (result.getResultIndicator()) {
		case VisitResult.RI_DotClass:
			return result.getFinal(); //"'"+translateClassString(transformClass(result.getContent().replaceAll("\\.class$", "")))+"'";

		default:
			return result.getContent();
		}
	}

	public String			translateResultSelect(VisitResult result, boolean isStatic)
	{
		switch (result.getResultIndicator()) {
		case VisitResult.RI_Super:
			return "super.";

		case VisitResult.RI_ClassIdentifier:
			return translateClassIdentifier(result.getContent())+".";

		case VisitResult.RI_DotClass:
			return result.getFinal(); //"'"+translateClassString(transformClass(result.getContent().replaceAll("\\.class$", "")))+"'";

		default:
			return isStatic ? (translateClassIdentifier(result.getResultClass().getName())+".") : ((result.isNull() ? "this" : result.getContent())+".");
		}
	}

	public TransClassAdapter	getClassAdapter(Class<?> clazz)
	{
		TransClassAdapter adapter;
		if ((adapter = classAdapters.get(clazz.getName())) == null) {
			if (clazz.isArray()) {
				adapter = clazz.getComponentType().isPrimitive() ? defaultPrimitiveArrayClassAdapter : defaultObjectsArrayClassAdapter;
			}
			else {
				adapter = defaultClassAdapter;
			}
		}
		return adapter;
	}

	public TransClassAdapter	getClassAdapter(String clazzName)
	{
		return getClassAdapter(loadClass(clazzName));
	}

	protected String		makeVariableDef(VariableTree node, Trees trees)
	{
		String name = node.getName().toString();
		StringBuilder r = new StringBuilder();
		BoundType boundType = resolveBoundType(node.getType());
		r.append(translateClassIdentifier(transformClass(boundType.getRawClass())));
		r.append(" ").append(formatVariableName(name));
		addIdentifier(IdentifierDef.createVariable(name, boundType));
		return r.toString();
	}

	protected void			addVisibleIdentifiers(Class<?> clazz)
	{
		for (Method m: clazz.getDeclaredMethods()) {
			if (java.lang.reflect.Modifier.isPublic(m.getModifiers()) || java.lang.reflect.Modifier.isProtected(m.getModifiers()))
				addIdentifier(IdentifierDef.createMethod(m.getName(), m.getModifiers(), BoundType.createRaw(m.getReturnType())));
		}
		for (Field f: clazz.getDeclaredFields()) {
			if (java.lang.reflect.Modifier.isPublic(f.getModifiers()) || java.lang.reflect.Modifier.isProtected(f.getModifiers()))
				addIdentifier(IdentifierDef.createField(f.getName(), f.getModifiers(), BoundType.createRaw(f.getType())));
		}
		for (Class<?> c: clazz.getDeclaredClasses()) {
			if (java.lang.reflect.Modifier.isPublic(c.getModifiers()) || java.lang.reflect.Modifier.isProtected(c.getModifiers()))
				addIdentifier(IdentifierDef.createClass(c.getSimpleName(), BoundType.createRaw(c)));
		}
	}

	protected void			addIdentifiersSupers(Class<?> clazz)
	{
		for (Class<?> i: clazz.getInterfaces()) {
			addIdentifiersSupers(i);
			addVisibleIdentifiers(i);
		}
		Class<?> superc;
		if ((superc = clazz.getSuperclass()) != null) {
			addIdentifiersSupers(superc);
			addVisibleIdentifiers(superc);
		}
	}

	protected int			convertModifiers(Set<javax.lang.model.element.Modifier> modifiers)
	{
		int out = 0;
		if (modifiers.contains(javax.lang.model.element.Modifier.PUBLIC))
			out |= Modifier.PUBLIC;
		if (modifiers.contains(javax.lang.model.element.Modifier.PROTECTED))
			out |= Modifier.PROTECTED;
		if (modifiers.contains(javax.lang.model.element.Modifier.PRIVATE))
			out |= Modifier.PRIVATE;
		if (modifiers.contains(javax.lang.model.element.Modifier.FINAL))
			out |= Modifier.FINAL;
		if (modifiers.contains(javax.lang.model.element.Modifier.STATIC))
			out |= Modifier.STATIC;
		if (modifiers.contains(javax.lang.model.element.Modifier.ABSTRACT))
			out |= Modifier.ABSTRACT;
		if (modifiers.contains(javax.lang.model.element.Modifier.TRANSIENT))
			out |= Modifier.TRANSIENT;
		return out;
	}

	public abstract VisitResult	processClass(ClassTree classTree, Trees trees);

	public Class<?>			convertExtends(Tree extendsTree)
	{
		Class<?> extend = resolveType(extendsTree);
		if (extend == Object.class)
			extend = null;
		return extend;
	}

	public Class<?>			convertImplements(Tree implementsTree)
	{
		Class<?> implement = resolveType(implementsTree);
		return implement;
	}

	public String			formatDocComment(String comment)
	{
		if (StringUtils.isEmpty(comment))
			return "";
		return "/**\n"+prependCommentStars.matcher(comment).replaceAll(" * ")+" */\n";
	}

	public VisitResult		formatClassAnnotations(ClassTree classTree, List<? extends AnnotationTree> nodes, Trees trees)
	{
		return processAnnotations(nodes, trees);
	}

	public VisitResult		formatMethodAnnotations(MethodTree methodTree, List<? extends AnnotationTree> nodes, Trees trees)
	{
		return processAnnotations(nodes, trees);
	}

	public VisitResult		formatFieldAnnotations(VariableTree variableTree, List<? extends AnnotationTree> nodes, Trees trees)
	{
		return processAnnotations(nodes, trees);
	}

	public String			formatVariableName(String name)
	{
		return name;
	}

	public String			formatConstName(String name)
	{
		return name;
	}

	public String			formatClassStaticPrefix(String path)
	{
		return path+".";
	}

	public String			formatThisStaticPrefix(String path)
	{
		return path+".";
	}

	public String			formatClassStaticField(String path, String name)
	{
		return path+"::"+name;
	}

	public String			formatThisStaticField(String name)
	{
		return name;
	}

	public String			formatThisInstanceField(String name)
	{
		return name;
	}

	public String			formatFullType(Tree type)
	{
		if (type instanceof ParameterizedTypeTree) {
			ParameterizedTypeTree parType = (ParameterizedTypeTree)type;
			StringBuilder sb = new StringBuilder(translateClassString(transformClass(resolveType(parType.getType()))));
			sb.append("<");
			int c = 0;
			for (Tree subType: parType.getTypeArguments()) {
				if (c++ > 0)
					sb.append(", ");
				sb.append(formatFullType(subType));
			}
			sb.append(">");
			return sb.toString();
		}
		else if (type instanceof WildcardTree) {
			WildcardTree wildType = (WildcardTree)type;
			switch (wildType.getKind()) {
			case UNBOUNDED_WILDCARD:
				return translateClassString("java.lang.Object");

			case EXTENDS_WILDCARD:
			case SUPER_WILDCARD:
				return formatFullType(wildType.getBound());

			default:
				throw new RuntimeException("unexpected wildcard kind: "+type.getKind());
			}
		}
		else if (type instanceof ArrayTypeTree) {
			return formatFullType(((ArrayTypeTree)type).getType())+"[]";
		}
		else {
			return translateClassString(resolveType(type));
		}
	}

	public String			formatPrimitiveDefault(Class<?> clazz)
	{
		return "null";
	}

	public String			formatThis()
	{
		return "this";
	}

	public String			formatSuper()
	{
		return "super";
	}

	public String			formatConstructorName(Class<?> clazz)
	{
		return clazz.getSimpleName();
	}

	public VisitResult		processClassMemberSelect(VisitResult left, MemberSelectTree node, Trees trees)
	{
		return left.appendString(".class")
			.updateResultBound(BoundType.createTyped(Class.class, new BoundType[]{left.getResultBound()}))
			.updateResultIndicator(VisitResult.RI_DotClass);
	}

	public List<VisitResult>	processExpressionList(List<? extends ExpressionTree> list, Trees trees)
	{
		List<VisitResult> results = new LinkedList<VisitResult>();
		for (ExpressionTree expression: list) {
			try {
				results.add(scan(expression, trees));
			}
			catch (Exception ex) {
				getLogger().error(ex.toString(), ex);
				results.add(new VisitResult(this.surroundFix(expression.toString(), ex))
						.updateResultIndicator(VisitResult.RI_Error));
			}
		}
		return results;
	}

	public VisitResult		processMethodInvocation(VisitResult path, Method method, List<VisitResult> arguments, MethodInvocationTree node, Trees trees)
	{
		for (Class<?> clazz: ClassUtil.listClassHierarchyClassFirst(path.getResultClass())) {
			try {
				clazz.getDeclaredMethod(method.getName(), method.getParameterTypes());
			}
			catch (NoSuchMethodException ex) {
			}
			VisitResult result;
			if ((result = getClassAdapter(clazz).processMethodInvocation(path, method, arguments, node, trees)) != null)
				return result;
		}
		return processMethodInvocationDefault(path, method, arguments, node, trees);
	}

	public VisitResult		checkCommonMethodInvocation(VisitResult path, Method method, List<VisitResult> arguments, MethodInvocationTree node, Trees trees)
	{
		return null;
	}

	public VisitResult		processMethodInvocationDefault(VisitResult path, Method method, List<VisitResult> arguments, MethodInvocationTree node, Trees trees)
	{
		VisitResult out;
		StringBuilder sb = new StringBuilder();
		if (java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
			sb.append(this.translateClassIdentifier(transformClass(path.getResultClass().getName()))).append("::");
		}
		else {
			if (path.getResultIndicator() == VisitResult.RI_Super) {
				sb.append("super.");
			}
			else {
				sb.append(path.getContent()).append(".");
			}
		}
		sb.append(method.getName());
		sb.append("(");
		appendArguments(sb, method, arguments);
		sb.append(")");
		return new VisitResult(sb);
	}

	public VisitResult		surroundIntegerDivide(VisitResult result)
	{
		return result;
	}

	public VisitResult		updateNumericResult(VisitResult result, VisitResult lo, VisitResult ro)
	{
		if (isClassIntegerish(lo.getResultClass()) && isClassFloatish(ro.getResultClass())) {
			result.updateResultClass(ro.getResultClass());
		}
		else if (isClassCharacterish(lo.getResultClass()) && isClassIntegerish(ro.getResultClass())) {
			result.updateResultClass(ro.getResultClass());
		}
		else {
			result.updateResultClass(lo.getResultClass());
		}
		return result;
	}

	public ExpressionTree		getSimpleExpression(ExpressionTree expressionTree)
	{
		if (expressionTree instanceof ParenthesizedTree)
			expressionTree = ((ParenthesizedTree)expressionTree).getExpression();
		return expressionTree;
	}

	public VisitResult		processEnhancedForLoop(EnhancedForLoopTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder("for (");
		sb.append(formatVariableName(node.getVariable().getName().toString())).append(": ");
		sb.append(scan(node.getExpression(), trees).getContent());
		sb.append(")");
		String s = node.getStatement() != null ? scan(node.getStatement(), trees).getContent() : ";\n";
		if (!s.equals(";\n")) {
			sb.append(FormatUtil.indentStatementString(s));
		}
		else {
			sb.append(" ").append(s);
		}
		return new VisitResult(sb.toString());
	}

	public VisitResult		processLambdaExpression(CatchingIdentifierStore caught, LambdaExpressionTree node, Trees trees)
	{
		return scan(node, trees);
	}

	public VisitResult		processAnnotations(List<? extends AnnotationTree> nodes, Trees trees)
	{
		pushContext(CodeContext.CCtx_Annotated);
		try {
			StringBuilder sb = new StringBuilder();
			for (AnnotationTree annoTree: nodes) {
				VisitResult r = this.visitAnnotation(annoTree, trees);
				if (r != null)
					FormatUtil.appendSbSafe(sb, r.getContent());
			}
			return sb.length() == 0 ? VisitResult.createNull() : new VisitResult(sb);
		}
		finally {
			popContext();
		}
	}

	public VisitResult		processUnaryOperator(VisitResult expression, TargetOperator operatorDef, UnaryTree node, Trees trees)
	{
		if (operatorDef.getText().startsWith("\b")) {
			surroundRightPrioritized(expression, operatorDef);
			return new VisitResult(expression.getContent()+operatorDef.getText().substring(1))
				.updateExpressionPriority(operatorDef.getPriority());
		}
		else {
			surroundRightPrioritized(expression, operatorDef);
			return new VisitResult(operatorDef.getText()+expression.getContent())
				.updateExpressionPriority(operatorDef.getPriority());
		}
	}

	public VisitResult		processBinaryOperator(VisitResult lo, VisitResult ro, TargetOperator operatorDef, BinaryTree node, Trees trees)
	{
		surroundLeftPrioritized(lo, operatorDef);
		surroundRightPrioritized(ro, operatorDef);
		return new VisitResult(lo.getContent()+operatorDef.getText()+ro.getContent())
			.updateExpressionPriority(operatorDef.getPriority());
	}

	public VisitResult		processCompareNull(VisitResult lo, VisitResult ro, TargetOperator operatorDef, BinaryTree node, Trees trees)
	{
		surroundLeftPrioritized(lo, operatorDef);
		surroundRightPrioritized(ro, operatorDef);
		return new VisitResult(lo.getContent()+operatorDef.getText()+ro.getContent())
			.updateExpressionPriority(operatorDef.getPriority());
	}

	public VisitResult		processCompareNotNull(VisitResult lo, VisitResult ro, TargetOperator operatorDef, BinaryTree node, Trees trees)
	{
		surroundLeftPrioritized(lo, operatorDef);
		surroundRightPrioritized(ro, operatorDef);
		return new VisitResult(lo.getContent()+operatorDef.getText()+ro.getContent())
			.updateExpressionPriority(operatorDef.getPriority());
	}

	public VisitResult		processCompoundAssignmentOperator(VisitResult lo, VisitResult ro, TargetOperator operatorDef, CompoundAssignmentTree node, Trees trees)
	{
		surroundLeftPrioritized(lo, operatorDef);
		surroundRightPrioritized(ro, operatorDef);
		return new VisitResult(lo.getContent()+operatorDef.getText()+ro.getContent())
			.updateExpressionPriority(operatorDef.getPriority());
	}

	public VisitResult		visitExpression(ExpressionTree variable, Trees trees)
	{
		return scan(variable, trees);
	}

	public VisitResult		visitOptionalTree(Tree node, Trees trees)
	{
		if (node == null)
			return VisitResult.createNull();
		return scan(node, trees);
	}

	@Override
	public VisitResult		visitCompilationUnit(CompilationUnitTree node, Trees trees)
	{
		getLogger().info("processing file "+this.currentFile);
		return super.visitCompilationUnit(node, trees);
	}

	@Override
	public VisitResult		visitImport(ImportTree node, Trees trees)
	{
		getLogger().debug("visiting import "+node.getQualifiedIdentifier());
		Class<?> clazz = loadClass(node.getQualifiedIdentifier().toString());
		String name = clazz.getSimpleName();
		addIdentifier(IdentifierDef.createClass(name, BoundType.createRaw(clazz)));
		addIdentifier(IdentifierDef.createPackage(node.getQualifiedIdentifier().toString().split("\\.")[0]));
		return null;
	}

	@Override
	public VisitResult		visitClass(ClassTree classTree, Trees trees)
	{
		ClassContext oldContext = this.currentContext;
		String oldPathName = this.currentPathName;
		ClassTree oldClassTree = this.currentClassTree;
		String oldClassName = this.currentClassName;
		String oldFullName = this.currentFullName;
		StringBuilder oldStaticInit = this.currentStaticInit;
		StringBuilder oldInstanceInit = this.currentInstanceInit;
		Class<?> oldClass = this.currentClass;
		Class<?> oldSuperClass = this.currentSuperClass;
		BoundType oldBoundType = this.currentBoundType;

		pushIdentifiers();
		pushContext(CodeContext.CCtx_Class);

		StringBuilder sb = new StringBuilder();
		try {
			this.currentClassTree = classTree;
			this.currentClassName = this.currentClassTree.getSimpleName().toString();
			this.currentFullName = this.currentPathName+"."+this.currentClassName;
			this.currentPathName = this.currentFullName;
			this.currentStaticInit = new StringBuilder();
			this.currentInstanceInit = new StringBuilder();
			this.currentClass = loadClass(this.currentFullName);
			this.currentSuperClass = this.currentClass.getSuperclass();
			this.currentContext = new ClassContext();
			this.currentContext.clazz = this.currentClass;
			this.registerClass(this.currentFullName);
			addIdentifier(IdentifierDef.createClass(currentClassName, BoundTypeResolver.resolveClassBasicBounds(this.currentClass)));
			addIdentifier(IdentifierDef.createThis(BoundTypeResolver.resolveClassBasicBounds(this.currentClass)));
			if (this.currentClass.getGenericSuperclass() != null)
				addIdentifier(IdentifierDef.createSuper(BoundTypeResolver.resolveInheritedBounds(this.currentClass.getGenericSuperclass(), BoundTypeResolver.resolveClassBasicBounds(this.currentClass))));
			getLogger().info("got class "+this.currentClassName);
			processClass(classTree, trees);
		}
		finally {
			popIdentifiers();
			popContext();

			this.currentContext = oldContext;
			this.currentPathName = oldPathName;
			this.currentClassTree = oldClassTree;
			this.currentClassName = oldClassName;
			this.currentFullName = oldFullName;
			this.currentStaticInit = oldStaticInit;
			this.currentInstanceInit = oldInstanceInit;
			this.currentBoundType = oldBoundType;
			this.currentClass = oldClass;
			this.currentSuperClass = oldSuperClass;
		}

		return VisitResult.createEmpty();
	}

	@Override
	public VisitResult		visitAnnotation(AnnotationTree node, Trees trees)
	{
		return super.visitAnnotation(node, trees);
	}

	@Override
	public VisitResult		visitModifiers(ModifiersTree node, Trees trees)
	{
		return super.visitModifiers(node, trees);
	}

	@Override
	public VisitResult		visitVariable(VariableTree node, Trees trees)
	{
		Tree typeTree = node.getType();
		StringBuilder sb = new StringBuilder();
		try {
			String name = node.getName().toString();
			String initializer = null;
			if (node.getInitializer() != null) {
				pushContext(CodeContext.CCtx_Value);
				try {
					ExpressionTree initializerTree = node.getInitializer();
					if (initializerTree instanceof NewClassTree && ((NewClassTree)initializerTree).getIdentifier() instanceof ParameterizedTypeTree) {
						final NewClassTree newClassTree = (NewClassTree)initializerTree;
						final ParameterizedTypeTree parameterizedTypeTree = (ParameterizedTypeTree)newClassTree.getIdentifier();
						if (parameterizedTypeTree.getTypeArguments() != null && parameterizedTypeTree.getTypeArguments().size() == 0 && typeTree instanceof ParameterizedTypeTree) {
							initializer = visitExpression(new NewClassTreeProxy(newClassTree) {
								@Override
								public ExpressionTree getIdentifier() {
									return new ParameterizedTypeAndExpressionTreeProxy(parameterizedTypeTree) {
										@Override
										public List<? extends Tree>	getTypeArguments()
										{
											return ((ParameterizedTypeTree)typeTree).getTypeArguments();
										}
									};
								}
							}, trees).getFinal();
						}
					}
					if (initializer == null)
						initializer = visitExpression(node.getInitializer(), trees).getFinal();
				}
				catch (Exception ex) {
					initializer = this.surroundFix(node.getInitializer().toString(), ex);
				}
				finally {
					popContext();
				}
			}
			if (getContext().ordinal() >= CodeContext.CCtx_MethodBody.ordinal()) {
				getLogger().debug("visiting variable "+node.getName());
				addIdentifier(IdentifierDef.createVariable(name, resolveBoundType(node.getType())));
				if (initializer == null || initializer.equals(""))
					return VisitResult.createEmpty();
				sb.append(formatVariableName(name)).append(" = ").append(initializer).append(";\n");
			}
			else {
				getLogger().debug("visiting field "+node.getName());
				String annos = formatFieldAnnotations(node, node.getModifiers().getAnnotations(), trees).getContent();
				int modifiers = convertModifiers(node.getModifiers().getFlags());
				if (Modifier.isPublic(modifiers))
					sb.append("public ");
				if (Modifier.isStatic(modifiers))
					sb.append("static ");
				Field field = lookupField(this.currentClass, name);
				Class<?> type = field.getType();
				boolean isConst = (Modifier.isFinal(modifiers) && field.getType().isPrimitive()) || this.currentContext.clazz.isEnum();
				if (isConst)
					sb = new StringBuilder().append("const ");
				if (sb.length() == 0)
					sb.append("protected ");
				//if (field.getAnnotation(NotNull.class) != null)
				//	sb.append(translateType(node.getType())).append(" ");
				sb.replace(sb.length()-1, sb.length(), "");
				FormatUtil.tabalign(sb, 32);
				sb.append(isConst ? formatConstName(name) : formatVariableName(name));
				if (annos != null)
					sb.insert(0, annos);
				sb.insert(0, formatDocComment(getDocCommentForTree(node, trees)));
				if (this.currentContext.clazz.isEnum()) {
					sb.append(" = ").append(this.currentContext.enumCounter++);
				}
				else if (initializer != null && !initializer.equals("")) {
					if (node.getInitializer() instanceof LiteralTree) {
						sb.append(" = ").append(initializer);
					}
					else if (Modifier.isStatic(modifiers)) {
						this.currentStaticInit.append(formatThisStaticField(name)).append(" = ").append(initializer).append(";\n");
					}
					else {
						this.currentInstanceInit.append(formatThisInstanceField(name)).append(" = ").append(initializer).append(";\n");
					}
				}
				else if (type.isPrimitive()) {
					sb.append(" = ");
					sb.append(formatPrimitiveDefault(type));
				}
				return new VisitResult(sb.append(";\n\n"));
			}
		}
		catch (RuntimeException ex) {
			getLogger().error(ex.toString(), ex);
			return new VisitResult(surroundFix(node.toString(), ex).toString());
		}
		return new VisitResult(sb.toString());
	}

	@SuppressWarnings("unchecked")
	@Override
	public VisitResult		visitMethod(MethodTree methodTree, Trees trees)
	{
		StringBuilder sb = new StringBuilder();
		pushIdentifiers();
		try {
			if (isAnnotation) {
				Tree annoDefault = methodTree.getDefaultValue();
				if (annoDefault != null) {
					annotationDefaults.put(methodTree.getName().toString(), visitExpression((ExpressionTree)annoDefault, trees).getFinal());
				}
				return new VisitResult(FormatUtil.tabalign("public ", 32)+methodTree.getName()+"() { return this.\""+methodTree.getName()+"\"; }\n");
			}
			getLogger().debug("visiting method "+methodTree.getName());
			List<? extends ExpressionTree> argumentDefaults = null;

			for (AnnotationTree annoTree: methodTree.getModifiers().getAnnotations()) {
				if (resolveType(annoTree.getAnnotationType()) == org.druf.trans.meta.NoDynamic.class)
					return VisitResult.createNull();
			}
			for (AnnotationTree annoTree: methodTree.getModifiers().getAnnotations()) {
				if (resolveType(annoTree.getAnnotationType()) == org.druf.trans.meta.DynamicDefaults.class) {
					for (ExpressionTree argument: annoTree.getArguments()) {
						AssignmentTree assignment = (AssignmentTree)argument;
						if (assignment.getVariable().toString().equals("defaults")) {
							if (assignment.getExpression() instanceof NewArrayTree) {
								argumentDefaults = ((NewArrayTree)assignment.getExpression()).getInitializers();
							}
							else {
								sb.append(surroundFix("expected array for @DynamicDefault.defaults", null));
							}
						}
					}
				}
			}
			for (TypeParameterTree typed: methodTree.getTypeParameters()) {
				BoundType extend = typed.getBounds().iterator().hasNext() ? resolveBoundType(typed.getBounds().iterator().next()) : BoundType.createRaw(Object.class);
				addIdentifier(IdentifierDef.createTyped(typed.getName().toString(), extend));
			}
			String annos = formatMethodAnnotations(methodTree, methodTree.getModifiers().getAnnotations(), trees).getContent();
			int modifiers = convertModifiers(methodTree.getModifiers().getFlags());
			if (Modifier.isPublic(modifiers) && !isInterface)
				sb.append("public ");
			if (Modifier.isStatic(modifiers))
				sb.append("static ");
			if (Modifier.isAbstract(modifiers) && !isInterface)
				sb.append("abstract ");
			FormatUtil.tabalign(sb, 32);
			String name = methodTree.getName().toString();
			if (name.equals("<init>"))
				name = formatConstructorName(this.currentClass);
			sb.append(name).append("(");
			Class<?>[] paramsTypes;
			Annotation[][] paramsAnnotations;
			Map<Class<?>, Annotation>[] paramsAnnotationsHash;
			{
				ArrayList<Class<?>> paramList = new ArrayList<Class<?>>();
				for (VariableTree param: methodTree.getParameters()) {
					paramList.add(resolveType(param.getType()));
				}
				try {
					if (name.equals(formatConstructorName(this.currentClass))) {
						Constructor<?> constructor = loadClass(this.currentFullName).getDeclaredConstructor(paramList.toArray(new Class<?>[0]));
						paramsTypes = constructor.getParameterTypes();
						paramsAnnotations = constructor.getParameterAnnotations();
					}
					else {
						Method method = loadClass(this.currentFullName).getDeclaredMethod(name, paramList.toArray(new Class<?>[0]));
						paramsTypes = method.getParameterTypes();
						paramsAnnotations = method.getParameterAnnotations();
					}
				}
				catch (SecurityException e) {
					throw new RuntimeException(e);
				}
				catch (NoSuchMethodException e) {
					if (name.equals(formatConstructorName(this.currentClass)) && paramList.isEmpty() && this.currentContext.clazz.isEnum()) {
						logger.debug("ignoring non-existing constructor for enum");
						return null;
					}
					throw new RuntimeException(e);
				}
			}
			{
				paramsAnnotationsHash = (Map<Class<?>, Annotation>[]) new Map<?, ?>[paramsTypes.length];
				for (int i = 0; i < paramsAnnotations.length; i++) {
					paramsAnnotationsHash[i] = new HashMap<Class<?>, Annotation>();
					for (Annotation annotation: paramsAnnotations[i])
						paramsAnnotationsHash[i].put(annotation.annotationType(), annotation);
				}
			}
			{
				int count = 0;
				for (VariableTree param: methodTree.getParameters()) {
					String paramName = param.getName().toString();
					if (count != 0)
						sb.append(", ");
					Class<?> clazz = resolveType(param.getType());
					if (clazz.isArray() && findAnnotation(param.getModifiers(), org.druf.trans.meta.Out.class) != null) {
						sb.append("&");
					}
					else if (paramsAnnotationsHash[count].get(NotNull.class) != null) {
						String transformed = getClassAdapter(clazz).transformClass(clazz.getName());
						if (transformed != null)
							sb.append(translateClassIdentifier(transformed)).append(" ");
					}
					sb.append(formatVariableName(paramName));
					if (argumentDefaults != null && count >= methodTree.getParameters().size()-argumentDefaults.size()) {
						sb.append(" = ").append(((LiteralTree)argumentDefaults.get(count-(methodTree.getParameters().size()-argumentDefaults.size()))).getValue());
					}
					addIdentifier(IdentifierDef.createVariable(paramName, resolveBoundType(param.getType())));
					count++;
				}
			}
			sb.append(")");
			if (annos != null)
				sb.insert(0, annos);
			sb.insert(0, formatDocComment(getDocCommentForTree(methodTree, trees)));
			pushContext(CodeContext.CCtx_MethodBody);
			try {
				if (methodTree.getBody() == null) {
					sb.append(";\n");
				}
				else {
					sb.append("\n");
					String r = scan(methodTree.getBody(), trees).getContent();
					sb.append(r);
				}
				sb.append("\n");
			}
			finally {
				popContext();
			}
		}
		catch (Exception ex) {
			getLogger().error(ex.toString(), ex);
			return new VisitResult(surroundFix(methodTree.toString(), ex).toString());
		}
		finally {
			popIdentifiers();
		}
		return new VisitResult(sb.toString());
	}

	@Override
	public VisitResult		visitBlock(BlockTree node, Trees trees)
	{
		BoundType resultBound = null;
		StringBuilder sb = new StringBuilder();
		pushIdentifiers();
		try {
			for (StatementTree statementTree: node.getStatements()) {
				VisitResult r = statementTree.accept(this, trees);
				sb.append(r.getContent());
				if (r.getResultIndicator() == VisitResult.RI_Return)
					resultBound = r.getResultBound();
			}
			FormatUtil.forceSbNl(sb);
			FormatUtil.indentSb(sb);
			sb.insert(0, "{\n");
			sb.append("}\n");

		}
		finally {
			popIdentifiers();
		}
		if (getContext() == CodeContext.CCtx_Class) {
			this.currentStaticInit.append(sb.toString());
			return null;
		}
		return new VisitResult(sb.toString())
			.updateResultBound(resultBound);
	}

	@Override
	public VisitResult		visitIdentifier(IdentifierTree node, Trees trees)
	{
		String name = node.getName().toString();
		IdentifierDef idef = findIdentifier(name);
		getLogger().debug("visiting identifier "+name+": "+idef);
		if (idef == null) {
			try {
				Class<?> currentResult = loadClassInternal(node.toString());
				return new VisitResult(currentResult.getName())
					.updateResultBound(BoundTypeResolver.resolveClassBasicBounds(currentResult));
			}
			catch (ClassNotFoundException e) {
				return new VisitResult(node.toString())
					.updateResultIndicator(VisitResult.RI_PackageIdentifier);
			}
		}
		switch (idef.type) {
		case IT_Package:
			return new VisitResult(name)
				.updateResultIndicator(VisitResult.RI_PackageIdentifier);
		case IT_Class:
			return new VisitResult(resolveType(node).getName())
				.updateResultBound(idef.getBoundType())
				.updateEmptyExpressionPriority(getPriorityLiteral());
		case IT_This:
			return new VisitResult(formatThis())
				.updateResultBound(idef.getBoundType())
				.updateResultIndicator(VisitResult.RI_This)
				.updateEmptyExpressionPriority(getPriorityLiteral());
		case IT_Super:
			return new VisitResult(formatSuper())
				.updateResultBound(idef.getBoundType())
				.updateResultIndicator(VisitResult.RI_Super)
				.updateEmptyExpressionPriority(getPriorityLiteral());
		case IT_Field:
			Field field = lookupField(loadClass(this.currentFullName), idef.name);
			Type fieldType = field.getGenericType();
			Class<?> fieldClass = field.getType();
			if (Modifier.isStatic(idef.modifiers)) {
				String prefix = field.getDeclaringClass() == this.currentClass ? formatThisStaticPrefix(this.currentClassName) : formatClassStaticPrefix(translateClassIdentifier(field.getDeclaringClass().getName()));
				return  updateFieldGenericResult(new VisitResult(prefix+(Modifier.isFinal(idef.modifiers) && fieldClass.isPrimitive() ? formatConstName(name) : formatVariableName(name))), field, this.currentBoundType)
					.updateEmptyExpressionPriority(getPriorityStatic());
			}
			else {
				return updateFieldGenericResult(new VisitResult(formatThisInstanceField(name)), field, this.currentBoundType)
					.updateEmptyExpressionPriority(getPriorityMember());
			}
		case IT_Method:
			throw new RuntimeException("should be never called");
		case IT_Variable:
			return new VisitResult(formatVariableName(name))
				.updateResultBound(idef.getBoundType())
				.updateEmptyExpressionPriority(getPriorityVariable());
		default:
			throw new RuntimeException("unexpected identifier type: "+idef.type);
		}
	}

	@Override
	public VisitResult		visitLiteral(LiteralTree node, Trees trees)
	{
		Class<?> resultClass;
		if (node.getValue() == null) {
			resultClass = void.class;
		}
		else {
			resultClass = node.getValue().getClass();
		}

		String r = node.toString();
		if (resultClass == String.class) {
			r = node.getValue().toString();
			r = r.replace("\\", "\\\\").replace("\"", "\\\"");
			r = r.replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
			for (int i = 0; i < r.length(); i++) {
				if (r.charAt(i) < 32) {
					r = r.substring(0, i)+"\\x"+String.format("%02x", (int)r.charAt(i))+r.substring(i+1);
					i += 5;
				}
			}
		}
		else if (resultClass == Long.class || resultClass == Integer.class) {
			r = String.valueOf(node.getValue());
		}
		else if (resultClass == Float.class || resultClass == Double.class) {
			r = r.replaceAll("[fF]$", "");
		}
		VisitResult result = new VisitResult(r)
			.updateResultClass(resultClass);
		if (resultClass == void.class)
			result.updateResultIndicator(VisitResult.RI_Null);
		result.updateEmptyExpressionPriority(getPriorityLiteral());
		return result;
	}

	@Override
	public VisitResult		visitMethodInvocation(MethodInvocationTree node, Trees trees)
	{
		String methodName;
		ExpressionTree methodSelect = node.getMethodSelect();
		List<VisitResult> arguments = processExpressionList(node.getArguments(), trees);
		VisitResult path;
		Method method;
		if (methodSelect instanceof MemberSelectTree) {
			MemberSelectTree select = (MemberSelectTree)methodSelect;
			methodName = select.getIdentifier().toString();
			path = scan(select.getExpression(), trees);
			getLogger().debug("visiting invocation "+methodName+" on "+path+" ("+path.getResultClass()+")");
			method = this.lookupMethod(path.getResultClass(), methodName, arguments);
		}
		else if (methodSelect instanceof IdentifierTree) {
			IdentifierTree identifier = (IdentifierTree)methodSelect;
			methodName = identifier.getName().toString();
			getLogger().debug("visiting invocation "+methodName);
			Class<?> clazz;
			if (methodName.equals("super")) {
				clazz = this.currentSuperClass;
				path = VisitResult.createNull()
					.updateResultClass(clazz)
					.updateResultIndicator(VisitResult.RI_Super);
				Constructor<?> constructor = this.lookupConstructor(clazz, arguments);
				return getClassAdapter(clazz).processSuperConstructorInvocation(path, constructor, arguments, node, trees);
			}
			else {
				clazz = this.currentClass;
				method = this.lookupMethod(clazz, methodName, arguments);
				path = new VisitResult(formatThis())
					.updateResultBound(this.currentBoundType);
			}
		}
		else {
			throw new RuntimeException("unexpected tree on method invocation "+node.getKind()+" "+node.getClass().getName()+" "+node);
		}

		LinkedHashMap<String, BoundType> callerBounds = new LinkedHashMap<>();
		if (node.getTypeArguments() != null) {
			for (int i = 0; i < node.getTypeArguments().size(); ++i) {
				callerBounds.put(method.getTypeParameters()[i].getName(), resolveBoundType(node.getTypeArguments().get(i)));
			}
		}
		if (method.getTypeParameters().length != 0) {
			for (TypeVariable<?> typeVariable: method.getTypeParameters()) {
				if (callerBounds.containsKey(typeVariable.getName()))
					continue;
			}
		}
		return updateMethodGenericResult(processMethodInvocation(path, method, arguments, node, trees), method, path.getResultBound(), path, callerBounds, arguments)
			.updateEmptyExpressionPriority(getPriorityCall());
	}

	@Override
	public VisitResult		visitAssert(AssertTree node, Trees trees)
	{
		return new VisitResult("if (!("+super.visitAssert(node, trees).getContent()+")) throw new RuntimeException(\"assert\");\n");
	}

	@Override
	public VisitResult		visitAssignment(AssignmentTree node, Trees trees)
	{
		VisitResult variable = visitExpression(node.getVariable(), trees);
		VisitResult value = visitExpression(node.getExpression(), trees);
		return getClassAdapter(variable.getResultClass()).processAssignment(variable, value, node, trees)
			.updateEmptyResultBound(variable.getResultBound())
			.updateEmptyExpressionPriority(getOperatorPriority("="));
	}

	@Override
	public VisitResult		visitCompoundAssignment(CompoundAssignmentTree node, Trees trees)
	{
		VisitResult lo = visitExpression(node.getVariable(), trees);
		VisitResult ro = visitExpression(node.getExpression(), trees);
		switch (node.getKind()) {
		case AND_ASSIGNMENT:
			return processCompoundAssignmentOperator(lo, ro, getTargetOperator("&="), node, trees);

		case DIVIDE_ASSIGNMENT:
			return processCompoundAssignmentOperator(lo, ro, getTargetOperator("/="), node, trees);

		case LEFT_SHIFT_ASSIGNMENT:
			return processCompoundAssignmentOperator(lo, ro, getTargetOperator("<<="), node, trees);

		case MINUS_ASSIGNMENT:
			return processCompoundAssignmentOperator(lo, ro, getTargetOperator("-="), node, trees);

		case MULTIPLY_ASSIGNMENT:
			return processCompoundAssignmentOperator(lo, ro, getTargetOperator("*="), node, trees);

		case OR_ASSIGNMENT:
			return processCompoundAssignmentOperator(lo, ro, getTargetOperator("|="), node, trees);

		case PLUS_ASSIGNMENT:
			if (lo.getResultClass() == String.class || ro.getResultClass() == String.class) {
				return processCompoundAssignmentOperator(lo, ro, getTargetOperator("string+="), node, trees)
					.updateExpressionPriority(getOperatorPriority("string+="));
			}
			return processCompoundAssignmentOperator(lo, ro, getTargetOperator("+="), node, trees);

		case REMAINDER_ASSIGNMENT:
			return processCompoundAssignmentOperator(lo, ro, getTargetOperator("%="), node, trees);

		case RIGHT_SHIFT_ASSIGNMENT:
			return processCompoundAssignmentOperator(lo, ro, getTargetOperator(">>="), node, trees);

		case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
			return processCompoundAssignmentOperator(lo, ro, getTargetOperator(">>>="), node, trees);

		case XOR_ASSIGNMENT:
			return processCompoundAssignmentOperator(lo, ro, getTargetOperator("^="), node, trees);

		default:
			throw new RuntimeException("unexpected assignment operator");
		}
	}

	@Override
	public VisitResult		visitUnary(UnaryTree node, Trees trees)
	{
		VisitResult expr = visitExpression(node.getExpression(), trees);
		switch (node.getKind()) {
		case BITWISE_COMPLEMENT:
			return processUnaryOperator(expr, getTargetOperator("unary~"), node, trees);

		case LOGICAL_COMPLEMENT:
			return processUnaryOperator(expr, getTargetOperator("unary!"), node, trees);

		case POSTFIX_DECREMENT:
			return processUnaryOperator(expr, getTargetOperator("post--"), node, trees);

		case POSTFIX_INCREMENT:
			return processUnaryOperator(expr, getTargetOperator("post++"), node, trees);

		case PREFIX_DECREMENT:
			return processUnaryOperator(expr, getTargetOperator("pre--"), node, trees);

		case PREFIX_INCREMENT:
			return processUnaryOperator(expr, getTargetOperator("pre++"), node, trees);

		case UNARY_MINUS:
			return processUnaryOperator(expr, getTargetOperator("unary-"), node, trees);

		case UNARY_PLUS:
			return processUnaryOperator(expr, getTargetOperator("unary+"), node, trees);

		default:
			throw new RuntimeException("unknown kind "+node.getKind()+" on BinaryTree");
		}
	}

	@Override
	public VisitResult		visitBinary(BinaryTree node, Trees trees)
	{
		VisitResult result;
		VisitResult lo = visitExpression(node.getLeftOperand(), trees);
		Class<?> lc = lo.getResultClass();
		VisitResult ro = visitExpression(node.getRightOperand(), trees);
		Class<?> rc = ro.getResultClass();
		switch (node.getKind()) {
		case AND:
			return updateNumericResult(processBinaryOperator(lo, ro, getTargetOperator("&"), node, trees), lo, ro);

		case CONDITIONAL_AND:
			return processBinaryOperator(lo, ro, getTargetOperator("&&"), node, trees)
				.updateEmptyResultClass(boolean.class);

		case CONDITIONAL_OR:
			return processBinaryOperator(lo, ro, getTargetOperator("||"), node, trees)
				.updateEmptyResultClass(boolean.class);

		case DIVIDE:
			result = processBinaryOperator(lo, ro, getTargetOperator("/"), node, trees);
			updateNumericResult(result, lo, ro);
			if (isClassIntegerish(lo.getResultClass()) && isClassIntegerish(ro.getResultClass()))
				return surroundIntegerDivide(result);
			return result;

		case EQUAL_TO:
			if (ro.getResultIndicator() == VisitResult.RI_Null) {
				return processCompareNull(lo, ro, getTargetOperator("=="), node, trees)
					.updateEmptyResultClass(boolean.class);
			}
			else {
				return processBinaryOperator(lo, ro, getTargetOperator("=="), node, trees)
					.updateEmptyResultClass(boolean.class);
			}

		case GREATER_THAN:
			return processBinaryOperator(lo, ro, getTargetOperator(">"), node, trees)
				.updateEmptyResultClass(boolean.class);

		case GREATER_THAN_EQUAL:
			return processBinaryOperator(lo, ro, getTargetOperator(">="), node, trees)
				.updateEmptyResultClass(boolean.class);

		case LEFT_SHIFT:
			return updateNumericResult(processBinaryOperator(lo, ro, getTargetOperator("<<"), node, trees), lo, ro);

		case LESS_THAN:
			return processBinaryOperator(lo, ro, getTargetOperator("<"), node, trees)
				.updateEmptyResultClass(boolean.class);

		case LESS_THAN_EQUAL:
			return processBinaryOperator(lo, ro, getTargetOperator("<="), node, trees)
				.updateEmptyResultClass(boolean.class);

		case MINUS:
			if (lc == char.class || lc == Character.class)
				lo.prependString("(int)(").appendString(")");
			if (rc == char.class || rc == Character.class)
				ro.prependString("(int)(").appendString(")");
			return updateNumericResult(processBinaryOperator(lo, ro, getTargetOperator("-"), node, trees), lo, ro);

		case MULTIPLY:
			return updateNumericResult(processBinaryOperator(lo, ro, getTargetOperator("*"), node, trees), lo, ro);

		case NOT_EQUAL_TO:
			if (ro.getResultIndicator() == VisitResult.RI_Null) {
				return processCompareNotNull(lo, ro, getTargetOperator("!="), node, trees)
					.updateEmptyResultClass(boolean.class);
			}
			else {
				return processBinaryOperator(lo, ro, getTargetOperator("!="), node, trees)
					.updateEmptyResultClass(boolean.class);
			}

		case OR:
			return updateNumericResult(processBinaryOperator(lo, ro, getTargetOperator("|"), node, trees), lo, ro);

		case PLUS:
			if (lc == String.class || rc == String.class) {
				return processBinaryOperator(lo, ro, getTargetOperator("string+"), node, trees)
					.updateEmptyResultClass(String.class);
			}
			else {
				if (lc == char.class || lc == Character.class)
					lo.prependString("(int)(").appendString(")");
				if (rc == char.class || rc == Character.class)
					ro.prependString("(int)(").appendString(")");
			}
			return updateNumericResult(processBinaryOperator(lo, ro, getTargetOperator("+"), node, trees), lo, ro);

		case REMAINDER:
			return updateNumericResult(processBinaryOperator(lo, ro, getTargetOperator("%"), node, trees), lo, ro);

		case RIGHT_SHIFT:
			return updateNumericResult(processBinaryOperator(lo, ro, getTargetOperator(">>"), node, trees), lo, ro);

		case UNSIGNED_RIGHT_SHIFT:
			return updateNumericResult(processBinaryOperator(lo, ro, getTargetOperator(">>>"), node, trees), lo, ro);

		case XOR:
			return updateNumericResult(processBinaryOperator(lo, ro, getTargetOperator("^"), node, trees), lo, ro);

		default:
			throw new RuntimeException("Unknown kind "+node.getKind()+" on BinaryTree");
		}
	}

	@Override
	public VisitResult		visitConditionalExpression(ConditionalExpressionTree node, Trees trees)
	{
		return node.getCondition().accept(this, trees).appendString(" ? ").appendReplaceType(node.getTrueExpression().accept(this, trees)).appendString(" : ").appendUpdateEmptyType(node.getFalseExpression().accept(this, trees));
	}

	@Override
	public VisitResult		visitParenthesized(ParenthesizedTree node, Trees trees)
	{
		ExpressionTree expr = node.getExpression();
		if (expr instanceof MethodInvocationTree || expr instanceof IdentifierTree || expr instanceof TypeCastTree)
			return super.visitParenthesized(node, trees);
		return surroundParentheses(super.visitParenthesized(node, trees));
	}

	@Override
	public VisitResult		visitNewClass(NewClassTree node, Trees trees)
	{
		Class<?> typeClass = resolveType(node.getIdentifier());
		BoundType boundType = resolveBoundType(node.getIdentifier());
		List<VisitResult> arguments = processExpressionList(node.getArguments(), trees);
		Constructor<?> constructor = typeClass.isInterface() ? lookupConstructor(Object.class, new LinkedList<VisitResult>()) : lookupConstructor(typeClass, arguments);

		LinkedHashMap<String, BoundType> callerBounds = new LinkedHashMap<>();
		if (node.getTypeArguments() != null && node.getTypeArguments().size() > 0) {
			for (int i = 0; i < node.getTypeArguments().size(); ++i) {
				callerBounds.put(typeClass.getTypeParameters()[i].getName(), resolveBoundType(node.getTypeArguments().get(i)));
			}
		}

		if (boundType.getTypeArguments().length == 0 && typeClass.getTypeParameters().length != 0) {
			boundType = BoundTypeResolver.resolveClassBasicBounds(typeClass);
		}

		return updateConstructorGenericResult(getClassAdapter(typeClass).processNewInstance(typeClass, constructor, arguments, node, trees), constructor, boundType, callerBounds, arguments)
			.updateEmptyResultIndicator(VisitResult.RI_NewInstance);
	}

	@Override
	public VisitResult		visitNewArray(NewArrayTree node, final Trees trees)
	{
		Class<?> elementClassName = node.getType() != null ? resolveType(node.getType()) : null;
		Class<?> elementClass = elementClassName == null ? null : elementClassName;
		BoundType elementBound = elementClassName == null ? null : resolveBoundType(node.getType());
		List<VisitResult> dimensions = node.getDimensions().stream().map((ExpressionTree et) -> scan(et, trees)).collect(Collectors.toList());
		List<VisitResult> initializers = node.getInitializers() != null ? node.getInitializers().stream().map((ExpressionTree et) -> et != null ? scan(et, trees) : null).collect(Collectors.toList()) : null;
		Class<?> arrayClass = elementClass != null ? Array.newInstance(elementClass, 0).getClass() : void.class;
		return getClassAdapter(arrayClass).processNewArray(elementClass, dimensions, initializers, node, trees)
			.updateEmptyResultBound(BoundType.createArray(elementBound == null ? BoundType.createRaw(Object.class) : elementBound));
	}

	@Override
	public VisitResult		visitArrayAccess(ArrayAccessTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder();
		VisitResult arrayResult = scan(node.getExpression(), trees);
		VisitResult indexResult = scan(node.getIndex(), trees);
		return getClassAdapter(arrayResult.getResultClass()).processArrayAccess(arrayResult, indexResult, node, trees)
			.updateEmptyResultBound(arrayResult.getResultBound().getElementBound())
			.updateEmptyExpressionPriority(getOperatorPriority("[]"));
	}

	@Override
	public VisitResult		visitMemberSelect(MemberSelectTree node, Trees trees)
	{
		String member = node.getIdentifier().toString();
		VisitResult left = visitExpression(node.getExpression(), trees);
		if (left == null)
			throw new NullPointerException("left of MemberSelect is null");
		Class<?> lc = left.getResultClass();
		getLogger().debug("visiting member "+member+" on "+left.getContent()+" ("+lc+")");
		if (left.getResultIndicator() == VisitResult.RI_PackageIdentifier) {
			String newpath = left.getContent()+"."+member;
			IdentifierDef idef;
			if ((idef = findIdentifier(newpath)) != null) {
				if (idef.type == IdentifierDef.IdentifierType.IT_Class)
					left = new VisitResult(left.getContent())
						.updateResultBound(BoundType.createRaw(loadClass(newpath)))
						.updateResultIndicator(VisitResult.RI_ClassIdentifier);
			}
			else {
				try {
					left = new VisitResult(left.getContent())
						.updateResultBound(BoundType.createRaw(Class.forName(newpath)))
						.updateResultIndicator(VisitResult.RI_ClassIdentifier);
				}
				catch (ClassNotFoundException e) {
					// ignore, continue with package
				}
			}
			return left.updateContent(newpath);
		}
		else if (member.equals("class")) {
			// continue with current currentResult
			return processClassMemberSelect(left, node, trees)
				.updateEmptyExpressionPriority(getPriorityMember());
		}
		else if (lc.isArray() && member.equals("length")) {
			return getClassAdapter(lc).processFieldAccess(left, null, node, trees)
				.updateEmptyResultBound(BoundType.createRaw(int.class))
				.updateEmptyExpressionPriority(getPriorityMember());
		}
		else {
			try {
				Field field = lookupFieldInternal(lc, member);
				return updateFieldGenericResult(getClassAdapter(left.getResultClass()).processFieldAccess(left, field, node, trees), field, left.getResultBound())
					.updateEmptyExpressionPriority(getPriorityMember());
			}
			catch (NoSuchFieldException e) {
				try {
					return left.appendString(".").appendString(member)
						.updateResultClass(loadClassInternal(lc.getName()+"$"+member))
						.updateEmptyExpressionPriority(getPriorityMember());
				}
				catch (ClassNotFoundException e1) {
					throw new RuntimeException(e.toString()+", "+e1.toString(), e);
				}
			}
		}
	}

	@Override
	public VisitResult		visitInstanceOf(InstanceOfTree node, Trees trees)
	{
		Class<?> clazz = resolveType(node.getType());
		return this.getClassAdapter(clazz).processInstanceOf(clazz, node, trees)
			.updateEmptyResultBound(BoundType.createRaw(boolean.class));
	}

	@Override
	public VisitResult		visitTypeCast(TypeCastTree node, Trees trees)
	{
		VisitResult r = scan(node.getExpression(), trees);
		BoundType boundType = resolveBoundType(node.getType());
		return this.getClassAdapter(boundType.getRawClass())
			.processTypeCast(boundType.getRawClass(), r, node, trees)
			.updateEmptyResultBound(boundType)
			.updateEmptyExpressionPriority(getPriorityCast());
	}

	@Override
	public VisitResult		visitEnhancedForLoop(EnhancedForLoopTree node, Trees trees)
	{
		pushIdentifiers();
		try {
			return processEnhancedForLoop(node, trees);
		}
		finally {
			popIdentifiers();
		}
	}

	@Override
	public VisitResult		visitLambdaExpression(LambdaExpressionTree node, Trees trees)
	{
		CatchingIdentifierStore caught = new CatchingIdentifierStore(this.getIdentifiers());
		this.pushIdentifiersSpecial(caught);
		this.pushIdentifiers();
		try {
			return processLambdaExpression(caught, node, trees);
		}
		finally {
			this.popIdentifiers();
			this.popIdentifiers();
		}
	}

	@Override
	public VisitResult		visitErroneous(ErroneousTree node, Trees trees)
	{
		throw new RuntimeException(node.getErrorTrees().toString());
		//return super.visitErroneous(node, trees);
	}

	@Override
	public VisitResult		visitSynchronized(SynchronizedTree node, Trees trees)
	{
		return super.visitSynchronized(node, trees);
	}

	@Override
	public VisitResult		visitParameterizedType(ParameterizedTypeTree node, Trees trees)
	{
		return super.visitParameterizedType(node, trees);
	}

	@Override
	public VisitResult		visitArrayType(ArrayTypeTree node, Trees trees)
	{
		return super.visitArrayType(node, trees);
	}

	@Override
	public VisitResult		visitPrimitiveType(PrimitiveTypeTree node, Trees trees)
	{
		return new VisitResult(node.toString())
			.updateResultClass(loadClass(node.toString()));
	}

	@Override
	public VisitResult		visitTypeParameter(TypeParameterTree node, Trees trees)
	{
		return super.visitTypeParameter(node, trees);
	}

	@Override
	public VisitResult		visitWildcard(WildcardTree node, Trees trees)
	{
		return super.visitWildcard(node, trees);
	}

	@Override
	public VisitResult		visitOther(Tree node, Trees trees)
	{
		throw new RuntimeException("unhandled visit");
		//return super.visitOther(node, trees);
	}

	@Override
	public VisitResult		reduce(VisitResult r1, VisitResult r2)
	{
		if (r1 == null || r1.isNull())
			return r2;
		else if (r2 == null || r2.isNull())
			return r1;
		return r2.appendReplaceType(r1);
	}

	protected Logger		logger;

	protected ProcessingEnvironment	processingEnv;

	protected TransClassAdapter	defaultClassAdapter;
	protected TransClassAdapter	defaultPrimitiveArrayClassAdapter;
	protected TransClassAdapter	defaultObjectsArrayClassAdapter;
	protected Map<String, TransClassAdapter> classAdapters;
	protected Map<String, String>	nativeClassMap;

	protected Map<String, TargetOperator> targetOperators;

	protected CompilationUnitTree	compilationUnitTree;
	byte[]				fileContent;

	protected ClassContext		currentContext;
	protected ClassTree		currentClassTree;
	protected Class<?>		currentClass;
	protected BoundType		currentBoundType;
	protected Class<?>		currentSuperClass;
	protected String		currentPackage;
	protected String		currentClassName;
	protected String		currentFullName;
	protected String		currentPathName;
	protected StringBuilder		currentStaticInit;
	protected StringBuilder		currentInstanceInit;

	protected boolean		isInterface;
	protected boolean		isAnnotation;
	protected Map<String, String>	annotationDefaults;

	protected File			currentFile;

	Stack<CodeContext>		contextStack;
	IdentifierStore			identifierStack;

	protected Map<String, Class<?>>	primitives;
	protected Map<String, Class<?>>	scalars;
	protected Map<String, String>	arrayIds;
	protected Map<String, String>	codeOutput = new HashMap<String, String>();

	public static final String	FIX_STRING = "(=f_I_x=)";
	public static final String	XIF_STRING = "(=x_I_f=)";

	protected Pattern		replaceTabs = Pattern.compile("^\t+", Pattern.MULTILINE);

	protected Pattern		prependCommentStars = Pattern.compile("^ ?", Pattern.MULTILINE);
}
