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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jautodoc.about.AboutGenerator;
import net.sf.jautodoc.preferences.AboutConstants;
import net.sf.jautodoc.preferences.Constants;
import net.sf.jautodoc.source.JavadocFormatter;
import net.sf.jautodoc.utils.Utils;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;


/**
 * Object action delegate for generating about information
 */
public class AddAboutInfoOAD extends AbstractOAD {

  /* (non-Javadoc)
   * @see net.sf.jautodoc.actions.AbstractOAD#getTask(java.lang.Object[], java.lang.Object[])
   */
  protected ITask getTask(final Map<ICompilationUnit, List<IMember>> cus) {
    return new AddAboutInfoTask(cus);
  }

  /**
   * Task for generating about information
   */
  private class AddAboutInfoTask implements ITask {

    private Exception exception;
    private ICompilationUnit compUnit;

    private final Map<ICompilationUnit, List<IMember>> cus;


    /**
     * Instantiates a new add aboutinfo task.
     *
     * @param compUnits the compilation units
     * @param members the selected members
     */
    public AddAboutInfoTask(final Map<ICompilationUnit, List<IMember>> cus) {
      this.cus = cus;
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
        Utils.out.println(Constants.CONSOLE_INIT + " - Add About Info");

        monitor.beginTask(Constants.TITLE_JDOC_TASK, cus.size());

//        Set<File> projectSet = new HashSet<File>();
        
        final IStructuredSelection selection = (IStructuredSelection) workbenchPage.getSelection();
        
        if (selection.getFirstElement() instanceof IJavaProject) {
          final Object[] projects = selection.toArray();

          for (int i = 0; i < projects.length; ++i) {
            File projectFile = new File(((IJavaProject)projects[i]).getProject().getLocation().toOSString());
            
            if (projectFile.exists()) {
              monitor.subTask(projectFile.getName());
              Utils.out.print(projectFile.getName());
              
              if (monitor.isCanceled()) {
                Utils.out.println("\tCANCELED");
                break;
              }
              
              if (AboutConstants.DEBUG_MODE) System.out.println(AboutConstants.DEBUG + projectFile.getPath());
              
              try {                      
                AboutGenerator aboutGen = new AboutGenerator(projectFile.getPath());
                aboutGen.populateJarLibrary();
                aboutGen.generateAboutFiles();
                aboutGen.generateAboutHTML();

                if (AboutConstants.DEBUG_MODE) {
                  FileUtils.deleteQuietly(aboutGen.getAboutFilesDir());
                  FileUtils.deleteQuietly(new File(aboutGen.getAboutFilePath()));
                }
              } catch (Exception e) {
                e.printStackTrace();
                Utils.out.println("\tFAILED");
                monitor.worked(1);
                continue;
              }
              
              monitor.worked(1);

              Utils.out.println("\tCOMPLETED");
              
            } else {
              Utils.out.println(Constants.LINE_SEPARATOR + "\tSKIPPING: Non-existing file \"" + projectFile.getName() + "\"");
            }
          }
        } else {
          Utils.out.println("ERROR: Not a selection of Java projects.");
        }

//        for (Map.Entry<ICompilationUnit, List<IMember>> entry : cus.entrySet()) {
//          compUnit = entry.getKey();
//          monitor.subTask(compUnit.getElementName());
//
//          if (monitor.isCanceled()) {
//            break;
//          }
//          File projectFile = new File (compUnit.getJavaProject().getProject().getLocation().toOSString());
//
//          if (projectFile.exists()) {
//            projectSet.add(projectFile);
//          }
//          monitor.worked(1);
//        }

//        if (AboutConstants.DEBUG_MODE) System.out.println(AboutConstants.DEBUG + projectSet.toString());

//        for (File project : projectSet) {
//          monitor.subTask(project.getName());
//
//          Utils.out.print(project.getName() + "... ");
//
//          if (monitor.isCanceled()) {
//            Utils.out.println("CANCELED");
//            break;
//          }
//
//          if (AboutConstants.DEBUG_MODE) System.out.println(AboutConstants.DEBUG + project.getPath());
//
//          try {                      
//            AboutGenerator aboutGen = new AboutGenerator(project.getPath());
//            aboutGen.populateJarLibrary();
//            aboutGen.generateAboutFiles();
//            aboutGen.generateAboutHTML();
//
//            if (AboutConstants.DEBUG_MODE) {
//              FileUtils.deleteQuietly(aboutGen.getAboutFilesDir());
//              FileUtils.deleteQuietly(new File(aboutGen.getAboutFilePath()));
//            }
//          } catch (Exception e) {
//            e.printStackTrace();
//            Utils.out.println("FAILED");
//            monitor.worked(1);
//            continue;
//          }
//
//          monitor.worked(1);
//
//          Utils.out.println("COMPLETED");
//        }
      } catch (Exception e) {
        exception = e;
      } finally {
        JavadocFormatter.getInstance().stopFormatting();
        monitor.done();

        Utils.out.println(Constants.CONSOLE_DONE + " - Add About Info");
      }
    }
  }
}
