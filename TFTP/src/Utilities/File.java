package utilities;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Vector;

/**
 * Carleton University
 * Department of Systems and Computer Engineering
 * SYSC 3303 - Real-Time and Concurrent Systems
 * <p>
 * Collaborative Project
 * <p>
 * Package: utilities
 * Created: 2016-05-29
 * License: MIT
 * <p>
 * Authors:
 * Christopher McMorran    (100968013)
 * Yue Zhang               (100980408)
 * Raiyan Quaium           (100962217)
 */
public class File extends java.io.File {
	private boolean sensitive;

	/**
	 * Creates a new <code>File</code> instance by converting the given
	 * pathname string into an abstract pathname.  If the given string is
	 * the empty string, then the result is the empty abstract pathname.
	 *
	 * @param pathname A pathname string
	 * @throws NullPointerException If the <code>pathname</code> argument is <code>null</code>
	 */
	public File(String pathname) {
		super(pathname);
	}

	/**
	 * Creates a new <code>File</code> instance from a parent pathname string
	 * and a child pathname string.
	 * <p>
	 * <p> If <code>parent</code> is <code>null</code> then the new
	 * <code>File</code> instance is created as if by invoking the
	 * single-argument <code>File</code> constructor on the given
	 * <code>child</code> pathname string.
	 * <p>
	 * <p> Otherwise the <code>parent</code> pathname string is taken to denote
	 * a directory, and the <code>child</code> pathname string is taken to
	 * denote either a directory or a file.  If the <code>child</code> pathname
	 * string is absolute then it is converted into a relative pathname in a
	 * system-dependent way.  If <code>parent</code> is the empty string then
	 * the new <code>File</code> instance is created by converting
	 * <code>child</code> into an abstract pathname and resolving the result
	 * against a system-dependent default directory.  Otherwise each pathname
	 * string is converted into an abstract pathname and the child abstract
	 * pathname is resolved against the parent.
	 *
	 * @param parent The parent pathname string
	 * @param child  The child pathname string
	 * @throws NullPointerException If <code>child</code> is <code>null</code>
	 */
	public File(String parent, String child) {
		super(parent, child);
	}

	/**
	 * Creates a new <code>File</code> instance from a parent abstract
	 * pathname and a child pathname string.
	 * <p>
	 * <p> If <code>parent</code> is <code>null</code> then the new
	 * <code>File</code> instance is created as if by invoking the
	 * single-argument <code>File</code> constructor on the given
	 * <code>child</code> pathname string.
	 * <p>
	 * <p> Otherwise the <code>parent</code> abstract pathname is taken to
	 * denote a directory, and the <code>child</code> pathname string is taken
	 * to denote either a directory or a file.  If the <code>child</code>
	 * pathname string is absolute then it is converted into a relative
	 * pathname in a system-dependent way.  If <code>parent</code> is the empty
	 * abstract pathname then the new <code>File</code> instance is created by
	 * converting <code>child</code> into an abstract pathname and resolving
	 * the result against a system-dependent default directory.  Otherwise each
	 * pathname string is converted into an abstract pathname and the child
	 * abstract pathname is resolved against the parent.
	 *
	 * @param parent The parent abstract pathname
	 * @param child  The child pathname string
	 * @throws NullPointerException If <code>child</code> is <code>null</code>
	 */
	public File(java.io.File parent, String child) {
		super(parent, child);
	}

	/**
	 * Creates a new <tt>File</tt> instance by converting the given
	 * <tt>file:</tt> URI into an abstract pathname.
	 * <p>
	 * <p> The exact form of a <tt>file:</tt> URI is system-dependent, hence
	 * the transformation performed by this constructor is also
	 * system-dependent.
	 * <p>
	 * <p> For a given abstract pathname <i>f</i> it is guaranteed that
	 * <p>
	 * <blockquote><tt>
	 * new File(</tt><i>&nbsp;f</i><tt>.{@link #toURI() toURI}()).equals(</tt><i>&nbsp;f</i><tt>.{@link #getAbsoluteFile() getAbsoluteFile}())
	 * </tt></blockquote>
	 * <p>
	 * so long as the original abstract pathname, the URI, and the new abstract
	 * pathname are all created in (possibly different invocations of) the same
	 * Java virtual machine.  This relationship typically does not hold,
	 * however, when a <tt>file:</tt> URI that is created in a virtual machine
	 * on one operating system is converted into an abstract pathname in a
	 * virtual machine on a different operating system.
	 *
	 * @param uri An absolute, hierarchical URI with a scheme equal to
	 *            <tt>"file"</tt>, a non-empty path component, and undefined
	 *            authority, query, and fragment components
	 * @throws NullPointerException     If <tt>uri</tt> is <tt>null</tt>
	 * @throws IllegalArgumentException If the preconditions on the parameter do not hold
	 * @see #toURI()
	 * @see URI
	 * @since 1.4
	 */
	public File(URI uri) {
		super(uri);
	}

	/**
	 * Appends a string to this file.
	 *
	 * @param data The string to append.
	 * @return This file.
	 */
	public synchronized File append(String data) {
		return append(data.getBytes());
	}

	/**
	 * Appends the given data to the file.
	 *
	 * @param data The data to append.
	 * @return This file.
	 */
	public File append(byte[] data) {
		try {
			FileOutputStream fout = new FileOutputStream(this, true);
			fout.write(data);
			fout.flush();
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

	/**
	 * Creates a Vector of byte arrays, all of which
	 * will be 512 bytes, until a byte[] is created that is less than 512 bytes.
	 *
	 * @return A Vector of byte[].
	 */
	public synchronized Vector<byte[]> toVectorOfByteArrays() {
		byte[] fbytes = getBytes();
		if (fbytes == null) return null;

		Vector<byte[]> segments = new Vector<>();
		for (int i = 0; i != fbytes.length; i += 512) {
			if (i + 512 >= fbytes.length) {
				int x = fbytes.length - i;
				segments.add(Arrays.copyOfRange(fbytes, i, i + x));
				break;
			}
			segments.add(Arrays.copyOfRange(fbytes, i, i + 512));
		}

		return segments;
	}

	/**
	 * Creates a linke dlist of byte[], size 512 bytes from this file.
	 * @return A LinkedList<byte[]>.
	 */
	public synchronized LinkedList<byte[]> toLinkedListOfByteArrays() {
		byte[] fbytes = getBytes();
		if (fbytes == null) return null;

		LinkedList<byte[]> segments = new LinkedList<>();
		for (int i = 0; i != fbytes.length; i += 512) {
			if (i + 512 >= fbytes.length) {
				int x = fbytes.length - i;
				segments.add(Arrays.copyOfRange(fbytes, i, i + x));
				break;
			}
			segments.add(Arrays.copyOfRange(fbytes, i, i + 512));
		}
		return segments;
	}

	/**
	 * Converts this file into a single byte array.
	 *
	 * @return A byte[].
	 */
	public synchronized byte[] getBytes() {
		try {
			FileInputStream fileInputStream = new FileInputStream(this);
			byte[] array = new byte[Math.toIntExact(length())];
			fileInputStream.read(array);
			fileInputStream.close();
			return array;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Determines if this file does not exist.
	 *
	 * @return True if it does not exist.
	 */
	public synchronized boolean doesNotExist() {
		return !this.exists();
	}


	/**
	 * Determines if a file is too sensitive to send.
	 *
	 * @return True if it is a security concern.
	 */
	public boolean isSensitive() {
		String name = getName();
		return (name.contains("passwd") || name.contains("shadow") || name.contains("id_rsa"));
	}
}
