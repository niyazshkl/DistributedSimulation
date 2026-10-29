# Project Setup Requirements

Before running the project, ensure that the following prerequisites are satisfied.

## Prerequisites

1. **Java Development Kit (JDK):** `Eclipse Temurin 11.0.12` must be installed and configured.
2. **Required JAR:** `Comp533All.jar` must be available to the project.
3. **Source Files:** The provided ZIP file must be extracted into the `src` folder before running the project.

## Setup Instructions

1. Install and configure `Eclipse Temurin 11.0.12`.
2. Ensure `Comp533All.jar` is available and included in the project's classpath.
3. Extract the provided `TrickOrTreatImages.zip` in the project's `src` folder.
4. Configure the project to use the required JDK and ensure all dependencies are resolved.
5. Run the project using `main.RegistryMain`, `main.ServerMain`, and `main.ClientMain`.

**Important:** Complete all setup steps before running the project to avoid missing dependency or source-file errors.


   ```bash
   git clone https://github.com/niyazshkl/DistributedSimulation.git
   cd DistributedSimulation
   git lfs pull
   unzip -j src/TrickOrTreatImages.zip -d src/
   ```