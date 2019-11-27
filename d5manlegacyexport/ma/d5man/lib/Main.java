package ma.d5man.lib;

// Generally, this class only exists to be able to build a JAR with the Makefile
// intended to be used for normal applications

import ma.tools2.MTC;

public class Main {

	public static void main(String[] args) {
		System.err.println("This is a library JAR which is not " +
						"intended to be run directly.");
		System.err.println("Use it in conjunction with other D5Man " +
				"Java components like D5Man-Server or such.");
		System.err.println("Tools Version = " + MTC.TOOLS_VERSION);
		System.exit(1);
	}

}
