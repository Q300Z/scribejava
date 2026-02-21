package com.github.scribejava.core.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ParameterListTest {

    private ParameterList params;

    @BeforeEach
    public void setUp() {
        this.params = new ParameterList();
    }

    @Test
    public void shouldThrowExceptionWhenAppendingNullMapToQuerystring() {
        assertThatThrownBy(() -> params.appendTo(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldAppendNothingToQuerystringIfGivenEmptyMap() {
        final String url = "http://www.example.com";
        assertThat(params.appendTo(url)).isEqualTo(url);
    }

    @Test
    public void shouldAppendParametersToSimpleUrl() {
        String url = "http://www.example.com";
        final String expectedUrl = "http://www.example.com?param1=value1&param2=value%20with%20spaces";

        params.add("param1", "value1");
        params.add("param2", "value with spaces");

        url = params.appendTo(url);
        assertThat(url).isEqualTo(expectedUrl);
    }

    @Test
    public void shouldAppendParametersToUrlWithQuerystring() {
        String url = "http://www.example.com?already=present";
        final String expectedUrl = "http://www.example.com?already=present&param1=value1&param2=value%20with%20spaces";

        params.add("param1", "value1");
        params.add("param2", "value with spaces");

        url = params.appendTo(url);
        assertThat(url).isEqualTo(expectedUrl);
    }

    @Test
    public void shouldProperlySortParameters() {
        params.add("param1", "v1");
        params.add("param6", "v2");
        params.add("a_param", "v3");
        params.add("param2", "v4");
        assertThat(params.sort().asFormUrlEncodedString()).isEqualTo("a_param=v3&param1=v1&param2=v4&param6=v2");
    }

    @Test
    public void shouldProperlySortParametersWithTheSameName() {
        params.add("param1", "v1");
        params.add("param6", "v2");
        params.add("a_param", "v3");
        params.add("param1", "v4");
        assertThat(params.sort().asFormUrlEncodedString()).isEqualTo("a_param=v3&param1=v1&param1=v4&param6=v2");
    }

    @Test
    public void shouldNotModifyTheOriginalParameterList() {
        params.add("param1", "v1");
        params.add("param6", "v2");

        assertThat(params.sort()).isNotSameAs(params);
    }
}
