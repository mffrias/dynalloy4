package ar.uba.dc.rfm.dynalloy.visualization;

public class AlloyCommand {

	private String fullCommand;
	private String assertionName;

	public AlloyCommand(String fullCommand, String assertionName) {
		super();
		setFullCommand(fullCommand);
		setAssertionName(assertionName);
	}
	
	public String getFullCommand() {
		return fullCommand;
	}

	public void setFullCommand(String fullCommand) {
		this.fullCommand = fullCommand;
	}

	public String getAssertionName() {
		return assertionName;
	}

	public void setAssertionName(String assertionName) {
		this.assertionName = assertionName;
	}
	
	@Override
	public String toString() {
		return fullCommand;
	}

}
