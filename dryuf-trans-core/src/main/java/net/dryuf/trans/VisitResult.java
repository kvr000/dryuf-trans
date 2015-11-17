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


import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

public class VisitResult extends java.lang.Object
{
	public static final int		RI_None				= 0;
	public static final int		RI_PackageIdentifier		= 1;
	public static final int		RI_ClassIdentifier		= 2;
	public static final int		RI_This				= 3;
	public static final int		RI_Super			= 4;
	public static final int		RI_DotClass			= 5;
	public static final int		RI_Expression			= 6;
	public static final int		RI_Null				= 7;
	public static final int		RI_Return			= 8;
	public static final int		RI_Break			= 9;
	public static final int		RI_Throw			= 10;
	public static final int		RI_Error			= 11;
	public static final int		RI_Lambda			= 12;
	public static final int		RI_NewInstance			= 13;
	public static final int		RI_Parenthesized		= 14;

	public				VisitResult()
	{
		this.contentBuilder = new StringBuilder();
	}

	public				VisitResult(String str)
	{
		this.contentBuilder = new StringBuilder(str);
	}

	public				VisitResult(StringBuilder sb)
	{
		this.contentBuilder = sb;
	}

	public static VisitResult	createEmpty()
	{
		return new VisitResult();
	}

	public static VisitResult	createNull()
	{
		VisitResult self = new VisitResult();
		self.contentBuilder = null;
		return self;
	}

	public boolean			isNull()
	{
		return this.contentBuilder == null;
	}

	public boolean			isNullEmpty()
	{
		return this.contentBuilder == null || this.contentBuilder.length() == 0;
	}

	public String			toString()
	{
		return contentBuilder.toString();
	}

	public VisitResult		resetMeta()
	{
		resultBound = null;
		resultIndicator = RI_None;
		resultStatic = null;
		return this;
	}

	public Class<?>			getResultClass()
	{
		return resultBound == null ? void.class : resultBound.getRawClass();
	}

	public BoundType		getOptionalBound(int i)
	{
		return resultBound != null && resultBound.getTypeArguments().length > i ? resultBound.getTypeArguments()[i] : null;
	}

	public BoundType		getDefaultBound(int i, BoundType defaultValue)
	{
		return resultBound != null && resultBound.getTypeArguments().length > i ? resultBound.getTypeArguments()[i] : defaultValue;
	}

	public BoundType		getMandatoryBound(int i)
	{
		if (resultBound != null && resultBound.getTypeArguments().length > i)
			return resultBound.getTypeArguments()[i];
		throw new RuntimeException("Getting bound from "+resultBound+"["+i+"] failed for "+resultBound);
	}

	public VisitResult		updateEmptyResultClass(Class<?> clazz)
	{
		if (resultBound == null)
			updateResultClass(clazz);
		return this;
	}

	public VisitResult		updateResultClass(Class<?> clazz)
	{
		return updateResultBound(BoundType.createRaw(clazz));
	}

	public VisitResult		resetResultType()
	{
		resultBound = null;
		return this;
	}

	public VisitResult		updateResultBound(BoundType genericType)
	{
		setResultBound(genericType);
		return this;
	}

	public VisitResult		updateEmptyResultBound(BoundType genericType)
	{
		if (resultBound == null) {
			setResultBound(genericType);
		}
		return this;
	}

	public VisitResult		updateResultIndicator(int resultIndicator)
	{
		this.resultIndicator = resultIndicator;
		return this;
	}

	public VisitResult		updateEmptyResultIndicator(int resultIndicator)
	{
		if (this.resultIndicator == RI_None)
			this.resultIndicator = resultIndicator;
		return this;
	}

	public VisitResult		updateExpressionPriority(int priority)
	{
		this.expressionPriority = priority;
		return this;
	}

	public VisitResult		updateEmptyExpressionPriority(int priority)
	{
		if (this.expressionPriority == 0x7fffffff)
			this.expressionPriority = priority;
		return this;
	}

	public String			getContent()
	{
		return this.contentBuilder == null ? null : this.contentBuilder.toString();
	}

	public String			getFinal()
	{
		return this.getContent();
	}

	public VisitResult		append(VisitResult appended)
	{
		this.resultIndicator = RI_None;
		return appendSb(appended.contentBuilder);
	}

	public VisitResult		appendSafe(VisitResult appended)
	{
		if (appended == null)
			return this;
		return appendSb(appended.contentBuilder);
	}

	public VisitResult		appendReplaceType(VisitResult appended)
	{
		append(appended);
		this.resultBound = appended.resultBound;
		this.resultIndicator = appended.resultIndicator;
		this.resultStatic = appended.resultStatic;
		return this;
	}

	public VisitResult		appendUpdateEmptyType(VisitResult appended)
	{
		append(appended);
		if (this.resultBound == null) {
			this.resultBound = appended.resultBound;
			this.resultStatic = appended.resultStatic;
		}
		return this;
	}

	public VisitResult		updateContent(String content)
	{
		if (contentBuilder == null)
			contentBuilder = new StringBuilder();
		contentBuilder.replace(0, contentBuilder.length(), content);
		return this;
	}

	public VisitResult		prependString(String str)
	{
		contentBuilder.insert(0, str);
		return this;
	}

	public VisitResult		appendString(String str)
	{
		contentBuilder.append(str);
		return this;
	}

	public VisitResult		appendSb(StringBuilder str)
	{
		contentBuilder.append(str);
		return this;
	}

	protected BoundType		resultBound = null;

	public BoundType		getResultBound()
	{
		return this.resultBound;
	}

	public VisitResult		setResultBound(BoundType resultBound_)
	{
		this.resultBound = resultBound_;
		return this;
	}

	protected Boolean		resultStatic = null;

	public Boolean			getResultStatic()
	{
		return this.resultStatic;
	}

	public VisitResult		setResultStatic(Boolean resultStatic_)
	{
		this.resultStatic = resultStatic_;
		return this;
	}

	protected int			resultIndicator = RI_None;

	public int			getResultIndicator()
	{
		return this.resultIndicator;
	}

	protected int			expressionPriority = 0x7fffffff;

	public int			getExpressionPriority()
	{
		return this.expressionPriority;
	}

	protected Map<String, Type>	parameterizedTypes = new LinkedHashMap<>();

	protected StringBuilder		contentBuilder = new StringBuilder();
}
