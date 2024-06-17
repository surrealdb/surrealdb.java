java_run: lib
	javac com/surrealdb/Surreal.java && RUST_BACKTRACE=full java -Djava.library.path=target/debug com.surrealdb.Surreal

.PHONY: lib

lib:
	cargo build