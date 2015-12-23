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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;

import com.sun.source.tree.LambdaExpressionTree;

import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EmptyStatementTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.Trees;
import org.apache.commons.lang3.StringUtils;


public abstract class CLikeTransVisitor extends TransVisitor
{
	public				CLikeTransVisitor(ProcessingEnvironment pe)
	{
		super(pe);
	}

	@Override
	public VisitResult		visitExpressionStatement(ExpressionStatementTree node, Trees trees)
	{
		try {
			return super.visitExpressionStatement(node, trees).appendString(";\n")
				.updateResultIndicator(VisitResult.RI_None);
		}
		catch (Exception ex) {
			getLogger().error(ex.toString(), ex);
			return new VisitResult(surroundFix(FormatUtil.forceEnding(node.toString(), ";"), ex).toString()+"\n");
		}
	}

	@Override
	public VisitResult		visitEmptyStatement(EmptyStatementTree node, Trees trees)
	{
		return new VisitResult(";\n");
	}

	@Override
	public VisitResult		visitSwitch(SwitchTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder("switch (");
		VisitResult switchExpression = getSimpleExpression(node.getExpression()).accept(this, trees);
		sb.append(switchExpression.getContent());
		sb.append(") {\n");
		for (CaseTree caseTree: node.getCases()) {
			ExpressionTree expression = caseTree.getExpression();
			if (expression != null) {
				sb.append("case ");
				if (switchExpression.getResultClass().isEnum()) {
					sb.append(getClassAdapter(switchExpression.getResultClass()).processFieldAccess(null, ReflectionUtil.getClassPublicField(switchExpression.getResultClass(), expression.toString()), null, trees).getContent());
				}
				else {
					sb.append(visitExpression(expression, trees).getContent());
				}
			}
			else {
				sb.append("default");
			}
			sb.append(":\n");
			VisitResult statementsResult = scan(caseTree.getStatements(), trees);
			if (statementsResult != null) {
				sb.append(FormatUtil.indentString(statementsResult.getContent()));
				if (statementsResult.getResultIndicator() == VisitResult.RI_Break || statementsResult.getResultIndicator() == VisitResult.RI_Return || statementsResult.getResultIndicator() == VisitResult.RI_Throw) {
					sb.append("\n");
				}
				else {
					sb.append("\t/* fall through */\n");
				}
			}
		}
		FormatUtil.removeSbNls(sb);
		FormatUtil.forceSbNl(sb);
		sb.append("}\n");
		return new VisitResult(sb.toString());
	}

	@Override
	public VisitResult		visitCase(CaseTree node, Trees trees)
	{
		ExpressionTree expression = node.getExpression();
		StringBuilder sb = new StringBuilder();
		if (expression != null) {
			sb.append("case ");
			sb.append(visitExpression(node.getExpression(), trees).getContent());
		}
		else {
			sb.append("default");
		}
		sb.append(":\n");
		VisitResult statementsResult = scan(node.getStatements(), trees);
		if (statementsResult != null) {
			sb.append(FormatUtil.indentString(statementsResult.getContent()));
			if (statementsResult.getResultIndicator() == VisitResult.RI_Break || statementsResult.getResultIndicator() == VisitResult.RI_Return || statementsResult.getResultIndicator() == VisitResult.RI_Throw) {
				sb.append("\n");
			}
			else {
				sb.append("\t/* fall through */\n");
			}
		}
		return new VisitResult(sb.toString());
	}

	@Override
	public VisitResult		visitBreak(BreakTree node, Trees trees)
	{
		return new VisitResult("break;\n")
			.updateResultIndicator(VisitResult.RI_Break);
	}

	@Override
	public VisitResult		visitIf(IfTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder("if (");
		sb.append(scan(getSimpleExpression(node.getCondition()), trees).getContent());
		sb.append(")");
		sb.append(FormatUtil.indentStatementString(scan(node.getThenStatement(), trees).getContent()));
		if (node.getElseStatement() != null) {
			sb.append("else");
			String st = scan(node.getElseStatement(), trees).getContent();
			sb.append(st.startsWith("if ") ? st : FormatUtil.indentStatementString(st));
		}
		return new VisitResult(sb.toString());
	}

