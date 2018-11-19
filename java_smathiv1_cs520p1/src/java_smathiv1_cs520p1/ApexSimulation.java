package java_smathiv1_cs520p1;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;


public class ApexSimulation {

	private static boolean BRANCH;
	private static int CYCLES;
	private static Integer FETCH_COUNT;
	private static boolean HALT;
	private static Integer MUL_COUNT;
	private static boolean isDataForwarding;
	private static InstructionInfo priorArithmeticObj;

	private static PipelineStage decodeRFStage;
	private static PipelineStage fetchStage;
	private static PipelineStage integerFU;
	private static PipelineStage memStage;
	private static PipelineStage writeBackStage;
	private static ForwardingUnit intDataForwardingUnit;

	private static DataMemoryInfo dataMemory = new DataMemoryInfo();
	private static PipelineFlags flags = new PipelineFlags();
	private static List<CodeLine> instructionCache = new ArrayList<CodeLine>();
	private static List<Register> registerFile = new ArrayList<Register>();
	private static List<Map<String, PipelineStage>> stageInfoArray = new ArrayList<Map<String, PipelineStage>>();

	private static Map<Integer, Integer> forwardingValues = new HashMap<Integer, Integer>();

	public static void main(String[] args) throws FileNotFoundException {
		int cyclesToSimulate = 0;
		int dataForwarding = 0;
		String filepath;

		try {
			filepath = (String) args[0];
		} catch (Exception e) {
			System.out.println("File argument is not provided");
			return;
		}

		Scanner scan = new Scanner(System.in);
		System.out.println("\n\t APEX SIMULATOR \t\n");

		while (true) {
			System.out.println(
					"\n Simulator Commands:\n 1 -- Initialize \n 2 -- Data Forwarding \n 3 -- Simulate \n 4 -- Display \n");
			String option = scan.next();

			switch (option) {
			case "1":
				init(filepath);
				System.out.println("\n\t===== SIMULATOR INITIALIZED =====\t\n");
				break;
			case "2":
				System.out.println("\n\t===== Data Forwarding (1/0 - true/false) =====\t\n");
				dataForwarding = scan.nextInt();
				if (dataForwarding == 1)
					isDataForwarding = true;
				else if (dataForwarding == 0)
					isDataForwarding = false;
				else
					System.out.println("Invalid Input");
				break;
			case "3":
				System.out.println("\n\t===== SIMULATION STARTED =====\t\n");
				System.out.print("Simulate: ");
				cyclesToSimulate = scan.nextInt();
				simulate(cyclesToSimulate);
				System.out.println("\n\t===== SIMULATION COMPLETED =====\t\n");
				break;
			case "4":
				System.out.println("\n\t===== DISPLAY CYCLE STAGES =====\t\n");
				displayStages();
				System.out.println("\n\t===== DISPLAY REGISTERS =====\t\n");
				displayRegisterInfo();
				System.out.println("\n\t===== DISPLAY DATA MEMORY =====\t\n");
				displayDataMemory(100);
				break;
			default:
				System.out.println("Invalid Input");
				continue;
			}
		}
	}

	public static void displayDataMemory(Integer count) {
		Integer index = 0;
		ArrayList<Integer> dataArrays = dataMemory.getDataArray();

		System.out.println("\n*******************************\n\tDATA MEMORY\n*******************************\n");
		if (dataArrays != null) {
			for (Integer dataArray : dataArrays) {
				if (count < 0)
					break;
				
				if (index <= 400)
					System.out.println("D" + index + " ==> " + dataArray);
				else if (dataArray != 0)
					System.out.println("D" + index + " ==> " + dataArray);
				index += 4;
				count = count - 1;
			}
		}
	}

	public static void displayRegisterInfo() {
		System.out.println("\n******************************\n\tREGISTER FILE\n******************************\n");
		for (int i = 0; i < registerFile.size(); i++) {
			System.out.println("R" + i + " ==> " + registerFile.get(i).getValue());
		}
		System.out.println("\n");
	}

