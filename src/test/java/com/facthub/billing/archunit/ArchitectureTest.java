package com.facthub.billing.archunit;

import com.tngtech.archunit.junit.AnalyzeClasses;

/**
 * Architectural tests for DDD structure.
 * Rules will be enforced as classes are added to the project.
 */
@AnalyzeClasses(packages = "com.facthub.billing")
public class ArchitectureTest {

    // Pending: Enable when controller classes are added
    // @ArchTest
    // static final ArchRule controllers_must_end_with_Controller =
    //         classes().that().resideInAPackage("..controller..")
    //                 .should().haveSimpleNameEndingWith("Controller");

    // Pending: Enable when we fix ArchUnit classpath scanning
    // @ArchTest
    // static final ArchRule dtos_must_end_with_Dto =
    //         classes().that().resideInAPackage("..dto..")
    //                 .should().haveSimpleNameEndingWith("Dto");
}
