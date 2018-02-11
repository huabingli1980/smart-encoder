/*package org.llrp.ltk.generated.messages;

import org.llrp.ltk.exceptions.InvalidLLRPMessageException;
import org.llrp.ltk.generated.parameters.AccessSpec;
import org.llrp.ltk.types.LLRPBitList;
import org.llrp.ltk.types.SignedShort;
import org.llrp.ltk.types.UnsignedShort;

public class MyADD_ACCESSSPEC extends ADD_ACCESSSPEC {

	public MyADD_ACCESSSPEC() {
		super();
	}

	@Override
	protected void decodeBinarySpecific(LLRPBitList binary) throws InvalidLLRPMessageException {
		byte position = 0;

		boolean tempLength = false;

		SignedShort type = null;
		boolean tempByteLength = false;
		int tempLength1 = 0;

		try {
			if (binary.get(position)) {

				type = new SignedShort(binary.subList(Integer.valueOf(position + 1), Integer.valueOf(7)));
			} else {
				type = new SignedShort(binary.subList(Integer.valueOf(position + 6), Integer.valueOf(10)));

				short tempByteLength1 = (new UnsignedShort(
						binary.subList(Integer.valueOf(position + 6 + 10), Integer.valueOf(UnsignedShort.length()))))
								.toShort();

				tempLength1 = 8 * tempByteLength1;
			}
		} catch (IllegalArgumentException arg9) {

			throw new InvalidLLRPMessageException("ADD_ACCESSSPEC misses non optional parameter of type AccessSpec");

		}

		if (binary.get(position)) {

			AccessSpec arg9999 = this.accessSpec;
			tempLength1 = AccessSpec.length().intValue();
		}

		if (type != null && type.equals(AccessSpec.TYPENUM)) {
			this.accessSpec = new AccessSpec(binary.subList(Integer.valueOf(position), Integer.valueOf(tempLength1)));
			int arg12 = position + tempLength1;

		} else {

			throw new InvalidLLRPMessageException("ADD_ACCESSSPEC misses non optional parameter of type AccessSpec");
		}
	}

	
	 * @Override protected LLRPBitList encodeBinarySpecific() throws
	 * InvalidLLRPMessageException { // TODO Auto-generated method stub String
	 * bt =
	 * "00000000110011110000000010000010000000000000000000000100110100000000000000000000000000010000000000000000000000000000000000000000000000001101000000000000000001110000000100000000000000010000000011010001000000000011111000000001010100100000000000001111000000010101001100000000000010110110000000000000000000000000000000000000000000000000000000000001010110110000000000011011000001001101000100000000000000000000000000000000010000000000000000000010000000000000011000000000101100000111101000010011010101000000001110101001100010000101010101010111000101000000011000000001010110000000000000010000000001001101001000000000000000000000000000000000000000010101100100000000000001100000000100000010000000001110111100000000000001010000000000000011111111110000000000101000000000000000000001100101000110100000000000000000000000000010100000000011111111110000000000001110000000000000000001100101000110100000000000000000000000000010100100000000000000100000001111111111000000000000111000000000000000000110010100011010000000000000000000000000001111110000000000000000";
	 * LLRPBitList llrpBitList = new LLRPBitList(bt); return llrpBitList; }
	 

}
*/