package ma.d5man.export;

import java.nio.file.Path;
import java.io.IOException;

import ma.d5man.lib.export.proc.real.D5Man2XMLProcess;

class MultipleD5Man2XMLProcesses implements AutoCloseable { // TODO z RED #5

	final D5Man2XMLProcess[] procs;

	MultipleD5Man2XMLProcesses(Path exe, int num) throws IOException {
		super();
		procs = new D5Man2XMLProcess[num];
		for(int i = 0; i < procs.length; i++) {
			procs[i] = new D5Man2XMLProcess(exe);
			procs[i].open();
		}
	}

	@Override
	public void close() throws IOException {
		for(D5Man2XMLProcess p: procs)
			p.close();
	}

}
