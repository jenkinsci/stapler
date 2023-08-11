package org.kohsuke.stapler.jelly;

import java.net.URL;
import java.util.Locale;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ResourceBundleTest {

    private static final URL RESOURCE_BUNDLE = ResourceBundleTest.class.getResource("/org/kohsuke/stapler/jelly/ResourceBundleTest/index.properties");
    private static final String FILE_PATH = RESOURCE_BUNDLE.toExternalForm().replace(".properties", "");
    private static final ResourceBundle resourceBundle = new ResourceBundle(FILE_PATH);

    @Test
    public void format() {
        String helloWorld = resourceBundle.format(Locale.ENGLISH, "hello_world");

        assertThat(helloWorld, is("Hello, World!"));
    }

    @Test
    public void formatFallsBackToDefaultIfMissing() {
        String helloWorld = resourceBundle.format(Locale.FRANCE, "hello_world");

        assertThat(helloWorld, is("Hello, World!"));
    }

    @Test
    public void formatLocaleOverrideInPlace() {
        String helloWorld = resourceBundle.format(Locale.CANADA, "hello_world");

        assertThat(helloWorld, is("Welcome, World!"));
    }

    @Test
    public void formatLocaleNonISO_8859_1() {
        String helloWorld = resourceBundle.format(Locale.CHINA, "hello_world");

        assertThat(helloWorld, is("你好，世界！简化"));
    }

    @Test
    public void formatLocaleNonISO_8859_1_encoded_with_utf8() {
        String helloWorld = resourceBundle.format(Locale.TRADITIONAL_CHINESE, "hello_world");

        assertThat(helloWorld, is("你好世界！"));
    }

    @Test
    public void formatLocaleISO_8859_1_high_range_character_invalid_utf_8() {
        String helloWorld = resourceBundle.format(Locale.FRANCE, "french");

        assertThat(helloWorld, is("Français"));
    }
}
