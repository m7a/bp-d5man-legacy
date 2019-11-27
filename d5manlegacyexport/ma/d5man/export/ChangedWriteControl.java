package ma.d5man.export;

import java.io.IOException;
import java.nio.file.Path;
import ma.d5man.lib.export.fs.WriteControl;
import ma.d5man.lib.j8.Consumer;
import ma.d5man.lib.lls.D5Stream;

class ChangedWriteControl implements WriteControl {

	private final WriteControl decider;
	private final Consumer<Path> consumer;

	ChangedWriteControl(WriteControl decider, Consumer<Path> consumer) {
		super();
		this.decider  = decider;
		this.consumer = consumer;
	}

	@Override
	public boolean decideOnWriting(D5Stream stream, Path dest)
							throws IOException {
		return decider.decideOnWriting(stream, dest);
	}

	@Override
	public void accept(Path path) {
		// decider.accept(path); (undefined semantics...)
		consumer.accept(path);
	}

}
