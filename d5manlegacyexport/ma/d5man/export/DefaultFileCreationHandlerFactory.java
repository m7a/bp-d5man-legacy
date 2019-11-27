package ma.d5man.export;

import java.nio.file.Path;
import ma.d5man.lib.j8.Supplier;
import ma.d5man.lib.j8.Consumer;

class DefaultFileCreationHandlerFactory implements Supplier<Consumer<Path>> {

	@Override
	public Consumer<Path> get() {
		return new Consumer<Path>() {
			@Override
			public void accept(Path p) {
				// nop TODO z MÖGLICHERWEISE EINFACH VERWENDEN, UM TABELLE ZU FÜLLEN!
			}
		};
	}

}
