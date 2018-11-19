package java_smathiv1_cs520p1;
import java.util.ArrayList;

public class DataMemoryInfo {

	private Integer baseAddress;
	private ArrayList<Integer> dataArray;
	
	public DataMemoryInfo() {
		
	}
	
	public DataMemoryInfo(Integer baseAddress, ArrayList<Integer> dataArray) {
		setBaseAddress(baseAddress);
		setDataArray(dataArray);
	}

	public Integer getBaseAddress() {
		return baseAddress;
	}

	public void setBaseAddress(Integer baseAddress) {
		this.baseAddress = baseAddress;
	}

	public ArrayList<Integer> getDataArray() {
		return dataArray;
	}

	public void setDataArray(ArrayList<Integer> dataArray) {
		this.dataArray = dataArray;
	}

}
