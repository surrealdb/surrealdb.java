package com.surrealdb;

import java.util.Iterator;
import java.util.NoSuchElementException;

class ValueObjectIterator<T> implements Iterator<T> {

	private final ValueClassConverter<T> converter;
	private final Iterator<Value> iterator;
	private boolean done;

	ValueObjectIterator(Class<T> clazz, Iterator<Value> iterator) {
		this.converter = new ValueClassConverter<>(clazz);
		this.iterator = iterator;
	}

	@Override
	final public boolean hasNext() {
		if (done) {
			return false;
		}
		if (iterator.hasNext()) {
			return true;
		}
		done = true;
		// Nobody else can reach a plain iterator, so free it now. A synchronized one
		// may still be in use by another thread and is left to the cleaner.
		if (iterator instanceof ValueIterator) {
			((ValueIterator) iterator).close();
		}
		return false;
	}

	@Override
	final public T next() {
		if (done) {
			throw new NoSuchElementException();
		}
		// Each element is converted to a T, then released.
		final Value value = iterator.next();
		try {
			return converter.convert(value);
		} finally {
			value.close();
		}
	}
}
