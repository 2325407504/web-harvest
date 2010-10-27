/*
 Copyright (c) 2006-2007, Vladimir Nikic
 All rights reserved.

 Redistribution and use of this software in source and binary forms,
 with or without modification, are permitted provided that the following
 conditions are met:

 * Redistributions of source code must retain the above
   copyright notice, this list of conditions and the
   following disclaimer.

 * Redistributions in binary form must reproduce the above
   copyright notice, this list of conditions and the
   following disclaimer in the documentation and/or other
   materials provided with the distribution.

 * The name of Web-Harvest may not be used to endorse or promote
   products derived from this software without specific prior
   written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.

 You can contact Vladimir Nikic by sending e-mail to
 nikic_vladimir@yahoo.com. Please include the word "Web-Harvest" in the
 subject line.
 */

package org.webharvest.runtime.processors.plugins.variable;

import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.mock.Mock;
import org.unitils.mock.annotation.Dummy;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.scripting.ScriptEngineFactory;
import org.webharvest.runtime.scripting.ScriptingLanguage;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.utils.KeyValuePair;

import static java.util.Arrays.asList;
import static org.webharvest.runtime.processors.plugins.PluginTestUtils.createPlugin;

@SuppressWarnings({"unchecked"})
public class SetVarPluginTest extends UnitilsTestNG {

    @Dummy
    Logger logger;

    Mock<ScraperContext> contextMock;
    Mock<Scraper> scraperMock;

    @BeforeMethod
    public void before() {
        scraperMock.returns(logger).getLogger();
        scraperMock.returns(contextMock.getMock()).getContext();
        scraperMock.returns(new ScriptEngineFactory(ScriptingLanguage.GROOVY, scraperMock.getMock())).getScriptEngineFactory();
    }

    @Test
    public void testExecutePlugin_valueAsAttr() throws Exception {
        contextMock.returns(
                asList(new KeyValuePair("name", new NodeVariable("World"))).iterator()).
                iterator();

        createPlugin(
                "<set var='greetings' value='Hello, ${name}!'/>",
                SetVarPlugin.class).executePlugin(scraperMock.getMock(), contextMock.getMock());

        contextMock.assertInvoked().setLocalVar("greetings", new NodeVariable("Hello, World!"));
        contextMock.assertNotInvoked().replaceExistingVar(null, null);
    }

    @Test
    public void testExecutePlugin_valueAsBody() throws Exception {
        contextMock.returns(
                asList(new KeyValuePair("name", new NodeVariable("World"))).iterator()).
                iterator();

        createPlugin(
                "<set var='greetings'><template>Hello, ${name}!</template></set>",
                SetVarPlugin.class).executePlugin(scraperMock.getMock(), contextMock.getMock());

        contextMock.assertInvoked().setLocalVar("greetings", new NodeVariable("Hello, World!"));
        contextMock.assertNotInvoked().replaceExistingVar(null, null);
    }

    @Test
    public void testExecutePlugin_bodyIgnoredWhenAttrSpecified() throws Exception {
        createPlugin(
                "<set var='x' value='actual'>ignored</set>",
                SetVarPlugin.class).executePlugin(scraperMock.getMock(), contextMock.getMock());

        contextMock.assertInvoked().setLocalVar("x", new NodeVariable("actual"));
        contextMock.assertNotInvoked().replaceExistingVar(null, null);
    }

    @Test
    public void testExecutePlugin_replace() throws Exception {
        contextMock.returns(true).containsVar("x");

        createPlugin(
                "<set var='x' value='new'/>",
                SetVarPlugin.class).executePlugin(scraperMock.getMock(), contextMock.getMock());

        contextMock.assertInvoked().replaceExistingVar("x", new NodeVariable("new"));
        contextMock.assertNotInvoked().setLocalVar("x", null);
    }
}