	@Override
	public VisitResult		visitForLoop(ForLoopTree node, Trees trees)
	{
		pushIdentifiers();
		StringBuilder sb = new StringBuilder("for (");
		{
			int counter = 0;
			for (StatementTree initializer: node.getInitializer()) {
				if (counter++ != 0)
					sb.append(", ");
				sb.append(scan(initializer, trees).getContent());
				FormatUtil.removeSbStatementEnd(sb);
			}
		}
		sb.append("; ");
		FormatUtil.appendSbSafe(sb, visitOptionalTree(node.getCondition(), trees).getContent());
		sb.append("; ");
		{
			int counter = 0;
			for (StatementTree update: node.getUpdate()) {
				if (counter++ != 0)
					sb.append(", ");
				sb.append(scan(update, trees).getContent());
				FormatUtil.removeSbStatementEnd(sb);
			}
		}
		sb.append(")");
		if (sb.length() == 10)
			sb.replace(0, 10, "for (;;)");
		{
			String s = node.getStatement() != null ? scan(node.getStatement(), trees).getContent() : ";\n";
			if (!s.equals(";\n")) {
				sb.append(FormatUtil.indentStatementString(s));
			}
			else {
				sb.append(" ").append(s);
			}
		}
		popIdentifiers();
		return new VisitResult(sb.toString());
	}

	@Override
	public VisitResult		visitWhileLoop(WhileLoopTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder("while (");
		FormatUtil.appendSbSafe(sb, visitOptionalTree(getSimpleExpression(node.getCondition()), trees).getContent());
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
	public VisitResult		visitDoWhileLoop(DoWhileLoopTree node, Trees trees)
	{
		StringBuilder sb = new StringBuilder("do");
		sb.append(FormatUtil.indentStatementString(scan(node.getStatement(), trees).getContent()));
		if (sb.substring(sb.length()-2).equals("}\n"))
			sb.replace(sb.length()-1, sb.length(), " ");
		sb.append("while (");
		FormatUtil.appendSbSafe(sb, visitOptionalTree(getSimpleExpression(node.getCondition()), trees).getContent());
		sb.append(");\n");
		return new VisitResult(sb.toString());
	}

	@Override
	public VisitResult		visitContinue(ContinueTree node, Trees trees)
	{
		return new VisitResult("continue;\n");
	}

	@Override
	public VisitResult		visitReturn(ReturnTree node, Trees trees)
	{
		try {
			VisitResult result = this.visitExpression(node.getExpression(), trees);
			if (result == null || result.isNull()) {
				result = new VisitResult("return")
						.updateResultClass(void.class);
			}
			else {
				result.prependString("return ");
			}
			result.appendString(";\n");
			return result
					.updateResultIndicator(VisitResult.RI_Return);
		}
		catch (Exception ex) {
			getLogger().error(ex.toString(), ex);
			return new VisitResult(surroundFix(FormatUtil.forceEnding(node.toString(), ";"), ex).toString()+"\n")
					.updateResultIndicator(VisitResult.RI_Return);
		}
	}

	@Override
	public VisitResult		visitLabeledStatement(LabeledStatementTree node, Trees trees)
	{
		return new VisitResult(node.getLabel().toString()+": "+scan(node.getStatement(), trees));
	}

	@Override
	public VisitResult		visitTry(TryTree node, Trees trees)
	{
		VisitResult result = new VisitResult("try");
		if (node.getResources() != null && node.getResources().size() > 0)
			result.appendString(surroundFix(StringUtils.join(node.getResources(), ", "), null));
		result.appendSb(FormatUtil.indentStatementString(scan(node.getBlock(), trees).getContent()));
		result.appendSafe(scan(node.getCatches(), trees));
		if (node.getFinallyBlock() != null) {
			result.appendString("finally ");
			result.appendSafe(scan(node.getFinallyBlock(), trees));
		}
		return result;
	}

	@Override
	public VisitResult		visitCatch(CatchTree node, Trees trees)
	{
		pushIdentifiers();
		try {
			StringBuilder sb = new StringBuilder("catch (");
			sb.append(makeVariableDef(node.getParameter(), trees));
			sb.append(") ");
			sb.append(scan(node.getBlock(), trees).getContent());
			return new VisitResult(sb.toString());
		}
		finally {
			popIdentifiers();
		}
	}

	@Override
	public VisitResult		visitThrow(ThrowTree node, Trees trees)
	{
		return scan(node.getExpression(), trees).prependString("throw ").appendString(";\n")
			.updateResultIndicator(VisitResult.RI_Throw);
	}
}
