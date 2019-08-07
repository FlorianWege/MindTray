package com.example.mindtray.memo;

public class AudioContent extends Content {
	//raw bytes
	private byte[] _bytes;

	public byte[] getBytes() {
		return _bytes;
	}

	public AudioContent(String name, byte[] bytes) {
		super(name);

		_bytes = bytes;
	}
}