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
        validateRequiredConfiguration();

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
        validatePackageName(layer, "layer package");
        return validatePackagePattern(basePackage + "." + layer + "..", "layer package");
    }

    private String applicationPackage(String packageName) {
        validatePackageName(packageName, "application package");
        return validatePackagePattern(basePackage + "." + applicationLayer + "." + packageName + "..",
                "application package");
    }

    private String configurationPackage() {
        return validatePackagePattern(basePackage + "." + configurationLayer + "..", "configuration package");
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
        packages.add(validatePackagePattern(ownPackage, "allowed access package"));

        for (Object packageGroup : packageGroups) {
            if (packageGroup instanceof String packageName) {
                packages.add(validatePackagePattern(packageName, "allowed access package"));
            }

            if (packageGroup instanceof String[] packageNames) {
                packages.addAll(List.of(packageNames).stream()
                        .map(packageName -> validatePackagePattern(packageName, "allowed access package"))
                        .toList());
            }
        }

        return packages.toArray(String[]::new);
    }

    private String[] packages(Object... packageGroups) {
        List<String> packages = new ArrayList<>();

        for (Object packageGroup : packageGroups) {
            if (packageGroup instanceof String packageName) {
                packages.add(validatePackagePattern(packageName, "rule package"));
            }

            if (packageGroup instanceof String[] packageNames) {
                packages.addAll(List.of(packageNames).stream()
                        .map(packageName -> validatePackagePattern(packageName, "rule package"))
                        .toList());
            }
        }

        return packages.toArray(String[]::new);
    }

    private void validateRequiredConfiguration() {
        validatePackageName(basePackage, "boundedContext");
        validatePackageName(domainLayer, "domain layer");
        validatePackageName(applicationLayer, "application layer");
        validatePackageName(configurationLayer, "configuration layer");
        validatePackageName(services, "application services");
        validatePackageName(incomingPorts, "incoming ports");
        validatePackageName(outgoingPorts, "outgoing ports");
        validatePackageNames(adapterLayers, "adapter layers");
        validatePackageNames(incomingAdapters, "incoming adapters");
        validatePackageNames(outgoingAdapters, "outgoing adapters");
    }

    private void validatePackageNames(List<String> packageNames, String configurationName) {
        if (packageNames.isEmpty()) {
            throw new IllegalStateException("Missing architecture configuration: " + configurationName);
        }

        packageNames.forEach(packageName -> validatePackagePattern(packageName, configurationName));
    }

    private String validatePackageName(String packageName, String configurationName) {
        if (isInvalidPackageName(packageName)) {
            throw new IllegalStateException("Missing architecture configuration: " + configurationName);
        }

        return packageName;
    }

    private String validatePackagePattern(String packagePattern, String configurationName) {
        if (isInvalidPackageName(packagePattern) || packagePattern.contains(".null.")) {
            throw new IllegalStateException("Invalid architecture package configuration: " + configurationName);
        }

        return packagePattern;
    }

    private boolean isInvalidPackageName(String packageName) {
        return packageName == null || packageName.isBlank() || "null".equals(packageName);
    }

    static class AdaptersLayer {

        private final HexagonalArchitecture architecture;
        private final String adapterLayer;

        private AdaptersLayer(HexagonalArchitecture architecture, String adapterLayer) {
            this.architecture = architecture;
            this.adapterLayer = adapterLayer;
        }

        AdaptersLayer incoming(String packageName) {
            architecture.validatePackageName(packageName, "incoming adapter");
            architecture.incomingAdapters.add(architecture.validatePackagePattern(
                    architecture.basePackage + "." + adapterLayer + "." + packageName + "..",
                    "incoming adapter"
            ));
            return this;
        }

        AdaptersLayer outgoing(String packageName) {
            architecture.validatePackageName(packageName, "outgoing adapter");
            architecture.outgoingAdapters.add(architecture.validatePackagePattern(
                    architecture.basePackage + "." + adapterLayer + "." + packageName + "..",
                    "outgoing adapter"
            ));
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
