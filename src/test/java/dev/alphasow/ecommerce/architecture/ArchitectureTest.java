package dev.alphasow.ecommerce.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import dev.alphasow.ecommerce.order.application.port.out.OrderRepository;
import dev.alphasow.ecommerce.order.domain.model.OrderItem;

/**
 * Règles d'architecture structurelles (DDD, Clean, hexagonale). Elles ne vérifient pas les
 * invariants métiers, couverts par les tests du domaine. Un should() sur un ensemble vide échoue
 * par défaut dans ArchUnit : les gardes ci-dessous empêchent donc tout faux succès par vacuité.
 */
@AnalyzeClasses(
    packages = "dev.alphasow.ecommerce",
    importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

  private static final String DOMAIN = "..order.domain..";
  private static final String APPLICATION = "..order.application..";
  private static final String WEB = "..order.infrastructure.web..";
  private static final String PERSISTENCE = "..order.infrastructure.persistence..";
  private static final String CONFIGURATION = "..order.infrastructure.configuration..";

  // ---------- Gardes anti-faux-succès ----------

  private static ArchRule packageIsImported(String pkg) {
    return classes().that().resideInAPackage(pkg).should().resideInAPackage(pkg);
  }

  @ArchTest
  static final ArchRule productionClassesAreImported =
      packageIsImported("dev.alphasow.ecommerce..");

  @ArchTest
  static final ArchRule domainModelIsImported = packageIsImported("..order.domain.model..");

  @ArchTest
  static final ArchRule useCasesAreImported = packageIsImported("..order.application.usecase..");

  @ArchTest
  static final ArchRule outputPortsAreImported =
      packageIsImported("..order.application.port.out..");

  @ArchTest static final ArchRule webIsImported = packageIsImported(WEB);

  @ArchTest
  static final ArchRule persistenceEntitiesAreImported =
      packageIsImported("..order.infrastructure.persistence.entity..");

  @ArchTest
  static final ArchRule persistenceAdaptersAreImported =
      packageIsImported("..order.infrastructure.persistence.adapter..");

  // ---------- Clean Architecture ----------

  @ArchTest
  static final ArchRule layers =
      layeredArchitecture()
          .consideringAllDependencies()
          .layer("Domain")
          .definedBy(DOMAIN)
          .layer("Application")
          .definedBy(APPLICATION)
          .layer("Infrastructure")
          .definedBy("..order.infrastructure..")
          .whereLayer("Domain")
          .mayOnlyBeAccessedByLayers("Application", "Infrastructure")
          .whereLayer("Application")
          .mayOnlyBeAccessedByLayers("Infrastructure")
          .whereLayer("Infrastructure")
          .mayNotBeAccessedByAnyLayer();

  @ArchTest
  static final ArchRule applicationIsFrameworkFree =
      noClasses()
          .that()
          .resideInAPackage(APPLICATION)
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.springframework..",
              "jakarta.persistence..",
              "jakarta.validation..",
              "org.hibernate..",
              "com.fasterxml.jackson..")
          .because("la couche application ne connaît aucun framework");

  @ArchTest
  static final ArchRule applicationDoesNotDependOnInfrastructure =
      noClasses()
          .that()
          .resideInAPackage(APPLICATION)
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..order.infrastructure..")
          .because("les dépendances pointent vers l'intérieur");

  // ---------- DDD ----------

  @ArchTest
  static final ArchRule domainDependsOnlyOnItselfAndJdk =
      classes()
          .that()
          .resideInAPackage(DOMAIN)
          .should()
          .onlyDependOnClassesThat()
          .resideInAnyPackage(DOMAIN, "java..")
          .because("le domaine est pur : ni framework, ni application, ni infrastructure");

  @ArchTest
  static final ArchRule domainExceptionsAreUnchecked =
      classes()
          .that()
          .resideInAPackage("..order.domain.exception..")
          .should()
          .beAssignableTo(RuntimeException.class)
          .because("les violations métier sont des exceptions non vérifiées");

  @ArchTest
  static final ArchRule orderItemIsInternalToAggregate =
      noClasses()
          .that()
          .resideOutsideOfPackages(
              DOMAIN, APPLICATION, "..order.infrastructure.persistence.mapper..")
          .should()
          .dependOnClassesThat()
          .areAssignableTo(OrderItem.class)
          .because(
              "OrderItem est interne à l'agrégat Order : le web ne l'expose pas, seul le mapper"
                  + " de persistance le reconstruit");

  // ---------- Architecture hexagonale ----------

  @ArchTest
  static final ArchRule outputPortsAreInterfaces =
      classes()
          .that()
          .resideInAPackage("..order.application.port.out..")
          .should()
          .beInterfaces()
          .because("un port sortant est un contrat, pas une implémentation");

  @ArchTest
  static final ArchRule outputPortsDependOnlyOnDomainAndJdk =
      classes()
          .that()
          .resideInAPackage("..order.application.port.out..")
          .should()
          .onlyDependOnClassesThat()
          .resideInAnyPackage(DOMAIN, "..order.application.port.out..", "java..")
          .because("un port ne doit pas fuiter de détail technique");

  @ArchTest
  static final ArchRule persistenceAdapterImplementsOutputPort =
      classes()
          .that()
          .resideInAPackage("..order.infrastructure.persistence.adapter..")
          .should()
          .implement(OrderRepository.class)
          .because("l'adaptateur de persistence réalise le port sortant");

  @ArchTest
  static final ArchRule webDoesNotDependOnPersistence =
      noClasses()
          .that()
          .resideInAPackage(WEB)
          .should()
          .dependOnClassesThat()
          .resideInAPackage(PERSISTENCE)
          .because("les adaptateurs sont indépendants entre eux");

  @ArchTest
  static final ArchRule persistenceDoesNotDependOnWeb =
      noClasses()
          .that()
          .resideInAPackage(PERSISTENCE)
          .should()
          .dependOnClassesThat()
          .resideInAPackage(WEB)
          .because("les adaptateurs sont indépendants entre eux");

  @ArchTest
  static final ArchRule webDoesNotUseOutputPorts =
      noClasses()
          .that()
          .resideInAPackage(WEB)
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..order.application.port.out..")
          .because("le web passe par les cas d'utilisation, pas par le repository");

  @ArchTest
  static final ArchRule webDoesNotUseDomainModel =
      noClasses()
          .that()
          .resideInAPackage(WEB)
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..order.domain.model..")
          .because(
              "le web passe par les cas d'utilisation et les commandes ; il ne doit pas"
                  + " dépendre de domain.model");

  @ArchTest
  static final ArchRule adaptersDoNotUseConfiguration =
      noClasses()
          .that()
          .resideInAnyPackage(WEB, PERSISTENCE)
          .should()
          .dependOnClassesThat()
          .resideInAPackage(CONFIGURATION)
          .because("seul le composition root câble les composants");

  @ArchTest
  static final ArchRule jpaIsConfinedToPersistence =
      noClasses()
          .that()
          .resideOutsideOfPackage(PERSISTENCE)
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "jakarta.persistence..", "org.springframework.data..", "org.hibernate..")
          .because("JPA et Spring Data sont des détails de l'adaptateur de persistence");

  @ArchTest
  static final ArchRule jpaEntitiesResideInPersistenceEntity =
      classes()
          .that()
          .areAnnotatedWith("jakarta.persistence.Entity")
          .should()
          .resideInAPackage("..order.infrastructure.persistence.entity..")
          .because("les entités JPA sont distinctes du modèle de domaine");

  @ArchTest
  static final ArchRule beanValidationIsConfinedToWeb =
      noClasses()
          .that()
          .resideOutsideOfPackage(WEB)
          .should()
          .dependOnClassesThat()
          .resideInAPackage("jakarta.validation..")
          .because("la validation des entrées est une préoccupation de l'adaptateur web");

  @ArchTest
  static final ArchRule webComponentsResideInWeb =
      classes()
          .that()
          .areMetaAnnotatedWith("org.springframework.stereotype.Controller")
          .or()
          .areMetaAnnotatedWith("org.springframework.web.bind.annotation.ControllerAdvice")
          .should()
          .resideInAPackage(WEB)
          .because("les points d'entrée HTTP (y compris méta-annotés) sont des adaptateurs web");

  @ArchTest
  static final ArchRule springConfigurationResidesInConfiguration =
      classes()
          .that()
          .areAnnotatedWith("org.springframework.context.annotation.Configuration")
          .should()
          .resideInAPackage(CONFIGURATION)
          .because("le câblage Spring est centralisé dans le composition root");
}
