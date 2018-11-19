package java_smathiv1_cs520p1;
import java.util.ArrayList;

public class InstructionInfo {

	private Integer pc;
	private String instructionString;
	private ArrayList<Integer> sourceRegisterAddrs;
	private ArrayList<Integer> sourceRegisterValues;
	private Integer literalValue;
	private Integer destinationRegister;
	private Integer targetMemAddress;
	private Integer targetMemData;
	
	public InstructionInfo() {
	}
	
	public InstructionInfo(Integer pc, String instString, ArrayList<Integer> srcRegisterAddrs, ArrayList<Integer> srcRegisterValues, Integer literal, Integer destRegister, Integer targetMemAddr, Integer targetMemData) {
		setPc(pc);
		setInstructionString(instString);
		setSourceRegisterAddrs(srcRegisterAddrs);
		setSourceRegisterValues(srcRegisterValues);
		setLiteralValue(literal);
		setDestinationRegister(destRegister);
		setTargetMemAddress(targetMemAddr);
		setTargetMemData(targetMemData);
	}

	public Integer getPc() {
		return pc;
	}

	public void setPc(Integer pc) {
		this.pc = pc;
	}

	public String getInstructionString() {
		return instructionString;
	}

	public void setInstructionString(String instructionString) {
		this.instructionString = instructionString;
	}


	public Integer getDestinationRegister() {
		return destinationRegister;
	}

	public void setDestinationRegister(Integer destinationRegister) {
		this.destinationRegister = destinationRegister;
	}

	public Integer getTargetMemAddress() {
		return targetMemAddress;
	}

	public void setTargetMemAddress(Integer targetMemAddress) {
		this.targetMemAddress = targetMemAddress;
	}

	public Integer getTargetMemData() {
		return targetMemData;
	}

	public void setTargetMemData(Integer targetMemData) {
		this.targetMemData = targetMemData;
	}

	public Integer getLiteralValue() {
		return literalValue;
	}

	public void setLiteralValue(Integer literalValue) {
		this.literalValue = literalValue;
	}

	public ArrayList<Integer> getSourceRegisterAddrs() {
		return sourceRegisterAddrs;
	}

	public void setSourceRegisterAddrs(ArrayList<Integer> sourceRegisterAddrs) {
		this.sourceRegisterAddrs = sourceRegisterAddrs;
	}

	public ArrayList<Integer> getSourceRegisterValues() {
		return sourceRegisterValues;
	}

	public void setSourceRegisterValues(ArrayList<Integer> sourceRegisterValues) {
		this.sourceRegisterValues = sourceRegisterValues;
	}

}
