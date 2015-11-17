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
package net.dryuf.trans.php;

import javax.annotation.processing.ProcessingEnvironment;
import javax.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssertTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.Trees;

import net.dryuf.trans.BoundType;
import net.dryuf.trans.BoundTypeResolver;
import net.dryuf.trans.CLikeTransVisitor;
import net.dryuf.trans.CatchingIdentifierStore;
import net.dryuf.trans.FormatUtil;
import net.dryuf.trans.IdentifierDef;
import net.dryuf.trans.LambdaWrap;
import net.dryuf.trans.TargetOperator;
import net.dryuf.trans.TransClassAdapter;
import net.dryuf.trans.TransUtil;
import net.dryuf.trans.VisitResult;
import net.dryuf.trans.php.adapter.BasicPhpAdapter;
import org.apache.commons.lang3.StringUtils;


public class PhpTransVisitor extends CLikeTransVisitor
{
	public				PhpTransVisitor(ProcessingEnvironment pe)
	{
		super(pe);
	}

	public void			init()
	{
		if (classAdapters == null) {
			this.classAdapters = new HashMap<String, TransClassAdapter>();
			for (Map.Entry<String, Class<TransClassAdapter>> entry: defaultClassAdapterMappings.entrySet()) {
				TransClassAdapter adapter = null;
				try {
					adapter = entry.getValue().newInstance();
				}
				catch (Exception ex) {
					throw new RuntimeException(ex);
				}
				adapter.setTransVisitor(this);
				adapter.init();
				classAdapters.put(entry.getKey(), adapter);
			}
		}

		defaultClassAdapter = classAdapters.get("");
		defaultPrimitiveArrayClassAdapter = classAdapters.get("[primitive]");
		defaultObjectsArrayClassAdapter = classAdapters.get("[objects]");

		if (nativeClassMap == null) {
			this.nativeClassMap = new HashMap<String, String>();
			for (Map.Entry<String, String> entry: defaultNativeClassMappings.entrySet()) {
				nativeClassMap.put(entry.getKey(), entry.getValue());
			}
		}

		if (targetOperators== null) {
			targetOperators = TransUtil.createLinkedHashMap(
					"literal",			new TargetOperator("", 05, 0),
					"()",				new TargetOperator("()", 05, 0),
					"member",			new TargetOperator("->", 10, -1),
					"static",			new TargetOperator("::", 10, -1),
					"call",				new TargetOperator("()", 10, -1),
					"new",				new TargetOperator("new", 15, -1),
					"[]",				new TargetOperator("[]", 15, -1),
					"pre++",			new TargetOperator("++", 20, -1),
					"pre--",			new TargetOperator("--", 20, -1),
					"post++",			new TargetOperator("\b++", 20, -1),
					"post--",			new TargetOperator("\b--", 20, -1),
					"cast",				new TargetOperator("()", 25, -1),
					"unary~",			new TargetOperator("~", 30, -1),
					"unary+",			new TargetOperator("+", 30, -1),
					"unary-",			new TargetOperator("-", 30, -1),
					"instanceof",			new TargetOperator(" instanceof ", 35, -1),
					"unary!",			new TargetOperator("!", 40, -1),
					"*",				new TargetOperator("*", 45, -1),
					"/",				new TargetOperator("/", 45, -1),
					"%",				new TargetOperator("%", 45, -1),
					"+",				new TargetOperator("+", 50, -1),
					"string+",			new TargetOperator(".", 50, -1),
					"-",				new TargetOperator("-", 50, -1),
					"<<",				new TargetOperator("<<", 55, -1),
					">>",				new TargetOperator(">>", 55, -1),
					"<",				new TargetOperator(" < ", 60, -1),
					"<=",				new TargetOperator(" <= ", 60, -1),
					">",				new TargetOperator(" > ", 60, -1),
					">=",				new TargetOperator(" >= ", 60, -1),
					"==",				new TargetOperator(" == ", 60, -1),
					"php===",			new TargetOperator(" !== ", 60, -1),
					"php!==",			new TargetOperator(" === ", 60, -1),
					"!=",				new TargetOperator(" != ", 60, -1),
					"&",				new TargetOperator("&", 65, -1),
					"^",				new TargetOperator("^", 66, -1),
					"|",				new TargetOperator("|", 67, -1),
					"&&",				new TargetOperator(" && ", 70, -1),
					"||",				new TargetOperator(" || ", 71, -1),
					"?:",				new TargetOperator("?:", 75, -1),
					"=",				new TargetOperator(" = ", 80, 1),
					"*=",				new TargetOperator(" *= ", 80, 1),
					"/=",				new TargetOperator(" /= ", 80, 1),
					"%=",				new TargetOperator(" %= ", 80, 1),
					"+=",				new TargetOperator(" += ", 80, 1),
					"string+=",			new TargetOperator(" .= ", 80, 1),
					"-=",				new TargetOperator(" -= ", 80, 1),
					"&=",				new TargetOperator(" &= ", 80, 1),
					"|=",				new TargetOperator(" |= ", 80, 1),
					"^=",				new TargetOperator(" ^= ", 80, 1),
					"<<=",				new TargetOperator(" <<= ", 80, 1),
					">>=",				new TargetOperator(" >>= ", 80, 1),
					"=>",				new TargetOperator(" => ", 85, -1),
					"nat:and",			new TargetOperator(" and ", 90, -1),
					"nat:xor",			new TargetOperator(" xor ", 91, -1),
					"nat:or",			new TargetOperator(" or ", 92, -1),
					",",				new TargetOperator(", ", 95, -1)
			);
		}

		super.init();
	}