	public static void displayStages() {
		displayStages(new HashMap<String, PipelineStage>());
	}

	public static void displayStages(Map<String, PipelineStage> info) {
		List<Map<String, PipelineStage>> sArray = new ArrayList<Map<String, PipelineStage>>();
		
		if (info.isEmpty())
			sArray = stageInfoArray;
		else
			sArray.add(info);

		List<String> pipelineStages = Arrays.asList("FETCH", "D/RF", "Exe-IntFU", "Mem", "WB");
		int cycle = 0;
		for (Map<String, PipelineStage> stageInfo : sArray) {
			System.out.println("\nCycle: " +  (cycle + 1) + "\n");
			for (String stage : pipelineStages) {
				String instName = null;
				InstructionInfo instInfo = stageInfo.get(stage).getOutputInstruction() == null
						? stageInfo.get(stage).getInputInstruction()
								: stageInfo.get(stage).getOutputInstruction();

						if (instInfo != null) {
							for (CodeLine code : instructionCache) {
								if (code.getAddress() == instInfo.getPc()) {
									instName = code.getInstructionString();
								}
							}
						}

						if (instInfo != null) {
							System.out.printf("%-10s :   (I"+((instInfo.getPc() - 4000)/4)+")   " + instName + "   " + (stageInfo.get(stage).isStalled() == true ? "Stalled\n" : " \n"), stage);
						}
						else {
							System.out.printf("%-10s :   Empty \n", stage);
						}
			}
			cycle = cycle + 1;
			System.out.println("\n");
		}
	}

	private static void executeBranching(InstructionInfo instInfo) {
		if (BRANCH == true) {
			FETCH_COUNT = FETCH_COUNT - 2;
			// compute the target PC values
			Integer literal = instInfo.getLiteralValue();
			//Integer pc = ((FETCH_COUNT * 4) + 4000);
			Integer pc = instInfo.getPc();
			Integer targetPC = literal + pc;
			executeJump(instInfo, targetPC);

			// flush current instruction in fetch and decode stage
			integerFU.setOutputInstruction(null);
			decodeRFStage.setOutputInstruction(null);
			decodeRFStage.setInputInstruction(null);
			fetchStage.setOutputInstruction(null);
			decodeRFStage.setStalled(false);
		}
	}

	private static void incrementCycle() {
		CYCLES += 1;
	}

	private static void init(String filepath) throws FileNotFoundException {
		CYCLES = 0;
		HALT = false;
		BRANCH = false;
		FETCH_COUNT = 0;
		MUL_COUNT = 0;
		isDataForwarding = true;
		priorArithmeticObj = null;

		flags = new PipelineFlags();
		dataMemory = new DataMemoryInfo();
		registerFile = new ArrayList<Register>();
		instructionCache = new ArrayList<CodeLine>();
		stageInfoArray = new ArrayList<Map<String, PipelineStage>>();

		intDataForwardingUnit = new ForwardingUnit();
		forwardingValues = new HashMap<Integer, Integer>();

		initRegisterFile();
		initDataMemory();
		initPipelineStages();
		readInstructions(filepath);
	}

	private static void initDataMemory() {
		ArrayList<Integer> dataArray = new ArrayList<Integer>();

		for (int i = 0; i <= 1000; i++) {
			dataArray.add(i, -1);
		}

		dataMemory.setBaseAddress(0);
		dataMemory.setDataArray(dataArray);
	}

	private static void initPipelineStages() {
		fetchStage = new PipelineStage();
		decodeRFStage = new PipelineStage();
		integerFU = new PipelineStage();
		memStage = new PipelineStage();
		writeBackStage = new PipelineStage();
	}

	private static void initRegisterFile() {
		for (int id = 0; id < 16; id++) { // Register files must be 16
			registerFile.add(new Register(0, ApexHelper.RegisterStatus.VALID));
		}
	}

	private static boolean isOutputDependancyExist(InstructionInfo insInfo) {
		if (insInfo.getDestinationRegister() != null) {
			if (registerFile.get(insInfo.getDestinationRegister()).getStatus() == ApexHelper.RegisterStatus.INVALID)
				return true;
		}
		return false;
	}

