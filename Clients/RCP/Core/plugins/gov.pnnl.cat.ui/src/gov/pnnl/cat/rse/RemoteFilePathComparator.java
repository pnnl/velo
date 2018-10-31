package gov.pnnl.cat.rse;

import java.util.Comparator;

import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;

/**
 * Compare the path of two remote files for order.  We are providing a case-insensitive ordering.
 *
 */
public class RemoteFilePathComparator implements Comparator<IRemoteFile> {

    public int compare(IRemoteFile file1, IRemoteFile file2) {
        return file1.getAbsolutePath().compareToIgnoreCase(file2.getAbsolutePath());
    }
    
    
}
