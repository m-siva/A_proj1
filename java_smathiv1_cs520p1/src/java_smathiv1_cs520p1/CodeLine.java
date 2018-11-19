package java_smathiv1_cs520p1;

public class CodeLine {
	private Integer fileLineNumber;
	private Integer address;
	private String instructionString;
	
	public CodeLine() {
		
	}
	
	public CodeLine(Integer fileLineNumber, Integer address, String instructionString) {
		setFileLineNumber(fileLineNumber);
		setAddress(address);
		setInstructionString(instructionString);
	}

	public Integer getFileLineNumber() {
		return fileLineNumber;
	}

	public void setFileLineNumber(Integer fileLineNumber) {
		this.fileLineNumber = fileLineNumber;
	}

	public Integer getAddress() {
		return address;
	}

	public void setAddress(Integer address) {
		this.address = address;
	}

	public String getInstructionString() {
		return instructionString;
	}

	public void setInstructionString(String instructionString) {
		this.instructionString = instructionString;
	}
	
}
