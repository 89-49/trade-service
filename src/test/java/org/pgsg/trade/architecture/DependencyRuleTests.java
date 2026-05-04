package org.pgsg.trade.architecture;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@DisplayName("헥사고날 아키텍처 검증")
class DependencyRuleTests {

    @DisplayName("성공: Trade 아키텍처 의존성 규칙을 만족한다.")
    @Test
    void validateTradeArchitecture() {
        // given & when & then
        HexagonalArchitecture.boundedContext("org.pgsg.trade")

                .withDomainLayer("domain")

                .withAdaptersLayer("presentation")
                .incoming("controller")
                .and()

                .withAdaptersLayer("infrastructure.adapter")
                .incoming("messaging.kafka.consumer")
                .outgoing("messaging.kafka.producer")
                .outgoing("persistence")
                .and()

                .withApplicationLayer("application")
                .services("service")
                .incomingPorts("port.in")
                .outgoingPorts("port.out")
                .and()

                .withConfiguration("configuration")
                .check(new ClassFileImporter()
                        .withImportOption(new ImportOption.DoNotIncludeTests())
                        .importPackages("org.pgsg.trade.."));
    }

    @DisplayName("성공: Domain 계층은 Application 계층에 의존하지 않는다.")
    @Test
    void validateDomainDependencies() {
        // given & when & then
        noClasses()
                .that()
                .resideInAPackage("org.pgsg.trade.domain..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("org.pgsg.trade.application..")
                .check(new ClassFileImporter()
                        .withImportOption(new ImportOption.DoNotIncludeTests())
                        .importPackages("org.pgsg.trade.."));
    }
}