	@Override
	public String			getSuffix()
	{
		return "php";
	}

	@Override
	public String			formatVariableName(String name)
	{
		return "$"+name;
	}

	@Override
	public String			formatClassStaticPrefix(String path)
	{
		return path+"::";
	}

	@Override
	public String			formatThisStaticPrefix(String path)
	{
		return "self::";
	}

	@Override
	public String			formatClassStaticField(String path, String name)
	{
		return path+"::$"+name;
	}

	@Override
	public String			formatThisStaticField(String name)
	{
		return "self::$"+name;
	}

	@Override
	public String			formatThisInstanceField(String name)
	{
		return "$this->"+name;
	}

	@Override
	public String			formatPrimitiveDefault(Class<?> clazz)
	{
		if (clazz == boolean.class) {
			return "false";
		}
		else if (clazz == char.class) {
			return "\"\\x00\"";
		}
		else if (clazz == float.class || clazz == double.class) {
			return "0.0";
		}
		else {
			return "0";
		}
	}

	@Override
	public VisitResult		formatClassAnnotations(ClassTree classTree, List<? extends AnnotationTree> nodes, Trees trees)
	{
		return processAnnotations(nodes, trees);
	}

	@Override
	public VisitResult		formatMethodAnnotations(MethodTree methodTree, List<? extends AnnotationTree> nodes, Trees trees)
	{
		StringBuilder sb = new StringBuilder("/**\n");
		if (methodTree.getReturnType() != null) {
			sb.append("@\\net\\dryuf\\core\\Type(type = '");
			sb.append(formatFullType(methodTree.getReturnType()));
			sb.append("')\n");
		}
		FormatUtil.appendSbSafe(sb, super.processAnnotations(nodes, trees).getContent());
		sb.append("*/\n");
		return new VisitResult(sb);
	}

	@Override
	public VisitResult		formatFieldAnnotations(VariableTree variableTree, List<? extends AnnotationTree> nodes, Trees trees)
	{
		StringBuilder sb = new StringBuilder("/**\n");
		sb.append("@\\net\\dryuf\\core\\Type(type = '");
		sb.append(formatFullType(variableTree.getType()));
		if ((convertModifiers(variableTree.getModifiers().getFlags())&Modifier.TRANSIENT) != 0)
			sb.append(", isTransient = true");
		sb.append("')\n");
		FormatUtil.appendSbSafe(sb, super.processAnnotations(nodes, trees).getContent());
		sb.append("*/\n");
		return new VisitResult(sb);
	}

	@Override
	public String			formatThis()
	{
		return "$this";
	}

	@Override
	public String			formatSuper()
	{
		return "parent::";
	}

	public String			formatConstructorName(Class<?> clazz)
	{
		return "__construct";
	}

	@Override
	public String			translateClassIdentifier(String className)
	{
		if (className == null)
			throw new IllegalArgumentException("className is null");
		return "\\"+className.replace('.', '\\').replace('$', '\\');
	}

	@Override
	public String			translateClassString(String className)
	{
		return className.replace('.', '\\').replace('$', '\\');
	}

	@Override
	public String			translateResultSelect(VisitResult result, boolean isStatic)
	{
		switch (result.getResultIndicator()) {
		case VisitResult.RI_Super:
			return "parent::";

		case VisitResult.RI_ClassIdentifier:
			return translateClassIdentifier(result.getContent())+"::";

		case VisitResult.RI_DotClass:
			return result.getFinal(); //"'"+translateClassString(transformClass(result.getContent().replaceAll("\\.class$", "")))+"'";

		default:
			return isStatic ? (translateClassIdentifier(result.getResultClass().getName())+"::") : ((result.isNull() ? "$this" : result.getContent())+"->");
		}
	}

	@Override
	public VisitResult		processClass(ClassTree classTree, Trees trees)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<?php\n\n").append("namespace "+this.currentPathName.replaceAll("\\.\\w+$", "").replace(".", "\\")+";\n\n\n");

		sb.append(formatDocComment(getDocCommentForTree(classTree, trees)));
		FormatUtil.appendSbSafe(sb, formatClassAnnotations(classTree, classTree.getModifiers().getAnnotations(), trees).getContent());

		{
			StringBuilder smodifiers = new StringBuilder();
			int modifiers = convertModifiers(classTree.getModifiers().getFlags());
			if (Modifier.isAbstract(modifiers))
				smodifiers.append("abstract ");
			isAnnotation = false;
			isInterface = false;
			Pattern interfacePattern = Pattern.compile(".*\\s+(@?)interface\\s+"+this.currentClassName+"\\W.*", Pattern.DOTALL);
			Matcher matcher = interfacePattern.matcher(classTree.toString());
			if (matcher.matches()) {
				if ("@".equals(matcher.group(1))) {
					isAnnotation = true;
					annotationDefaults = new LinkedHashMap<String, String>();
				}
				else
					isInterface = true;
			}
			sb.append(smodifiers+(isInterface ? "interface " : "class ")+this.currentClassName);
			if (isAnnotation) {
				sb.append(" extends \\net\\dryuf\\core\\php\\PhpAnnotationBase ");
			}
		}