	private static boolean isDependantValuesAvailable(InstructionInfo insInfo) {
		if (forwardingValues.isEmpty()) {
			for (int srcRegister : insInfo.getSourceRegisterAddrs()) {
				storeForwardingValues(srcRegister);
			}
		} else {
			for (Entry<Integer, Integer> entry : forwardingValues.entrySet()) {
				if (entry.getValue() == null) {
					storeForwardingValues(entry.getKey());
				}
			}
		}
		return !forwardingValues.values().contains(null);
	}

	private static void storeForwardingValues(Integer srcRegister) {
		if (registerFile.get(srcRegister).getStatus() == ApexHelper.RegisterStatus.INVALID) {
			if (intDataForwardingUnit.getDataBus() != null && srcRegister == intDataForwardingUnit.getDataBus().getRegisterAddr()) {
				forwardingValues.put(srcRegister, intDataForwardingUnit.getDataBus().getRegisterValue());
			} else if (intDataForwardingUnit.getDataBus() != null && srcRegister == intDataForwardingUnit.getDataBus().getMemRegisterAddr()) {
				forwardingValues.put(srcRegister, intDataForwardingUnit.getDataBus().getMemRegisterValue());
			} else {
				forwardingValues.put(srcRegister, null);
			}
		} else {
			forwardingValues.put(srcRegister, registerFile.get(srcRegister).getValue());
		}	
	}

	private static boolean isTrueDependancyExist(InstructionInfo insInfo) {
		for (int srcRegister : insInfo.getSourceRegisterAddrs()) {
			if (registerFile.get(srcRegister).getStatus() == ApexHelper.RegisterStatus.INVALID) {
				return true;
			}
		}

		return false;
	}

	private static InstructionInfo parseInstruction(CodeLine instruction) {
		String instName;
		ArrayList<Integer> srcRegisterAddrs = new ArrayList<Integer>();
		ArrayList<Integer> srcRegisterValues = new ArrayList<Integer>();
		Integer literalVal = null;
		Integer destRegister = null;
		Integer targetMemAddr = null;
		Integer targetMemData = null;

		ArrayList<String> instArr = new ArrayList<String>();
		Scanner scan = new Scanner(instruction.getInstructionString());

		while (scan.hasNext()) {
			String token = scan.next().trim();
			if (token.contains(",")) {
				if (token.endsWith(","))
					token = token.substring(0, token.length() - 1);
				if (token.contains(",")) {
					for (String tok : token.split(","))
						instArr.add(tok);
					continue;
				}
				instArr.add(token);
			} else {
				instArr.add(token);
			}
		}

		scan.close();

		instName = instArr.get(0).toUpperCase();

		if (instName.equalsIgnoreCase("ADD") || instName.equalsIgnoreCase("SUB") || instName.equalsIgnoreCase("MUL")
				|| instName.equalsIgnoreCase("AND") || instName.equalsIgnoreCase("EXOR")
				|| instName.equalsIgnoreCase("OR") || instName.equalsIgnoreCase("DIV")) {

			destRegister = Integer.parseInt(instArr.get(1).substring(1));
			srcRegisterAddrs.add(Integer.parseInt(instArr.get(2).substring(1)));
			srcRegisterAddrs.add(Integer.parseInt(instArr.get(3).substring(1)));
		} else if (instName.equalsIgnoreCase("STORE")) {
			srcRegisterAddrs.add(Integer.parseInt(instArr.get(1).substring(1)));
			srcRegisterAddrs.add(Integer.parseInt(instArr.get(2).substring(1)));
			if (instArr.size() == 4)
				literalVal = Integer.parseInt(instArr.get(3).substring(1));
			else
				literalVal = 0;
		} else if (instName.equalsIgnoreCase("LOAD") || instName.equalsIgnoreCase("JAL")) {
			destRegister = Integer.parseInt(instArr.get(1).substring(1));
			srcRegisterAddrs.add(Integer.parseInt(instArr.get(2).substring(1)));
			if (instArr.size() == 4)
				literalVal = Integer.parseInt(instArr.get(3).substring(1));
			else
				literalVal = 0;
		} else if (instName.equalsIgnoreCase("MOVC")) {
			destRegister = Integer.parseInt(instArr.get(1).substring(1));
			literalVal = Integer.parseInt(instArr.get(2).substring(1));
		} else if (instName.equalsIgnoreCase("JUMP")) {
			srcRegisterAddrs.add(Integer.parseInt(instArr.get(1).substring(1)));
			literalVal = Integer.parseInt(instArr.get(2).substring(1));
		} else if (instName.equalsIgnoreCase("BZ") || instName.equalsIgnoreCase("BNZ")) {
			literalVal = Integer.parseInt(instArr.get(1).substring(1));
		}

		return new InstructionInfo(instruction.getAddress(), instName, srcRegisterAddrs, srcRegisterValues, literalVal,
				destRegister, targetMemAddr, targetMemData);
	}

