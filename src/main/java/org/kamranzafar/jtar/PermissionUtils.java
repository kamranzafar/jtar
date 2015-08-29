package org.kamranzafar.jtar;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Helps dealing with file permissions.
 */
public class PermissionUtils {

	private static Map<PosixFilePermission, Integer> permissionToInteger = new HashMap<>();

	static {
		permissionToInteger.put(PosixFilePermission.OWNER_EXECUTE, 0100);
		permissionToInteger.put(PosixFilePermission.OWNER_WRITE, 0200);
		permissionToInteger.put(PosixFilePermission.OWNER_READ, 0400);

		permissionToInteger.put(PosixFilePermission.GROUP_EXECUTE, 0010);
		permissionToInteger.put(PosixFilePermission.GROUP_WRITE, 0020);
		permissionToInteger.put(PosixFilePermission.GROUP_READ, 0040);

		permissionToInteger.put(PosixFilePermission.OTHERS_EXECUTE, 0001);
		permissionToInteger.put(PosixFilePermission.OTHERS_WRITE, 0002);
		permissionToInteger.put(PosixFilePermission.OTHERS_READ, 0004);
	}

	public static Integer permissions(File f) {
		Integer number = 0;
		try {
			Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(f.toPath());
			for (Map.Entry<PosixFilePermission, Integer> entry : permissionToInteger.entrySet()) {
				if (permissions.contains(entry.getKey())) {
					number += entry.getValue();
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return number;
	}
}
