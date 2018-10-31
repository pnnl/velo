/*******************************************************************
 * Copyright (c) 2006 - 2010, Martin Kesting, All rights reserved.
 *
 * This software is licenced under the Eclipse Public License v1.0,
 * see the LICENSE file or http://www.eclipse.org/legal/epl-v10.html
 * for details.
 *******************************************************************/
/*
 * Modified by: Cody Curry, Zoe Guillen
 * 
 * gov.pnnl.velo
 */
package net.sf.jautodoc.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import net.sf.jautodoc.locate.HeaderLocator;
import net.sf.jautodoc.preferences.Constants;
import net.sf.jautodoc.utils.Utils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;


/**
 * Object action delegate for finding file headers.
 */
public class FindHeadersOAD extends AbstractOAD {

    /* (non-Javadoc)
     * @see net.sf.jautodoc.actions.AbstractOAD#getTask(java.lang.Object[], java.lang.Object[])
     */
    protected ITask getTask(final Map<ICompilationUnit, List<IMember>> cus) {
        return new FindHeaderTask(cus.keySet().toArray(new ICompilationUnit[cus.size()]));
    }

    /**
     * Task for finding file headers.
     */
    private class FindHeaderTask implements ITask {

        private ICompilationUnit compUnit;

        private ICompilationUnit[] compUnits;
        private Exception exception;


        /**
         * Instantiates a new find header task.
         *
         * @param compUnits the compilation units
         */
        public FindHeaderTask(ICompilationUnit[] compUnits) {
            this.compUnits = compUnits;
        }

        /* (non-Javadoc)
         * @see net.sf.jautodoc.actions.AbstractOAD.ITask#getCompilationUnit()
         */
        public ICompilationUnit getCompilationUnit() {
            return compUnit;
        }

        /* (non-Javadoc)
         * @see net.sf.jautodoc.actions.AbstractOAD.ITask#checkSuccess()
         */
        public void checkSuccess() throws Exception {
            if (exception != null) {
                throw exception;
            }
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
         */
        public void run(IProgressMonitor monitor)
            throws InvocationTargetException, InterruptedException {
            try {
                Utils.out.println(Constants.CONSOLE_INIT + " - Find Headers");
    
                monitor.beginTask(Constants.TITLE_HEADER_TASK, compUnits.length);
    
                for (int i = 0; i < compUnits.length; i++) {
                    compUnit = compUnits[i];
                    monitor.subTask(compUnit.getElementName());
                    if (monitor.isCanceled()) {
                        break;
                    }
      
                    ITextSelection selection = null;
      
                    IEditorPart editor = Utils.findEditor(compUnit);
                    if (editor != null) { 
                        editor.getEditorSite().getPage().bringToTop(editor);
                        selection = (ITextSelection) editor.getEditorSite()
                            .getSelectionProvider().getSelection();
                    }
      
                    ICompilationUnit workingCopy = Utils.getWorkingCopy(compUnit, editor);
                    HeaderLocator hl = new HeaderLocator(workingCopy);
                    if (selection != null) {
                        hl.setCursorPosition(selection.getOffset());
                    }
                    hl.addJavadoc(new IMember[0], null);
      
                    if (editor == null) {
                        // not open in editor -> commit + discard
                        workingCopy.commitWorkingCopy(false, null);
                        workingCopy.discardWorkingCopy();
                    }
                    else if (editor != null) {
                        ((ITextEditor) editor).selectAndReveal(hl.getCursorPosition(), 0);
                    }
      
                    monitor.worked(1);
                }
            } catch (Exception e) {
                exception = e;
            } finally {
                monitor.done();
                
                Utils.out.println(Constants.CONSOLE_DONE + " - Find Headers");
            }
        }
    }
}
