package org.kohsuke.stapler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * @author Vivek Pandey
 */
class AcceptHeaderTest {

    @Test
    void simpleHeader() {
        AcceptHeader.Atom atom = new AcceptHeader("text/html").match("text/html");
        assertNotNull(atom);
        assertEquals("text/html", atom.toString());
    }

    @Test
    void headerSelect1() {
        String type = new AcceptHeader("text/html").select("text/html", "text/plain");
        assertNotNull(type);
        assertEquals("text/html", type);
    }

    @Test
    void headerSelect2() {
        String type = new AcceptHeader("text/*").select("text/html");
        assertNotNull(type);
        assertEquals("text/html", type);
    }

    @Test
    void headerSelectUnsupportedMediaType() {
        assertThrows(HttpResponses.HttpResponseException.class, () -> new AcceptHeader("text/html")
                .select("application/json"));
    }

    @Test
    void simpleHeaderWithParam() {
        AcceptHeader acceptHeader = new AcceptHeader("application/json;charset=utf-8");

        AcceptHeader.Atom atom = acceptHeader.match("application/json");
        assertNotNull(atom);
        assertEquals("application/json;charset=utf-8", atom.toString());

        String type = acceptHeader.select("application/json");
        assertNotNull(type);
        assertEquals("application/json;charset=utf-8", atom.toString());
    }

    @Test
    void wildCard() {
        String type = new AcceptHeader("text/*").select("text/html");
        assertNotNull(type);
        assertEquals("text/html", type);
    }

    @Test
    void qualityFactor1() {
        String type = new AcceptHeader("image/*;q=0.5, image/png;q=1").select("image/jpeg", "image/png");
        assertNotNull(type);
        assertEquals("image/png", type);
    }

    @Test
    void qualityFactor2() {
        String type = new AcceptHeader("text/*;q=0.5, *;q=0.1").select("application/xbel+xml", "text/xml");
        assertNotNull(type);
        assertEquals("text/xml", type);
    }

    @Test
    void qualityFactor3() {
        String type = new AcceptHeader("text/xml;q=0.5, application/json;q=0.1, text/html;q=0.1")
                .select("application/json", "text/xml");
        assertNotNull(type);
        assertEquals("text/xml", type);
    }

    @Test
    void qualityFactor4() {
        String type = new AcceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .select("application/xml");
        assertNotNull(type);
        assertEquals("application/xml", type);

        type = new AcceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8").select("foo/bar");
        assertNotNull(type);
        assertEquals("foo/bar", type);
    }

    @Test
    void qualityFactor5() {
        AcceptHeader acceptHeader = new AcceptHeader(
                "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");

        String type = acceptHeader.select("text/html", "image/png");
        assertNotNull(type);
        assertEquals("image/png", type);

        type = acceptHeader.select("text/plain", "image/png");
        assertNotNull(type);
        assertEquals("image/png", type);

        type = acceptHeader.select("text/html", "image/png");
        assertNotNull(type);
        assertEquals("image/png", type);

        type = acceptHeader.select("text/plain", "image/png", "application/xml");
        assertNotNull(type);
        assertEquals("image/png", type);
    }

    @Test
    void qualityFactor6() {
        AcceptHeader acceptHeader =
                new AcceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");

        String type = acceptHeader.select("text/plain", "text/html");
        assertNotNull(type);
        assertEquals("text/html", type); // 1 > 0.8

        type = acceptHeader.select("text/plain", "application/xml");
        assertNotNull(type);
        assertEquals("application/xml", type); // 0.9 > 0.8
    }
}
