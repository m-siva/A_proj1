package java_smathiv1_cs520p1;

public class ForwardingUnit {
	
	public static class DataBus {
		private Integer registerAddr;
		private Integer registerValue;
		private Integer memRegisterAddr;
		private Integer memRegisterValue;
		
		public DataBus() {
			
		}

		public Integer getRegisterAddr() {
			return registerAddr;
		}

		public void setRegisterAddr(Integer registerAddr) {
			this.registerAddr = registerAddr;
		}

		public Integer getRegisterValue() {
			return registerValue;
		}

		public void setRegisterValue(Integer registerValue) {
			this.registerValue = registerValue;
		}

		public Integer getMemRegisterAddr() {
			return memRegisterAddr;
		}

		public void setMemRegisterAddr(Integer memRegisterAddr) {
			this.memRegisterAddr = memRegisterAddr;
		}

		public Integer getMemRegisterValue() {
			return memRegisterValue;
		}

		public void setMemRegisterValue(Integer memRegisterValue) {
			this.memRegisterValue = memRegisterValue;
		}
	}
	
	private DataBus dataBus;
	private PipelineFlags flagBus;
	
	public ForwardingUnit() {
		
	}

	public ForwardingUnit(DataBus dataBus, PipelineFlags flagBus) {
		setDataBus(dataBus);
		setFlagBus(flagBus);
	}

	public DataBus getDataBus() {
		return dataBus;
	}

	public void setDataBus(DataBus dataBus) {
		this.dataBus = dataBus;
	}

	public PipelineFlags getFlagBus() {
		return flagBus;
	}

	public void setFlagBus(PipelineFlags flagBus) {
		this.flagBus = flagBus;
	}
	
	public void clearForwardingUnit() {
		this.setDataBus(null);
		this.setFlagBus(null);
	}
}
