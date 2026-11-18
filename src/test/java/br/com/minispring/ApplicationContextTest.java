
package br.com.minispring;

import br.com.minispring.exemplo.Application;
import br.com.minispring.exemplo.repository.ServicoRepository;
import br.com.minispring.exemplo.service.ServicoService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = Application.class)
class ApplicationContextTest {

    interface Storage {}

    static class Memory implements Storage {}

    static class OtherMemory implements Storage {}

    static class StorageService {
        final Storage storage;

        StorageService(Storage storage) {
            this.storage = storage;
        }
    }

    static class A {
        A(B b) {}
    }

    static class B {
        B(A a) {}
    }

    static class Configured {
        final String name;

        Configured(String name) {
            this.name = name;
        }
    }

    @org.springframework.beans.factory.annotation.Autowired
    private ServicoService servicoService;

    @org.springframework.beans.factory.annotation.Autowired
    private ServicoRepository servicoRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.context.ApplicationContext context;

    @Test
    void injectsImplementationAndReusesSingleton() {
        assertNotNull(servicoService);
        assertNotNull(servicoRepository);

        assertSame(
                servicoService,
                context.getBean(ServicoService.class)
        );

        assertSame(
                servicoRepository,
                context.getBean(ServicoRepository.class)
        );
    }

    @Test
    void failsOnMissingDependency() {
        try (var testContext = new AnnotationConfigApplicationContext()) {
            testContext.registerBean(StorageService.class);

            assertThrows(
                    UnsatisfiedDependencyException.class,
                    testContext::refresh
            );
        }
    }

    @Test
    void failsOnAmbiguousDependency() {
        try (var testContext = new AnnotationConfigApplicationContext()) {
            testContext.registerBean(StorageService.class);
            testContext.registerBean(Memory.class);
            testContext.registerBean(OtherMemory.class);

            var exception = assertThrows(
                    BeanCreationException.class,
                    testContext::refresh
            );

            assertTrue(
                    exception.getCause() instanceof
                            NoUniqueBeanDefinitionException
            );
        }
    }

    @Test
    void explainsCircularDependency() {
        try (var testContext = new AnnotationConfigApplicationContext()) {
            testContext.registerBean(A.class);
            testContext.registerBean(B.class);

            assertThrows(
                    BeanCreationException.class,
                    testContext::refresh
            );
        }
    }

    @Test
    void injectsConfigurationProperty() {
        try (var testContext = new AnnotationConfigApplicationContext()) {

            testContext.getEnvironment()
                    .getPropertySources()
                    .addFirst(
                            new MapPropertySource(
                                    "testProperties",
                                    java.util.Map.of(
                                            "test.name", "Pet Shop de teste"
                                    )
                            )
                    );

            testContext.registerBean(
                    Configured.class,
                    () -> new Configured(
                            testContext.getEnvironment()
                                    .getRequiredProperty("test.name")
                    )
            );

            testContext.refresh();

            assertEquals(
                    "Pet Shop de teste",
                    testContext.getBean(Configured.class).name
            );
        }
    }
}
