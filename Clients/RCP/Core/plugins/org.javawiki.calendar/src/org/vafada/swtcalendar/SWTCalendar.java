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
 *  SWTCalendar.java  - A calendar component for SWT
 *  Author: Mark Bryan Yu
 *  Modified by: Sergey Prigogin
 *  swtcalendar.sourceforge.net
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.vafada.swtcalendar;

import java.util.Calendar;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 */
public class SWTCalendar extends Composite {
    /**
     * Style constant for making Sundays red.
     */
    public static final int RED_SUNDAY = SWTDayChooser.RED_SUNDAY;
    /**
     * Style constant for making weekends red.
     */
    public static final int RED_WEEKEND = SWTDayChooser.RED_WEEKEND;
  
    private boolean settingDate;

    private Spinner yearChooser;
    private SWTMonthChooser monthChooser;
    private SWTDayChooser dayChooser;
    private boolean settingYearMonth;

    /**
     * Constructs a calendar control.
     *
     * @param parent a parent container.
     * @param style  FLAT to make the buttons flat, or NONE.
     */
    public SWTCalendar(Composite parent, int style) {
        super(parent, (style & ~(SWT.FLAT | RED_WEEKEND)));

        Calendar calendar = Calendar.getInstance();

        {
            final GridLayout gridLayout = new GridLayout();
            gridLayout.marginHeight = 0;
            gridLayout.marginWidth = 0;
            gridLayout.horizontalSpacing = 2;
            gridLayout.verticalSpacing = 2;
            setLayout(gridLayout);
        }

        final Composite header = new Composite(this, SWT.NONE);
        header.setBackground(new Color(Display.getCurrent(),255,255,255));
        {
            {
                final GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
                header.setLayoutData(gridData);
                final GridLayout gridLayout = new GridLayout();
                gridLayout.numColumns = 3;
                gridLayout.marginWidth = 0;
                gridLayout.marginHeight = 0;
                header.setLayout(gridLayout);
            }

            final Button prevMonthButton = new Button(header, SWT.ARROW | SWT.LEFT | SWT.CENTER | (style & SWT.FLAT));
            prevMonthButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL));
            prevMonthButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    previousMonth();
                }
            });

            final Composite composite = new Composite(header, SWT.NONE);
            composite.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_CENTER));
            
            {
                final GridLayout gridLayout = new GridLayout();
                gridLayout.numColumns = 2;
                gridLayout.marginWidth = 0;
                gridLayout.marginHeight = 0;
                composite.setLayout(gridLayout);
            }
            header.setTabList(new Control[]{composite});

            monthChooser = new SWTMonthChooser(composite);
            monthChooser.setLayoutData(new GridData(GridData.FILL_VERTICAL));
            monthChooser.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    if (!settingYearMonth) {
                        dayChooser.setMonth(monthChooser.getMonth());
                    }
                }
            });

            yearChooser = new Spinner(composite, SWT.BORDER);
            yearChooser.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL));
            yearChooser.setMinimum(1);
            yearChooser.setMaximum(9999);
            yearChooser.setValue(calendar.get(Calendar.YEAR));
            yearChooser.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    if (!settingYearMonth) {
                        dayChooser.setYear(yearChooser.getValue());
                    }
                }
            });

            final Button nextMonthButton = new Button(header, SWT.ARROW | SWT.RIGHT | SWT.CENTER | (style & SWT.FLAT));
            nextMonthButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL));
            nextMonthButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    nextMonth();
                }
            });
        }

        {
            dayChooser = new SWTDayChooser(this, SWT.BORDER | (style & RED_WEEKEND));
            GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
            gridData.horizontalSpan = 3;
            dayChooser.setLayoutData(gridData);
            dayChooser.addSWTCalendarListener(new SWTCalendarListener() {
                public void dateChanged(SWTCalendarEvent event) {
                    refreshYearMonth(event.getCalendar());
                }
            });
        }

        setTabList(new Control[]{header, dayChooser});

        setFont(parent.getFont());
    }

    /**
     * Constructor for SWTCalendar.
     * @param parent Composite
     */
    public SWTCalendar(Composite parent) {
        this(parent, SWT.FLAT);
    }

    /**
     * Method setCalendar.
     * @param cal Calendar
     */
    public void setCalendar(Calendar cal) {
        settingDate = true;
        try {
            refreshYearMonth(cal);
            dayChooser.setCalendar(cal);
        } finally {
            settingDate = false;
        }
    }

    /**
     * Method refreshYearMonth.
     * @param cal Calendar
     */
    private void refreshYearMonth(Calendar cal) {
        settingYearMonth = true;
        yearChooser.setValue(cal.get(Calendar.YEAR));
        monthChooser.setMonth(cal.get(Calendar.MONTH));
        settingYearMonth = false;
    }

    public void nextMonth() {
        Calendar cal = dayChooser.getCalendar();
        cal.add(Calendar.MONTH, 1);
        refreshYearMonth(cal);
        dayChooser.setCalendar(cal);
    }

    public void previousMonth() {
        Calendar cal = dayChooser.getCalendar();
        cal.add(Calendar.MONTH, -1);
        refreshYearMonth(cal);
        dayChooser.setCalendar(cal);
    }

    /**
     * Method getCalendar.
     * @return Calendar
     */
    public Calendar getCalendar() {
        return dayChooser.getCalendar();
    }

    /**
     * Method addSWTCalendarListener.
     * @param listener SWTCalendarListener
     */
    public void addSWTCalendarListener(SWTCalendarListener listener) {
        dayChooser.addSWTCalendarListener(listener);
    }

    /**
     * Method removeSWTCalendarListener.
     * @param listener SWTCalendarListener
     */
    public void removeSWTCalendarListener(SWTCalendarListener listener) {
        dayChooser.removeSWTCalendarListener(listener);
    }

    /**
     * Method setLocale.
     * @param locale Locale
     */
    public void setLocale(Locale locale) {
        monthChooser.setLocale(locale);
        dayChooser.setLocale(locale);
        yearChooser.setValue(getCalendar().get(Calendar.YEAR));
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.widgets.Control#setFont(org.eclipse.swt.graphics.Font)
     */
    public void setFont(Font font) {
        super.setFont(font);
        monthChooser.setFont(font);
        yearChooser.setFont(font);
        dayChooser.setFont(font);
    }

    /**
     * Method isSettingDate.
     * @return boolean
     */
    public boolean isSettingDate() {
        return settingDate;
    }
}
