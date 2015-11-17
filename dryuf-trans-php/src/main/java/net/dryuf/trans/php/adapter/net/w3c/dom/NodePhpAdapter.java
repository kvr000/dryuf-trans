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
package net.dryuf.trans.php.adapter.org.w3c.dom;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.Trees;

import net.dryuf.trans.VisitResult;
import net.dryuf.trans.php.adapter.BasicPhpAdapter;
import net.dryuf.trans.php.PhpTransVisitor;


public class NodePhpAdapter extends BasicPhpAdapter
{
	@Override
	public VisitResult		processFieldAccess(VisitResult path, Field field, MemberSelectTree node, Trees trees)
	{
		if (
				field.getName().equals("ELEMENT_NODE") ||
				field.getName().equals("ELEMENT_NODE") ||
				field.getName().equals("ATTRIBUTE_NODE") ||
				field.getName().equals("TEXT_NODE") ||
				field.getName().equals("CDATA_SECTION_NODE") ||
				field.getName().equals("ENTITY_REF_NODE") ||
				field.getName().equals("ENTITY_NODE") ||
				field.getName().equals("PI_NODE") ||
				field.getName().equals("COMMENT_NODE") ||
				field.getName().equals("DOCUMENT_NODE") ||
				field.getName().equals("DOCUMENT_TYPE_NODE") ||
				field.getName().equals("DOCUMENT_FRAG_NODE") ||
				field.getName().equals("NOTATION_NODE") ||
				field.getName().equals("HTML_DOCUMENT_NODE") ||
				field.getName().equals("DTD_NODE") ||
				field.getName().equals("ELEMENT_DECL_NODE") ||
				field.getName().equals("ATTRIBUTE_DECL_NODE") ||
				field.getName().equals("ENTITY_DECL_NODE") ||
				field.getName().equals("NAMESPACE_DECL_NODE")
				) {
			return new VisitResult("XML_"+field.getName());
		}
		return super.processFieldAccess(path, field, node, trees);
	}

	@Override
	public VisitResult		processMethodInvocation(VisitResult path, Method method, List<VisitResult> arguments, MethodInvocationTree node, Trees trees)
	{
		if (
				method.getName().equals("getNodeName") ||
				method.getName().equals("getNodeValue") ||
				method.getName().equals("getNodeType") ||
				method.getName().equals("getParentNode") ||
				method.getName().equals("getFirstChild") ||
				method.getName().equals("getLastChild") ||
				method.getName().equals("getPreviousSibling") ||
				method.getName().equals("getNextSibling") ||
				method.getName().equals("getAttributes") ||
				method.getName().equals("getOwnerDocument") ||
				method.getName().equals("getNamespaceURI") ||
				method.getName().equals("getPrefix") ||
				method.getName().equals("getLocalName") ||
				method.getName().equals("getBaseURI") ||
				method.getName().equals("getTextContent")
				) {
			return new VisitResult(path.getContent()+"->"+StringUtils.uncapitalize(method.getName().substring(3)));
		}
		return super.processMethodInvocation(path, method, arguments, node, trees);
	}
}
