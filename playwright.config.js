const { defineConfig, devices } = require("@playwright/test");

module.exports = defineConfig({
  testDir: "./tests/e2e",
  timeout: 30000,
  use: {
    baseURL: "http://127.0.0.1:4173",
    trace: "on-first-retry",
    channel: "msedge",
    ...devices["Desktop Edge"]
  },
  webServer: {
    command: "mvn spring-boot:run -Dspring-boot.run.profiles=test -Dspring-boot.run.arguments=--server.port=4173",
    url: "http://127.0.0.1:4173",
    reuseExistingServer: true,
    timeout: 120000
  }
});
