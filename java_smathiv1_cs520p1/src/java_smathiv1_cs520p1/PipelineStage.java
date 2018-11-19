package java_smathiv1_cs520p1;

public class PipelineStage {
	
	private InstructionInfo inputInstruction;
	private InstructionInfo outputInstruction;
	private boolean isStalled = false;
	
	public PipelineStage() {

	}
	
	public PipelineStage(InstructionInfo input, InstructionInfo output, boolean isStalled) {
		setInputInstruction(input);
		setOutputInstruction(output);
		setStalled(isStalled);
	}
	
	public InstructionInfo getInputInstruction() {
		return inputInstruction;
	}

	public void setInputInstruction(InstructionInfo inputInstruction) {
		this.inputInstruction = inputInstruction;
	}

	public InstructionInfo getOutputInstruction() {
		return outputInstruction;
	}

	public void setOutputInstruction(InstructionInfo outputInstruction) {
		this.outputInstruction = outputInstruction;
	}

	public boolean isStalled() {
		return isStalled;
	}

	public void setStalled(boolean stalled) {
		this.isStalled = stalled;
	}

}
