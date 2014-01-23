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
import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.zip.GZIPInputStream;

import org.junit.Before;
import org.junit.Test;

public class JTarTest {
	static final int BUFFER = 2048;

	private File dir;

	@Before
	public void setup() throws IOException {
		dir = Files.createTempDirectory("tartest").toFile();
		dir.mkdirs();
	}

	/**
	 * Tar the given folder
	 * 
	 * @throws IOException
	 */
	@Test
	public void tar() throws IOException {
		FileOutputStream dest = new FileOutputStream(dir.getAbsolutePath() + "/tartest.tar");
		TarOutputStream out = new TarOutputStream(new BufferedOutputStream(dest));

		File tartest = new File(dir.getAbsolutePath(), "tartest");
		tartest.mkdirs();

		TestUtils.writeStringToFile("HPeX2kD5kSTc7pzCDX", new File(tartest, "one"));
		TestUtils.writeStringToFile("gTzyuQjfhrnyX9cTBSy", new File(tartest, "two"));
		TestUtils.writeStringToFile("KG889vdgjPHQXUEXCqrr", new File(tartest, "three"));
		TestUtils.writeStringToFile("CNBDGjEJNYfms7rwxfkAJ", new File(tartest, "four"));
		TestUtils.writeStringToFile("tT6mFKuLRjPmUDjcVTnjBL", new File(tartest, "five"));
		TestUtils.writeStringToFile("jrPYpzLfWB5vZTRsSKqFvVj", new File(tartest, "six"));

		tarFolder(null, dir.getAbsolutePath() + "/tartest/", out);

		out.close();

		assertEquals(TarUtils.calculateTarSize(new File(dir.getAbsolutePath() + "/tartest")), new File(dir.getAbsolutePath() + "/tartest.tar").length());
	}

	/**
	 * Untar the tar file
	 * 
	 * @throws IOException
	 */
	@Test
	public void untarTarFile() throws IOException {
		File destFolder = new File(dir, "untartest");
		destFolder.mkdirs();

		File zf = new File("src/test/resources/tartest.tar");

		TarInputStream tis = new TarInputStream(new BufferedInputStream(new FileInputStream(zf)));
		untar(tis, destFolder.getAbsolutePath());

		tis.close();

		assertFileContents(destFolder);
	}

	/**
	 * Untar the tar file
	 * 
	 * @throws IOException
	 */
	@Test
	public void untarTarFileDefaultSkip() throws IOException {
		File destFolder = new File(dir, "untartest/skip");
		destFolder.mkdirs();

		File zf = new File("src/test/resources/tartest.tar");

		TarInputStream tis = new TarInputStream(new BufferedInputStream(new FileInputStream(zf)));
		tis.setDefaultSkip(true);
		untar(tis, destFolder.getAbsolutePath());

		tis.close();

		assertFileContents(destFolder);

	}

