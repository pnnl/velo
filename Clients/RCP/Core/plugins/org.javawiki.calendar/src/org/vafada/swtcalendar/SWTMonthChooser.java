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

 *  SWTMonthChooser.java  - A month chooser component for SWT

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



import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;



/**
 */
public class SWTMonthChooser extends Composite {

    private SWTDayChooser dayChooser;

    private Combo comboBox;

    private Locale locale;



    /**
     * Constructor for SWTMonthChooser.
     * @param parent Composite
     */
    public SWTMonthChooser(Composite parent) {

        super(parent, SWT.NONE);



        locale = Locale.getDefault();

        setLayout(new FillLayout());

        comboBox = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);



        initNames();



        setMonth(Calendar.getInstance().get(Calendar.MONTH));

        setFont(parent.getFont());

    }



    private void initNames() {

        DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(locale);

        String[] monthNames = dateFormatSymbols.getMonths();



        int month = comboBox.getSelectionIndex();

        if (comboBox.getItemCount() > 0) {

            comboBox.removeAll();

        }



        for (int i = 0; i < monthNames.length; i++) {

            String name = monthNames[i];

            if (name.length() > 0) {

                comboBox.add(name);

            }

        }



        if (month < 0) {

            month = 0;

        } else if (month >= comboBox.getItemCount()) {

            month = comboBox.getItemCount() - 1;

        }



        comboBox.select(month);

    }



    /**
     * Method addSelectionListener.
     * @param listener SelectionListener
     */
    public void addSelectionListener(SelectionListener listener) {

        comboBox.addSelectionListener(listener);

    }



    /**
     * Method removeSelectionListener.
     * @param listener SelectionListener
     */
    public void removeSelectionListener(SelectionListener listener) {

        comboBox.removeSelectionListener(listener);

    }



    /**
     * Method setMonth.
     * @param newMonth int
     */
    public void setMonth(int newMonth) {

        comboBox.select(newMonth);

    }



    /**
     * Method getMonth.
     * @return int
     */
    public int getMonth() {

        return comboBox.getSelectionIndex();

    }



    /**
     * Method setLocale.
     * @param locale Locale
     */
    public void setLocale(Locale locale) {

        this.locale = locale;

        initNames();

    }



    /* (non-Javadoc)

     * @see org.eclipse.swt.widgets.Control#setFont(org.eclipse.swt.graphics.Font)

     */

    public void setFont(Font font) {

        super.setFont(font);

        comboBox.setFont(getFont());

    }

}

