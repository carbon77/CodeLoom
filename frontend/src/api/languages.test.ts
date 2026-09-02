import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("./client", () => ({ apiFetch: vi.fn() }));
import { apiFetch } from "./client";
import { fetchLanguageDisplay, fetchLanguageSystem } from "./languages";

describe("language API contracts", () => {
  beforeEach(() => vi.mocked(apiFetch).mockResolvedValue([]));

  it("fetches display information", async () => {
    await fetchLanguageDisplay();
    expect(apiFetch).toHaveBeenCalledWith("/v1/languages/display");
  });

  it("fetches system information", async () => {
    await fetchLanguageSystem();
    expect(apiFetch).toHaveBeenCalledWith("/v1/languages/system");
  });
});