	private static void readInstructions(String filePath) throws FileNotFoundException {
		int fileLineNumber = 1;
		int address = 4000;
		Scanner scan = new Scanner(new File(filePath));
		scan.useDelimiter(System.getProperty("line.separator"));

		while (scan.hasNextLine()) {
			instructionCache.add(new CodeLine(fileLineNumber, address, scan.nextLine()));
			fileLineNumber += 1;
			address += 4;
		}

		scan.close();
	}

	private static void setDecodeStage() {
		if (decodeRFStage.getInputInstruction() == null && fetchStage.getOutputInstruction() != null
				&& !decodeRFStage.isStalled() && !HALT) {
			InstructionInfo instInfo = fetchStage.getOutputInstruction();
			if (!integerFU.isStalled()) {
				fetchStage.setOutputInstruction(null);
				decodeRFStage.setInputInstruction(instInfo);
				decodeRFStage.setStalled(false);
			}
		}
	}

	private static void setExecuteStage() {
		if (decodeRFStage.getOutputInstruction() != null) {
			if (integerFU.getInputInstruction() == null && !integerFU.isStalled()) {
				InstructionInfo instInfo = decodeRFStage.getOutputInstruction();
				decodeRFStage.setOutputInstruction(null);
				integerFU.setInputInstruction(instInfo);
			}
		}
	}

	private static void setFetchStage() {
		writeBackStage.setOutputInstruction(null);

		if (fetchStage.getInputInstruction() == null && instructionCache.size() > FETCH_COUNT
				&& !decodeRFStage.isStalled() && !HALT && fetchStage.getOutputInstruction() == null) {
			CodeLine instruction = instructionCache.get(FETCH_COUNT);
			InstructionInfo instInfo = parseInstruction(instruction);
			fetchStage.setOutputInstruction(instInfo);
			FETCH_COUNT += 1;
		}
	}

	private static void setMemoryStage() {
		if (memStage.getInputInstruction() == null) {
			if (integerFU.getOutputInstruction() != null) {
				InstructionInfo instInfo = integerFU.getOutputInstruction();
				integerFU.setOutputInstruction(null);
				memStage.setInputInstruction(instInfo);
				integerFU.setStalled(false);
			}
		}
	}

	private static void setSrcRegisterValues(InstructionInfo instInfo, boolean isDependant) {
		ArrayList<Integer> srcRegisterValues = new ArrayList<Integer>();
		for (int srcRegisterAddr : instInfo.getSourceRegisterAddrs()) {
			if (!isDependant) {
				srcRegisterValues.add(registerFile.get(srcRegisterAddr).getValue());
			} else {
				srcRegisterValues.add(forwardingValues.get(srcRegisterAddr));
			}
		}

		instInfo.setSourceRegisterValues(srcRegisterValues);
	}

	private static void setWriteBackStage() {
		if (writeBackStage.getInputInstruction() == null && memStage.getOutputInstruction() != null) {
			InstructionInfo instInfo = memStage.getOutputInstruction();
			memStage.setOutputInstruction(null);
			writeBackStage.setInputInstruction(instInfo);
		}
	}

