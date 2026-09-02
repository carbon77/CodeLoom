import { render, screen } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import LanguagesPage from "./LanguagesPage";
import { fetchLanguageDisplay, fetchLanguageSystem } from "../api/languages";

vi.mock("../api/languages", () => ({
  fetchLanguageDisplay: vi.fn(),
  fetchLanguageSystem: vi.fn(),
}));

describe("LanguagesPage", () => {
  beforeEach(() => {
    vi.mocked(fetchLanguageDisplay).mockResolvedValue([
      { key: "java", name: "Java" },
      { key: "python", name: "Python" },
    ]);
    vi.mocked(fetchLanguageSystem).mockResolvedValue([
      {
        key: "java",
        compileCommand: "javac Main.java",
        runCommand: "java Main",
      },
      { key: "python", compileCommand: null, runCommand: "python3 main.py" },
    ]);
  });

  it("joins display and system details and combines commands", async () => {
    render(<LanguagesPage />);

    const javaRow = await screen.findByRole("row", { name: /Java java/i });
    const pythonRow = screen.getByRole("row", { name: /Python python/i });

    expect(javaRow).toHaveTextContent(/javac Main\.java\s+java Main/);
    expect(pythonRow).toHaveTextContent("python3 main.py");
    expect(pythonRow).not.toHaveTextContent("null");
  });

  it("shows an error when language information cannot be loaded", async () => {
    vi.mocked(fetchLanguageDisplay).mockRejectedValue(new Error("offline"));
    render(<LanguagesPage />);

    expect(await screen.findByRole("alert")).toHaveTextContent(
      "Unable to load language information.",
    );
  });
});
