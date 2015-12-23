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


import java.io.FileInputStream;
import java.io.InputStream;


/**
 * Class to test try blocks
 *
 * @author
 * 	Zbyněk Vyškovský
 */
public class PhpTransTestedTryBlock extends Object
{
	public void			basicTryCatch()
	{
		try {
			++i;
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public void			tryFinally()
	{
		try {
			++i;
		}
		finally {
			--i;
		}
	}

	public void			tryResourcesFull()
	{
		try (InputStream stream = new FileInputStream("a.txt")) {
			++i;
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		finally {
			--i;
		}
	}

	public void			tryResourcesEmpty() throws Exception
	{
		try (InputStream stream = new FileInputStream("a.txt")) {
			++i;
		}
	}

	protected Integer		i = 0;
}
