package org.kohsuke.stapler;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Vivek Pandey
 */
public class AcceptHeaderTest {

    @Test
    public void simpleHeader() {
        AcceptHeader.Atom atom = new AcceptHeader("text/html").match("text/html");
        Assert.assertNotNull(atom);
        Assert.assertEquals("text/html", atom.toString());
    }

    @Test
    public void headerSelect1() {
        String type = new AcceptHeader("text/html").select("text/html", "text/plain");
        Assert.assertNotNull(type);
        Assert.assertEquals("text/html", type);
    }

    @Test
    public void headerSelect2() {
        String type = new AcceptHeader("text/html").select("text/plain");
        Assert.assertNotNull(type);
        Assert.assertEquals("text/html", type);
    }

    @Test(expected = HttpResponses.HttpResponseException.class)
    public void headerSelectUnsupportedMediaType() {
        new AcceptHeader("text/html").select("application/json");
    }
    @Test
    public void simpleHeaderWithParam() {
        AcceptHeader acceptHeader = new AcceptHeader("application/json;charset=utf-8");

        AcceptHeader.Atom atom = acceptHeader.match("application/json");
        Assert.assertNotNull(atom);
        Assert.assertEquals("application/json;charset=utf-8", atom.toString());

        String type = acceptHeader.select("application/json");
        Assert.assertNotNull(type);
        Assert.assertEquals("application/json;charset=utf-8", atom.toString());
    }


    @Test
    public void wildCard() {
        String type = new AcceptHeader("text/*").select("text/html");
        Assert.assertNotNull(type);
        Assert.assertEquals("text/html", type);
    }

    @Test
    public void qualityFactor1() {
        String type = new AcceptHeader("image/*;q=0.5, image/png;q=1").select("image/jpeg", "image/png");
        Assert.assertNotNull(type);
        Assert.assertEquals(type, "image/png");
    }

    @Test
    public void qualityFactor2() {
        String type = new AcceptHeader("text/*;q=0.5, *;q=0.1").select("application/xbel+xml", "text/xml");
        Assert.assertNotNull(type);
        Assert.assertEquals(type, "text/xml");
    }

    @Test
    public void qualityFactor3() {
        String type = new AcceptHeader("text/xml;q=0.5, application/json;q=0.1, text/html;q=0.1").select("application/json", "text/xml");
        Assert.assertNotNull(type);
        Assert.assertEquals(type, "text/xml");
    }

    @Test
    public void qualityFactor4(){
        String type = new AcceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8").select("application/xml");
        Assert.assertNotNull(type);
        Assert.assertEquals(type, "application/xml");

        type = new AcceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8").select("foo/bar");
        Assert.assertNotNull(type);
        Assert.assertEquals(type, "foo/bar");
    }

    @Test
    public void qualityFactor5(){
         AcceptHeader acceptHeader = new AcceptHeader("application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");

        String type = acceptHeader.select("text/html", "image/png");
        Assert.assertNotNull(type);
        Assert.assertEquals(type, "image/png");

        type = acceptHeader.select("text/plain", "image/png");
        Assert.assertNotNull(type);
        Assert.assertEquals(type, "image/png");

        type = acceptHeader.select("text/html", "image/png");
        Assert.assertNotNull(type);
        Assert.assertEquals(type, "image/png");

        type = acceptHeader.select("text/plain", "image/png","application/xml");
        Assert.assertNotNull(type);
        Assert.assertEquals(type, "image/png");

    }
}
