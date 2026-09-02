import { apiFetch } from "./client";

export interface LanguageDisplay {
  key: string;
  name: string;
}

export interface LanguageSystem {
  key: string;
  compileCommand: string | null;
  runCommand: string;
}

export function fetchLanguageDisplay(): Promise<LanguageDisplay[]> {
  return apiFetch<LanguageDisplay[]>("/v1/languages/display");
}

export function fetchLanguageSystem(): Promise<LanguageSystem[]> {
  return apiFetch<LanguageSystem[]>("/v1/languages/system");
}
