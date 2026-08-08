package br.com.fiap.postech.it.cucumber.hooks;

import br.com.fiap.postech.it.cucumber.context.CucumberTestContext;
import io.cucumber.java.Before;

public class CucumberHooks {
    @Before
    public void setUp() {
        CucumberTestContext.getInstance().reset();
    }
}
