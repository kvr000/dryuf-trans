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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import net.dryuf.trans.php.PhpTransProcessor;


public class PhpTransTest extends java.lang.Object
{
	public static final String	SRC_PATH	= "src/test/java/";
	public static final String	BUILD_PATH	= "trans/php/_build/";
	public static final String	CMP_PATH	= "src/test/resources/";

	public void			runSingleFileTest(String filename) throws FileNotFoundException, IOException
	{
		String path = FilenameUtils.getPath(filename);
		String base = FilenameUtils.getBaseName(filename);
		new File(BUILD_PATH+path+base+".php").delete();
		(new PhpTransProcessor()).runCompiler("trans/php/", Arrays.asList(new String[]{
			SRC_PATH+filename
		}));
		Assert.assertTrue("translation comparison failed: diff "+CMP_PATH+path+"cmp-"+base+".php "+BUILD_PATH+path+base+".php",
			IOUtils.contentEqualsIgnoreEOL(new FileReader(BUILD_PATH+path+base+".php"),  new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("cmp-"+base+".php"), "cmp-"+base+".php not found"))));
	}

	@Test
	public void			testBasicTranslation() throws FileNotFoundException, IOException
	{
		new File("trans/php/_build/net/dryuf/trans/php/test/PhpTransTested.php").delete();
		(new PhpTransProcessor()).runCompiler("trans/php/", Arrays.asList(new String[]{
			"src/test/java/net/dryuf/trans/php/test/PhpTransTested.java"
		}));
		Assert.assertTrue("translation comparison failed: diff src/test/resources/net/dryuf/trans/php/test/cmp-PhpTransTested.php trans/php/_build/net/dryuf/trans/php/test/PhpTransTested.php",
			IOUtils.contentEqualsIgnoreEOL(new FileReader("trans/php/_build/net/dryuf/trans/php/test/PhpTransTested.php"),  new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("cmp-PhpTransTested.php"), "cmp-PhpTransTested.php not found"))));
	}

	@Test
	public void			testCollections() throws FileNotFoundException, IOException
	{
		runSingleFileTest("net/dryuf/trans/php/test/PhpTransTestedCollections.java");
	}

	@Test
	public void			testEnumTranslation() throws FileNotFoundException, IOException
	{
		new File("trans/php/_build/net/dryuf/trans/php/test/PhpTransTestedEnum.php").delete();
		(new PhpTransProcessor()).runCompiler("trans/php/", Arrays.asList(new String[]{
			"src/test/java/net/dryuf/trans/php/test/PhpTransTestedEnum.java"
		}));
		Assert.assertTrue("translation comparison failed: diff src/test/resources/net/dryuf/trans/php/test/cmp-PhpTransTestedEnum.php trans/php/_build/net/dryuf/trans/php/test/PhpTransTestedEnum.php",
			IOUtils.contentEqualsIgnoreEOL(new FileReader("trans/php/_build/net/dryuf/trans/php/test/PhpTransTestedEnum.php"),  new InputStreamReader(getClass().getResourceAsStream("cmp-PhpTransTestedEnum.php"))));
	}

	@Test
	public void			testTypedTranslation() throws FileNotFoundException, IOException
	{
		new File("trans/php/_build/net/dryuf/trans/php/test/PhpTransTestedTyped.php").delete();
		(new PhpTransProcessor()).runCompiler("trans/php/", Arrays.asList(new String[]{
			"src/test/java/net/dryuf/trans/php/test/PhpTransTestedTyped.java"
		}));
		Assert.assertTrue("translation comparison failed: diff src/test/resources/net/dryuf/trans/php/test/cmp-PhpTransTestedTyped.php trans/php/_build/net/dryuf/trans/php/test/PhpTransTestedTyped.php",
			IOUtils.contentEqualsIgnoreEOL(new FileReader("trans/php/_build/net/dryuf/trans/php/test/PhpTransTestedTyped.php"),  new InputStreamReader(getClass().getResourceAsStream("cmp-PhpTransTestedTyped.php"))));
	}

	@Test
	public void			testTypeParams() throws FileNotFoundException, IOException
	{
		new File("trans/php/_build/net/dryuf/trans/php/test/PhpTransTestedTypeParams.php").delete();
		(new PhpTransProcessor()).runCompiler("trans/php/", Arrays.asList(new String[]{
			"src/test/java/net/dryuf/trans/php/test/PhpTransTestedTypeParams.java"
		}));
		Assert.assertTrue("translation comparison failed: diff src/test/resources/net/dryuf/trans/php/test/cmp-PhpTransTestedTypeParams.php trans/php/_build/net/dryuf/trans/php/test/PhpTransTestedTypeParams.php",
			IOUtils.contentEqualsIgnoreEOL(new FileReader("trans/php/_build/net/dryuf/trans/php/test/PhpTransTestedTypeParams.php"),  new InputStreamReader(getClass().getResourceAsStream("cmp-PhpTransTestedTypeParams.php"))));
	}

	@Test
	public void			testLambda() throws FileNotFoundException, IOException
	{
		runSingleFileTest("net/dryuf/trans/php/test/PhpTransTestedLambda.java");
	}

	@Test
	public void			testByteArrays() throws FileNotFoundException, IOException
	{
		runSingleFileTest("net/dryuf/trans/php/test/PhpTransTestedByteArrays.java");
	}

	@Test
	public void			testTryBlock() throws FileNotFoundException, IOException
	{
		runSingleFileTest("net/dryuf/trans/php/test/PhpTransTestedTryBlock.java");
	}
}
