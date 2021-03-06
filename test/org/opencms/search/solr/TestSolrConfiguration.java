/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.search.solr;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.search.CmsSearchIndex;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsFileUtil;

import java.io.File;
import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrCore;

/**
 * Tests the Solr configuration.<p>
 * 
 * @since 8.5.0
 */
public class TestSolrConfiguration extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestSolrConfiguration(String arg0) {

        super(arg0);
    }

    /**
     * Test suite for this test class.<p>
     * 
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);

        TestSuite suite = new TestSuite();
        suite.setName(TestSolrConfiguration.class.getName());
        suite.addTest(new TestSolrConfiguration("testFieldConfiguration"));
        suite.addTest(new TestSolrConfiguration("testXSDFields"));
        suite.addTest(new TestSolrConfiguration("testMandantoryFields"));
        suite.addTest(new TestSolrConfiguration("testDynamicFields"));
        suite.addTest(new TestSolrConfiguration("testDefaultFields"));
        suite.addTest(new TestSolrConfiguration("testCmsSolrPostProcessor"));
        suite.addTest(new TestSolrConfiguration("testShutDown"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms("simpletest", "/");
                // disable all lucene indexes
                for (String indexName : OpenCms.getSearchManager().getIndexNames()) {
                    if (!indexName.equalsIgnoreCase(AllTests.SOLR_ONLINE)) {
                        CmsSearchIndex index = OpenCms.getSearchManager().getIndex(indexName);
                        if (index != null) {
                            index.setEnabled(false);
                        }
                    }
                }
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Tests the CmsSearch with folder names with upper case letters.<p>
     * 
     * @throws Exception in case the test fails
     */
    public void testCmsSolrPostProcessor() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing Solr link processor");

        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(AllTests.SOLR_ONLINE);
        // String query = "+text:Alkacon +text:OpenCms +text:Text +parent-folders:/sites/default/types/*";
        String query = "q=+text:>>SearchEgg1<<";
        CmsSolrResultList results = index.search(cms, query);
        assertEquals(1, results.size());
        assertEquals("/sites/default/xmlcontent/article_0001.html", results.get(0).getRootPath());

        String link = results.get(0).getDocument().getFieldValueAsString("link");
        assertEquals("/data/opencms/xmlcontent/article_0001.html", link);
    }

    /**
     * @throws Throwable
     */
    public void testDefaultFields() throws Throwable {

        // TODO: implement
    }

    /**
     * @throws Throwable
     */
    public void testDynamicFields() throws Throwable {

        // TODO: implement
    }

    /**
     * @throws Throwable
     */
    public void testFieldConfiguration() throws Throwable {

        // TODO: implement
    }

    /**
     * @throws Throwable
     */
    public void testMandantoryFields() throws Throwable {

        // TODO: implement
    }

    /**
     * Tests shutting down Solr.<p>
     * 
     * @throws Throwable
     */
    public void testShutDown() throws Throwable {

        CmsSolrIndex index = new CmsSolrIndex(AllTests.INDEX_TEST);
        index.setProject("Offline");
        index.setLocale(Locale.GERMAN);
        index.setRebuildMode(CmsSearchIndex.REBUILD_MODE_AUTO);
        index.setFieldConfigurationName("solr_fields");
        index.addSourceName("solr_source2");
        OpenCms.getSearchManager().addSearchIndex(index);
        OpenCms.getSearchManager().rebuildIndex(AllTests.INDEX_TEST, new CmsShellReport(Locale.ENGLISH));
        index.search(getCmsObject(), "q=*:*");

        // shut down
        CoreContainer container = ((EmbeddedSolrServer)index.m_solr).getCoreContainer();
        for (SolrCore core : container.getCores()) {
            core.closeSearcher();
            core.close();
        }
        container.shutdown();

        // wait for a moment
        Thread.sleep(500);

        // success ?
        CmsFileUtil.purgeDirectory(new File(index.getPath()));
        assertTrue(!new File(index.getPath()).exists());
    }

    /**
     * @throws Throwable
     */
    public void testXSDFields() throws Throwable {

        // TODO: implement
    }
}
