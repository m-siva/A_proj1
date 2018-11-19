package java_smathiv1_cs520p1;

public class PipelineFlags {
	
	private boolean zero;
	private boolean carry;
	private boolean negative;
	
	public PipelineFlags() {
		
	}

	public PipelineFlags(boolean zero, boolean carry, boolean negative) {
		setZero(zero);
		setCarry(carry);
		setNegative(negative);
	}
	
	public boolean isZero() {
		return zero;
	}

	public void setZero(boolean zero) {
		this.zero = zero;
	}

	public boolean isCarry() {
		return carry;
	}

	public void setCarry(boolean carry) {
		this.carry = carry;
	}

	public boolean isNegative() {
		return negative;
	}

	public void setNegative(boolean negative) {
		this.negative = negative;
	}

}
