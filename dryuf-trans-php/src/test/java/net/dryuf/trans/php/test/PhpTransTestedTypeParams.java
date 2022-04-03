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
package net.dryuf.trans.php.test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Class to test parameterized types translation.
 *
 * @author
 * 	Zbyněk Vyškovský
 * @since
 * 	2015-06-05
 */
public class PhpTransTestedTypeParams<T, U extends String> extends HashMap<T, U>
{
	private static final long	serialVersionUID = 0;

	@SuppressWarnings("unchecked")
	@Override
	public U			put(T key, U value)
	{
		if (get(key) != null)
			value = (U)((String) get(key)+value);
		super.put(key, value);
		return value;
	}

	public int			testFields()
	{
		return currentT.hashCode()+currentU.length()+this.currentT.hashCode()+this.currentU.length();
	}

	protected T			currentT;

	protected U			currentU;
}
