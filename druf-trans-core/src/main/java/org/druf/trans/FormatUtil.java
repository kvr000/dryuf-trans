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


public class FormatUtil extends java.lang.Object
{
	public static StringBuilder	forceSbNl(StringBuilder sb)
	{
		if (sb.length() > 0 && sb.charAt(sb.length()-1) != '\n')
			sb.append('\n');
		return sb;
	}

	public static StringBuilder	forceSbOneNl(TransVisitor transVisitor, StringBuilder sb)
	{
		return forceSbNl(removeSbNls(sb));
	}

	public static boolean		checkRemoveSbEnd(StringBuilder sb, String removing)
	{
		if (sb.length() > removing.length() && sb.substring(sb.length()-removing.length()).equals(removing)) {
			sb.replace(sb.length()-removing.length(), sb.length(), "");
			return true;
		}
		return false;
	}

	public static StringBuilder	removeSbEndSafe(StringBuilder sb, String removing)
	{
		if (sb.length() > removing.length() && sb.substring(sb.length()-removing.length()).equals(removing))
			sb.replace(sb.length()-removing.length(), sb.length(), "");
		return sb;
	}

	public static StringBuilder	removeSbNls(StringBuilder sb)
	{
		if (sb.length() > 0 && sb.charAt(sb.length()-1) == '\n')
			sb.replace(sb.length()-1, sb.length(), "");
		return sb;
	}

	public static StringBuilder	removeSbStatementEnd(StringBuilder sb)
	{
		if (sb.length() >= 2 && sb.charAt(sb.length()-1) == '\n' && sb.charAt(sb.length()-2) == ';')
			sb.replace(sb.length()-2, sb.length(), "");
		return sb;
	}

	public static StringBuilder	indentSb(StringBuilder sb)
	{
		for (int p = sb.length()-2; (p = sb.lastIndexOf("\n", p)) >= 0; --p) {
			if (sb.charAt(p+1) != '\n')
				sb.insert(p+1, "\t");
		}
		if (sb.length() > 0)
			sb.insert(0, "\t");
		return sb;
	}

	public static StringBuilder	indentString(String s)
	{
		return indentSb(new StringBuilder(s));
	}

	public static StringBuilder	indentStatementString(String s)
	{
		if (s.startsWith("{"))
			return new StringBuilder(" ").append(s);
		else
			return indentString(s).insert(0, "\n");
	}

	public static String		tabalign(String s, int size)
	{
		StringBuilder sb = new StringBuilder(s);
		int curlen = s.length()&~7;
		if (curlen >= size) {
			sb.append(' ');
		}
		else {
			while (curlen < size) {
				sb.append('\t');
				curlen += 8;
			}
		}
		return sb.toString();
	}

	public static StringBuilder	tabalign(StringBuilder sb, int size)
	{
		int curlen = sb.length()&~7;
		if (curlen >= size) {
			sb.append(' ');
		}
		else {
			while (curlen < size) {
				sb.append('\t');
				curlen += 8;
			}
		}
		return sb;
	}

	public static StringBuilder	forceSbEnding(StringBuilder sb, String ending)
	{
		if (!sb.substring(sb.length()-ending.length()).equals(ending))
			sb.append(ending);
		return sb;
	}

	public static String		forceEnding(String s, String ending)
	{
		if (!s.endsWith(ending)) {
			s += ending;
		}
		return s;
	}

	public static StringBuilder	appendSbSafe(StringBuilder sb, String s)
	{
		if (s != null)
			sb.append(s);
		return sb;
	}
}
