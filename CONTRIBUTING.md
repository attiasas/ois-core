# ⚒️ Building the library

To build the library sources, please follow these steps:

1. Clone the code from Git.
2. Build the plugin by running the following Gradle command:
    ```bash
    ./gradlew clean build
    ```
   <details>
   <summary>windows</summary>
   
    ```bash
    ./gradlew.bat clean build
    ```
   </details>

To build the library sources and publish it locally, run the following Gradle command:
```bash
./gradlew publishToMavenLocal
```
<details>
<summary>windows</summary>
   
```bash
./gradlew.bat publishToMavenLocal
```
</details>

---

# 🧪 Testing the plugin

To test the library sources, please follow these steps:
```bash
./gradlew clean check
```
<details>
<summary>windows</summary>

```bash
./gradlew.bat clean check
```
</details>