package org.pgsg.trade.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchRule;

import java.util.ArrayList;
import java.util.List;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class HexagonalArchitecture {

    private final String basePackage;
    private String domainLayer;
    private String applicationLayer;
    private String configurationLayer;
    private String services;
    private String incomingPorts;
    private String outgoingPorts;
    private final List<String> adapterLayers = new ArrayList<>();
    private final List<String> incomingAdapters = new ArrayList<>();
    private final List<String> outgoingAdapters = new ArrayList<>();

    private HexagonalArchitecture(String basePackage) {
        this.basePackage = basePackage;
    }

    static HexagonalArchitecture boundedContext(String basePackage) {
        return new HexagonalArchitecture(basePackage);
    }

    HexagonalArchitecture withDomainLayer(String packageName) {
        this.domainLayer = packageName;
        return this;
    }

    AdaptersLayer withAdaptersLayer(String packageName) {
        this.adapterLayers.add(packageName);
        return new AdaptersLayer(this, packageName);
    }

    ApplicationLayer withApplicationLayer(String packageName) {
        this.applicationLayer = packageName;
        return new ApplicationLayer(this);
    }

    HexagonalArchitecture withConfiguration(String packageName) {
        this.configurationLayer = packageName;
        return this;
    }

    void check(JavaClasses classes) {
        List<ArchRule> rules = List.of(
                domainShouldNotDependOnApplicationAdaptersOrPresentation(),
                applicationShouldNotDependOnAdaptersOrPresentation(),
                servicesShouldOnlyBeAccessedByIncomingAdaptersAndConfiguration(),
                incomingPortsShouldOnlyBeAccessedByIncomingAdaptersAndApplication(),
                outgoingPortsShouldOnlyBeAccessedByApplicationAndOutgoingAdapters()
        );

        rules.forEach(rule -> rule.check(classes));
    }

    private ArchRule domainShouldNotDependOnApplicationAdaptersOrPresentation() {
        return noClasses()
                .that()
                .resideInAPackage(layerPackage(domainLayer))
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(packages(
                        layerPackage(applicationLayer),
                        adapterLayerPackages()
                ));
    }

    private ArchRule applicationShouldNotDependOnAdaptersOrPresentation() {
        return noClasses()
                .that()
                .resideInAPackage(layerPackage(applicationLayer))
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(packages(
                        adapterLayerPackages(),
                        layerPackage("infrastructure.persistence")
                ));
    }

    private ArchRule servicesShouldOnlyBeAccessedByIncomingAdaptersAndConfiguration() {
        return classes()
                .that()
                .resideInAPackage(applicationPackage(services))
                .should()
                .onlyBeAccessed()
                .byAnyPackage(allowedAccessPackages(applicationPackage(services),
                        incomingAdapterPackages(),
                        configurationPackage()));
    }

    private ArchRule incomingPortsShouldOnlyBeAccessedByIncomingAdaptersAndApplication() {
        return classes()
                .that()
                .resideInAPackage(applicationPackage(incomingPorts))
                .should()
                .onlyBeAccessed()
                .byAnyPackage(allowedAccessPackages(applicationPackage(incomingPorts),
                        layerPackage(applicationLayer),
                        incomingAdapterPackages(),
                        configurationPackage()));
    }

    private ArchRule outgoingPortsShouldOnlyBeAccessedByApplicationAndOutgoingAdapters() {
        return classes()
                .that()
                .resideInAPackage(applicationPackage(outgoingPorts))
                .should()
                .onlyBeAccessed()
                .byAnyPackage(allowedAccessPackages(applicationPackage(outgoingPorts),
                        layerPackage(applicationLayer),
                        outgoingAdapterPackages(),
                        configurationPackage()));
    }

    private String layerPackage(String layer) {
        return basePackage + "." + layer + "..";
    }

    private String applicationPackage(String packageName) {
        return basePackage + "." + applicationLayer + "." + packageName + "..";
    }

    private String configurationPackage() {
        return basePackage + "." + configurationLayer + "..";
    }

    private String[] incomingAdapterPackages() {
        return incomingAdapters.toArray(String[]::new);
    }

    private String[] outgoingAdapterPackages() {
        return outgoingAdapters.toArray(String[]::new);
    }

    private String[] adapterLayerPackages() {
        return adapterLayers.stream()
                .map(this::layerPackage)
                .toArray(String[]::new);
    }

    private String[] allowedAccessPackages(String ownPackage, Object... packageGroups) {
        List<String> packages = new ArrayList<>();
        packages.add(ownPackage);

        for (Object packageGroup : packageGroups) {
            if (packageGroup instanceof String packageName) {
                packages.add(packageName);
            }

            if (packageGroup instanceof String[] packageNames) {
                packages.addAll(List.of(packageNames));
            }
        }

        return packages.toArray(String[]::new);
    }

    private String[] packages(Object... packageGroups) {
        List<String> packages = new ArrayList<>();

        for (Object packageGroup : packageGroups) {
            if (packageGroup instanceof String packageName) {
                packages.add(packageName);
            }

            if (packageGroup instanceof String[] packageNames) {
                packages.addAll(List.of(packageNames));
            }
        }

        return packages.toArray(String[]::new);
    }

    static class AdaptersLayer {

        private final HexagonalArchitecture architecture;
        private final String adapterLayer;

        private AdaptersLayer(HexagonalArchitecture architecture, String adapterLayer) {
            this.architecture = architecture;
            this.adapterLayer = adapterLayer;
        }

        AdaptersLayer incoming(String packageName) {
            architecture.incomingAdapters.add(architecture.basePackage + "." + adapterLayer + "." + packageName + "..");
            return this;
        }

        AdaptersLayer outgoing(String packageName) {
            architecture.outgoingAdapters.add(architecture.basePackage + "." + adapterLayer + "." + packageName + "..");
            return this;
        }

        HexagonalArchitecture and() {
            return architecture;
        }
    }

    static class ApplicationLayer {

        private final HexagonalArchitecture architecture;

        private ApplicationLayer(HexagonalArchitecture architecture) {
            this.architecture = architecture;
        }

        ApplicationLayer services(String packageName) {
            architecture.services = packageName;
            return this;
        }

        ApplicationLayer incomingPorts(String packageName) {
            architecture.incomingPorts = packageName;
            return this;
        }

        ApplicationLayer outgoingPorts(String packageName) {
            architecture.outgoingPorts = packageName;
            return this;
        }

        HexagonalArchitecture and() {
            return architecture;
        }
    }
}
