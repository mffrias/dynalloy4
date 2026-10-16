/*
 * TACO: Translation of Annotated COde
 * Copyright (c) 2010 Universidad de Buenos Aires
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA,
 * 02110-1301, USA
 */
package ar.uba.dc.rfm.dynalloy.analyzer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.Locale;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;

public final class AlloyJNILibraryPath {



	/**
	 * The system-specific file separator (forward-slash on UNIX, back-slash on
	 * Windows, etc.)
	 */
	private static final String fs = System.getProperty("file.separator");

	/** This variable caches the result of alloyHome() function call. */
	private static String alloyHome = null;

	/** Whether copyFromJAR already replaced outdated files in this JVM. */
	private static boolean outdatedReplaced = false;

	public AlloyJNILibraryPath() {

	}

	public void setupJNILibraryPath() {
		copyFromJAR();
		final String binary = alloyHome() + fs + "binary";

		// Add the new JNI location to the java.library.path
		try {
			System.setProperty("java.library.path", binary);
			// The above line is actually useless on Sun JDK/JRE (see Sun's bug
			// ID 4280189)
			// The following 4 lines should work for Sun's JDK/JRE (though they
			// probably won't work for others)
			String[] newarray = new String[] { binary };
			java.lang.reflect.Field old = ClassLoader.class.getDeclaredField("usr_paths");
			old.setAccessible(true);
			old.set(null, newarray);
		} catch (Throwable ex) {
		}
	}

	/**
	 * Find a temporary directory to store Alloy files; it's guaranteed to be a
	 * canonical absolute path.
	 */
	private static synchronized String alloyHome() {
		if (alloyHome != null)
			return alloyHome;
		String temp = System.getProperty("java.io.tmpdir");
		if (temp == null || temp.length() == 0)
			throw new RuntimeException("Error. JVM need to specify a temporary directory using java.io.tmpdir property.");
		String username = System.getProperty("user.name");
		File tempfile = new File(temp + File.separatorChar + "alloy4tmp40-" + (username == null ? "" : username));
		tempfile.mkdirs();
		String ans = Util.canon(tempfile.getPath());
		if (!tempfile.isDirectory()) {
			throw new RuntimeException("Error. Cannot create the temporary directory " + ans);
		}
		if (!Util.onWindows()) {
			String[] args = { "chmod", "700", ans };
			try {
				Runtime.getRuntime().exec(args).waitFor();
			} catch (Throwable ex) {
			} // We only intend to make a best effort.
		}
		return alloyHome = ans;
	}

	/** Copy the required files from the JAR into a temporary directory. */
	private void copyFromJAR() {
		// Compute the appropriate platform
		String os = System.getProperty("os.name").toLowerCase(Locale.US).replace(' ', '-');
		if (os.startsWith("mac-"))
			os = "mac";
		else if (os.startsWith("windows-"))
			os = "windows";
		else if (os.startsWith("linux"))
			os = "linux";
		String arch = System.getProperty("os.arch").toLowerCase(Locale.US).replace(' ', '-');
		if (arch.equals("powerpc"))
			arch = "ppc-" + os;
		else
			arch = arch.replaceAll("\\Ai[3456]86\\z", "x86") + "-" + os;
		if (os.equals("mac"))
			arch = "x86-mac"; // our pre-compiled binaries are all universal binaries
//nrosner june 25 2013, removed code prevented amd64 architecture from being correctly determined
//		else if (os.equals("linux"))
//			arch = "x86-linux";
				
		// Find out the appropriate Alloy directory
		final String platformBinary = alloyHome() + fs + "binary";
		// Write a few test files
		try {
			(new File(platformBinary)).mkdirs();
			Util.writeAll(platformBinary + fs + "tmp.cnf", "p cnf 3 1\n1 0\n");
		} catch (Err er) {
			// The error will be caught later by the "berkmin" or "spear" test
		}
		// Copy the platform-dependent binaries
		String[] executables = { arch + "/libminisat.so", arch + "/libminisatx1.so", arch + "/libminisat220.jnilib", arch + "/libminisat220.so", arch + "/libminisat.jnilib", arch + "/libminisatprover.so",
				arch + "/libminisatproverx1.so", arch + "/libminisatprover.jnilib", arch + "/libzchaff.so", arch + "/libzchaffx1.so", arch
						+ "/libzchaff.jnilib", arch + "/berkmin", arch + "/spear" };
		String[] others = { arch + "/minisat.dll", arch + "/minisatprover.dll", arch + "/zchaff.dll", arch + "/berkmin.exe", arch
				+ "/spear.exe" };
		synchronized (AlloyJNILibraryPath.class) {
			if (!outdatedReplaced) {
				replaceOutdated(true, platformBinary, executables);
				replaceOutdated(false, platformBinary, others);
				outdatedReplaced = true;
			}
		}
		Util.copy(true, false, platformBinary, executables);
		Util.copy(false, false, platformBinary, others);
		// Copy the model files

	}

	/**
	 * Replaces the files in dir that differ from the jar's copy of the named
	 * resources (copyFromJAR does it once per JVM). Util.copy only copies a
	 * file that is not there yet, so a library extracted by a run with an
	 * older alloy4.jar would be used for good: on Apple Silicon, for example,
	 * the x86_64-only MiniSat 2.2 that an arm64 JVM cannot load. The new copy
	 * is renamed over the old file, so another JVM that has the old one loaded
	 * keeps it; like Util.copy, it is made readable (and executable, if
	 * executable is set) by the owner only. Files that are not there yet, and
	 * resources that are not in the jar, are left to Util.copy.
	 */
	private static void replaceOutdated(boolean executable, String dir, String... names) {
		for (String name : names) {
			File file = new File(dir, name.substring(name.lastIndexOf('/') + 1));
			if (!file.isFile())
				continue;
			File copy = null;
			try {
				byte[] inJar = readResource(name);
				if (inJar == null || Arrays.equals(inJar, Files.readAllBytes(file.toPath())))
					continue;
				copy = File.createTempFile(file.getName(), ".tmp", file.getParentFile());
				Files.write(copy.toPath(), inJar);
				if (!Util.onWindows())
					Files.setPosixFilePermissions(copy.toPath(),
							PosixFilePermissions.fromString(executable ? "rwx------" : "rw-------"));
				Files.move(copy.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING,
						StandardCopyOption.ATOMIC_MOVE);
			} catch (IOException e) {
				System.err.println("Could not replace " + file + " with the copy in the jar: " + e);
			} finally {
				if (copy != null)
					copy.delete(); // only still there if the move failed
			}
		}
	}

	/**
	 * The bytes of the named resource, read the way Util.copy reads it, or
	 * null if there is no such resource.
	 */
	private static byte[] readResource(String name) throws IOException {
		try (InputStream in = Util.class.getClassLoader().getResourceAsStream(name)) {
			if (in == null)
				return null;
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			byte[] buffer = new byte[16384];
			for (int n; (n = in.read(buffer)) >= 0;)
				bytes.write(buffer, 0, n);
			return bytes.toByteArray();
		}
	}

}
