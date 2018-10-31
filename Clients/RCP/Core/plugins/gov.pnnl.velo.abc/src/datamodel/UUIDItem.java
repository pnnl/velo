package datamodel;

import java.util.UUID;

public class UUIDItem extends NamedItem {

	private static final long serialVersionUID = 1L;
	
	protected UUID uuid;
	
	public UUIDItem(String alias) {
		super(alias);
		uuid = UUID.randomUUID();
	}
	
	public UUID getUUID() {
		return uuid;
	}
	
	@Override
	public boolean equals(Object object) {
		if(object instanceof UUIDItem) {
			return ((UUIDItem)object).getUUID().equals(uuid);
		}
		return false;
	}
}
