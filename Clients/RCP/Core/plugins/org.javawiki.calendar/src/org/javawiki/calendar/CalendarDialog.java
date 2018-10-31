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

import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.vafada.swtcalendar.SWTCalendar;
import org.vafada.swtcalendar.SWTCalendarEvent;
import org.vafada.swtcalendar.SWTCalendarListener;

/**
 * @author Tom Seidel
 *
 * @version $Revision: 1.0 $
 */
public class CalendarDialog extends Dialog implements SWTCalendarListener{

    private SWTCalendar swtcal = null;
    SWTCalendarListener listener = null;
    SWTCalendarEvent calendarEvent=null;
    
    private Calendar calendar = null;
    
    /**
     * Constructor for CalendarDialog.
     * @param parentShell Shell
     */
    public CalendarDialog(Shell parentShell) {
        super(parentShell);
        this.calendar = Calendar.getInstance();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell) {
        newShell.setText("Calendar"); //$NON-NLS-1$
        super.configureShell(newShell);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent) {
        
        FormToolkit toolkit = new FormToolkit(Display.getDefault());
        ScrolledForm form = toolkit.createScrolledForm(parent);
        form.getBody().setLayout(new GridLayout(1,false));
        
        Section section = toolkit.createSection(form.getBody(), ExpandableComposite.TITLE_BAR);
        section.setActiveToggleColor(toolkit.getHyperlinkGroup().getActiveForeground());
        GridData gd = new GridData(SWT.CENTER,SWT.CENTER,true,false);
        section.setLayoutData(gd);
        Composite client = toolkit.createComposite(section, SWT.WRAP);
        GridLayout layout = new GridLayout(3, false);        
        client.setLayout(layout);
        toolkit.paintBordersFor(client);
        section.setText("Select your Date..."); //$NON-NLS-1$
        section.setClient(client);
        section.setExpanded(true);
         
        this.swtcal = new SWTCalendar(client);
        gd = new GridData();
        gd.horizontalSpan=3;
        gd.horizontalAlignment = SWT.CENTER;
        gd.verticalAlignment = SWT.CENTER;
        this.swtcal.setLayoutData(gd);
        this.swtcal.addSWTCalendarListener(this);
        this.swtcal.setCalendar(this.calendar);
        this.swtcal.setBackground(new Color(Display.getCurrent(),255,255,255));

        Label c1= new Label(client,SWT.FLAT);
        gd = new GridData(SWT.FILL,SWT.CENTER,true,false);        
        gd.heightHint=20;
        c1.setLayoutData(gd);
        c1.setVisible(false);
        
        Button save= new Button(client,SWT.FLAT);
        gd = new GridData(SWT.END,SWT.CENTER,false,false);        
        gd.heightHint=20;
        save.setText("Save"); //$NON-NLS-1$
        save.setLayoutData(gd);
        
        Button cancel= new Button(client,SWT.FLAT);
        gd = new GridData(SWT.END,SWT.CENTER,false,false);        
        gd.heightHint=20;
        cancel.setText("Cancel"); //$NON-NLS-1$
        cancel.setLayoutData(gd);
        
        cancel.addSelectionListener(new SelectionAdapter(){
        	public void widgetSelected(SelectionEvent e) {
        		close();
        	}
        });
        
        save.addSelectionListener(new SelectionAdapter(){
        	public void widgetSelected(SelectionEvent e) {
        		if(CalendarDialog.this.calendarEvent !=null && CalendarDialog.this.listener!=null)
        			CalendarDialog.this.listener.dateChanged(CalendarDialog.this.calendarEvent);
        		close();
        	}
        });
        
        return client;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected void createButtonsForButtonBar(Composite parent) {
        // do nothing. We don't want any buttons.
    }
    
    /**
     * Method getCalendar.
     * @return Calendar
     */
    public Calendar getCalendar() {
        return this.swtcal.getCalendar();
    }

    /**
     * Method setDate.
     * @param date Date
     */
    public void setDate(Date date) {
       this.calendar = Calendar.getInstance();
        this.calendar.setTime(date);
        
    }

    /**
     * Method addDateChangedListener.
     * @param pListener SWTCalendarListener
     */
    public void addDateChangedListener(SWTCalendarListener pListener) {
        this.listener = pListener;
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
     */
    protected void buttonPressed(int buttonId) {
        this.swtcal.removeSWTCalendarListener(this.listener);
        close();
    }

	/* (non-Javadoc)
	 * @see org.vafada.swtcalendar.SWTCalendarListener#dateChanged(org.vafada.swtcalendar.SWTCalendarEvent)
	 */
	public void dateChanged(SWTCalendarEvent pCalendarEvent) {
		this.calendarEvent = pCalendarEvent;
		
	}
    

    
}
