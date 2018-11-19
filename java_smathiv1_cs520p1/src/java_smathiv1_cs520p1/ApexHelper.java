package java_smathiv1_cs520p1;
import java.util.Arrays;
import java.util.List;

public class ApexHelper {

	public static List<String> R2R_INSTRUCTIONS = Arrays.asList("ADD", "SUB", "MUL", "OR", "EX-OR", "AND", "DIV");
	public static List<String> MEM_INSTRUCTIONS = Arrays.asList("STORE", "LOAD");
	public static List<String> CTRL_FLOW_INSTRUCTIONS = Arrays.asList("BZ", "BNZ", "JUMP", "HALT");
	
	public enum RegisterStatus {
		VALID,
		INVALID
	}

}
