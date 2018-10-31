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
package gov.pnnl.cat.intercept.extracter;

import java.lang.reflect.Method;
import java.util.List;

import org.alfresco.repo.content.metadata.MetadataExtracter;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 */
public class MetadataExtracterRegistryIntercept implements MethodInterceptor {

	private List<MetadataModifier> metadataModifiers;
	
	/**
	 * Method setMetadataModifiers.
	 * @param metadataModifiers List<MetadataModifier>
	 */
	public void setMetadataModifiers(List<MetadataModifier> metadataModifiers) {
		this.metadataModifiers = metadataModifiers;
	}
	
	/**
	 * Intercept all calls to the MetadataExtracterRegistry
	 * @param methodInvocation MethodInvocation
	 * @return Object
	 * @throws Throwable
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(MethodInvocation)
	 */
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		Method method = methodInvocation.getMethod();

		if (method.getName().equals("register")) {
			// this is really the only one we care about
			return invokeRegisterInterceptor(methodInvocation);
		} 
		// all others can proceed
		return methodInvocation.proceed();
	}
	
	/**
	 * Method invokeRegisterInterceptor.
	 * @param methodInvocation MethodInvocation
	 * @return Object
	 * @throws Throwable
	 */
	private Object invokeRegisterInterceptor(MethodInvocation methodInvocation) throws Throwable {
		// someone is invoking MetadataExtracterRegistry.register(MetadataExtracter extracter)
		// wrap the provided MetadataExtracter with a WrappedMetadataExtracter
		Object[] arguments = methodInvocation.getArguments();
		MetadataExtracter extracter = (MetadataExtracter)arguments[0];
		WrappedMetadataExtracter wrappedExtracter = new WrappedMetadataExtracter(extracter, metadataModifiers);
		methodInvocation.getArguments()[0] = wrappedExtracter;
		
		return methodInvocation.proceed();
	}



}
