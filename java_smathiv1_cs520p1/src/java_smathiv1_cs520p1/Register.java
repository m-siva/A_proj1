package java_smathiv1_cs520p1;

public class Register {
	
	private Integer value;
	private ApexHelper.RegisterStatus status;
	
	public Register() {	
	}

	public Register(Integer value, ApexHelper.RegisterStatus status) {
		setValue(value);
		setStatus(status);
	}
	
	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		this.value = value;
	}

	public ApexHelper.RegisterStatus getStatus() {
		return status;
	}

	public void setStatus(ApexHelper.RegisterStatus status) {
		this.status = status;
	}
}
