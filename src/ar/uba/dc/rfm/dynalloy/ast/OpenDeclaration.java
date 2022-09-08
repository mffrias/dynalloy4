package ar.uba.dc.rfm.dynalloy.ast;

public final class OpenDeclaration {

	private final String moduleId;
	private final String aliasModuleId;

	public OpenDeclaration(String moduleId, String aliasModuleId) {
		super();
		this.moduleId = moduleId;
		this.aliasModuleId = aliasModuleId;
	}

	public String getModuleId() {
		return moduleId;
	}

	public String getAliasModuleId() {
		return aliasModuleId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((aliasModuleId == null) ? 0 : aliasModuleId.hashCode());
		result = prime * result
				+ ((moduleId == null) ? 0 : moduleId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final OpenDeclaration other = (OpenDeclaration) obj;
		if (aliasModuleId == null) {
			if (other.aliasModuleId != null)
				return false;
		} else if (!aliasModuleId.equals(other.aliasModuleId))
			return false;
		if (moduleId == null) {
			if (other.moduleId != null)
				return false;
		} else if (!moduleId.equals(other.moduleId))
			return false;
		return true;
	}

	public Object accept(IDynalloyVisitor visitor) {
		return visitor.visit(this);
	}

	@Override
	public String toString() {
		return "open " + this.getModuleId() + " as " + this.getAliasModuleId() ;
	}

}
