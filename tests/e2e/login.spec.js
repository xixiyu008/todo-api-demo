const { test, expect } = require("@playwright/test");

test("admin login switches the page into authenticated view", async ({ page }) => {
  await page.addInitScript(() => {
    window.localStorage.clear();
    window.sessionStorage.clear();
  });
  await page.goto("/");

  await expect(page.locator("#loginForm")).toBeVisible();
  await expect(page.locator("#registerPanel")).toBeVisible();
  await expect(page.locator("#loginButton")).toContainText("\u767b\u5f55");

  await page.locator("#username").fill("admin");
  await page.locator("#password").fill("password123");
  await page.locator("#loginButton").click();

  await expect(page.locator("#logoutButton")).toBeVisible();
  await expect(page.locator("#currentUserPanel")).toBeVisible();
  await expect(page.locator("#registerPanel")).toHaveCount(0);
  await expect(page.locator("#currentUserCard")).toContainText("admin");
  await expect(page.locator("#statusMessage")).toContainText("\u767b\u5f55\u6210\u529f");

  await page.locator("#openAdminPanelButton").click();
  await expect(page.locator("#adminUserPanel")).toBeVisible();
  await expect(page.locator("#closeAdminPanelButton")).toBeVisible();
});
