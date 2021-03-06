/*
 * The MIT License
 *
 * Copyright (c) <2011> <Bruno P. Kinoshita>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.plugins.testlink.result.testng;

import hudson.model.BuildListener;
import hudson.plugins.testlink.parser.ParserException;
import hudson.plugins.testlink.parser.testng.Suite;
import hudson.plugins.testlink.parser.testng.Test;
import hudson.plugins.testlink.parser.testng.TestMethod;
import hudson.plugins.testlink.parser.testng.TestNGParser;
import hudson.plugins.testlink.result.TestCaseWrapper;
import hudson.plugins.testlink.result.TestResultSeekerException;
import hudson.plugins.testlink.util.Messages;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import br.eti.kinoshita.testlinkjavaapi.model.Attachment;
import br.eti.kinoshita.testlinkjavaapi.model.CustomField;
import br.eti.kinoshita.testlinkjavaapi.model.ExecutionStatus;
import br.eti.kinoshita.testlinkjavaapi.model.TestCase;

/**
 * Seeks for test results of TestNG tests.
 * 
 * @author Bruno P. Kinoshita - http://www.kinoshita.eti.br
 * @since 2.5
 */
public class TestNGClassesTestResultSeeker<T extends hudson.plugins.testlink.parser.testng.Class>
extends AbstractTestNGTestResultSeeker<hudson.plugins.testlink.parser.testng.Class>
{

	private static final long serialVersionUID = 4734537106225737934L;

	protected final TestNGParser parser = new TestNGParser();
	
	/**
	 * Map of Wrappers for TestLink Test Cases.
	 */
	protected final Map<Integer, TestCaseWrapper<hudson.plugins.testlink.parser.testng.Class>> results = new LinkedHashMap<Integer, TestCaseWrapper<hudson.plugins.testlink.parser.testng.Class>>();
	
	public TestNGClassesTestResultSeeker(String includePattern, TestCase[] automatedTestCases,
			String keyCustomFieldName, BuildListener listener)
	{
		super(includePattern, automatedTestCases, keyCustomFieldName, listener);
	}
	
	/* (non-Javadoc)
	 * @see hudson.plugins.testlink.result.TestResultSeeker#seek(java.io.File, java.lang.String)
	 */
	@Override
	public Map<Integer, TestCaseWrapper<hudson.plugins.testlink.parser.testng.Class>> seek( File directory )
			throws TestResultSeekerException
	{
		listener.getLogger().println( Messages.Results_TestNG_LookingForTestClasses() );
		
		if ( StringUtils.isBlank(includePattern) ) // skip TestNG
		{
			listener.getLogger().println( Messages.Results_TestNG_NoPattern() );
		}
		else
		{
			try
			{
				String[] testNGReports = this.scan(directory, includePattern, listener);
				
				listener.getLogger().println( Messages.Results_TestNG_NumberOfReportsFound( testNGReports.length ) );
				
				this.processTestNGReports( directory, testNGReports );
			} 
			catch (IOException e)
			{
				throw new TestResultSeekerException( Messages.Results_TestNG_IOException( includePattern, e.getMessage() ), e );
			}
			catch( Throwable t ) 
			{
				throw new TestResultSeekerException( Messages.Results_TestNG_UnkownInternalError(), t );
			}
		}
		
		return this.results;
	}

	/**
	 * Processes TestNG reports.
	 */
	protected void processTestNGReports( 
		File directory, 
		String[] testNGReports ) 
	{
		
		for ( int i = 0 ; i < testNGReports.length ; ++i )
		{
			final File testNGFile = new File(directory, testNGReports[i]);
			
			try
			{
				final Suite testNGSuite = parser.parse( testNGFile );
				
				this.processTestNGSuite( testNGSuite, testNGFile );
			}
			catch ( ParserException e )
			{
				e.printStackTrace( listener.getLogger() );
			}
		}
	}
	
	/**
	 * Processes a TestNG suite.
	 */
	protected void processTestNGSuite( 
		Suite testNGSuite, 
		File testNGFile
	)
	{
		final List<Test> testNGTests = testNGSuite.getTests();
		
		for( Test testNGTest : testNGTests )
		{
			this.processTestNGTest( testNGTest, testNGSuite, testNGFile );
		}
	}
	
	/**
	 * Processes a TestNG test.
	 */
	protected void processTestNGTest( Test testNGTest, Suite testNGSuite, File testNGFile )
	{
		final List<hudson.plugins.testlink.parser.testng.Class> classes = 
			testNGTest.getClasses();
		
		for ( hudson.plugins.testlink.parser.testng.Class clazz : classes )
		{
			this.processTestClass( clazz, testNGSuite, testNGFile );
		}
	}

	/**
	 * Processes a TestNG test class.
	 */
	protected void processTestClass( hudson.plugins.testlink.parser.testng.Class clazz, Suite testNGSuite, File testNGFile )
	{
		final String testNGTestClassName = clazz.getName();
		
		if ( ! StringUtils.isBlank( testNGTestClassName ) )
		{
			for ( br.eti.kinoshita.testlinkjavaapi.model.TestCase testLinkTestCase : this.automatedTestCases )
			{
				this.findTestResults( testNGSuite, clazz, testLinkTestCase, testNGFile );
			}
		}
	}

	/**
	 * Looks for test results in a TestNG test case.
	 */
	protected void findTestResults( Suite testNGSuite, hudson.plugins.testlink.parser.testng.Class clazz, TestCase testLinkTestCase, File testNGFile )
	{
		final List<CustomField> customFields = testLinkTestCase.getCustomFields();

		final CustomField keyCustomField = this.getKeyCustomField( customFields );
		if ( keyCustomField != null )
		{
			final String[] commaSeparatedValues = this.split ( keyCustomField.getValue() );
			
			for ( String value : commaSeparatedValues )
			{
				if ( clazz.getName().equals( value ) && ExecutionStatus.BLOCKED != testLinkTestCase.getExecutionStatus() )
				{
					final TestCaseWrapper<hudson.plugins.testlink.parser.testng.Class> testResult = 
						new TestCaseWrapper<hudson.plugins.testlink.parser.testng.Class>( testLinkTestCase, commaSeparatedValues, clazz );
					
					final ExecutionStatus status = this.getTestNGExecutionStatus( clazz );
					testResult.addCustomFieldAndStatus(value, status);
					
					String notes = this.getTestNGNotes( testNGSuite, clazz );
					
					try
					{
						Attachment testNGAttachment = this.getTestNGAttachment( testNGFile );
						testResult.addAttachment( testNGAttachment );
					}
					catch ( IOException ioe )
					{
						notes += Messages.Results_TestNG_AddAttachmentsFail( ioe.getMessage() );
						ioe.printStackTrace( listener.getLogger() );
					}
					
					testResult.appendNotes( notes );
					
					this.addOrUpdate( testResult );
				}
			} 
		} 
	}
	
	/**
	 * Adds or updates a test result.
	 */
	protected void addOrUpdate( TestCaseWrapper<hudson.plugins.testlink.parser.testng.Class> testResult )
	{
		final TestCaseWrapper<hudson.plugins.testlink.parser.testng.Class> temp = 
			this.results.get(testResult.getId());
		
		if ( temp == null )
		{
			this.results.put(testResult.getId(), testResult);
		}
		else
		{
			temp.appendNotes( testResult.getNotes() );
			for( Attachment attachment : testResult.getAttachments() )
			{
				temp.addAttachment(attachment);
			}
			temp.getCustomFieldAndStatus().putAll( testResult.getCustomFieldAndStatus() );
		}
	}

	/**
	 * Retrieves the Execution Status for a TestNG test class. It is done 
	 * iterating over all the class methods. If a method has the status 
	 * FAIL, then we return the Execution Status failed, otherwise passed.
	 * 
	 * @param clazz The TestNG Test class.
	 * @return passed if the TestNG Test class contains no test methods with 
	 * status equals FAIL, otherwise failed.
	 */
	protected ExecutionStatus getTestNGExecutionStatus( hudson.plugins.testlink.parser.testng.Class clazz )
	{
		ExecutionStatus status = ExecutionStatus.PASSED;
		
		for( TestMethod method : clazz.getTestMethods() )
		{
			if ( StringUtils.isNotBlank(method.getStatus()) && !method.getStatus().equals("PASS"))
			{
				status = ExecutionStatus.FAILED;
				break; // It's enough, one single failed is enough to invalidate a test class
			}
		}
		
		return status;
	}
	
	/**
	 * Retrieves notes for TestNG suite and test class.
	 * 
	 * @param suite TestNG suite.
	 * @param clazz TestNG test class.
	 * @return notes for TestNG suite and test class.
	 */
	protected String getTestNGNotes( Suite suite, hudson.plugins.testlink.parser.testng.Class clazz )
	{
		StringBuilder notes = new StringBuilder();
		
		notes.append( 
				Messages.Results_TestNG_NotesForSuiteAndClass(
						suite.getName(), 
						suite.getDurationMs(), 
						suite.getStartedAt(), 
						suite.getFinishedAt(), suite.getTests().size(), 
						clazz.getName(), 
						clazz.getTestMethods().size()
				)
		);
		
		for( TestMethod method : clazz.getTestMethods() )
		{
			
			notes.append(
					Messages.Results_TestNG_NotesForMethods(
						method.getName(), 
						method.getIsConfig(), 
						method.getSignature(), 
						method.getStatus(), 
						method.getDurationMs(), 
						method.getStartedAt(), 
						method.getFinishedAt()
					)
			);
		}
		
		return notes.toString();
	}
	
}
