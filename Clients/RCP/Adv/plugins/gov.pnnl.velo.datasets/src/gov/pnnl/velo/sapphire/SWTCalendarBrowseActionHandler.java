package gov.pnnl.velo.sapphire;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.forms.BrowseActionHandler;
import org.eclipse.sapphire.ui.forms.PropertyEditorCondition;
import org.eclipse.sapphire.ui.forms.PropertyEditorPart;
import org.eclipse.sapphire.ui.forms.swt.FormComponentPresentation;
import org.eclipse.sapphire.util.MutableReference;
import org.javawiki.calendar.CalendarDialog;
import org.vafada.swtcalendar.SWTCalendarEvent;
import org.vafada.swtcalendar.SWTCalendarListener;

/**
 * Opens a calendar to allow date selection. Activates if the property is a value property of type Date.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class SWTCalendarBrowseActionHandler extends BrowseActionHandler {
  public static final String ID = "Sapphire.Browse.SWTCalendar";

  public SWTCalendarBrowseActionHandler() {
    setId(ID);
  }

  @Override
  protected String browse(final Presentation context) {
    final MutableReference<String> result = new MutableReference<String>();
    final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
    final SWTCalendarListener fromDateChangedListener = new SWTCalendarListener() {
      public void dateChanged(SWTCalendarEvent event) {
        property().write(event.getCalendar().getTime());
        result.set(sdf.format(event.getCalendar().getTime()));
      }
    };

    // Shell shell = ((FormComponentPresentation) context).shell();
    // Point cursorLocation = shell.getDisplay().getCursorLocation();
    // Point popupLocation = ((PropertyEditorPresentation2) context).getActionPopupPosition(cursorLocation.x, cursorLocation.y);
    // shell.setLocation(popupLocation);
    // CalendarDialog calDialog = new CalendarDialog(shell);
    CalendarDialog calDialog = new CalendarDialog(((FormComponentPresentation) context).shell());
    final String existing = (String) property().content();

    if (existing != null) {
      try {
        calDialog.setDate(sdf.parse(existing));
        // calDialog.setDate(existing);
      } catch (ParseException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    calDialog.addDateChangedListener(fromDateChangedListener);
    calDialog.setBlockOnOpen(true);
    calDialog.open();
    return result.get();

    // final Popup dialog = new Popup( ( (FormComponentPresentation) context ).shell(), null )
    // {
    // private DateTime calendar;
    //
    // @Override
    // protected Point getInitialLocation( final Point size )
    // {
    // return ( (PropertyEditorPresentation2) context ).getActionPopupPosition( size.x, size.y );
    // }
    //
    // @Override
    // protected Control createContentArea( final Composite parent )
    // {
    // this.calendar = new DateTime( parent, SWT.CALENDAR );
    //
    // final Date existing = (Date) property().content();
    //
    // if( existing != null )
    // {
    // final Calendar cal = Calendar.getInstance();
    // cal.setTime( existing );
    //
    // this.calendar.setYear( cal.get( Calendar.YEAR ) );
    // this.calendar.setMonth( cal.get( Calendar.MONTH ) );
    // this.calendar.setDay( cal.get( Calendar.DATE ) );
    // }
    //
    // this.calendar.addMouseListener
    // (
    // new MouseAdapter()
    // {
    // @Override
    // public void mouseDoubleClick( final MouseEvent event )
    // {
    // registerSelectionAndClose();
    // }
    // }
    // );
    //
    // this.calendar.addKeyListener
    // (
    // new KeyAdapter()
    // {
    // @Override
    // public void keyPressed( final KeyEvent event )
    // {
    // if( event.character == SWT.CR )
    // {
    // registerSelectionAndClose();
    // }
    // }
    // }
    // );
    //
    // return calendar;
    // }
    //
    // private void registerSelectionAndClose()
    // {
    // final Calendar cal = Calendar.getInstance();
    // cal.set( this.calendar.getYear(), this.calendar.getMonth(), this.calendar.getDay() );
    // final Date date = cal.getTime();
    //
    // result.set( property().service( MasterConversionService.class ).convert( date, String.class ) );
    //
    // close();
    // }
    // };
    //
    // dialog.setBlockOnOpen( true );
    //
    // dialog.open();
    //
    // return result.get();
  }

  public static final class Condition extends PropertyEditorCondition {
    @Override
    protected boolean evaluate(final PropertyEditorPart part) {
      return (part.property().definition().getTypeClass() == Date.class);
    }
  }

}