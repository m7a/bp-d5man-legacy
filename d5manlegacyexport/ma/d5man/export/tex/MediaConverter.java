package ma.d5man.export.tex;

import java.io.*;

import ma.tools2.util.BufferUtils;
import ma.tools2.concurrent.StderrCacher;

import ma.d5man.lib.lls.*;
import ma.d5man.lib.export.proc.BGCopyThread;

class MediaConverter {

	private final ProcessBuilder pb;

	MediaConverter(String[] program) {
		super();
		pb = new ProcessBuilder(program);
	}

	D5Stream convertSVG2PDF(D5Stream in) throws IOException,
							InterruptedException {
		Process proc = pb.start();
		StderrCacher cacher = new StderrCacher(proc);
		cacher.start();
		new BGCopyThread(in.openInputStreamRepresentation(),
					proc.getOutputStream(), null).start();
		ByteArrayOutputStream str = new ByteArrayOutputStream();
		BufferUtils.copy(proc.getInputStream(), str);
		int ecode = proc.waitFor();
		if(ecode != 0)
			throw createMediaConversionFailureException(ecode,
					cacher.getOutput(), str.toByteArray());
		return new StringD5Stream(str.toByteArray());
	}

	private static D5IOException createMediaConversionFailureException(
				int ecode, String cacherOutput, byte[] data) {
		byte[] lastBytes;
		if(data.length >= 4096) {
			lastBytes = new byte[4096];
			int j = data.length - 4096 - 1;
			for(int i = 0; i < lastBytes.length; i++)
				lastBytes[i] = data[j++];
		} else {
			lastBytes = data;
		}
		return new D5IOException("Failed to perform media conversion " +
				"-- exit code " + ecode + ": " + cacherOutput +
				". The extended error " + "information " +
				"contains the last 4 KiB of data which have " +
				"been discarded.", XT2HXV.work(lastBytes));
	}

}