	/**
	 * Untar the gzipped-tar file
	 * 
	 * @throws IOException
	 */
	@Test
	public void untarTGzFile() throws IOException {
		File destFolder = new File(dir, "untargztest");
		File zf = new File("src/test/resources/tartest.tar.gz");

		TarInputStream tis = new TarInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(zf))));

		untar(tis, destFolder.getAbsolutePath());

		tis.close();

		assertFileContents(destFolder);
	}


	@Test
	public void testOffset() throws IOException {
		File destFolder = new File(dir, "untartest");
		destFolder.mkdirs();

		File zf = new File("src/test/resources/tartest.tar");

		TarInputStream tis = new TarInputStream(new BufferedInputStream(new FileInputStream(zf)));
		tis.getNextEntry();
		assertEquals(TarConstants.HEADER_BLOCK, tis.getCurrentOffset());
		tis.getNextEntry();
		TarEntry entry = tis.getNextEntry(); 
		// All of the files in the tartest.tar file are smaller than DATA_BLOCK
		assertEquals(TarConstants.HEADER_BLOCK * 3 + TarConstants.DATA_BLOCK * 2, tis.getCurrentOffset());
		tis.close();
		
		RandomAccessFile rif = new RandomAccessFile(zf, "r");
		rif.seek(TarConstants.HEADER_BLOCK * 3 + TarConstants.DATA_BLOCK * 2);
		byte[] data = new byte[(int)entry.getSize()];
		rif.read(data);
		assertEquals("gTzyuQjfhrnyX9cTBSy", new String(data, "UTF-8"));
		rif.close();
	}
	
	private void untar(TarInputStream tis, String destFolder) throws IOException {
		BufferedOutputStream dest = null;

		TarEntry entry;
		while ((entry = tis.getNextEntry()) != null) {
			System.out.println("Extracting: " + entry.getName());
			int count;
			byte data[] = new byte[BUFFER];

			if (entry.isDirectory()) {
				new File(destFolder + "/" + entry.getName()).mkdirs();
				continue;
			} else {
				int di = entry.getName().lastIndexOf('/');
				if (di != -1) {
					new File(destFolder + "/" + entry.getName().substring(0, di)).mkdirs();
				}
			}

			FileOutputStream fos = new FileOutputStream(destFolder + "/" + entry.getName());
			dest = new BufferedOutputStream(fos);

			while ((count = tis.read(data)) != -1) {
				dest.write(data, 0, count);
			}

			dest.flush();
			dest.close();
		}
	}

	public void tarFolder(String parent, String path, TarOutputStream out) throws IOException {
		BufferedInputStream origin = null;
		File f = new File(path);
		String files[] = f.list();

		// is file
		if (files == null) {
			files = new String[1];
			files[0] = f.getName();
		}

		parent = ((parent == null) ? (f.isFile()) ? "" : f.getName() + "/" : parent + f.getName() + "/");

		for (int i = 0; i < files.length; i++) {
			System.out.println("Adding: " + files[i]);
			File fe = f;
			byte data[] = new byte[BUFFER];

			if (f.isDirectory()) {
				fe = new File(f, files[i]);
			}

			if (fe.isDirectory()) {
				String[] fl = fe.list();
				if (fl != null && fl.length != 0) {
					tarFolder(parent, fe.getPath(), out);
				} else {
					TarEntry entry = new TarEntry(fe, parent + files[i] + "/");
					out.putNextEntry(entry);
				}
				continue;
			}

			FileInputStream fi = new FileInputStream(fe);
			origin = new BufferedInputStream(fi);
			TarEntry entry = new TarEntry(fe, parent + files[i]);
			out.putNextEntry(entry);

			int count;

			while ((count = origin.read(data)) != -1) {
				out.write(data, 0, count);
			}

			out.flush();

			origin.close();
		}
	}

	@Test
	public void fileEntry() throws IOException {
		String fileName = "file.txt";
		long fileSize = 14523;
		long modTime = System.currentTimeMillis() / 1000;

		// Create a header object and check the fields
		TarHeader fileHeader = TarHeader.createHeader(fileName, fileSize, modTime, false);
		assertEquals(fileName, fileHeader.name.toString());
		assertEquals(TarHeader.LF_NORMAL, fileHeader.linkFlag);
		assertEquals(fileSize, fileHeader.size);
		assertEquals(modTime, fileHeader.modTime);

		// Create an entry from the header
		TarEntry fileEntry = new TarEntry(fileHeader);
		assertEquals(fileName, fileEntry.getName());

		// Write the header into a buffer, create it back and compare them
		byte[] headerBuf = new byte[TarConstants.HEADER_BLOCK];
		fileEntry.writeEntryHeader(headerBuf);
		TarEntry createdEntry = new TarEntry(headerBuf);
		assertTrue(fileEntry.equals(createdEntry));
	}

	private void assertFileContents(File destFolder) throws UnsupportedEncodingException, FileNotFoundException, IOException {
		assertEquals("HPeX2kD5kSTc7pzCDX", TestUtils.readFile(new File(destFolder, "tartest/one")));
		assertEquals("gTzyuQjfhrnyX9cTBSy", TestUtils.readFile(new File(destFolder, "tartest/two")));
		assertEquals("KG889vdgjPHQXUEXCqrr", TestUtils.readFile(new File(destFolder, "tartest/three")));
		assertEquals("CNBDGjEJNYfms7rwxfkAJ", TestUtils.readFile(new File(destFolder, "tartest/four")));
		assertEquals("tT6mFKuLRjPmUDjcVTnjBL", TestUtils.readFile(new File(destFolder, "tartest/five")));
		assertEquals("jrPYpzLfWB5vZTRsSKqFvVj", TestUtils.readFile(new File(destFolder, "tartest/six")));
	}
}