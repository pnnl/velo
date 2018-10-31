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
package gov.pnnl.cat.core.resources.tests;


/**
 */
public class InstanceOfPerformanceTest extends CatTest {
//	private static final int MULTIPLIER = 100000;
//  protected static Logger logger = CatLogger.getLogger(InstanceOfPerformanceTest.class);
////	private static final String STRINGNAME = "java.lang.String";
//
//	public void testPerformance() throws ResourceException {
//		Vector resources = new Vector();
//		CmsPath path = new CmsPath("/physical/");
//		IResource referenceLibrary = this.mgr.getResource(path);
//		addChildren(referenceLibrary, resources);
//
//		Vector filesOnly = filterResources(resources, IResource.FILE);
//		Vector foldersOnly = filterResources(resources, IResource.FOLDER);
//
////		System.out.println("Found " + resources.size() + " resources:");
////		System.out.println("\t" + filesOnly.size() + " files");
////		System.out.println("\t" + foldersOnly.size() + " folders");
//    logger.debug("Found " + resources.size() + " resources:");
//    logger.debug("\t" + filesOnly.size() + " files");
//    logger.debug("\t" + foldersOnly.size() + " folders");
//
//		long t1 = doInstanceOf(resources);
//		long t2 = doIsType(resources);
//		//System.out.println("\nRUN 1 (mixed):\ninstanceof: " + t1 + " ms\nisType(): " + t2 + " ms\n");
//    logger.debug("\nRUN 1 (mixed):\ninstanceof: " + t1 + " ms\nisType(): " + t2 + " ms\n");
//		long t3 = doInstanceOf(filesOnly);
//		long t4 = doIsType(filesOnly);
//		//System.out.println("RUN 2 (miss):\ninstanceof: " + t3 + " ms\nisType(): " + t4 + " ms\n");
//    logger.debug("RUN 2 (miss):\ninstanceof: " + t3 + " ms\nisType(): " + t4 + " ms\n");
//		long t5 = doInstanceOf(foldersOnly);
//		long t6 = doIsType(foldersOnly);
//		//System.out.println("RUN 3 (hit):\ninstanceof: " + t5 + " ms\nisType(): " + t6 + " ms\n");
//    logger.debug("RUN 3 (hit):\ninstanceof: " + t5 + " ms\nisType(): " + t6 + " ms\n");
//	}
//
//	private Vector filterResources(Vector resources, int type) throws ResourceException {
//		Vector filteredResources = new Vector();
//
//		for (Iterator iter = resources.iterator(); iter.hasNext();) {
//			IResource resource = (IResource) iter.next();
//			if (resource.isType(type)) {
//				filteredResources.add(resource);
//			}
//		}
//
//		return filteredResources;
//	}
//
//	/**
//	 * Taken from:
//	 * http://forum.java.sun.com/thread.jspa?threadID=382690&messageID=1641501
//	 *
//	 */
//	public void testReflectionPerformance()
//	{
//		int c = 1000000;
//		
//		List content = new ArrayList(c);
////		
//		for(int i = 0; i < c; i++ )
//		{
//			content.add(new String("" + i));
//		}
//		
////		boolean ignoreState = false;
////		Iterator i;
////		
//		long start = new Date().getTime();
////		
////		i = content.iterator();
////		while(i.hasNext())
////		{
////			Object o = i.next();
////			// Instance of (miss)
////			ignoreState = (o instanceof List);
////		}
////		
//		long middle1 = new Date().getTime();
////		
////		i = content.iterator();
////		while(i.hasNext())
////		{
////			Object o = i.next();
////			// Instance of (hit)
////			ignoreState = (o instanceof String);
////		}
////		
//		long middle2 = new Date().getTime();
////		
////		i = content.iterator();
////		while(i.hasNext())
////		{
//////			Object o = i.next();
////			// Instance of (arithmetic)
////			ignoreState = ((2 + 2) > 3);
////		}
////		
//		long middle3 = new Date().getTime();
////		
////		i = content.iterator();
////		while(i.hasNext())
////		{
////			Object o = i.next();
////			ignoreState = ( o.getClass().getName().equals(STRINGNAME) );
////		}
//		
//		long end = new Date().getTime();
//		
////		System.out.println("Instanceof (Miss) : " + (middle1 - start) + "ms");
////		System.out.println("Instanceof (Hit)  : " + (middle2 - middle1) + "ms");
////		System.out.println("Arithmetic        : " + (middle3 - middle2) + "ms");
////		System.out.println("Reflection        : " + (end - middle3) + "ms");
//    logger.debug("Instanceof (Miss) : " + (middle1 - start) + "ms");
//    logger.debug("Instanceof (Hit)  : " + (middle2 - middle1) + "ms");
//    logger.debug("Arithmetic        : " + (middle3 - middle2) + "ms");
//    logger.debug("Reflection        : " + (end - middle3) + "ms");
//	}
//
//	private long doInstanceOf(Vector resources) {
//		long total;
//		int foldersFound = 0;
//		IResource[] res = (IResource[]) resources.toArray(new IResource[] {});
//		IResource resource;
//		long begin = System.currentTimeMillis();
//
//		for (int i = 0; i < MULTIPLIER; i++) {
//			for (int j = 0; j < res.length; j++) {
//				resource = res[j];
//
//				if (resource instanceof IFile) {
//					foldersFound++;
//				}
//			}
//		}
//
//		total = System.currentTimeMillis() - begin;
//
////		System.out.println("instanceof found " + foldersFound + " folders");
//		return total;
//	}
//
//	private long doIsType(Vector resources) throws ResourceException {
//		long total;
//		int foldersFound = 0;
//		IResource[] res = (IResource[]) resources.toArray(new IResource[] {});
//		IResource resource;
//		long begin = System.currentTimeMillis();
//
//		for (int i = 0; i < MULTIPLIER; i++) {
//			for (int j = 0; j < res.length; j++) {
//				resource = res[j];
//
//				if (resource.isType(IResource.FILE)) {
//					foldersFound++;
//				}
//			}
//		}
//
//		total = System.currentTimeMillis() - begin;
//		
////		System.out.println("isType found " + foldersFound + " folders");
//		return total;
//	}
//
//	private void addChildren(IResource resource, Vector resources) throws ResourceException {
//		Vector children;
//
//		// if we have a folder then we can load its children
//		if (resource.isType(IResource.FOLDER) && !resource.isType(IResource.LINK)) {
//			children = ((IFolder) resource).getChildren();
//
//			// add each child to the resources vector and then recursively add the child's children.
//			for (Iterator iter = children.iterator(); iter.hasNext();) {
//				IResource child = (IResource) iter.next();
//				resources.add(child);
//				addChildren(child, resources);
//			}
//		}
//	}
}
