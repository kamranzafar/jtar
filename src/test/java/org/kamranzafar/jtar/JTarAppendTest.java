/**
 * Copyright 2012 Kamran Zafar 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 */

package org.kamranzafar.jtar;

import static org.junit.Assert.assertEquals;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;

import org.junit.Before;
import org.junit.Test;

public class JTarAppendTest {
	static final int BUFFER = 2048;

	private File dir;
	private File outDir;
	private File inDir;

	@Before
	public void setup() throws IOException {
		dir = Files.createTempDirectory("apnd").toFile();
		dir.mkdirs();
		outDir = new File(dir, "out");
		outDir.mkdirs();
		inDir = new File(dir, "in");
		inDir.mkdirs();
	}

	@Test
	public void testSingleOperation() throws IOException {
		TarOutputStream tar = new TarOutputStream(new FileOutputStream(new File(dir, "tar.tar")));
		tar.putNextEntry(new TarEntry(TestUtils.writeStringToFile("a", new File(inDir, "afile")), "afile"));
		copyFileToStream(new File(inDir, "afile"), tar);
		tar.putNextEntry(new TarEntry(TestUtils.writeStringToFile("b", new File(inDir, "bfile")), "bfile"));
		copyFileToStream(new File(inDir, "bfile"), tar);
		tar.putNextEntry(new TarEntry(TestUtils.writeStringToFile("c", new File(inDir, "cfile")), "cfile"));
		copyFileToStream(new File(inDir, "cfile"), tar);
		tar.close();

		untar();

		assertInEqualsOut();
	}

	@Test
	public void testAppend() throws IOException {
		TarOutputStream tar = new TarOutputStream(new FileOutputStream(new File(dir, "tar.tar")));
		tar.putNextEntry(new TarEntry(TestUtils.writeStringToFile("a", new File(inDir, "afile")), "afile"));
		copyFileToStream(new File(inDir, "afile"), tar);
		tar.close();

		tar = new TarOutputStream(new File(dir, "tar.tar"), true);
		tar.putNextEntry(new TarEntry(TestUtils.writeStringToFile("b", new File(inDir, "bfile")), "bfile"));
		copyFileToStream(new File(inDir, "bfile"), tar);
		tar.putNextEntry(new TarEntry(TestUtils.writeStringToFile("c", new File(inDir, "cfile")), "cfile"));
		copyFileToStream(new File(inDir, "cfile"), tar);
		tar.close();

		untar();

		assertInEqualsOut();
	}

	private void copyFileToStream(File file, OutputStream out) throws IOException {
		final byte[] buffer = new byte[BUFFER];
		int length = 0;

		try (FileInputStream in = new FileInputStream(file)) {
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}
		}
	}

	/**
	 * Make sure that the contents of the input & output dirs are identical.
	 */
	private void assertInEqualsOut() throws UnsupportedEncodingException, FileNotFoundException, IOException {
		assertEquals(inDir.list().length, outDir.list().length);
		for (File in : inDir.listFiles()) {
			assertEquals(TestUtils.readFile(in), TestUtils.readFile(new File(outDir, in.getName())));
		}
	}

	private void untar() throws FileNotFoundException, IOException {
		try (TarInputStream in = new TarInputStream(new FileInputStream(new File(dir, "tar.tar")))) {
			TarEntry entry;

			while ((entry = in.getNextEntry()) != null) {
				int count;
				byte data[] = new byte[2048];
				try (BufferedOutputStream dest = new BufferedOutputStream(new FileOutputStream(outDir + "/" + entry.getName()))) {
					while ((count = in.read(data)) != -1) {
						dest.write(data, 0, count);
					}
				}
			}
		}
	}

}