package abc.containers;

import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import vabc.ABCStyle;
import vabc.IABCUserObject;
import datamodel.DataItem;
import datamodel.Key;
import datamodel.collections.RecursiveDataItemMap;

/**
 * ABC UI's will be made of components which will contain
 * other components and primitives.  Each level in the hierarchy will
 * know only about its children, not its parents (except ABC parent?). 
 * 
 * @author port091
 */
public abstract class ABCComponent extends JPanel {

	private static final long serialVersionUID = 1L;

	// If this component is in recursive collection
	// the identifier will be the key in the parent
	// recursive collection's childCollections map
	protected DataItem identifier; // Might be null
	
	// This level and down, we will apply rules to 
	// only children
	protected RecursiveDataItemMap rCollection;
	
	// Component children of this component.  We need
	// to keep the hierarchy  for expanded lists
	protected List<ABCComponent> components;	

	// Will contain primitives of this component only
	// not any of this components component children's
	// primitives
	protected List<ABCPrimitive> primitives;
	
	// Separate from the key, this is the label for the
	// border
	protected String label;
		
	protected ABC abcParent;
	
  private IABCUserObject userObject;
  
  private boolean dynamic;
  
	public ABCComponent(ABC abcParent, String key, String label, DataItem identifier, IABCUserObject userObject) {
    
	  if(identifier != null) {
			this.identifier = identifier;
			if(identifier.getLinkedObject() != null) {
			  Object potentialUserObject = abcParent.dataProvider.getObject(identifier.getLinkedObject());
			  if(potentialUserObject instanceof IABCUserObject) {
			    this.userObject = (IABCUserObject)potentialUserObject;
			  }
			}
		} else if(key != null) { 
      // If the user specifies a key for a section, then we'll put the data in a sub map
      this.identifier = new DataItem(new Key(key, label));
      
      Object potentialUserObject = abcParent.dataProvider.getObject(key);
      if(potentialUserObject instanceof IABCUserObject) {
        this.userObject = (IABCUserObject)potentialUserObject;
      }
    }
		
		if(userObject != null && this.userObject == null)
		  this.userObject = userObject; // In case it was attached to the parent? 
		
		this.label = label;		
		rCollection = new RecursiveDataItemMap();
		primitives = new ArrayList<ABCPrimitive>();
		components = new ArrayList<ABCComponent>();
		setBackground(ABCStyle.style().getBackgroundColor());
		if(label != null) {
			setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(ABCStyle.style().getBorderColor()), label, 
					TitledBorder.LEFT, TitledBorder.CENTER, ABCStyle.style().getBorderFont()));	
		} 
		
		this.abcParent = abcParent;
		this.dynamic = false;
		
	}
	
	public String getLabel() {
		return label;
	}	

	public DataItem getIdentifier() {
		return identifier;
	}

	public ABC getABCContainer() {
		return abcParent;
	}
	
	public RecursiveDataItemMap getRCollection() {
		return rCollection;
	}	
	
	public List<ABCPrimitive> getPrimitives() {
		return primitives;
	}
	
	public List<ABCComponent> getABCComponents() {
		return components;
	}
	
	public int getLabelLength() {
		return abcParent.labelLength;
	}
	
	public int getUnitLength() {
		return abcParent.unitLength;
	}	
	
	public ABCComponent getABCComponent(DataItem trigger) {
    if(identifier != null && identifier.equals(trigger))
      return this; // This one...    
		// Search for this data item in our primitives
		for(ABCPrimitive primitive: primitives) {
			if(primitive.getData().contains(trigger)) {
				return this;
			}
		}
		// Didn't find it at this level, check my children
		for(ABCComponent component: components) {
			ABCComponent child = component.getABCComponent(trigger);
			if(child != null)
				return child;
		}
		return null;
	}
	
	/**
	 * Shows elements at this level only, not in children
	 * @param key
	 * @param shouldShow
	 * @return true if at least one instance of the key was found
	 */
	public boolean show(Key key, boolean shouldShow) {
		boolean found = false;
		for(ABCPrimitive primitive: primitives) {
			if(primitive.show(key, shouldShow)) {
				found = true;
				// Will show all instances
			}
		}
		// This one too
		if(key == null || (identifier != null && identifier.matches(key))) {
			this.setVisible(shouldShow);
			if(this.getParent() instanceof ABCTabs) {
				System.out.println("TODO, ABCComponent.123, parent is tab and child is trying to hide...remove the tab?");
			}
		}
		return found;
	}
	
	/**
	 * Recursive show, shows elements in children
	 * @param key
	 * @param shouldShow
	 * @return true if at least one instance of the key was found
	 */
	public boolean rShow(Key key, boolean shouldShow) {
		boolean found = show(key, shouldShow);
		for(ABCComponent childComponent: components) {
			if(childComponent.rShow(key, shouldShow))
				found = true;
		}
		return found;
	}
	
	public DataItem find(Key key) {
		DataItem di = null;
		for(ABCPrimitive primitive: primitives) {
		  di = primitive.find(key);
			if(di != null) {
				break;
			}
		}		
		return di;	 
	  
	}

	public DataItem rFind(Key key) {
    DataItem di = find(key);
    if (di != null) return di;

    for(ABCComponent childComponent: components) {
      /*
      if(childComponent.getIdentifier() != null) {
        if(childComponent.getIdentifier().getKey().equals(key))
          return true;
      }
      */
      di = childComponent.rFind(key);
      if (di != null)
        break;
    }
    return di;
  }
	
	public boolean exists(Key key) {
	  
		boolean found = false;
		for(ABCPrimitive primitive: primitives) {
			if(primitive.exists(key)) {
				found = true;
				break;
			}
		}		
		return found;	 
	}
	
	public boolean rExists(Key key) {
    boolean found = exists(key);
    if (found) return found;
    for(ABCComponent childComponent: components) {
      if(childComponent.getIdentifier() != null) {
        if(childComponent.getIdentifier().getKey().equals(key))
          return true;
      }
      if(childComponent.rExists(key))
        found = true;
    }
    return found;
  }
	
	/**
	 * Enables elements at this level only, not in children
	 * @param key
	 * @param shouldEnable
	 * @return
	 */
	public boolean enable(Key key, boolean shouldEnable) {
	//  System.out.println("enable: " + key + ", " + shouldEnable);
		boolean found = false;
		for(ABCPrimitive primitive: primitives) {
			if(primitive.enable(key, shouldEnable)) {
				found = true;
			}
		}
    if(key != null && key.getAlias().equalsIgnoreCase("Transport")) {
   //   System.out.println("HERE");
    }
		// If our key is this level, enable everything under it? 
		if(key != null && (identifier != null && identifier.matches(key))) {
			rEnable(null, shouldEnable);	
			return true;			
		} 
		
		return found;
	}
	
	/**
	 * Recursive enable, enables elements in children
	 * @param key
	 * @param shouldEnable
	 * @return true if at least one instance of the key was found
	 */
	public boolean rEnable(Key key, boolean shouldEnable) {
	  boolean found = enable(key, shouldEnable);
		for(ABCComponent childComponent: components) {
			if(childComponent.rEnable(key, shouldEnable)) {
				found = true;  // do not break - may be more of them
			}
		}
		return found;
	}


  /**
   * ReadOnlys elements at this level only, not in children
   * @param key
   * @param isReadOnly
   * @return
   */
  public boolean readOnly(Key key, boolean isReadOnly) {
    boolean found = false;
    for(ABCPrimitive primitive: primitives) {
      if(primitive.readOnly(key, isReadOnly)) {
        found = true;
      }
    }

    // If our key is this level, ReadOnly everything under it? 
    if(key != null && (identifier != null && identifier.matches(key))) {
      rReadOnly(null, isReadOnly);  
      return true;      
    } 
    
    return found;
  }
  
  /**
   * Recursive ReadOnly, ReadOnlys elements in children
   * @param key
   * @param isReadOnly
   * @return true if at least one instance of the key was found
   */
  public boolean rReadOnly(Key key, boolean isReadOnly) {
    boolean found = readOnly(key, isReadOnly);
    for(ABCComponent childComponent: components) {
      if(childComponent.rReadOnly(key, isReadOnly)) {
        found = true;  // do not break - may be more of them
      }
    }
    return found;
  }

	
	/**
	 * Searches for a given key, returns activity of found primitive
	 * @param key
	 * @return true if key is found and primitive is active
	 */
	public boolean isActive(Key key) {
		for(ABCPrimitive primitive: primitives) {
			if(primitive.isActive(key))
				return true;
		}
		return false;
	}
	
	/**
	 * Recursively searches for a given key,
	 * returns activity of found primitive
	 * @param key
	 * @return true if key is found and primitive is active
	 */
	public boolean rIsActive(Key key) {
		if(isActive(key))
			return true; // Found it at our level
		
		// Try sub components
		for(ABCComponent subComponent: components) {
			if(subComponent.rIsActive(key))
				return true;
		}
		return false;
	}
	
	
	/**
	 * Searches for a given key, selects the UI component if found
	 * @param key
	 * @param shouldSelect
	 * @return true if the key was found
	 */
	public boolean select(Key key, boolean shouldSelect) {
		boolean selected = false;
		for(ABCPrimitive primitive: primitives) {
			if(primitive.select(key, shouldSelect))
				selected = true;
		}		
		return selected;
	}

	/**
	 * Recursively searches for a given key, selects the UI component if found
	 * @param key
	 * @param shouldSelect
	 * @return true if the key was found
	 */
	public boolean rSelect(Key key, boolean shouldSelect) {
		boolean selected = select(key, shouldSelect);
		for(ABCComponent childComponent: components) {			
			if(childComponent.rSelect(key, shouldSelect))
				selected = true;
		}
		return selected;		
	}	
	
	public boolean isSelected(Key key) {
		for(ABCPrimitive primitive: primitives) {
			if(primitive.isSelected(key))
				return true;
		}
		return false;
	}
	
	public boolean rIsSelected(Key key) {
		if(isSelected(key))
			return true; // Found it at our level
		
		// Try sub components
		for(ABCComponent subComponent: components) {
			if(subComponent.rIsSelected(key))
				return true;
		}
		return false;
	}
	
	public boolean isDynamic() {
	  return dynamic;
	}
	
	public void addComponent(ABCComponent component) {
	  	  	  
	  // We have two components (this and component), if either is just an empty shell,
	  // we'll go ahead and merge their data into the the other... this should avoid cases
	  // where we would otherwise end up with many levels in our rCollection
	  
		// Merge into this level
		if(component.getIdentifier() == null) {
	
			// Component has no data, bipass its collection, merge into this ones
			if(component.rCollection.getCollection().isEmpty() && component.rCollection.getSubCollection().isEmpty()) {
			
				for(ABCPrimitive primitive: component.getPrimitives())
					addPrimitive(primitive); // Add to our list of primitives, merge the data into this collection
				
				this.rCollection.copyObserversFrom(component.rCollection);	// Move observers to our collection
				component.rCollection = this.rCollection; // Set the components collection to this one
				component.primitives = this.primitives;
				component.components = this.components; // We will now bipass this component
				
				if(component.isDynamic())
				  this.dynamic = true;
			
				// Component has no children, move data over and merge
			}  else if(component.rCollection.getSubCollection().isEmpty()) {
	
			  for(ABCPrimitive primitive: component.getPrimitives())
					addPrimitive(primitive); // Add to our list of primitives, merge the data into this collection
				
				// Make sure we merged all the collection items
				boolean found = false;
				for(DataItem theirItem: component.rCollection.getCollection()) {
					found = false;
					for(DataItem ourItem: this.rCollection.getCollection()) {
						if(theirItem.equals(ourItem)) {
							found = true;
							break;
						}
					}
					if(!found) {
						System.out.println("PROBLEM, ABCComponent.351" + component + ", " + this);
						break;
					}
				}
				
				if(found) {

					this.rCollection.copyObserversFrom(component.rCollection);	// Move observers to our collection
					component.rCollection = this.rCollection; // Set the components collection to this one
					component.components = this.components; // We will now bipass this component
					component.primitives = this.primitives;
					
		       if(this.isDynamic()) // And inherit its dynamicism .. that ought to be a word
		         component.dynamic = true;
						
				} else {
					System.out.println("PROBLEM, TODO: Fix, ABCComponent.364" + component + ", " + this);;
				}
				// We have no data, put our primitives into the components
			} else if(this.rCollection.getCollection().isEmpty() && this.rCollection.getSubCollection().isEmpty()) {
       
				for(ABCPrimitive primitive: this.getPrimitives())	
					component.addPrimitive(primitive); // Add to our list of primitives, merge the data into this collection

				component.rCollection.copyObserversFrom(this.rCollection);	// Move observers to our collection
				this.rCollection = component.rCollection; // Set the components collection to this one
				this.components = component.components; // We will now bipass this component
				this.primitives = component.primitives;
				
        if(component.isDynamic())
          this.dynamic = true;
				
			} else if(this.rCollection.getSubCollection().isEmpty()) {

			  for(ABCPrimitive primitive: this.getPrimitives())
					component.addPrimitive(primitive); // Add to our list of primitives, merge the data into this collection
				
				// Make sure we merged all the collection items
				boolean found = false;
				for(DataItem ourItem: this.rCollection.getCollection()) {
					found = false;
					for(DataItem theirItem: component.rCollection.getCollection()) {
						if(ourItem.equals(theirItem)) {
							found = true;
							break;
						}
					}
					if(!found) {
						System.out.println("PROBLEM, ABCComponent.393" + component + ", " + this);
						break;
					}
				}
				
				if(found) {
				
					component.rCollection.copyObserversFrom(this.rCollection);	// Move observers to our collection
					this.rCollection = component.rCollection; // Set the components collection to this one
					this.components = component.components; // We will now bipass this component
					this.primitives = component.primitives;
					
	        if(component.isDynamic())
	          this.dynamic = true;
	        
				} else {
					System.out.println("PROBLEM, TODO: Fix, ABCComponent.406" + component + ", " + this);
				}
				
			} else {
			
			  // One case where we end up here, we are adding a section with children to another section with children...
				
				if(component.rCollection.getCollection().isEmpty() && this.rCollection.getCollection().isEmpty() && 
				    !component.rCollection.getSubCollection().isEmpty() && !this.rCollection.getSubCollection().isEmpty()) {
				  
				  // Merge in the child collections from the component?
				  for(DataItem item: component.rCollection.getSubCollection().keySet()) {
				    this.rCollection.put(item, component.rCollection.getSubCollection().get(item), ABCComponent.this);
				  }
		      
				  // Move the primitives
	        for(ABCPrimitive primitive: component.getPrimitives())
	          addPrimitive(primitive); // Add to our list of primitives, merge the data into this collection
	        
	        // Merge the components?
	        this.components.addAll(component.getABCComponents());
	        
	        this.rCollection.copyObserversFrom(component.rCollection);  // Move observers to our collection
	        component.rCollection = this.rCollection; // Set the components collection to this one
	        component.primitives = this.primitives;
	        component.components = this.components; // We will now bipass this component
	        
	        if(component.isDynamic())
	          this.dynamic = true;
				  
				} else {
	        System.out.println("PROBLEM, TODO: Fix, ABCComponent.411" + component + ", " + this);
				}
				
			}
			
		} else {
			// Add as a child component
			components.add(component);
			this.rCollection.put(component.getIdentifier(),  component.getRCollection(), this);
		}

		addComponentToUI(component);
	}
	
	protected void addPrimitive(ABCPrimitive primitive) {
	  // Components with no data do not get added to the UI??
	  if(primitive.getData().isEmpty())
	    return; // Skip it?
	  
		primitives.add(primitive);
		// This should be the call we care about
		// for sending a new item event??
		rCollection.merge(primitive.getData(), this);		
	}
	
	
	public abstract void addComponentToUI(ABCComponent component);
	
	@Override
	public String toString() {
		return this.getClass().getName() + (identifier != null ? identifier : label);
	}

	public void selectNextComponent(ABCPrimitive current, boolean next) {
		System.out.println("Selecting next component");
		ABCPrimitive previousPrimitive = null;
		ABCPrimitive nextPrimitive = null;
		for(int i = 0; i < primitives.size(); i++) {
			if(primitives.get(i).equals(current)) {
				if(i > 1)
					previousPrimitive = primitives.get(i-1);
				if(i + 1 < primitives.size())
					nextPrimitive = primitives.get(i+1);
			}			
		}
		
		if(next && nextPrimitive != null) {
			nextPrimitive.transferFocusToFirst();
			return;
		}
		if(!next && previousPrimitive != null) {
			previousPrimitive.transferFocusToLast();
			return;
		}
		
		// Transfer focus to first component?
		for(ABCComponent component: components) {
			for(ABCPrimitive primitive: component.getPrimitives()) {
				primitive.transferFocusToFirst(); // ??? will this work??
				return;
			}
		}
		
		
	}

  public IABCUserObject getUserObject() {
    return userObject;
  }

}