		List<BoundType> typeBounds = new LinkedList<>();
		for (TypeParameterTree typed: classTree.getTypeParameters()) {
			BoundType extend = typed.getBounds().iterator().hasNext() ? resolveBoundType(typed.getBounds().iterator().next()) : BoundType.createRaw(Object.class);
			addIdentifier(IdentifierDef.createTyped(typed.getName().toString(), extend));
			typeBounds.add(extend); // TODO bound
		}
		this.currentBoundType = BoundType.createTyped(this.currentClass, typeBounds.toArray(new BoundType[0]));

		{
			Tree extendsTree = classTree.getExtendsClause();
			if (extendsTree != null) {
				Class<?> extend = convertExtends(extendsTree);
				String extendString = null;
				if (extend != null) {
					addIdentifier(IdentifierDef.createSuper(BoundTypeResolver.resolveInheritedBounds(this.currentClass.getGenericSuperclass(), this.currentBoundType)));
					extendString = getClassAdapter(extend).transformClass(extend.getName());
				}
				if (extendString != null) {
					sb.append(" extends "+translateClassIdentifier(extendString));
				}
				else if (!isAnnotation && !isInterface) {
					sb.append(" extends "+translateClassIdentifier("net.dryuf.core.Object"));
				}
			}
			else if (!isAnnotation && !isInterface && !this.currentClass.isEnum()) {
				sb.append(" extends "+translateClassIdentifier("net.dryuf.core.Object"));
			}
		}

		{
			boolean first = true;
			for (Tree implementsTree: classTree.getImplementsClause()) {
				Class<?> implement = convertImplements(implementsTree);
				String implementString = null;
				if (implement != null)
					implementString = getClassAdapter(implement).transformClass(implement.getName());
				if (implementString != null) {
					sb.append((first ? isInterface ? " extends " : " implements " : ", ")+translateClassIdentifier(implementString));
					first = false;
				}
			}
		}
		sb.append("\n{\n");

		addIdentifiersSupers(this.currentClass);
		/*
		   for (Tree member: classTree.getMembers()) {
		   switch (member.getKind()) {
		   case VARIABLE:
		   addIdentifier(IdentifierDef.createField(((VariableTree)member).getName().toString(), convertModifiers(((VariableTree)member).getModifiers().getFlags()), null));
		   break;
		   case METHOD:
		   addIdentifier(IdentifierDef.createMethod(((MethodTree)member).getName().toString(), convertModifiers(((MethodTree)member).getModifiers().getFlags()), null));
		   break;
		   case CLASS:
		   addIdentifier(IdentifierDef.createClass(((ClassTree)member).getSimpleName().toString(), this.currentFullName+((ClassTree)member).getSimpleName().toString()));
		   break;
		   default:
		   break;
		   }
		   }
		 */
		for (Method m: this.currentClass.getDeclaredMethods()) {
			addIdentifier(IdentifierDef.createMethod(m.getName(), m.getModifiers(), BoundType.createRaw(m.getReturnType())));
		}
		for (Field f: this.currentClass.getDeclaredFields()) {
			addIdentifier(IdentifierDef.createField(f.getName(), f.getModifiers(), BoundType.createRaw(f.getType())));
		}
		for (Class<?> c: this.currentClass.getDeclaredClasses()) {
			addIdentifier(IdentifierDef.createClass(c.getSimpleName(), BoundType.createRaw(c)));
		}

		VisitResult r = scan(classTree.getMembers(), trees);
		StringBuilder inner = new StringBuilder(r == null ? "" : r.getContent());
		FormatUtil.forceSbOneNl(this, inner);
		if (this.isAnnotation && !annotationDefaults.isEmpty()) {
			inner.append("\npublic function\t\t\t__construct($args)\n{\n\tparent::__construct(array_merge(array(\n");
			for (Map.Entry<String, String> entry: annotationDefaults.entrySet()) {
				inner.append("\t\t\t").append(FormatUtil.tabalign(new StringBuilder().append("'").append(entry.getKey()).append("'"), 32).append(" => ").append(entry.getValue()).append(",\n"));
			}
			inner.append("\t\t),\n\t\t$args\n\t));\n}\n\n");
		}
		if (this.currentInstanceInit.length() != 0) {
			FormatUtil.indentSb(currentInstanceInit);
			currentInstanceInit.insert(0, "\n");
			String old = inner.toString();
			inner = new StringBuilder(StringUtils.replace(old, "\"\"\"__INSTANCE__INIT__\"\"\"", currentInstanceInit.toString()));
			if (inner.toString().equals(old)) {
				// we got no constructor, let define one
				inner.append("\npublic function\t\t\t__construct()\n{\n").append(currentInstanceInit).append("\t\tparent::__construct();\n\t}\n");
			}
		}
		else {
			inner = new StringBuilder(StringUtils.replace(inner.toString(), "\"\"\"__INSTANCE__INIT__\"\"\"", ""));
		}
		if (this.currentStaticInit.length() != 0) {
			inner.append("\npublic static function\t\t_initManualStatic()\n{\n");
			inner.append(FormatUtil.indentSb(this.currentStaticInit));
			inner.append("}\n\n");
		}
		FormatUtil.indentSb(inner);
		sb.append(inner);
		sb.append("};\n\n");
		if (this.currentStaticInit.length() != 0) {
			sb.append(translateClassIdentifier(this.currentFullName)).append("::_initManualStatic();\n\n");
		}
		sb.append("\n");
		sb.append("?>\n");

