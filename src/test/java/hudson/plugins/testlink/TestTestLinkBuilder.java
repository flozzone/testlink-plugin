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
package hudson.plugins.testlink;

import hudson.plugins.testlink.result.ReportFilesPatterns;
import hudson.tasks.BuildStep;
import hudson.tasks.Shell;

import java.util.ArrayList;
import java.util.List;

import org.jvnet.hudson.test.HudsonTestCase;

/**
 * Tests TestLinkBuilder class.
 * 
 * @author Bruno P. Kinoshita - http://www.kinoshita.eti.br
 * @since 2.1
 */
public class TestTestLinkBuilder 
extends HudsonTestCase
{

	private TestLinkBuilder builder = null;	
	
	private String junitXmlReportFilesPattern = "**/TEST-*.xml";
	private String testNgXmlReportFilesPattern = "**/testng-results.xml";
	private String tapReportFilesPattern = "**/*.tap";
	
	public void setUp() 
	throws Exception
	{
		super.setUp();
		
		builder = new TestLinkBuilder(
				"No testlink", 
				"No project",
				"No plan", 
				"No build", 
				"No platform",
				"class, time", 
				"dir",
				null, 
				null, 
				null, 
				null, 
				Boolean.FALSE, 
				Boolean.FALSE,  
				junitXmlReportFilesPattern,
				testNgXmlReportFilesPattern, 
				tapReportFilesPattern);
	}
	
	/**
	 * Tests the ReportPatterns object.
	 */
	public void testReportPatterns() 
	throws Exception
	{
		ReportFilesPatterns reportPatterns = builder.getReportFilesPatterns();
		assertNotNull(reportPatterns);
		assertEquals(reportPatterns.getJunitXmlReportFilesPattern(),
				junitXmlReportFilesPattern);
		assertEquals(reportPatterns.getTestNGXmlReportFilesPattern(),
				testNgXmlReportFilesPattern);
		assertEquals(reportPatterns.getTapStreamReportFilesPattern(),
				tapReportFilesPattern);
	}
	
	/**
	 * Tests the generated list of custom fields.
	 */
	public void testListOfCustomFields()
	{
		String[] customFieldsNames = builder.createArrayOfCustomFieldsNames();
		
		assertNotNull( customFieldsNames );
		assertTrue( customFieldsNames.length == 2 );
		assertEquals( customFieldsNames[0], "class" );
		assertEquals( customFieldsNames[1], "time" );
	}
	
	public void testNull()
	{
		builder = new TestLinkBuilder(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null );
		
		assertNotNull( builder );
		
		assertNull( builder.getTestLinkName() );
		
		assertNull( builder.getTestProjectName() );
		
		assertNull( builder.getTestPlanName() );
		
		assertNull( builder.getBuildName() );

		assertNull( builder.getPlatformName() );
		
		assertNull( builder.getSingleBuildSteps() );
		
		assertNull( builder.getBeforeIteratingAllTestCasesBuildSteps() );
		
		assertNull( builder.getIterativeBuildSteps() );
		
		assertNull( builder.getAfterIteratingAllTestCasesBuildSteps() );
		
		assertNull( builder.getCustomFields() );
		
		assertNull( builder.getTransactional() );
		
		assertNull( builder.getKeyCustomField() );
		
		assertNotNull( builder.getReportFilesPatterns() );
		
		assertNull( builder.getReportFilesPatterns().getJunitXmlReportFilesPattern() );
		
		assertNull( builder.getReportFilesPatterns().getTestNGXmlReportFilesPattern() );
		
		assertNull( builder.getReportFilesPatterns().getTapStreamReportFilesPattern() );
	}
	
	/**
	 * Tests getters methods.
	 */
	public void testGetters()
	{
		
		Shell shell = new Shell("ls -la");
		List<BuildStep> singleBuildSteps = new ArrayList<BuildStep>();
		singleBuildSteps.add(shell);
		
		builder = new TestLinkBuilder(
			"No testlink", 
			"No project",
			"No plan", 
			"No build", 
			"No platform",
			"class, time", 
			"dir",
			singleBuildSteps, 
			null, 
			null, 
			null, 
			Boolean.FALSE, 
			Boolean.FALSE,  
			junitXmlReportFilesPattern,
			testNgXmlReportFilesPattern, 
			tapReportFilesPattern);
		
		assertNotNull( hudson );
		//FreeStyleProject project = new FreeStyleProject(hudson, "No project");
		//assertNotNull ( (AbstractProject<?, ?>)builder.getProjectAction(project) );
		
		assertNotNull( builder.getTestLinkName() );
		assertEquals( builder.getTestLinkName(), "No testlink" );
		
		assertNotNull( builder.getTestProjectName() );
		assertEquals( builder.getTestProjectName(), "No project" );
		
		assertNotNull( builder.getTestPlanName() );
		assertEquals( builder.getTestPlanName(), "No plan" );
		
		assertNotNull( builder.getBuildName() );
		assertEquals( builder.getBuildName(), "No build" );

		assertNotNull( builder.getPlatformName() );
		assertEquals( builder.getPlatformName(), "No platform" );
		
		assertNotNull( builder.getSingleBuildSteps() );
		assertEquals( builder.getSingleBuildSteps() , singleBuildSteps);
		assertEquals( builder.getSingleBuildSteps().size(), 1);
		
		assertNotNull( builder.getCustomFields() );
		assertEquals( builder.getCustomFields(), "class, time" );
		
		assertFalse( builder.getTransactional() );
		
		assertNotNull( builder.getKeyCustomField() );
		assertEquals( builder.getKeyCustomField(), "dir" );
		
		assertNotNull( builder.getReportFilesPatterns().getJunitXmlReportFilesPattern());
		assertEquals( builder.getReportFilesPatterns().getJunitXmlReportFilesPattern(), "**/TEST-*.xml" );
		
		assertNotNull( builder.getReportFilesPatterns().getTestNGXmlReportFilesPattern() );
		assertEquals( builder.getReportFilesPatterns().getTestNGXmlReportFilesPattern(), "**/testng-results.xml" );
		
		assertNotNull( builder.getReportFilesPatterns().getTapStreamReportFilesPattern() );
		assertEquals( builder.getReportFilesPatterns().getTapStreamReportFilesPattern(), "**/*.tap" );
	}

}
