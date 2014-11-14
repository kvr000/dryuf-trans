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
package org.druf.trans.php;

import java.util.HashMap;
import java.util.Map;


public class PhpTranslator extends java.lang.Object
{
	public static void		main(String[] args)
	{
		Map<String, Object> options = new HashMap<String, Object>();

		int parameterIndex = 0;

		for (; parameterIndex < args.length && args[parameterIndex].startsWith("-"); ++parameterIndex) {
			switch (args[parameterIndex].substring(1)) {
			case "f":
				options.put("force", true);
				break;

			default:
				throw new IllegalArgumentException(args[parameterIndex]+": unknown option");
			}
		}

		if (args.length-parameterIndex != 2) {
			System.err.println("Two parameters required: trans-root files-list-file");
			System.exit(126);
		}
		String transPath = args[parameterIndex+0];
		String transFile = args[parameterIndex+1];

		PhpTransProcessor processor = new PhpTransProcessor();
		processor.runFull(transPath, transFile, options);
	}
}
