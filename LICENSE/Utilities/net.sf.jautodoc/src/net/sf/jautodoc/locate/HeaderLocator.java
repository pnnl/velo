package net.sf.jautodoc.locate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.ui.console.IHyperlink;

import net.sf.jautodoc.preferences.Configuration;
import net.sf.jautodoc.preferences.Constants;
import net.sf.jautodoc.source.AbstractSourceProcessor;
import net.sf.jautodoc.utils.SourceUtils;
import net.sf.jautodoc.utils.Utils;

public class HeaderLocator extends AbstractSourceProcessor {
  private int cursorPosition;
  private int cursorOffset;

  public HeaderLocator(ICompilationUnit compUnit) {
    this(compUnit, null);
  }

  public HeaderLocator(ICompilationUnit compUnit, Configuration  config) {
    super(compUnit, config);
  }

  public void addJavadoc(IMember[] members, IProgressMonitor monitor) throws Exception {
    doProcessing(members, monitor);
  }

  public void setCursorPosition(int cursorPosition) {
    this.cursorOffset   = 0;
    this.cursorPosition = cursorPosition;
  }

  public int getCursorPosition() {
    return cursorPosition + cursorOffset;
  }

  @Override
  protected void startProcessing() {}

  @Override
  protected void processFileHeader() throws Exception {
    findFileHeader();
  }

  @Override
  protected void processTodoForAutodoc(final IMember[] members) throws Exception {}

  @Override
  protected void processMember(final IMember member) throws Exception {}

  @Override
  protected void stopProcessing() throws Exception {}

  @Override
  protected String getTaskName() {
    return Constants.TITLE_JDOC_TASK;
  }

  private void findFileHeader() throws Exception {
    /**
     * TODO: Figure out a more optimized way to get header comments from top of document
     *    without getting entire document first. Original implementation stops before 'package'
     *    or 'import' line (whichever is first).
     */
//    final ISourceReference element = SourceUtils.getPackageOrImportReference(compUnit);
//    if (element == null) {
//      return;
//    }
//
//    final ISourceRange range = element.getSourceRange();
//    final ISourceRange headerRange = SourceUtils.findCommentSourceRange(document, 0, range.getOffset()
//        + range.getLength(), commentScanner, !config.isMultiCommentHeader());
//    if (headerRange.getLength() > 0 && !config.isReplaceHeader()) {
//      return;
//    }
//
//    final String existingHeader = document.get(headerRange.getOffset(), headerRange.getLength());

    final String existingHeader = document.get();

    Pattern p = Pattern.compile(Constants.MATCH_COMMENT);
    Matcher m = p.matcher(existingHeader);

    String msgOut = " in " + compUnit.getPath();

    //    IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(compUnit.getPath());
    //    //    FileLink fileLink = new FileLink(file, null, -1, -1, -1);
    //    IHyperlink link;

    int legalCount = 0;
    boolean veloFound = false;

    while (m.find()) {
      if (m.group().toLowerCase().contains("gnu")) {
        Utils.out.println(Constants.CONSOLE_FOUND + Constants.CONSOLE_GNU + msgOut);
        return;
      }

      if (!veloFound && m.group().matches(Constants.MATCH_VELO)) {
        veloFound = true; 
      }

      if (m.group().toLowerCase().contains("copyright") || m.group().toLowerCase().contains("license")) {
        if (++legalCount > 1) {
          Utils.out.println(Constants.CONSOLE_FOUND + Constants.CONSOLE_MULTIPLE + msgOut);
          return;
        }
      }
    }

    if (!veloFound) {
      Utils.out.println(Constants.CONSOLE_MISSING + Constants.CONSOLE_NO_VELO + msgOut);
      return;
    }

    if (legalCount < 1) {
      Utils.out.println(Constants.CONSOLE_MISSING + Constants.CONSOLE_NOT_FOUND + msgOut);
    }
  }
}
