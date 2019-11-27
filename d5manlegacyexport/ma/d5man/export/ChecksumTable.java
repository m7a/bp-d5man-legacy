package ma.d5man.export;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Path;
import java.nio.file.Files;

import ma.tools2.util.BinaryOperations;

import ma.d5man.lib.lls.D5Stream;
import ma.d5man.lib.export.fs.WriteControl;

import static java.nio.charset.StandardCharsets.UTF_8;

class ChecksumTable implements WriteControl {

	// extended page state information table comma separated value (8.3)
	private static final String FN = "xpgsinft.csv";

	private final Path root;

	private final Map<Path,ChecksumTableEntry> t;

	ChecksumTable(Path root, int nthreads) throws IOException {
		super();
		t = new ConcurrentHashMap<Path,ChecksumTableEntry>(512, 0.75f,
								nthreads);
		this.root = root;
		if(Files.exists(root.resolve(FN)))
			read(root, t);
	}

	private static void read(Path root, Map<Path,ChecksumTableEntry> t)
							throws IOException {
		
		try(BufferedReader in = Files.newBufferedReader(
						root.resolve(FN), UTF_8)) {
			String line;
			while((line = in.readLine()) != null)
				processLine(root, line, t);
		}
	}

	private static void processLine(Path root, String line,
					Map<Path,ChecksumTableEntry> t) {
		String[] parts = line.split(",", -1);
		if(parts.length != 4)
			return;
		t.put(root.resolve(parts[0]).toAbsolutePath(),
			new ChecksumTableEntry(
				parts[1].equals("_")? null: parts[1], 
				parts[2].equals("_")? -1:
							Long.parseLong(parts[2]),
				parts[3].equals("_")? null:
						BinaryOperations.
						decodeHexString(parts[3]),
				false
			));
	}

	void save() throws IOException {
		try(BufferedWriter out = Files.newBufferedWriter(
						root.resolve(FN), UTF_8)) {
			for(Map.Entry<Path,ChecksumTableEntry> e:
								t.entrySet()) {
				out.write(root.relativize(e.getKey()).
						toString() + "," +
						e.getValue().toString());
				out.newLine();
			}
		}
	}

	/** updates parameters accordingly */
	@Override
	public boolean decideOnWriting(D5Stream stream, Path dest)
							throws IOException {
		ChecksumTableEntry entryNow = new ChecksumTableEntry(
					stream.getEtag(), stream.getTimestamp(),
					stream.getMD5(), true);
		ChecksumTableEntry entryPrev;

		if((entryPrev = t.get(dest)) != null &&
						entryPrev.equals(entryNow)) {
			entryPrev.touch();
			return Files.exists(dest);
		} else {
			t.put(dest, entryNow);
			return true;
		}
	}

	@Override
	public void accept(Path path) {
		// NOP
	}

}
