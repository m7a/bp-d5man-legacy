package ma.d5man.export;

import java.nio.file.Path;
import java.io.IOException;

import ma.d5man.lib.export.proc.real.D5ManIOResolveProcess;

class MultipleD5ManIOResolveProcesses implements AutoCloseable { // TODO z RED #5

	final D5ManIOResolveProcess[] procs;

	MultipleD5ManIOResolveProcesses(Path exe, int num) throws IOException {
		super();
		procs = new D5ManIOResolveProcess[num];
		for(int i = 0; i < procs.length; i++) {
			procs[i] = new D5ManIOResolveProcess(exe);
			procs[i].open();
		}
	}

	@Override
	public void close() throws IOException {
		for(D5ManIOResolveProcess p: procs)
			p.close();
	}

}
