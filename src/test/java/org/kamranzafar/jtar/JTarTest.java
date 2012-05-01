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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarInputStream;
import org.kamranzafar.jtar.TarOutputStream;
import org.kamranzafar.jtar.TarUtils;

@RunWith(JUnit4.class)
public class JTarTest {
	static final int BUFFER = 2048;

	/**
	 * Tar the given folder
	 * 
	 * @throws IOException
	 */
	@Test
	public void tar() throws IOException {
		FileOutputStream dest = new FileOutputStream("c:/tmp/tartest/test.tar");
		TarOutputStream out = new TarOutputStream(
				new BufferedOutputStream(dest));

		tarFolder(null, "c:/tmp/untartest", out);

		out.close();

		System.out.println("Calculated tar size: "
				+ TarUtils.calculateTarSize(new File("c:/tmp/untartest")));
		System.out.println("Actual tar size: "
				+ new File("c:/tmp/tartest/test.tar").length());
	}

	/**
	 * Untar the tar file
	 * 
	 * @throws IOException
	 */
	@Test
	public void untarTarFile() throws IOException {
		String destFolder = "c:/tmp/untartest";
		File zf = new File("c:/tmp/tartest/test.tar");

		TarInputStream tis = new TarInputStream(new BufferedInputStream(
				new FileInputStream(zf)));

		untar(tis, destFolder);

		tis.close();
	}

	/**
	 * Untar the gzipped-tar file
	 * 
	 * @throws IOException
	 */
	@Test
	public void untarTGzFile() throws IOException {
		String destFolder = "c:/tmp/untartest";
		File zf = new File("c:/tmp/test.tar.gz");

		TarInputStream tis = new TarInputStream(new BufferedInputStream(
				new GZIPInputStream(new FileInputStream(zf))));

		untar(tis, destFolder);

		tis.close();
	}

	private void untar(TarInputStream tis, String destFolder)
			throws IOException {
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
					new File(destFolder + "/"
							+ entry.getName().substring(0, di)).mkdirs();
				}
			}

			FileOutputStream fos = new FileOutputStream(destFolder + "/"
					+ entry.getName());
			dest = new BufferedOutputStream(fos);

			while ((count = tis.read(data)) != -1) {
				dest.write(data, 0, count);
			}

			dest.flush();
			dest.close();
		}
	}

	public void tarFolder(String parent, String path, TarOutputStream out)
			throws IOException {
		BufferedInputStream origin = null;
		File f = new File(path);
		String files[] = f.list();

		// is file
		if (files == null) {
			files = new String[1];
			files[0] = f.getName();
		}

		parent = ((parent == null) ? (f.isFile()) ? "" : f.getName() + "/"
				: parent + f.getName() + "/");

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
					out.write(new byte[(int) entry.getSize()]);
				}
				continue;
			}

			FileInputStream fi = new FileInputStream(fe);
			origin = new BufferedInputStream(fi);

			TarEntry entry = new TarEntry(fe, parent + files[i]);
			out.putNextEntry(entry);

			int count;
			int bc = 0;
			while ((count = origin.read(data)) != -1) {
				out.write(data, 0, count);
				bc += count;
			}

			out.flush();

			origin.close();
		}
	}
}
