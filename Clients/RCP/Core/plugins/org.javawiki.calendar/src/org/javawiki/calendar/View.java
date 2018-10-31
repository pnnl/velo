/*******************************************************************************
 * .
 *                           Velo 1.0
 *  ----------------------------------------------------------
 * 
 *            Pacific Northwest National Laboratory
 *                      Richland, WA 99352
 * 
 *                      Copyright (c) 2013
 *            Pacific Northwest National Laboratory
 *                 Battelle Memorial Institute
 * 
 *   Velo is an open-source collaborative content management
 *                and job execution environment
 *              distributed under the terms of the
 *           Educational Community License (ECL) 2.0
 *   A copy of the license is included with this distribution
 *                   in the LICENSE.TXT file
 * 
 *                        ACKNOWLEDGMENT
 *                        --------------
 * 
 * This software and its documentation were developed at Pacific 
 * Northwest National Laboratory, a multiprogram national
 * laboratory, operated for the U.S. Department of Energy by 
 * Battelle under Contract Number DE-AC05-76RL01830.
 ******************************************************************************/
/*
 * Copyright (c) 2005 Tom Seidel. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment:
 *
 *       "This product includes software developed by Tom Seidel on javawiki.org.
 *        (http://www.javawiki.org/)."
 *
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL TOM SEIDEL
 * OR OTHER CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.javawiki.calendar;



import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;
import org.vafada.swtcalendar.SWTCalendarEvent;
import org.vafada.swtcalendar.SWTCalendarListener;

/**
 * 
 * The ViewPart that provides the date-text.
 * @author Tom Seidel
 * @version $Revision: 1.0 $
 */
public class View extends ViewPart {
    public static final String ID = "org.javawiki.calendar.view"; //$NON-NLS-1$
    public static final String DATE_PATTERN = "yyyy-MM-dd"; //$NON-NLS-1$
    final SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
    ScrolledForm form;
    CalendarDialog dialog;
    
    SWTCalendarListener dateChangedListener = new SWTCalendarListener() {
        public void dateChanged(SWTCalendarEvent event) {
            View.this.dateText.setText(View.this.sdf.format(event.getCalendar().getTime()));
        }
    };
    
    
    Text dateText;
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent) {
        FormToolkit toolkit = new FormToolkit(Display.getDefault());
        this.form = toolkit.createScrolledForm(parent);
        GridLayout layout = new GridLayout(1, false);
        this.form.getBody().setLayout(layout);
        
        
        
        Section section = toolkit.createSection(this.form.getBody(), ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
        section.setActiveToggleColor(toolkit.getHyperlinkGroup().getActiveForeground());
        section.addExpansionListener(new ExpansionAdapter() {
            public void expansionStateChanged(ExpansionEvent e) {
                View.this.form.reflow(true);
            }
        }); 
        section.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
        Composite client = toolkit.createComposite(section, SWT.WRAP);
        client.setLayout(layout);
        toolkit.paintBordersFor(client);
        section.setText("Click on the date.."); //$NON-NLS-1$
        section.setClient(client);
        section.setExpanded(true);
        
        this.dateText = toolkit.createText(client,getFormattedData(),SWT.CENTER);
        GridData gd = new GridData(SWT.BEGINNING,SWT.BEGINNING,false,false);
        gd.widthHint = 100;
        this.dateText.setLayoutData(gd);
        this.dateText.setEditable(false);
        this.dateText.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
        this.dateText.addListener(SWT.MouseUp,new Listener() {
            public void handleEvent(Event event) {
                try {
                    View.this.dialog = new CalendarDialog(getViewSite().getShell());
                    View.this.dialog.setDate(View.this.sdf.parse(View.this.dateText.getText()));
                    View.this.dialog.addDateChangedListener(View.this.dateChangedListener);
                    View.this.dialog.open();
                } catch (ParseException e1) {
                    MessageDialog.openError(getViewSite().getShell(),"Format-Error","Couldn't parse date."); //$NON-NLS-1$ //$NON-NLS-2$
                }
                
            }
        });
        
        
        
        
    }
    
    /**
     * Returns the formatted date-string.
    
     * @return the formatted date-string. */
    private String getFormattedData() {
        return this.sdf.format(new Date());
    }
    
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus() {
        this.form.setFocus();
    }
}
