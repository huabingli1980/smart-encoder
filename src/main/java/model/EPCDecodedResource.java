package model;

public class EPCDecodedResource {
	String version;
	EPC epc;
	GS1 gs1;
	Tag tag;

	static class EPC {
		String[] epcComponents;
		String epcUri;
		String epcScheme;
	}

	static class GS1 {
		String elementString;
		String gtin;
		String gtinSerial;
		String humanReadable;
	}

	static class Tag {
		String tagUri;
		String epcHex;
		String epcTagScheme;
		String rawUri;
	}
}