	private static void simulate(Integer simulate_cycles) {
		while (CYCLES < simulate_cycles) {
			Map<String, PipelineStage> stageInfo = new HashMap<String, PipelineStage>();

			setWriteBackStage();
			setMemoryStage();
			setExecuteStage();
			setDecodeStage();
			setFetchStage();

			// Execution of WriteBack Stage
			if (writeBackStage.getInputInstruction() != null) {
				InstructionInfo instInfo = writeBackStage.getInputInstruction();

				if (priorArithmeticObj == instInfo)
					priorArithmeticObj = null;

				if (instInfo.getInstructionString().equalsIgnoreCase("HALT")) {
					//writeBackStage.setStalled(true);
					HALT = false;
				}

				if (writeBackStage.getInputInstruction().getDestinationRegister() != null) {
					writeBackStage.setInputInstruction(null);

					Register register = registerFile.get(instInfo.getDestinationRegister());
					Integer targetData = instInfo.getTargetMemData();
					register.setValue(targetData);
					register.setStatus(ApexHelper.RegisterStatus.VALID);

					if (ApexHelper.R2R_INSTRUCTIONS.contains(instInfo.getInstructionString())) {
						if (targetData != null && targetData == 0)
							flags.setZero(true);
						else
							flags.setZero(false);

						if (targetData != null && targetData < 0)
							flags.setNegative(true);
						else
							flags.setNegative(false);
					}
				} else {
					writeBackStage.setInputInstruction(null);
				}

				writeBackStage.setOutputInstruction(instInfo);
			}

			// Execution of Memory Stage
			if (memStage.getInputInstruction() != null) {
				InstructionInfo instInfo = memStage.getInputInstruction();
				memStage.setInputInstruction(null);
				Integer targetIndex;

				switch (instInfo.getInstructionString()) {
				case "LOAD":
					targetIndex = Math.floorDiv(instInfo.getTargetMemAddress(), 4);
					if (targetIndex <= 1000) {
						Integer destVal = dataMemory.getDataArray().get(targetIndex);
						instInfo.setTargetMemData(destVal);
					} else {
						System.out.println("*******DATA MEMORY --- Index Out Of Bound Exception*********");
						return;
					}
					break;
				case "STORE":
					targetIndex = Math.floorDiv(instInfo.getTargetMemAddress(), 4);
					if (targetIndex <= 1000) {
						dataMemory.getDataArray().add(targetIndex, instInfo.getTargetMemData());
					} else {
						System.out.println("*******DATA MEMORY --- Index Out Of Bound Exception*********");
						return;
					}
					break;
				}

				memStage.setOutputInstruction(instInfo);
				if (ApexHelper.R2R_INSTRUCTIONS.contains(instInfo.getInstructionString().toUpperCase()) 
						|| instInfo.getInstructionString().equalsIgnoreCase("MOVC")) {
					ForwardingUnit.DataBus dataBus = new ForwardingUnit.DataBus();
					dataBus.setMemRegisterAddr(instInfo.getDestinationRegister());
					dataBus.setMemRegisterValue(instInfo.getTargetMemData());
					PipelineFlags flags = new PipelineFlags();
					flags.setZero(false);

					if (instInfo.getTargetMemData() == 0) {
						flags.setZero(true);
					}
					if (isDataForwarding) {
						intDataForwardingUnit.setDataBus(dataBus);
						intDataForwardingUnit.setFlagBus(flags);
					}
				}
			}

			// Execution of IntegerFU in Execute Stage
			if (integerFU.getInputInstruction() != null) {
				InstructionInfo instInfo = integerFU.getInputInstruction();
				if (instInfo.getInstructionString().equalsIgnoreCase("MUL") && MUL_COUNT == 0) {
					integerFU.setStalled(true);
					MUL_COUNT = MUL_COUNT + 1;
				} else if ((MUL_COUNT == 1 && instInfo.getInstructionString().equalsIgnoreCase("MUL")) || !instInfo.getInstructionString().equalsIgnoreCase("MUL")) {
					integerFU.setStalled(false);
					MUL_COUNT = 0;

					integerFU.setInputInstruction(null);

					if (instInfo.getDestinationRegister() != null)
						registerFile.get(instInfo.getDestinationRegister()).setStatus(ApexHelper.RegisterStatus.INVALID);

					Integer source1 = 0;
					Integer source2 = 0;
					Integer literal = instInfo.getLiteralValue();
					String InstName = instInfo.getInstructionString();

					if (ApexHelper.R2R_INSTRUCTIONS.contains(InstName)) {
						source1 = instInfo.getSourceRegisterValues().get(0);
						source2 = instInfo.getSourceRegisterValues().get(1);
						priorArithmeticObj = instInfo;
					}

					switch (InstName) {
					case "ADD":
						instInfo.setTargetMemData(source1 + source2);
						break;
					case "SUB":
						instInfo.setTargetMemData(source1 - source2);
						break;
					case "AND":
						instInfo.setTargetMemData(source1 & source2);
						break;
					case "OR":
						instInfo.setTargetMemData(source1 | source2);
						break;
					case "MUL":
						instInfo.setTargetMemData(source1 * source2);
						break;
					case "EXOR":
						instInfo.setTargetMemData(source1 ^ source2);
						break;
					case "MOVC":
						instInfo.setTargetMemData(instInfo.getLiteralValue());
						break;
					case "LOAD":
						source1 = instInfo.getSourceRegisterValues().get(0);
						instInfo.setTargetMemAddress(source1 + literal);
						break;
					case "STORE":
						source2 = instInfo.getSourceRegisterValues().get(1);
						instInfo.setTargetMemAddress(source2 + literal);
						source1 = instInfo.getSourceRegisterValues().get(0);
						instInfo.setTargetMemData(source1);
						break;
					case "JUMP":
						FETCH_COUNT = FETCH_COUNT - 2;
						source1 = instInfo.getSourceRegisterValues().get(0);
						Integer targetAddrs = source1 + literal;
						executeJump(instInfo, targetAddrs);

						decodeRFStage.setInputInstruction(null);
						fetchStage.setOutputInstruction(null);
						decodeRFStage.setStalled(false);
						break;
					case "BZ":
						executeBranching(instInfo);
						break;
					case "BNZ":
						executeBranching(instInfo);
						break;
					case "HALT":
						integerFU.setStalled(true);
						decodeRFStage.setStalled(true);
						decodeRFStage.setInputInstruction(null);
						fetchStage.setOutputInstruction(null);
						decodeRFStage.setStalled(false);
						HALT = true;
						FETCH_COUNT = FETCH_COUNT - 2;
						break;
					}

					integerFU.setOutputInstruction(instInfo);

					if (ApexHelper.R2R_INSTRUCTIONS.contains(instInfo.getInstructionString().toUpperCase()) 
							|| instInfo.getInstructionString().equalsIgnoreCase("MOVC")) {
						ForwardingUnit.DataBus dataBus = intDataForwardingUnit.getDataBus();
						if (dataBus == null) {
							dataBus = new ForwardingUnit.DataBus();
						}
						dataBus.setRegisterAddr(instInfo.getDestinationRegister());
						dataBus.setRegisterValue(instInfo.getTargetMemData());
						PipelineFlags flags = new PipelineFlags();
						flags.setZero(false);

						if (instInfo.getTargetMemData() == 0) {
							flags.setZero(true);
						}
						if (isDataForwarding) {
							intDataForwardingUnit.setDataBus(dataBus);
							intDataForwardingUnit.setFlagBus(flags);
						}
					}
				}
			}

			// Execution of Decode Stage
			if (decodeRFStage.getInputInstruction() != null) {
				InstructionInfo instInfo = decodeRFStage.getInputInstruction();

				if (isOutputDependancyExist(instInfo)) {
					decodeRFStage.setStalled(true);
				} else if(isTrueDependancyExist(instInfo)) {
					if (isDependantValuesAvailable(instInfo) && isDataForwarding) {
						decodeRFStage.setStalled(false);
						setSrcRegisterValues(instInfo, true);
						decodeRFStage.setOutputInstruction(instInfo);
						decodeRFStage.setInputInstruction(null);
						forwardingValues.clear();
					} else {
						decodeRFStage.setStalled(true);
					}

					if (isDataForwarding)
						intDataForwardingUnit.clearForwardingUnit();
				} else if (instInfo.getInstructionString().equals("BNZ")
						|| instInfo.getInstructionString().equals("BZ")) {
					decodeRFStage.setStalled(true);
					if (priorArithmeticObj == null) {
						decodeRFStage.setStalled(false);
						decodeRFStage.setOutputInstruction(instInfo);
						decodeRFStage.setInputInstruction(null);
						if (!flags.isZero() && instInfo.getInstructionString().equals("BNZ"))
							BRANCH = true;
						else if (flags.isZero() && instInfo.getInstructionString().equals("BZ"))
							BRANCH = true;
						else
							BRANCH = false;
					}else if (isDataForwarding) { 
						if (intDataForwardingUnit.getDataBus() == null) {
							decodeRFStage.setStalled(true);
						} else {
							boolean isBranch = false;
							if (intDataForwardingUnit.getDataBus() != null 
									&& (priorArithmeticObj.getDestinationRegister() == intDataForwardingUnit.getDataBus().getRegisterAddr() 
									|| priorArithmeticObj.getDestinationRegister() == intDataForwardingUnit.getDataBus().getMemRegisterAddr())) {
								if (!intDataForwardingUnit.getFlagBus().isZero() && instInfo.getInstructionString().equals("BNZ"))
									isBranch = true;
								else if (intDataForwardingUnit.getFlagBus().isZero() && instInfo.getInstructionString().equals("BZ"))
									isBranch = true;
							}

							if (isBranch) {
								BRANCH = true;
							} else {
								BRANCH = false;
							}

							decodeRFStage.setStalled(false);
							decodeRFStage.setOutputInstruction(instInfo);
							decodeRFStage.setInputInstruction(null);

							if (isDataForwarding)
								intDataForwardingUnit.clearForwardingUnit();
						}
					} else {
						decodeRFStage.setStalled(true);
					}
				} else {
					decodeRFStage.setStalled(false);
					setSrcRegisterValues(instInfo, false);
					decodeRFStage.setOutputInstruction(instInfo);
					decodeRFStage.setInputInstruction(null);

					if (isDataForwarding) {
						forwardingValues.clear();
						intDataForwardingUnit.clearForwardingUnit();
					}
				}
			}

			stageInfo.put("FETCH", new PipelineStage(fetchStage.getInputInstruction(),
					fetchStage.getOutputInstruction(), fetchStage.isStalled()));
			stageInfo.put("D/RF", new PipelineStage(decodeRFStage.getInputInstruction(),
					decodeRFStage.getOutputInstruction(), decodeRFStage.isStalled()));
			stageInfo.put("Exe-IntFU", new PipelineStage(integerFU.getInputInstruction(),
					integerFU.getOutputInstruction(), integerFU.isStalled()));
			stageInfo.put("Mem", new PipelineStage(memStage.getInputInstruction(), memStage.getOutputInstruction(),
					memStage.isStalled()));
			stageInfo.put("WB", new PipelineStage(writeBackStage.getInputInstruction(),
					writeBackStage.getOutputInstruction(), writeBackStage.isStalled()));
			stageInfoArray.add(stageInfo);

			//displayStages(stageInfo);
			incrementCycle();
		}
	}

	public static void executeJump(InstructionInfo instInfo, Integer targetAddrs) {
		//Integer pcValue = ((FETCH_COUNT * 4) + 4000);
		Integer pcValue = instInfo.getPc();

		if (targetAddrs < pcValue) {
			FETCH_COUNT = Math.floorDiv((targetAddrs - 4000), 4);
		} else {
			FETCH_COUNT = Math.floorDiv((targetAddrs - 4000), 4);
		}
	}
}
