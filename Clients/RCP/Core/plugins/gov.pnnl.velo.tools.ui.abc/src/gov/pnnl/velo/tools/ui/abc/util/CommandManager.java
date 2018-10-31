package gov.pnnl.velo.tools.ui.abc.util;

import java.util.Stack;
import java.util.Observable;

/**
 * For now assumes all command execution will be synchronous as it is being executed
 * in the UI thread.  If we decide to run commands in a background thread (doubtful),
 * then we need to synchronize the stack.
 * @author D3K339
 *
 */
public class CommandManager extends Observable {  
  public final int MAX_UNDO_STACK = 20;
  private Stack<Command> undoStack = new Stack<Command>();
  
  public CommandManager() {  
  
  }
  
  /**
   * Create new command and puts it on the undo stack if it executed
   * successfully.
   * @param commandId
   * @param parameters
   */
  public void executeCommand(Command command) { 
    try {
      command.execute();
      
      if(command.isUndoable()) {
        undoStack.push(command);

        // If the undo stack is over the size limit, get rid of the 
        // bottom element
        if(undoStack.size() > MAX_UNDO_STACK) {
          undoStack.remove(0);
        }
        fireUndoStackChanged();
      }
    } catch (Throwable e) {
      // undo the command to roll back changes
      if(command.isUndoable()) {
        command.undo();
      }
      throw new RuntimeException(e);
    }
  }
  
  private void fireUndoStackChanged() {
    setChanged();
    notifyObservers();    
  }
  
  /**
   * Undo the last undoable command that was executed
   */
  public void undo() {
    Command command = undoStack.pop();
    if(command != null) {
      command.undo();
      fireUndoStackChanged();
    }
  }
  
  public Command undoStackPeek() {
    return undoStack.peek();
  }

  public int undoStackSize() {
    return undoStack.size();
  }
    
}