		this.codeOutput.put(this.currentFullName, sb.toString());

		return VisitResult.createEmpty();
	}

	@Override
	public VisitResult		processEnhancedForLoop(EnhancedForLoopTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder("foreach (");
		sb.append(scan(node.getExpression(), trees).getContent());
		sb.append(" as ");
		scan(node.getVariable(), trees);
		sb.append("$").append(node.getVariable().getName().toString());
		sb.append(")");
		{
			String s = node.getStatement() != null ? scan(node.getStatement(), trees).getContent() : ";\n";
			if (!s.equals(";\n")) {
				sb.append(FormatUtil.indentStatementString(s));
			}
			else {
				sb.append(" ").append(s);
			}
		}
		return new VisitResult(sb.toString());
	}

	@Override
	public VisitResult		processLambdaExpression(CatchingIdentifierStore caught, LambdaExpressionTree node, Trees trees)
	{
		List<BoundType> signature = new LinkedList<>();
		StringBuilder sb = new StringBuilder("function (");
		{
			int count = 0;
			for (VariableTree par: node.getParameters()) {
				if (count++ != 0)
					sb.append(", ");
				BoundType argBound = this.resolveBoundType(par.getType());
				signature.add(argBound);
				this.addIdentifier(IdentifierDef.createVariable(par.getName().toString(), argBound));
				sb.append("$").append(node.getParameters().get(0).getName());
			}
		}
		sb.append(") ");
		VisitResult body = this.scan(node.getBody(), trees);
		if (node.getBodyKind() == LambdaExpressionTree.BodyKind.EXPRESSION) {
			body.prependString("{ return ").appendString("; }");
		}
		else {
			// no change, just reuse bound result from body
		}
		StringBuilder uses = new StringBuilder();
		for (Map.Entry<String, IdentifierDef> idef: caught.getCatched().entrySet()) {
			switch (idef.getValue().getType()) {
			case IT_Variable:
				uses.append("$").append(idef.getKey()).append(", ");
				break;
			default:
				break;
			}
		}
		if (uses.length() > 0) {
			uses.replace(uses.length()-2, uses.length(), "");
			sb.append("use (").append(uses).append(") ");
		}
		sb.append(body.getContent());
		signature.add(body.getResultBound());
		return new VisitResult(sb)
			.updateResultBound(BoundType.createTyped(LambdaWrap.class, signature.toArray(new BoundType[signature.size()])))
			.updateResultIndicator(VisitResult.RI_Lambda);
	}

	@Override
	public VisitResult		processAnnotations(List<? extends AnnotationTree> nodes, Trees trees)
	{
		VisitResult r = super.processAnnotations(nodes, trees);
		if (!r.isNull()) {
			r.prependString("/**\n").appendString("*/\n");
		}
		return r;
	}

	@Override
	public VisitResult		processMethodInvocation(VisitResult path, Method method, List<VisitResult> arguments, MethodInvocationTree node, Trees trees)
	{
		if (path.getResultIndicator() == VisitResult.RI_NewInstance) {
			path.prependString("(");
			path.appendString(")");
		}
		return super.processMethodInvocation(path, method, arguments, node, trees);
	}

	@Override
	public VisitResult		checkCommonMethodInvocation(VisitResult path, Method method, List<VisitResult> arguments, MethodInvocationTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder();
		if (method.getName().equals("getClass") && method.getParameterTypes().length == 0) {
			sb.append("get_class(");
			sb.append(!path.isNull() ? path.getContent() : "$this");
			sb.append(")");
			return new VisitResult(sb);
		}
		else if (method.getName().equals("toString") && method.getParameterTypes().length == 0) {
			if (path.getResultIndicator() == VisitResult.RI_Super) {
				sb.append("parent::toString()");
			}
			else {
				sb.append("strval(");
				sb.append(!path.isNull() ? path.getContent() : "$this");
				sb.append(")");
			}
			return new VisitResult(sb);
		}
		else if (method.getName().equals("equals") && method.getParameterTypes().length == 1) {
			sb.append("\\net\\dryuf\\core\\Dryuf::equalsObject(");
			sb.append(!path.isNull() ? path.getContent() : "$this");
			sb.append(", ");
			appendArguments(sb, method, arguments);
			sb.append(")");
			return new VisitResult(sb);
		}
		else if (method.getName().equals("hashCode") && method.getParameterTypes().length == 0) {
			sb.append("\\net\\dryuf\\core\\Dryuf::hashCodeObject(");
			sb.append(!path.isNull() ? path.getContent() : "$this");
			sb.append(")");
			return new VisitResult(sb);
		}
		return null;
	}

	@Override
	public VisitResult		processMethodInvocationDefault(VisitResult path, Method method, List<VisitResult> arguments, MethodInvocationTree node, Trees trees)
	{
		VisitResult out;
		StringBuilder sb = new StringBuilder();
		if (java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
			sb.append(this.translateClassIdentifier(transformClass(path.getResultClass().getName()))).append("::");
		}
		else if ((out = checkCommonMethodInvocation(path, method, arguments, node, trees)) != null) {
			return out;
		}
		else {
			if (path.getResultIndicator() == VisitResult.RI_Super) {
				sb.append("parent::");
			}
			else {
				sb.append(path.getContent()).append("->");
			}
		}
		sb.append(method.getName());
		sb.append("(");
		appendArguments(sb, method, arguments);
		sb.append(")");
		return new VisitResult(sb);
	}

	@Override
	public VisitResult		processClassMemberSelect(VisitResult left, MemberSelectTree node, Trees trees)
	{
		return new VisitResult("'"+translateClassString(transformClass(left.getResultClass().getName()))+"'")
			.updateResultBound(BoundType.createTyped(Class.class, new BoundType[]{left.getResultBound()}))
			.updateEmptyExpressionPriority(getPriorityLiteral());
	}

	public VisitResult		processCompareNull(VisitResult lo, VisitResult ro, TargetOperator operator, BinaryTree node, Trees trees)
	{
		return new VisitResult("is_null("+lo.getContent()+")")
			.updateExpressionPriority(getPriorityCall());
	}

	public VisitResult		processCompareNotNull(VisitResult lo, VisitResult ro, TargetOperator operator, BinaryTree node, Trees trees)
	{
		return new VisitResult("!is_null("+lo.getContent()+")")
			.updateExpressionPriority(getOperatorPriority("unary!"));
	}

	@Override
	public VisitResult		surroundIntegerDivide(VisitResult result)
	{
		return result.prependString("intval(").appendString(")")
			.updateExpressionPriority(getOperatorPriority("call"));
	}

	@Override
	public VisitResult		visitAnnotation(AnnotationTree node, Trees trees)
	{
		Class<?> annoType = this.resolveType(node.getAnnotationType());
		VisitResult r = getClassAdapter(annoType).processAnnotation(annoType, node, trees);
		if (r != null)
			return r.appendString("\n");
		return r;
	}

	@SuppressWarnings("unchecked")
	@Override
	public VisitResult		visitMethod(MethodTree methodTree, Trees trees)
	{
		StringBuilder sb = new StringBuilder();
		pushIdentifiers();
		try {
			boolean isConstructor = false;
			if (isAnnotation) {
				Tree annoDefault = methodTree.getDefaultValue();
				if (annoDefault != null) {
					annotationDefaults.put(methodTree.getName().toString(), visitExpression((ExpressionTree)annoDefault, trees).getFinal());
				}
				return new VisitResult("public function\t\t\t"+methodTree.getName()+"() { return $this->__call(\""+methodTree.getName()+"\"); }\n");
			}
			getLogger().debug("visiting method "+methodTree.getName());
			List<? extends ExpressionTree> argumentDefaults = null;

			String name = methodTree.getName().toString();
			if (name.equals("<init>")) {
				name = "__construct";
				isConstructor = true;
			}

			for (AnnotationTree annoTree: methodTree.getModifiers().getAnnotations()) {
				if (resolveType(annoTree.getAnnotationType()) == net.dryuf.trans.meta.NoDynamic.class)
					return VisitResult.createNull();
			}
			for (AnnotationTree annoTree: methodTree.getModifiers().getAnnotations()) {
				if (resolveType(annoTree.getAnnotationType()) == net.dryuf.trans.meta.DynamicDefaults.class) {
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
			if (!isConstructor) {
				// the condition above is because cannot change access to constructor in subclass
				if (Modifier.isPublic(modifiers) && !isInterface) {
					sb.append("public ");
				}
				else if (Modifier.isProtected(modifiers) && !isInterface) {
					sb.append("protected ");
				}
				else if (Modifier.isPrivate(modifiers) && !isInterface) {
					sb.append("private ");
				}
			}
			if (Modifier.isStatic(modifiers))
				sb.append("static ");
			if (Modifier.isAbstract(modifiers) && !isInterface)
				sb.append("abstract ");
			sb.append("function");
			FormatUtil.tabalign(sb, 32);
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
					if (isConstructor) {
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
					if (isConstructor && paramList.isEmpty() && this.currentContext.clazz.isEnum()) {
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
					if (clazz.isArray() && findAnnotation(param.getModifiers(), net.dryuf.trans.meta.Out.class) != null) {
						sb.append("&");
					}
					else if (paramsAnnotationsHash[count].get(NotNull.class) != null) {
						String transformed = getClassAdapter(clazz).transformClass(clazz.getName());
						if (transformed != null)
							sb.append(translateClassIdentifier(transformed)).append(" ");
					}
					sb.append("$").append(paramName);
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
					if (isConstructor && !Pattern.compile("^\\{\\s+parent::__construct\\(.*$", Pattern.DOTALL).matcher(r).matches()) {
						r = r.replaceAll("^\\{", "{\n\tparent::__construct();");
					}
					if (isConstructor) {
						if (!r.startsWith("{"))
							throw new RuntimeException("body does not start with '{'");
						r = "{"+"\"\"\"__INSTANCE__INIT__\"\"\""+r.substring(1);
					}
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
	public VisitResult		visitAssert(AssertTree node, Trees trees)
	{
		return new VisitResult("if (!("+scan(node.getCondition(), trees).getContent()+")) throw new \\net\\dryuf\\core\\RuntimeException(\"assert\");\n");
	}

	@Override
	public VisitResult		visitBinary(BinaryTree node, Trees trees)
	{
		VisitResult lo = visitExpression(node.getLeftOperand(), trees);
		Class<?> lc = lo.getResultClass();
		VisitResult ro = visitExpression(node.getRightOperand(), trees);
		Class<?> rc = ro.getResultClass();
		switch (node.getKind()) {
		case MINUS:
			if (lc == char.class || lc == Character.class)
				lo.prependString("ord(").appendString(")");
			if (rc == char.class || rc == Character.class)
				ro.prependString("ord(").appendString(")");
			return updateNumericResult(processBinaryOperator(lo, ro, getTargetOperator("-"), node, trees), lo, ro);

		case PLUS:
			if (lc == String.class || rc == String.class) {
				return processBinaryOperator(lo, ro, getTargetOperator("string+"), node, trees)
					.updateEmptyResultClass(String.class);
			}
			else {
				if (lc == char.class || lc == Character.class)
					lo.prependString("ord(").appendString(")");
				if (rc == char.class || rc == Character.class)
					ro.prependString("ord(").appendString(")");
			}
			return updateNumericResult(processBinaryOperator(lo, ro, getTargetOperator("+"), node, trees), lo, ro);

		default:
			return super.visitBinary(node, trees);
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
		if (resultClass == Character.class) {
			char c = ((Character)node.getValue()).charValue();
			switch (c) {
			case 0x00: case 0x01: case 0x02: case 0x03: case 0x04: case 0x05: case 0x06: case 0x07: case 0x08: case 0x09: case 0x0b: case 0x0c: case 0x0d: case 0x0e: case 0x0f:
			case 0x10: case 0x11: case 0x12: case 0x13: case 0x14: case 0x15: case 0x16: case 0x17: case 0x18: case 0x19: case 0x1a: case 0x1b: case 0x1c: case 0x1d: case 0x1e: case 0x1f:
				r = "chr("+(int)c+")";
				break;

			case 0x0a:
				r = "\"\\n\"";
				break;

			case 0x5c:
				r = "\"\\\\\"";
				break;

			case 0x27:
				r = "\"'\"";
				break;

			default:
				r = "'"+c+"'";
				break;
			}
		}
		else if (resultClass == String.class) {
			r = node.getValue().toString();
			r = r.replace("\\", "\\\\").replace("\"", "\\\"");
			r = r.replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
			for (int i = 0; i < r.length(); i++) {
				if (r.charAt(i) < 32) {
					r = r.substring(0, i)+"\\x"+String.format("%02x", (int)r.charAt(i))+r.substring(i+1);
					i += 5;
				}
			}
			r = "\""+r.replace("$", "\\$")+"\"";
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

	@SuppressWarnings("unchecked")
	public static Map<String, Class<TransClassAdapter>> defaultClassAdapterMappings = TransUtil.createLinkedHashMap(
		"",						(Class<TransClassAdapter>)(Class<?>)BasicPhpAdapter.class,
		"[primitive]",					net.dryuf.trans.php.adapter.PrimitiveArrayPhpAdapter.class,
		"[objects]",					net.dryuf.trans.php.adapter.ObjectsArrayPhpAdapter.class,
		"byte",						net.dryuf.trans.php.adapter.BytePrimitivePhpAdapter.class,
		"[B",						net.dryuf.trans.php.adapter.ByteArrayPhpAdapter.class,
		"[C",						net.dryuf.trans.php.adapter.CharArrayPhpAdapter.class,
		"[F",						net.dryuf.trans.php.adapter.PrimitiveArrayPhpAdapter.class,
		"[D",						net.dryuf.trans.php.adapter.PrimitiveArrayPhpAdapter.class,
		"[I",						net.dryuf.trans.php.adapter.PrimitiveArrayPhpAdapter.class,
		"[J",						net.dryuf.trans.php.adapter.PrimitiveArrayPhpAdapter.class,
		"[S",						net.dryuf.trans.php.adapter.PrimitiveArrayPhpAdapter.class,
		"char",						net.dryuf.trans.php.adapter.CharPrimitivePhpAdapter.class,
		"short",					net.dryuf.trans.php.adapter.IntPrimitivePhpAdapter.class,
		"int",						net.dryuf.trans.php.adapter.IntPrimitivePhpAdapter.class,
		"long",						net.dryuf.trans.php.adapter.IntPrimitivePhpAdapter.class,

		"java.lang.Class",				net.dryuf.trans.php.adapter.java.lang.ClassPhpAdapter.class,
		"java.lang.Character",				net.dryuf.trans.php.adapter.java.lang.CharacterPhpAdapter.class,
		"java.lang.String",				net.dryuf.trans.php.adapter.java.lang.StringPhpAdapter.class,
		"java.lang.Number",				net.dryuf.trans.php.adapter.java.lang.NumberPhpAdapter.class,
		"java.lang.Boolean",				net.dryuf.trans.php.adapter.java.lang.BooleanPhpAdapter.class,
		"java.lang.Integer",				net.dryuf.trans.php.adapter.java.lang.IntegerPhpAdapter.class,
		"java.lang.Long",				net.dryuf.trans.php.adapter.java.lang.LongPhpAdapter.class,
		"java.lang.Float",				net.dryuf.trans.php.adapter.java.lang.FloatPhpAdapter.class,
		"java.lang.Double",				net.dryuf.trans.php.adapter.java.lang.DoublePhpAdapter.class,
		"java.lang.Iterable",				net.dryuf.trans.php.adapter.java.util.IterablePhpAdapter.class,
		"java.lang.Math",				net.dryuf.trans.php.adapter.java.lang.MathPhpAdapter.class,
		"java.lang.System",				net.dryuf.trans.php.adapter.java.lang.SystemPhpAdapter.class,
		"java.lang.StringBuilder",			net.dryuf.trans.php.adapter.java.lang.StringBuilderPhpAdapter.class,
		"java.lang.Runnable",				net.dryuf.trans.php.adapter.java.lang.RunnablePhpAdapter.class,

		"java.lang.reflect.Field",			net.dryuf.trans.php.adapter.WrongPhpAdapter.class,
		"java.lang.reflect.Method",			net.dryuf.trans.php.adapter.WrongPhpAdapter.class,

		"java.io.File",					net.dryuf.trans.php.adapter.java.io.FilePhpAdapter.class,
		"java.io.ByteArrayInputStream",			net.dryuf.trans.php.adapter.java.io.ByteArrayInputStreamPhpAdapter.class,
		"java.io.ByteArrayOutputStream",		net.dryuf.trans.php.adapter.java.io.ByteArrayOutputStreamPhpAdapter.class,
		"java.io.InputStream",				net.dryuf.trans.php.adapter.java.io.InputStreamPhpAdapter.class,
		"java.io.OutputStream",				net.dryuf.trans.php.adapter.java.io.OutputStreamPhpAdapter.class,
		"java.io.PrintStream",				net.dryuf.trans.php.adapter.java.io.PrintStreamPhpAdapter.class,

		"java.net.URLEncoder",				net.dryuf.trans.php.adapter.java.net.URLEncoderPhpAdapter.class,
		"java.net.URLConnection",			net.dryuf.trans.php.adapter.java.net.URLConnectionPhpAdapter.class,

		"java.sql.Connection",				net.dryuf.trans.php.adapter.java.sql.ConnectionPhpAdapter.class,

		"java.util.concurrent.Callable",		net.dryuf.trans.php.adapter.java.util.concurrent.CallablePhpAdapter.class,
		"java.util.function.Function",			net.dryuf.trans.php.adapter.java.util.function.FunctionPhpAdapter.class,
		"java.util.Comparator",				net.dryuf.trans.php.adapter.java.util.ComparatorPhpAdapter.class,
		"java.util.Collection",				net.dryuf.trans.php.adapter.java.util.PhpCollectionPhpAdapter.class,
		"java.util.Date",				net.dryuf.trans.php.adapter.java.util.DatePhpAdapter.class,
		"java.util.List",				net.dryuf.trans.php.adapter.java.util.ListPhpAdapter.class,
		"java.util.LinkedList",				net.dryuf.trans.php.adapter.java.util.ListPhpAdapter.class,
		"java.util.ArrayList",				net.dryuf.trans.php.adapter.java.util.ListPhpAdapter.class,
		"java.util.Map",				net.dryuf.trans.php.adapter.java.util.MapPhpAdapter.class,
		"java.util.HashMap",				net.dryuf.trans.php.adapter.java.util.PhpHashMapPhpAdapter.class,
		"java.util.LinkedHashMap",			net.dryuf.trans.php.adapter.java.util.PhpHashMapPhpAdapter.class,
		"java.util.HashSet",				net.dryuf.trans.php.adapter.java.util.PhpHashSetPhpAdapter.class,
		"java.util.LinkedHashSet",			net.dryuf.trans.php.adapter.java.util.PhpHashSetPhpAdapter.class,
		"java.util.Collections",			net.dryuf.trans.php.adapter.java.util.CollectionsPhpAdapter.class,
		"java.util.Arrays",				net.dryuf.trans.php.adapter.java.util.ArraysPhpAdapter.class,

		"org.w3c.dom.Document",				net.dryuf.trans.php.adapter.org.w3c.dom.DocumentPhpAdapter.class,
		"org.w3c.dom.Element",				net.dryuf.trans.php.adapter.org.w3c.dom.ElementPhpAdapter.class,
		"org.w3c.dom.Node",				net.dryuf.trans.php.adapter.org.w3c.dom.NodePhpAdapter.class,
		"org.w3c.dom.NodeList",				net.dryuf.trans.php.adapter.org.w3c.dom.NodeListPhpAdapter.class,

		"org.apache.commons.codec.binary.Base64",	net.dryuf.trans.php.adapter.org.apache.commons.codec.binary.Base64PhpAdapter.class,
		"org.apache.commons.io.IOUtils",		net.dryuf.trans.php.adapter.org.apache.commons.io.IOUtilsPhpAdapter.class,
		"org.apache.commons.io.FileUtils",		net.dryuf.trans.php.adapter.org.apache.commons.io.FileUtilsPhpAdapter.class,
		"org.apache.commons.io.FilenameUtils",		net.dryuf.trans.php.adapter.org.apache.commons.io.FilenameUtilsPhpAdapter.class,
		"org.apache.commons.lang3.ArrayUtils",		net.dryuf.trans.php.adapter.org.apache.commons.lang3.ArrayUtilsPhpAdapter.class,
		"org.apache.commons.lang3.StringUtils",		net.dryuf.trans.php.adapter.org.apache.commons.lang3.StringUtilsPhpAdapter.class,
		"org.apache.commons.lang3.ObjectUtils",		net.dryuf.trans.php.adapter.org.apache.commons.lang3.ObjectUtilsPhpAdapter.class,
		"org.apache.commons.lang3.RandomUtils",		net.dryuf.trans.php.adapter.org.apache.commons.lang3.RandomUtilsPhpAdapter.class,

		"org.junit.Assert",				net.dryuf.trans.php.adapter.org.junit.AssertPhpAdapter.class,

		"org.springframework.util.Assert",		net.dryuf.trans.php.adapter.org.springframework.util.AssertPhpAdapter.class,

		"net.dryuf.core.Dryuf",				net.dryuf.trans.php.adapter.net.dryuf.core.DryufPhpAdapter.class,
		"net.dryuf.util.CollectionUtil",		net.dryuf.trans.php.adapter.net.dryuf.core.CollectionUtilPhpAdapter.class,
		"net.dryuf.util.MapUtil",			net.dryuf.trans.php.adapter.net.dryuf.core.MapUtilPhpAdapter.class,
		"net.dryuf.web.oper.ObjectOperMethod",		net.dryuf.trans.php.adapter.net.dryuf.web.oper.ObjectOperMethodPhpAdapter.class,
		"net.dryuf.xml.util.XmlFormat",			net.dryuf.trans.php.adapter.net.dryuf.xml.util.XmlFormatPhpAdapter.class,

		"com.google.common.base.Objects",		net.dryuf.trans.php.adapter.com.google.common.base.ObjectsPhpAdapter.class,
		"com.google.common.base.Function",		net.dryuf.trans.php.adapter.com.google.common.base.FunctionPhpAdapter.class,
		"com.google.common.base.Predicate",		net.dryuf.trans.php.adapter.com.google.common.base.PredicatePhpAdapter.class,
		"com.google.common.collect.Collections2",	net.dryuf.trans.php.adapter.com.google.common.collect.Collections2PhpAdapter.class,
		"com.google.common.collect.Lists",		net.dryuf.trans.php.adapter.com.google.common.collect.ListsPhpAdapter.class,
		"com.google.common.collect.Sets",		net.dryuf.trans.php.adapter.com.google.common.collect.SetsPhpAdapter.class
	);

	public static Map<String, String> defaultNativeClassMappings = TransUtil.createLinkedHashMap(
		"java.lang.Object",				"net.dryuf.core.Object",
		"java.lang.Byte",				"integer",
		"java.lang.Short",				"integer",
		"java.lang.Integer",				"integer",
		"java.lang.Long",				"integer",
		"java.lang.Float",				"double",
		"java.lang.Double",				"double",
		"java.lang.String",				"string",
		"java.lang.StringBuilder",			"net.dryuf.core.StringBuilder",
		"java.io.Serializable",				null,
		"java.util.Collection",				"net.dryuf.util.Collection",
		"java.util.List",				"net.dryuf.util.Listable",
		"java.util.LinkedList",				"net.dryuf.util.LinkedList",
		"java.util.ArrayList",				"net.dryuf.util.ArrayList",
		"java.util.Map",				"net.dryuf.util.Map",
		"java.util.HashMap",				"net.dryuf.util.HashMap",
		"java.util.LinkedHashMap",			"net.dryuf.util.LinkedHashMap",
		"java.util.concurrent.ConcurrentHashMap",	"net.dryuf.util.HashMap",
		"java.util.Set",				"net.dryuf.util.Set",
		"java.util.HashSet",				"net.dryuf.util.HashSet",
		"java.util.LinkedHashSet",			"net.dryuf.util.LinkedHashSet",

		"java.lang.ArrayIndexOutOfBoundsException",	"net.dryuf.core.ArrayIndexOutOfBoundsException",
		"java.lang.Exception",				"net.dryuf.core.Exception",
		"java.lang.RuntimeException",			"net.dryuf.core.RuntimeException",
		"java.lang.NullPointerException",		"net.dryuf.core.NullPointerException",
		"java.lang.UnsupportedOperationException",	"net.dryuf.core.UnsupportedOperationException",
		"java.lang.IllegalArgumentException",		"net.dryuf.core.IllegalArgumentException",
		"java.lang.IllegalStateException",		"net.dryuf.core.IllegalStateException",
		"java.lang.SecurityException",			"net.dryuf.core.SecurityException",
		"java.lang.NumberFormatException",		"net.dryuf.core.NumberFormatException",
		"java.lang.ClassNotFoundException",		"net.dryuf.core.ClassNotFoundException",
		"java.io.FileNotFoundException",		"net.dryuf.io.FileNotFoundException",
		"java.io.IOException",				"net.dryuf.io.IoException",

		"java.sql.SQLException",			"net.dryuf.sql.SqlException",

		"java.lang.Override",				null,
		"java.lang.SuppressWarnings",			null,
		"java.lang.annotation.Retention",		null,
		"java.lang.annotation.RetentionPolicy",		null,

		"net.dryuf.trans.meta.NoDynamic",		null,
		"net.dryuf.trans.meta.DynamicDefaults",		null
	);
}
