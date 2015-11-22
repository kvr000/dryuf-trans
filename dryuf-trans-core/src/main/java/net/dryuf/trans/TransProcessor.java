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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes("*")
public abstract class TransProcessor extends AbstractProcessor
{
	public				TransProcessor()
	{
		logger = LogManager.getLogger(getClass());
	}

	public void			setTargetSuffix(String targetSuffix)
	{
		this.targetSuffix = targetSuffix;
	}

	public List<String>		copyFiles(String targetDir, InputStream filesListStream) throws IOException
	{
		if (!targetDir.endsWith("/"))
			targetDir += "/";
		Pattern emptyPattern = Pattern.compile("^(#.*|\\s*)$");
		Pattern javaPattern = Pattern.compile("^((?:[^/]+/[^/]+/java/|target/generated-sources/)((.+/)([^/]+).java))\n*$");
		List<String> filesList = new LinkedList<String>();
		for (String fileName: IOUtils.readLines(filesListStream)) {
			Matcher matcher;
			if (emptyPattern.matcher(fileName).matches()) {
				// skip the comment
			}
			else if ((matcher = javaPattern.matcher(fileName)).matches()) {
				String newName = targetDir+"_build/"+matcher.group(2);
				new File(targetDir+"_build/"+matcher.group(3)).mkdirs();
				filesList.add(newName);
				byte[] src = FileUtils.readFileToByteArray(new File(matcher.group(1)));
				try {
					byte[] old = FileUtils.readFileToByteArray(new File(newName));
					if (Arrays.equals(src, old))
						continue;
				}
				catch (IOException e) {
				}
				FileUtils.writeByteArrayToFile(new File(newName), src);
			}
			else {
				throw new IOException("No known action for file "+fileName);
			}
		}
		return filesList;
	}

	public List<String>		filterListByTime(List<String> files)
	{
		List<String> results = new LinkedList<>();
		for (String fileName: files) {
			if (fileName.endsWith(".java")) {
				File in = new File(fileName);
				File out = new File(fileName.substring(0, fileName.length()-".java".length())+targetSuffix);
				if (out.exists() && out.lastModified() >= in.lastModified())
					continue;
			}
			results.add(fileName);
		}
		return results;
	}

	public void			runCompiler(String transRoot, List<String> files)
	{
		this.transRoot = transRoot;

		//Get an instance of java compiler
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
//		JavaCompiler compiler = new EclipseCompiler();

		getLogger().info(compiler.getSourceVersions().toString());
		//Get a new instance of the standard file manager implementation
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, Charset.forName("UTF-8"));

		// Get the list of java file objects, in this case we have only
		// one file, TestMain.java
		Iterable<? extends JavaFileObject> compilationUnits1 = fileManager.getJavaFileObjectsFromStrings(files);

		CompilationTask task = compiler.getTask(
				null,
				fileManager,
				null,
				Arrays.asList(new String[]{ "-source", "8", "-target", "8" }),
				null,
				compilationUnits1
			);

		// Add an annotation processor to the list
		// Set the annotation processor to the compiler task
		task.setProcessors(Collections.singleton(this));

		// Perform the compilation task.
		task.call();
	}

	public boolean			runDiff(String transDirectory)
	{
		Process proc;
		try {
			proc = Runtime.getRuntime().exec("diff -urN --exclude=*.class --exclude=*.java --exclude=*.sw? "+transDirectory+"direct/ "+transDirectory+"_build/");
			FileUtils.copyInputStreamToFile(proc.getInputStream(), new File(transDirectory+"_trans.diff"));
			IOUtils.copy(proc.getErrorStream(), System.err);
			int err = proc.waitFor();
			logger.error("diff result: "+err);
			if (err == 0)
				return true;
			else if (err <= 2)
				return false;
			throw new IOException("executing diff failed: "+err);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void			runFull(String transRoot, String transFile, Map<String, Object> options)
	{
		if (!transRoot.isEmpty() && !transRoot.endsWith("/"))
			transRoot += "/";

		List<String> files;
		try {
			files = copyFiles(transRoot, new FileInputStream(transFile));
		}
		catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		if (!(Boolean)options.getOrDefault("force", false))
			files = filterListByTime(files);
		if (!files.isEmpty())
			runCompiler(transRoot, files);
		runDiff(transRoot);
	}

	@Override
	public void			init(ProcessingEnvironment pe)
	{
		super.init(pe);
		visitor = createTransVisitor(pe);
		trees = Trees.instance(pe);
	}

	public abstract TransVisitor	createTransVisitor(ProcessingEnvironment pe);

	@Override
	public boolean			process(Set<? extends TypeElement> sets, RoundEnvironment roundEnvironment)
	{
		if (!roundEnvironment.processingOver()) {
			getLogger().info("processing "+roundEnvironment);
			getLogger().info("sets: "+sets);
			for (Element e: roundEnvironment.getRootElements()) {
				getLogger().info("processing "+processingEnv.getElementUtils().getPackageOf(e)+"."+e.getSimpleName());
				TreePath tp = trees.getPath(e);
				visitor.process(e, tp, trees);
			}
		}
		else {
			getLogger().info("processing over");
			return visitor.processOver(trees);
		}
		return true;
	}

	public void			setLogger(Logger logger)
	{
		this.logger = logger;
	}

	public Logger			getLogger()
	{
		return logger;
	}

	protected String		transRoot;

	protected String		targetSuffix;

	protected Logger		logger;

	protected TransVisitor		visitor;

	private Trees			trees;
}
